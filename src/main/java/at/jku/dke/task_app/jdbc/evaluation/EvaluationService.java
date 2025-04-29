package at.jku.dke.task_app.jdbc.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskRepository;
import at.jku.dke.task_app.jdbc.dto.JDBCSubmissionDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that evaluates submissions.
 */
@Service
public class EvaluationService {
    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    private final JDBCTaskRepository taskRepository;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link EvaluationService}.
     *
     * @param taskRepository The task repository.
     * @param messageSource  The message source.
     */
    public EvaluationService(JDBCTaskRepository taskRepository, MessageSource messageSource) {
        this.taskRepository = taskRepository;
        this.messageSource = messageSource;
    }

    /**
     * Evaluates a input.
     *
     * @param submission The input to evaluate.
     * @return The evaluation result.
     */
    @Transactional
    public GradingDto evaluate(SubmitSubmissionDto<JDBCSubmissionDto> submission) {
        // find task
        var task = this.taskRepository.findById(submission.taskId()).orElseThrow(() -> new EntityNotFoundException("Task " + submission.taskId() + " does not exist."));


        // evaluate input
        LOG.info("Evaluating input for task {} with mode {} and feedback-level {}", submission.taskId(), submission.mode(), submission.feedbackLevel());
        Locale locale = Locale.of(submission.language());
        BigDecimal points = BigDecimal.ZERO;
        List<CriterionDto> criteria = new ArrayList<>();
        String feedback = "";
        Boolean allPassed = false;
        int feedbackLevel = submission.feedbackLevel();

        int penaltyAutocommit = task.getAutocommitPenalty() != null ? task.getAutocommitPenalty() : 0;
        int penaltyOutput = task.getWrongOutputPenalty() != null ? task.getWrongOutputPenalty() : 0;
        int penaltyException = task.getExceptionHandlingPenalty() != null ? task.getExceptionHandlingPenalty() : 0;
        int penaltyDatabase = task.getWrongDbContentPenalty() != null ? task.getWrongDbContentPenalty() : 0;

        //Unparsed
        String inputString = "";
        try {
            inputString = submission.submission().input();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            //error = ex;
        }

        //System.out.println("Input: " + inputString + ", " + inputString.getClass().getSimpleName());
        String[] tables = Arrays.stream(task.getTables().split(",")).map(String::trim).toArray(String[]::new);
        //System.out.println("Solution: " + task.getSolution() + ", " + task.getSolution().getClass().getSimpleName() );
        //System.out.println("GroupSetting: " + task.getTaskGroup().getSchema());
        //System.out.println("Mode: " + submission.mode());

        //***  TEST DATA ***///
        String studentInput = Solutions.studentInput;
        //String studentInput = Solutions.studentInputNoExceptionHandling;
        //String studentInput = Solutions.studentInputWrongSyntax;
        //String studentInput = Solutions.studentInputAutocommitNotDisabled;
        //String studentInput = Solutions.studentInputTooFewDbRows;
        //String studentInput = Solutions.studentInputTooManyDbRows;

        Result testResult = null;

        // evaluate and grade
        switch (submission.mode()) {

            case RUN: {
                String createStatements = task.getTaskGroup().getCreateStatements();
                String insertStatements = task.getTaskGroup().getInsertStatementsDiagnose();
                String dbSchema = createStatements + ";" + insertStatements;

                testResult = AssessmentService.assessTask(studentInput,dbSchema , "", tables, false);
                // Syntaxcheck vor Ausführung
                //testResult = new Result();
                boolean syntaxOk = new AssessmentFunctions().checkSyntax(studentInput, testResult);

                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.syntax", null, locale),
                    null,
                    syntaxOk,
                    messageSource.getMessage(
                        syntaxOk ? "criterium.syntax.valid" : "criterium.syntax.invalid",
                        null,
                        locale
                    ) + (syntaxOk ? "" : ": " + testResult.getSyntaxError())
                ));
                if (!syntaxOk) {
                    feedback = messageSource.getMessage("incorrect", null, locale);
                    return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
                }
                feedback = "";
                if (feedbackLevel >= 2) {
                    feedback += "<br/><br/><strong>Output:</strong><br/>";
                    feedback += testResult.getStudentOutput() != null ? testResult.getStudentOutput().replaceAll("\n", "<br/>"): "";
                }

                if (feedbackLevel >= 3) {
                    feedback += "<br/><strong>Database State:</strong>";
                    feedback += renderTableDumps(testResult.getStudentQueryResult());
                }

                break;
            }

            case DIAGNOSE, SUBMIT: {
                // Syntaxcheck vor Ausführung
                testResult = new Result();
                boolean syntaxOk = new AssessmentFunctions().checkSyntax(studentInput, testResult);

                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.syntax", null, locale),
                    null,
                    syntaxOk,
                    messageSource.getMessage(
                        syntaxOk ? "criterium.syntax.valid" : "criterium.syntax.invalid",
                        null,
                        locale
                    ) + (syntaxOk ? "" : ": " + testResult.getSyntaxError())
                ));
                if (!syntaxOk) {
                    feedback = messageSource.getMessage("incorrect", null, locale);
                    return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
                }

                // Vorbereitung
                String taskSolution = Solutions.taskSolution;
                String createStatements = task.getTaskGroup().getCreateStatements();

                String insertStatements = submission.mode().toString().equals("DIAGNOSE")
                    ? task.getTaskGroup().getInsertStatementsDiagnose()
                    : task.getTaskGroup().getInsertStatementsSubmission();
                String dbSchema = createStatements + ";" + insertStatements;

                testResult = AssessmentService.assessTask(studentInput, dbSchema, taskSolution, tables, true);

                if (testResult == null) throw new RuntimeException("Assessment failed – result is null");

                // Ergebnisbewertung nach Feedbacklevel
                if (feedbackLevel >= 1) {
                    if(task.isCheckAutocommit()) criteria.add(makeCriterion("criterium.autocommit", testResult.getAutoCommitResult(), testResult.getAutoCommitMessage(), locale, feedbackLevel, testResult));
                    criteria.add(makeCriterion("criterium.output", testResult.getOutputComparisionResult(), testResult.getOutputComparisionMessage(), locale, feedbackLevel, testResult));
                    criteria.add(makeCriterion("criterium.database", testResult.getDatabaseResult(), testResult.getDatabaseMessage(), locale, feedbackLevel, testResult));
                    criteria.add(makeCriterion("criterium.exception", testResult.getExceptionResult(), testResult.getExceptionMessage(), locale, feedbackLevel, testResult));
                }

                allPassed = Boolean.TRUE.equals(testResult.getSyntaxResult())
                    && (task.isCheckAutocommit() ? Boolean.TRUE.equals(testResult.getAutoCommitResult()) : Boolean.TRUE)
                    && Boolean.TRUE.equals(testResult.getOutputComparisionResult())
                    && Boolean.TRUE.equals(testResult.getDatabaseResult())
                    && Boolean.TRUE.equals(testResult.getExceptionResult());

                feedback = messageSource.getMessage(allPassed ? "correct" : "incorrect", null, locale);

                //if (feedbackLevel >= 3) feedback += "\n" + testResult.getStudentQueryResult();
                if (feedbackLevel >= 2) {
                    feedback += "<br/><br/><strong>Output:</strong><br/>";
                    feedback += testResult.getStudentOutput().replaceAll("\n", "<br/>");
                }
                if (feedbackLevel >= 3) {
                    feedback += "<br/><strong>Database State:</strong>";
                    feedback += renderTableDumps(testResult.getStudentQueryResult());
                }
                //Calculte Points

                if (submission.mode() == SubmissionMode.SUBMIT){
                    points = task.getMaxPoints();

                    if (!Boolean.TRUE.equals(testResult.getSyntaxResult())) {
                        points = BigDecimal.ZERO;
                    } else {
                        if (task.isCheckAutocommit() && !Boolean.TRUE.equals(testResult.getAutoCommitResult()))
                            points = points.subtract(BigDecimal.valueOf(penaltyAutocommit));

                        if (!Boolean.TRUE.equals(testResult.getOutputComparisionResult()))
                            points = points.subtract(BigDecimal.valueOf(penaltyOutput)
                                .multiply(BigDecimal.valueOf(testResult.getMissingOutputs().size() + testResult.getSuperfluousOutputs().size())));

                        if (!Boolean.TRUE.equals(testResult.getDatabaseResult()))
                            points = points.subtract(BigDecimal.valueOf(penaltyDatabase)
                                .multiply(BigDecimal.valueOf(testResult.getMissingTuples().size() + testResult.getSuperfluousTuples().size())));

                        if (!Boolean.TRUE.equals(testResult.getExceptionResult()))
                            points = points.subtract(BigDecimal.valueOf(penaltyException));

                        if (points.compareTo(BigDecimal.ZERO) < 0)
                            points = BigDecimal.ZERO;
                    }
                }

                if (allPassed) points = task.getMaxPoints();

                break;
            }

            default: throw new IllegalStateException("Unexpected mode: " + submission.mode());
        }



        return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
    }

    private CriterionDto makeCriterion(String key, Boolean passed, String detail, Locale locale, int level, Result result) {
        String message;

        if (Boolean.TRUE.equals(passed)) {
            message = messageSource.getMessage(key + ".valid", null, locale);
        } else {
            if (key.equals("criterium.exception")) {
                message = messageSource.getMessage(key + ".invalid.level" + Math.min(level, 3), new Object[]{detail}, locale);
            }
            //Database
            else if (key.equals("criterium.database") && level >= 2) {

                List<List<String>> missing = result.getMissingTuples();
                List<List<String>> extra = result.getSuperfluousTuples();

                if (level == 2) {
                    message = "";
                    if (!missing.isEmpty()) {
                        message += message = messageSource.getMessage("criterium.database.missingCount", new Object[]{missing.size()}, locale);
                    }
                    message += "";
                    if (!extra.isEmpty()) {
                        message += messageSource.getMessage("criterium.database.extraCount", new Object[]{extra.size()}, locale);
                    }
                } else if (level == 3) {
                    message = "";
                    if (!missing.isEmpty()) {
                        message += messageSource.getMessage("criterium.database.missingList", new Object[]{
                            missing.size(),
                            toHtmlTable(missing)
                        }, locale) + "<br/>";
                    }
                    if (!extra.isEmpty()) {
                        message += messageSource.getMessage("criterium.database.extraList", new Object[]{
                            extra.size(),
                            toHtmlTable(extra)
                        }, locale);
                    }
                } else {
                    message = messageSource.getMessage(key + ".invalid", null, locale);
                }
            }
            //Output
            else if (key.equals("criterium.output") && level >= 2) {
                List<String> missing = result.getMissingOutputs();
                List<String> extra = result.getSuperfluousOutputs();

                if (level == 2) {
                    message = "";
                    if (!missing.isEmpty()) {
                        message += messageSource.getMessage("criterium.output.missingCount", new Object[]{missing.size()}, locale);
                    }
                    if (!extra.isEmpty()) {
                        if (!message.isEmpty()) message += " ";
                        message += messageSource.getMessage("criterium.output.extraCount", new Object[]{extra.size()}, locale);
                    }
                } else if (level == 3) {
                    message = "";
                    if (!missing.isEmpty()) {
                        message += messageSource.getMessage("criterium.output.missingList", new Object[]{
                            missing.size(),
                            String.join("<br/>", missing)
                        }, locale) + "<br/>";
                    }
                    if (!extra.isEmpty()) {
                        message += messageSource.getMessage("criterium.output.extraList", new Object[]{
                            extra.size(),
                            String.join("<br/>", extra)
                        }, locale);
                    }
                } else {
                    message = messageSource.getMessage(key + ".invalid", null, locale);
                }
            }
            else if (level == 1) {
                message = messageSource.getMessage("feedback.generalHint", null, locale);
            } else {
                message = messageSource.getMessage(key + ".invalid", null, locale);
            }
        }

        return new CriterionDto(
            messageSource.getMessage(key, null, locale),
            null,
            passed,
            message
        );
    }

    private String toHtmlTable(List<List<String>> tuples) {
        if (tuples.isEmpty()) return "";

        StringBuilder html = new StringBuilder();
        html.append("<table border='2px' style='margin: 8px 0 8px 0;'><tbody>");

        for (List<String> row : tuples) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td style='padding: 4px;'>").append(cell).append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        return html.toString();
    }

    private String renderTableDumps(List<TableDump> dumps) {
        StringBuilder html = new StringBuilder();

        if (dumps == null) return "<p>No tables found</p>";
        for (TableDump dump : dumps) {
            html.append("<br/><b>Table: ").append(dump.tableName()).append("</b><br/>");
            html.append("<table border='1' style='border-collapse: collapse;'>");

            // Header
            html.append("<tr>");
            for (String col : dump.columns())
                html.append("<th>").append(col).append("</th>");
            html.append("</tr>");

            // Rows
            for (List<String> row : dump.rows()) {
                html.append("<tr>");
                for (String cell : row)
                    html.append("<td>").append(cell).append("</td>");
                html.append("</tr>");
            }

            html.append("</table>");
        }

        return html.toString();
    }


}

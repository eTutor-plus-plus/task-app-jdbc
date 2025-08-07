package at.jku.dke.task_app.jdbc.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskRepository;
import at.jku.dke.task_app.jdbc.dto.JDBCSubmissionDto;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service that evaluates submissions.
 */
@Service
public class EvaluationService {
    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    private final JDBCTaskRepository taskRepository;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class EvaluationService
     *
     * @param taskRepository The task repository.
     * @param messageSource  The message source.
     */
    public EvaluationService(JDBCTaskRepository taskRepository, MessageSource messageSource) {
        this.taskRepository = taskRepository;
        this.messageSource = messageSource;
    }

    /**
     * Evaluates an input.
     *
     * @param submission The input to evaluate.
     * @return The evaluation result.
     */
    @Transactional
    public GradingDto evaluate(SubmitSubmissionDto<JDBCSubmissionDto> submission) {
        // Find task
        var task = this.taskRepository.findById(submission.taskId()).orElseThrow(() -> new EntityNotFoundException("Task " + submission.taskId() + " does not exist."));

        // Evaluate input
        LOG.info("Evaluating input for task {} with mode {} and feedback-level {}", submission.taskId(), submission.mode(), submission.feedbackLevel());
        Locale locale = Locale.of(submission.language());
        BigDecimal points = BigDecimal.ZERO;
        List<CriterionDto> criteria = new ArrayList<>();
        String feedback = "";
        boolean allPassed = false;
        int feedbackLevel = submission.feedbackLevel();

        //Penalties
        int penaltyAutocommit = task.getAutocommitPenalty() != null ? task.getAutocommitPenalty() : 0;
        int penaltyOutput = task.getWrongOutputPenalty() != null ? task.getWrongOutputPenalty() : 0;
        int penaltyException = task.getExceptionHandlingPenalty() != null ? task.getExceptionHandlingPenalty() : 0;
        int penaltyDatabase = task.getWrongDbContentPenalty() != null ? task.getWrongDbContentPenalty() : 0;

        //Deductions
        String deductionAutocommit = "0";
        String deductionOutput = "0";
        String deductionException = "0";
        String deductionDatabase = "0";

        //Variables
        String variables = task.getVariables() != null ? task.getVariables() : "";
        String[] tables = Arrays.stream(task.getTables().split(",")).map(String::trim).toArray(String[]::new);
        String studentInput = submission.submission().input();
        Result testResult;

        // Evaluate and Grade
        switch (submission.mode()) {
            //RUN Mode => Only syntax check
            case RUN: {
                String createStatements = task.getTaskGroup().getCreateStatements();
                String insertStatements = task.getTaskGroup().getInsertStatementsDiagnose();
                String dbSchema = createStatements + ";" + insertStatements;

                testResult = AssessmentService.assessTask(studentInput,dbSchema , "", tables, variables, false);
                assert testResult != null;
                boolean syntaxOk = Boolean.TRUE.equals(testResult.getSyntaxResult());

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
                //Stop when syntax is not okay
                if (!syntaxOk) {
                    feedback = messageSource.getMessage("incorrect", null, locale);
                    return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
                }
                feedback = "";
                //Database content before executing Students' code

                feedback += "<br/><br/><details><summary><strong>" + messageSource.getMessage("text.databasebefore", new Object[]{}, locale) + "</strong></summary>";
                feedback += renderTableDumps(testResult.getDatabaseBefore());
                feedback += "</details><br/><hr>";

                //Output
                feedback += "<br/><strong>" + messageSource.getMessage("text.output", new Object[]{}, locale) + "</strong><br/><br/>";
                feedback += testResult.getStudentOutput() != null ? testResult.getStudentOutput().replaceAll("\n", "<br/>"): "";
                feedback += "<br/>";



                //Database State
                feedback += "<hr><br/><details><summary style='cursor: pointer;'><strong>" + messageSource.getMessage("text.databaseafter", new Object[]{}, locale) + "</strong></summary>";
                feedback += renderTableDumps(testResult.getStudentQueryResult());
                feedback += "</details><br/>";

                return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
            }

            case DIAGNOSE, SUBMIT: {
                // Syntax check before execution
                testResult = new Result();
                boolean syntaxOk = new AssessmentFunctions().checkSyntax(studentInput, variables, testResult);

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

                // Preparation of variables
                String taskSolution = task.getSolution();
                String createStatements = task.getTaskGroup().getCreateStatements();

                String insertStatements = submission.mode().toString().equals("DIAGNOSE")
                    ? task.getTaskGroup().getInsertStatementsDiagnose()
                    : task.getTaskGroup().getInsertStatementsSubmission();
                String dbSchema = createStatements + ";" + insertStatements;
                testResult = AssessmentService.assessTask(studentInput, dbSchema, taskSolution, tables, variables, true);

                if (testResult == null) throw new RuntimeException("Assessment failed – result is null");

                // Grading according to Feedback-Level
                if (feedbackLevel >= 1) {
                    if(task.isCheckAutocommit()) criteria.add(makeCriterion("criterium.autocommit", testResult.getAutoCommitResult(), testResult.getAutoCommitMessage(), locale, feedbackLevel, testResult));
                    criteria.add(makeCriterion("criterium.output", testResult.getOutputComparisonResult(), testResult.getOutputComparisonMessage(), locale, feedbackLevel, testResult));
                    criteria.add(makeCriterion("criterium.database", testResult.getDatabaseResult(), testResult.getDatabaseMessage(), locale, feedbackLevel, testResult));
                    criteria.add(makeCriterion("criterium.exception", testResult.getExceptionResult(), testResult.getExceptionMessage(), locale, feedbackLevel, testResult));
                }

                allPassed = Boolean.TRUE.equals(testResult.getSyntaxResult())
                    && (task.isCheckAutocommit() ? Boolean.TRUE.equals(testResult.getAutoCommitResult()) : Boolean.TRUE)
                    && Boolean.TRUE.equals(testResult.getOutputComparisonResult())
                    && Boolean.TRUE.equals(testResult.getDatabaseResult())
                    && Boolean.TRUE.equals(testResult.getExceptionResult());

                feedback = messageSource.getMessage(allPassed ? "correct" : "incorrect", null, locale);

                //Database content before executing Students' code
                if (feedbackLevel >= 3) {
                    feedback += "<br/><br/><details><summary><strong>" + messageSource.getMessage("text.databasebefore", new Object[]{}, locale) + "</strong></summary>";
                    feedback += renderTableDumps(testResult.getDatabaseBefore());
                    feedback += "</details><br/><hr>";
                }
                //Output
                if (feedbackLevel >= 2) {
                    feedback += "<br/><strong>" + messageSource.getMessage("text.output", new Object[]{}, locale) + "</strong><br/><br/>";
                    feedback += testResult.getStudentOutput().replaceAll("\n", "<br/>");
                    feedback += "<br/>";
                }
                //Database State
                if (feedbackLevel >= 3) {
                    feedback += "<hr><br/><details><summary style='cursor: pointer;'><strong>" + messageSource.getMessage("text.databaseafter", new Object[]{}, locale) + "</strong></summary>";
                    feedback += renderTableDumps(testResult.getStudentQueryResult());
                    feedback += "</details><br/>";
                }
                //Calculate Points

                if (submission.mode() == SubmissionMode.SUBMIT || submission.mode() == SubmissionMode.DIAGNOSE) {
                    points = task.getMaxPoints();

                    if (!Boolean.TRUE.equals(testResult.getSyntaxResult())) {
                        points = BigDecimal.ZERO;
                    } else {
                        if (task.isCheckAutocommit() && !Boolean.TRUE.equals(testResult.getAutoCommitResult())) {
                            BigDecimal deductionPoints = BigDecimal.valueOf(penaltyAutocommit);
                            deductionAutocommit = messageSource.getMessage("deduction.autocommit", new Object[]{deductionPoints}, locale);
                            points = points.subtract(deductionPoints);
                        }

                        if (!Boolean.TRUE.equals(testResult.getOutputComparisonResult())) {
                            BigDecimal deductionPoints = BigDecimal.valueOf(penaltyOutput);
                            deductionOutput = messageSource.getMessage("deduction.output", new Object[]{deductionPoints}, locale);
                            points = points.subtract(deductionPoints);
                        }

                        if (!Boolean.TRUE.equals(testResult.getDatabaseResult())) {
                            Set<String> affectedTables = new HashSet<>();
                            if (testResult.getMissingTuples() != null)
                                testResult.getMissingTuples().forEach(tuple -> affectedTables.add(tuple.getFirst()));
                            if (testResult.getSuperfluousTuples() != null)
                                testResult.getSuperfluousTuples().forEach(tuple -> affectedTables.add(tuple.getFirst()));

                            int tablePenaltyCount = Math.max(1, affectedTables.size());
                            BigDecimal deductionPoints = BigDecimal.valueOf(penaltyDatabase)
                                .multiply(BigDecimal.valueOf(tablePenaltyCount));
                            deductionDatabase = messageSource.getMessage("deduction.database", new Object[]{deductionPoints, tablePenaltyCount}, locale);
                            points = points.subtract(deductionPoints);
                        }

                        if (!Boolean.TRUE.equals(testResult.getExceptionResult())) {
                            BigDecimal deductionPoints = BigDecimal.valueOf(penaltyException);
                            deductionException = messageSource.getMessage("deduction.exception", new Object[]{deductionPoints}, locale);
                            points = points.subtract(deductionPoints);
                        }
                    }
                    if (points.compareTo(BigDecimal.ZERO) < 0) {
                        points = BigDecimal.ZERO;
                    }
                }
                break;
            }

            default: throw new IllegalStateException("Unexpected mode: " + submission.mode());
        }
        //Explanation of Deductions in Feedback-Level 3
        if (feedbackLevel >= 3 && (submission.mode() == SubmissionMode.SUBMIT || submission.mode() == SubmissionMode.DIAGNOSE)) {
            feedback += "<hr><br/><strong>" + messageSource.getMessage("deduction.heading", null, locale) + "</strong>";

            boolean anyDeduction = false;
            StringBuilder table = new StringBuilder("<table>");

            if (!"0".equals(deductionAutocommit)) {
                table.append("<tr><td>Autocommit: </td><td>").append(deductionAutocommit).append("</td></tr>");
                anyDeduction = true;
            }
            if (!"0".equals(deductionOutput)) {
                table.append("<tr><td>Output: </td><td>").append(deductionOutput).append("</td></tr>");
                anyDeduction = true;
            }
            if (!"0".equals(deductionDatabase)) {
                table.append("<tr><td>Database: </td><td>").append(deductionDatabase).append("</td></tr>");
                anyDeduction = true;
            }
            if (!"0".equals(deductionException)) {
                table.append("<tr><td>Exception handling: </td><td>").append(deductionException).append("</td></tr>");
                anyDeduction = true;
            }

            table.append("</table>");

            if (anyDeduction) {
                feedback += table.toString();
            } else {
                feedback += "<p>None</p><br/>";
            }
        }

        return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
    }

    /**
     * Creates a {@link CriterionDto} for a given evaluation aspect.
     *
     * @param key     The message key used for localization (e.g., "criterium.output").
     * @param passed  Whether the criterion has been passed.
     * @param detail  Additional detail for feedback.
     * @param locale  The locale to use for messages.
     * @param level   The feedback level.
     * @param result  The evaluation result.
     * @return The generated {@code CriterionDto}.
     */
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
                        message += messageSource.getMessage("criterium.database.missingCount", new Object[]{missing.size()}, locale);
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

    /**
     * Converts a list of tuple rows into an HTML table.
     *
     * @param tuples A list of rows, each represented as a list of strings.
     * @return The HTML representation of the table.
     */
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

    /**
     * Renders a list of {@link TableDump}s as HTML tables.
     *
     * @param dumps The list of table dumps to render.
     * @return A HTML string containing formatted tables or a fallback if no tables are found.
     */
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

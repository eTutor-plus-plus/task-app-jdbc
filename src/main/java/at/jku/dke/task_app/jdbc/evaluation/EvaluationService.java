package at.jku.dke.task_app.jdbc.evaluation;

import at.jku.dke.etutor.task_app.dto.CriterionDto;
import at.jku.dke.etutor.task_app.dto.GradingDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String feedback;

        //Unparsed
        String inputString = "";
        try {
            inputString = submission.submission().input();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            //error = ex;
        }
        System.out.println("Input: " + inputString + ", " + inputString.getClass().getSimpleName());
        //System.out.println("Solution: " + task.getSolution() + ", " + task.getSolution().getClass().getSimpleName() );
        System.out.println("GroupSetting: " + task.getTaskGroup().getSchema());
        System.out.println("Mode: " + submission.mode());

        //***  TEST DATA ***///
        //String studentInput = Solutions.studentInput;
        //String studentInput = Solutions.studentInputNoExceptionHandling;
        String studentInput = Solutions.studentInputWrongSyntax;
        //String studentInput = Solutions.studentInputAutocommitNotDisabled;

        String taskSolution = Solutions.taskSolution;
        String dbSchema = Solutions.dbSchema;
        Result testResult = AssessmentService.assessTask(studentInput, dbSchema, taskSolution);
        if (testResult == null) {
            throw new RuntimeException("Assessment failed – result is null");
        }
        System.out.println(testResult.toString());
        //***  END TEST DATA ***///


        // parse input (BS)

        Integer input = null;
        NumberFormatException error = null;
        try {
            input = Integer.parseInt(submission.submission().input());
        } catch (NumberFormatException ex) {
            error = ex;
        }

        Boolean syntaxResult = testResult.getSyntaxResult();
        String errorMsg = (testResult.getSyntaxError() != null && !testResult.getSyntaxError().isEmpty())
            ? testResult.getSyntaxError()
            : "Error Message not defined";
        if (syntaxResult == false) {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntax", null, locale),
                null,
                false,
                this.messageSource.getMessage("criterium.syntax.invalid", null, locale) + ": " + errorMsg));
            feedback = messageSource.getMessage("incorrect", null, locale);
            return new GradingDto(task.getMaxPoints(), points, feedback, criteria);

        } else {
            criteria.add(new CriterionDto(
                this.messageSource.getMessage("criterium.syntax", null, locale),
                null,
                true,
                this.messageSource.getMessage("criterium.syntax.valid", null, locale)));
        }

        // evaluate and grade
        switch (submission.mode()) {
            case RUN:
                feedback = this.messageSource.getMessage("input", new Object[]{submission.submission().input()}, locale);
                break;


            //I WORK ON THIS ->
            case DIAGNOSE:
                /*
                // === Add syntax result ===
                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.syntax", null, locale),
                    null,
                    Boolean.TRUE.equals(testResult.getSyntaxResult()),
                    Boolean.TRUE.equals(testResult.getSyntaxResult())
                        ? messageSource.getMessage("criterium.syntax.valid", null, locale)
                        : (testResult.getSyntaxError() != null && !testResult.getSyntaxError().isEmpty()
                        ? testResult.getSyntaxError()
                        : messageSource.getMessage("criterium.syntax.invalid", null, locale))
                ));
                */
                // === Add technical criteria from testResult ===
                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.autocommit", null, locale),
                    null,
                    Boolean.TRUE.equals(testResult.getAutoCommitResult()),
                    messageSource.getMessage(
                        Boolean.TRUE.equals(testResult.getAutoCommitResult())
                            ? "criterium.autocommit.valid"
                            : "criterium.autocommit.invalid",
                        null,
                        locale
                    )
                ));

                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.output", null, locale),
                    null,
                    Boolean.TRUE.equals(testResult.getOutputComparisionResult()),
                    messageSource.getMessage(
                        Boolean.TRUE.equals(testResult.getOutputComparisionResult())
                            ? "criterium.output.valid"
                            : "criterium.output.invalid",
                        null,
                        locale
                    )
                ));

                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.database", null, locale),
                    null,
                    Boolean.TRUE.equals(testResult.getDatabaseResult()),
                    messageSource.getMessage(
                        Boolean.TRUE.equals(testResult.getDatabaseResult())
                            ? "criterium.database.valid"
                            : "criterium.database.invalid",
                        null,
                        locale
                    )
                ));

                criteria.add(new CriterionDto(
                    messageSource.getMessage("criterium.exception", null, locale),
                    null,
                    Boolean.TRUE.equals(testResult.getExceptionResult()),
                    messageSource.getMessage(
                        Boolean.TRUE.equals(testResult.getExceptionResult())
                            ? "criterium.exception.valid"
                            : "criterium.exception.invalid",
                        null,
                        locale
                    )
                ));

                // === Overall feedback ===
                boolean allPassed = Boolean.TRUE.equals(testResult.getSyntaxResult())
                    && Boolean.TRUE.equals(testResult.getAutoCommitResult())
                    && Boolean.TRUE.equals(testResult.getOutputComparisionResult())
                    && Boolean.TRUE.equals(testResult.getDatabaseResult())
                    && Boolean.TRUE.equals(testResult.getExceptionResult());

                feedback = messageSource.getMessage(allPassed ? "correct" : "incorrect", null, locale);

                feedback += "\n" + testResult.getStudentQueryResult();



                // === Optional: assign full points if all checks passed ===
                if (allPassed) {
                    points = task.getMaxPoints();
                }

                break;


            /*
            case SUBMIT:
                if (error == null && input.equals(task.getSolution())) {
                    feedback = this.messageSource.getMessage("correct", null, locale);
                    points = task.getMaxPoints();
                } else
                    feedback = this.messageSource.getMessage("incorrect", null, locale);
                break;*/
            default:
                throw new IllegalStateException("Unexpected value: " + submission.mode());
        }

        return new GradingDto(task.getMaxPoints(), points, feedback, criteria);
    }
}

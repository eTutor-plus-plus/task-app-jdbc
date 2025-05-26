package at.jku.dke.task_app.jdbc.evaluation;

import at.jku.dke.etutor.task_app.dto.*;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTask;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskRepository;
import at.jku.dke.task_app.jdbc.dto.JDBCSubmissionDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import jakarta.persistence.EntityNotFoundException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private JDBCTaskRepository taskRepository;

    private MessageSource messageSource;
    private EvaluationService evaluationService;
    private JDBCTask mockTask;
    private JDBCTaskGroup mockGroup;
    private SubmitSubmissionDto<JDBCSubmissionDto> validSubmission;

    @BeforeEach
    void setup() {
        ResourceBundleMessageSource bundle = new ResourceBundleMessageSource();
        bundle.setBasename("messages");
        bundle.setDefaultEncoding("UTF-8");
        bundle.setUseCodeAsDefaultMessage(true);
        this.messageSource = bundle;

        this.evaluationService = new EvaluationService(taskRepository, messageSource);

        validSubmission = new SubmitSubmissionDto<>(
            "user1", "assign1", 1L, "de", SubmissionMode.SUBMIT, 3,
            new JDBCSubmissionDto("")
        );

        mockGroup = new JDBCTaskGroup(
            "CREATE TABLE books(book_id INT, status VARCHAR); CREATE TABLE loans(user_id INT, book_id INT, loan_date DATE);",
            "INSERT INTO books VALUES (1, 'available');",
            "INSERT INTO books VALUES (1, 'available');"
        );

        mockTask = new JDBCTask(BigDecimal.TEN, TaskStatus.DRAFT, mockGroup, "solution", "books,loans", "int book = 1;\nint user = 42;");
        mockTask.setAutocommitPenalty(1);
        mockTask.setWrongOutputPenalty(1);
        mockTask.setWrongDbContentPenalty(1);
        mockTask.setExceptionHandlingPenalty(1);
        mockTask.setCheckAutocommit(true);
    }

    @Test
    void testEvaluate_runMode_validSyntax() {
        var submission = new SubmitSubmissionDto<>(
            "user1", "assign1", 1L, "de", SubmissionMode.RUN, 3,
            new JDBCSubmissionDto("SELECT 1;")
        );

        Result result = new Result();
        result.setSyntaxResult(true);
        result.setStudentOutput("OK");
        result.setStudentQueryResult(List.of());
        result.setDatabaseBefore(List.of());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));

        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(false)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(submission);

            assertEquals(BigDecimal.ZERO, grading.points());
            assertTrue(grading.criteria().stream().anyMatch(c -> Boolean.TRUE.equals(c.passed())));
        }
    }

    @Test
    void testEvaluate_runMode_invalidSyntax() {
        var submission = new SubmitSubmissionDto<>(
            "user1", "assign1", 1L, "de", SubmissionMode.RUN, 3,
            new JDBCSubmissionDto("NOT VALID SQL")
        );

        Result result = new Result();
        result.setSyntaxResult(false);
        result.setSyntaxError("Syntax error");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));

        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(false)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(submission);

            assertEquals(BigDecimal.ZERO, grading.points());
            assertTrue(grading.generalFeedback().contains(messageSource.getMessage("incorrect", null, Locale.GERMAN)));
        }
    }

    @Test
    void testEvaluate_diagnoseMode_allCorrect() {
        Result result = new Result();
        result.setSyntaxResult(true);
        result.setAutoCommitResult(true);
        result.setOutputComparisonResult(true);
        result.setDatabaseResult(true);
        result.setExceptionResult(true);
        result.setStudentOutput("Correct");
        result.setStudentQueryResult(List.of());
        result.setDatabaseBefore(List.of());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(validSubmission);
            System.out.println(grading.toString());
            assertEquals(mockTask.getMaxPoints(), grading.points());
            assertTrue(grading.generalFeedback().contains(messageSource.getMessage("correct", null, Locale.GERMAN)));
        }
    }

    @Test
    void testEvaluate_taskNotFound_shouldThrowException() {
        var submission = new SubmitSubmissionDto<>(
            "user1", "assign1", 99L, "de", SubmissionMode.RUN, 3,
            new JDBCSubmissionDto("SELECT 1;")
        );

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> evaluationService.evaluate(submission));
    }

    @Test
    void testEvaluate_diagnoseMode_autocommitIncorrect() {
        Result result = new Result();
        result.setSyntaxResult(true);
        result.setAutoCommitResult(false);
        result.setOutputComparisonResult(true);
        result.setDatabaseResult(true);
        result.setExceptionResult(true);
        result.setStudentOutput("OK");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(validSubmission);

            assertTrue(grading.generalFeedback().toLowerCase().contains("autocommit"));
            assertTrue(grading.points().compareTo(mockTask.getMaxPoints()) < 0);
        }
    }

    @Test
    void testEvaluate_diagnoseMode_outputIncorrect() {
        Result result = new Result();
        result.setSyntaxResult(true);
        result.setAutoCommitResult(true);
        result.setOutputComparisonResult(false);
        result.setDatabaseResult(true);
        result.setExceptionResult(true);
        result.setStudentOutput("Wrong output");
        result.setStudentQueryResult(List.of());
        result.setDatabaseBefore(List.of());
        result.setMissingTuples(Collections.emptyList());
        result.setSuperfluousTuples(Collections.emptyList());
        result.setMissingOutputs(Collections.emptyList());
        result.setSuperfluousOutputs(Collections.emptyList());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(validSubmission);

            assertEquals(BigDecimal.valueOf(9), grading.points());
            Optional<CriterionDto> outputCrit = grading.criteria().stream()
                .filter(c -> c.name().toLowerCase().contains("ausgabe"))
                .findFirst();

            assertTrue(outputCrit.isPresent());
            assertFalse(outputCrit.get().passed());
        }
    }

    @Test
    void testEvaluate_diagnoseMode_databaseIncorrect() {
        Result result = new Result();
        result.setSyntaxResult(true);
        result.setAutoCommitResult(true);
        result.setOutputComparisonResult(true);
        result.setDatabaseResult(false);
        result.setExceptionResult(true);
        result.setMissingTuples(List.of(List.of("books", "1")));
        result.setSuperfluousTuples(List.of());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(validSubmission);

            assertTrue(grading.generalFeedback().toLowerCase().contains("database"));
            assertTrue(grading.points().compareTo(mockTask.getMaxPoints()) < 0);
        }
    }

    @Test
    void testEvaluate_diagnoseMode_exceptionIncorrect() {
        Result result = new Result();
        result.setSyntaxResult(true);
        result.setAutoCommitResult(true);
        result.setOutputComparisonResult(true);
        result.setDatabaseResult(true);
        result.setExceptionResult(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(validSubmission);

            assertTrue(grading.generalFeedback().toLowerCase().contains("exception"));
            assertTrue(grading.points().compareTo(mockTask.getMaxPoints()) < 0);
        }
    }

    @Test
    void testRenderTableDumps_shouldGenerateCorrectHtml() throws Exception {
        TableDump dump = new TableDump(
            "test_table",
            List.of("id", "name"),
            List.of(List.of("1", "Michael"), List.of("2", "Testuser"))
        );
        Method method = EvaluationService.class.getDeclaredMethod("renderTableDumps", List.class);
        method.setAccessible(true);

        String html = (String) method.invoke(evaluationService, List.of(dump));

        assertTrue(html.contains("Table: test_table"));
        assertTrue(html.contains("<th>id</th>"));
        assertTrue(html.contains("<td>Michael</td>"));
        assertTrue(html.contains("<td>Testuser</td>"));
    }

    @Test
    void testNegativePointsAreClampedToZero() {
        mockTask.setMaxPoints(BigDecimal.ONE);
        mockTask.setAutocommitPenalty(1);
        mockTask.setWrongOutputPenalty(1);
        mockTask.setWrongDbContentPenalty(1);
        mockTask.setExceptionHandlingPenalty(1);
        mockTask.setCheckAutocommit(true);

        SubmitSubmissionDto<JDBCSubmissionDto> submission = new SubmitSubmissionDto<>(
            "user", "assign", 1L, "de", SubmissionMode.SUBMIT, 3,
            new JDBCSubmissionDto("INSERT INTO books VALUES (99, 'fail');")
        );

        Result result = new Result();
        result.setSyntaxResult(true);
        result.setAutoCommitResult(false);
        result.setOutputComparisonResult(false);
        result.setDatabaseResult(false);
        result.setExceptionResult(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        try (MockedStatic<AssessmentService> service = mockStatic(AssessmentService.class)) {
            service.when(() -> AssessmentService.assessTask(any(), any(), any(), any(), any(), eq(true)))
                .thenReturn(result);

            GradingDto grading = evaluationService.evaluate(submission);
            assertEquals(BigDecimal.ZERO, grading.points());
        }
    }

}

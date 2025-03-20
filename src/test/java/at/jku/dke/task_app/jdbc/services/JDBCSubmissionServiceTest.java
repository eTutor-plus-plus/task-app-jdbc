package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.task_app.jdbc.data.entities.JDBCSubmission;
import at.jku.dke.task_app.jdbc.dto.JDBCSubmissionDto;
import at.jku.dke.task_app.jdbc.evaluation.EvaluationService;
import at.jku.dke.task_app.jdbc.services.JDBCSubmissionService;

import at.jku.dke.task_app.jdbc.services.JDBCSubmissionService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JDBCSubmissionServiceTest {

    @Test
    void createSubmissionEntity() {
        // Arrange
        SubmitSubmissionDto<JDBCSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new JDBCSubmissionDto("33"));
        JDBCSubmissionService service = new JDBCSubmissionService(null, null, null);

        // Act
        JDBCSubmission submission = service.createSubmissionEntity(dto);

        // Assert
        assertEquals(dto.submission().input(), submission.getSubmission());
    }

    @Test
    void mapSubmissionToSubmissionData() {
        // Arrange
        JDBCSubmission submission = new JDBCSubmission("33");
        JDBCSubmissionService service = new JDBCSubmissionService(null, null, null);

        // Act
        JDBCSubmissionDto dto = service.mapSubmissionToSubmissionData(submission);

        // Assert
        assertEquals(submission.getSubmission(), dto.input());
    }

    @Test
    void evaluate() {
        // Arrange
        var evalService = mock(EvaluationService.class);
        SubmitSubmissionDto<JDBCSubmissionDto> dto = new SubmitSubmissionDto<>("test-user", "test-quiz", 3L, "de", SubmissionMode.SUBMIT, 2, new JDBCSubmissionDto("33"));
        JDBCSubmissionService service = new JDBCSubmissionService(null, null, evalService);

        // Act
        var result = service.evaluate(dto);

        // Assert
        verify(evalService).evaluate(dto);
    }

}

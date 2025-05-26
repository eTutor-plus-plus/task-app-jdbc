package at.jku.dke.task_app.jdbc.data.entities;

import at.jku.dke.etutor.task_app.dto.SubmissionMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class JDBCSubmissionTest {

    @Test
    void testDefaultConstructor() {
        JDBCSubmission submission = new JDBCSubmission();
        assertNotNull(submission);
    }

    @Test
    void testConstructorWithOnlySubmissionText() {
        String expected = "SELECT * FROM books;";
        JDBCSubmission submission = new JDBCSubmission(expected);
        assertEquals(expected, submission.getSubmission());
    }

    @Test
    void testFullConstructor() {
        String userId = "user123";
        String assignmentId = "assignmentABC";
        String language = "de";
        int feedbackLevel = 3;
        SubmissionMode mode = SubmissionMode.SUBMIT;
        String submissionText = "DELETE FROM users;";
        JDBCTask task = new JDBCTask("SELECT 1;", "users");

        JDBCSubmission submission = new JDBCSubmission(userId, assignmentId, task, language, feedbackLevel, mode, submissionText);

        assertEquals(userId, submission.getUserId());
        assertEquals(assignmentId, submission.getAssignmentId());
        assertEquals(task, submission.getTask());
        assertEquals(language, submission.getLanguage());
        assertEquals(feedbackLevel, submission.getFeedbackLevel());
        assertEquals(mode, submission.getMode());
        assertEquals(submissionText, submission.getSubmission());
    }

    @Test
    void testGetSetSubmission() {
        JDBCSubmission submission = new JDBCSubmission();
        String expected = "UPDATE books SET status = 'borrowed' WHERE id = 1;";
        submission.setSubmission(expected);
        assertEquals(expected, submission.getSubmission());
    }
}

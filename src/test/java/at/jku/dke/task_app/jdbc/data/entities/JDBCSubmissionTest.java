package at.jku.dke.task_app.jdbc.data.entities;

import org.junit.jupiter.api.Test;

import at.jku.dke.task_app.jdbc.data.entities.JDBCSubmission;

import at.jku.dke.task_app.jdbc.data.entities.JDBCSubmission;

import static org.junit.jupiter.api.Assertions.*;

class JDBCSubmissionTest {

    @Test
    void testConstructor() {
        // Arrange
        var expected = "test";

        // Act
        var submission = new JDBCSubmission(expected);
        var actual = submission.getSubmission();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testGetSetSubmission() {
        // Arrange
        var submission = new JDBCSubmission();
        var expected = "test";

        // Act
        submission.setSubmission(expected);
        var actual = submission.getSubmission();

        // Assert
        assertEquals(expected, actual);
    }

}

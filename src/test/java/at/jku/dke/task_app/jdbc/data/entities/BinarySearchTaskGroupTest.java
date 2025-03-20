package at.jku.dke.task_app.jdbc.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class jdbcTaskGroupTest {

    @Test
    void testConstructor1() {
        // Arrange
        final int expectedMinNumber = 21;
        final int expectedMaxNumber = 42;

        // Act
        jdbcTaskGroup jdbcTaskGroup = new jdbcTaskGroup(expectedMinNumber, expectedMaxNumber);
        int actualMinNumber = jdbcTaskGroup.getMinNumber();
        int actualMaxNumber = jdbcTaskGroup.getMaxNumber();

        // Assert
        assertEquals(expectedMinNumber, actualMinNumber);
        assertEquals(expectedMaxNumber, actualMaxNumber);
    }

    @Test
    void testConstructor2() {
        // Arrange
        final TaskStatus status = TaskStatus.APPROVED;
        final int expectedMinNumber = 21;
        final int expectedMaxNumber = 42;

        // Act
        jdbcTaskGroup jdbcTaskGroup = new jdbcTaskGroup(status, expectedMinNumber, expectedMaxNumber);
        TaskStatus actualStatus = jdbcTaskGroup.getStatus();
        int actualMinNumber = jdbcTaskGroup.getMinNumber();
        int actualMaxNumber = jdbcTaskGroup.getMaxNumber();

        // Assert
        assertEquals(status, actualStatus);
        assertEquals(expectedMinNumber, actualMinNumber);
        assertEquals(expectedMaxNumber, actualMaxNumber);
    }

    @Test
    void testConstructor3() {
        // Arrange
        final long expectedId = 21;
        final TaskStatus status = TaskStatus.APPROVED;
        final int expectedMinNumber = 21;
        final int expectedMaxNumber = 42;

        // Act
        jdbcTaskGroup jdbcTaskGroup = new jdbcTaskGroup(expectedId, status, expectedMinNumber, expectedMaxNumber);
        long actualId = jdbcTaskGroup.getId();
        TaskStatus actualStatus = jdbcTaskGroup.getStatus();
        int actualMinNumber = jdbcTaskGroup.getMinNumber();
        int actualMaxNumber = jdbcTaskGroup.getMaxNumber();

        // Assert
        assertEquals(expectedId, actualId);
        assertEquals(status, actualStatus);
        assertEquals(expectedMinNumber, actualMinNumber);
        assertEquals(expectedMaxNumber, actualMaxNumber);
    }

    @Test
    void testGetSetMinNumber() {
        // Arrange
        jdbcTaskGroup jdbcTaskGroup = new jdbcTaskGroup();
        final int expected = 21;

        // Act
        jdbcTaskGroup.setMinNumber(expected);
        int actual = jdbcTaskGroup.getMinNumber();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testGetSetMaxNumber() {
        // Arrange
        jdbcTaskGroup jdbcTaskGroup = new jdbcTaskGroup();
        final int expected = 21;

        // Act
        jdbcTaskGroup.setMaxNumber(expected);
        int actual = jdbcTaskGroup.getMaxNumber();

        // Assert
        assertEquals(expected, actual);
    }

}

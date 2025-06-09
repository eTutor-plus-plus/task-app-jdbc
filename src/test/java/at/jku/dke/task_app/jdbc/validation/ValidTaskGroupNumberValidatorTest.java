package at.jku.dke.task_app.jdbc.validation;

import org.junit.jupiter.api.Test;

import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;
import at.jku.dke.task_app.jdbc.validation.ValidTaskGroupNumberValidator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidTaskGroupNumberValidatorTest {
    @Test
    void isValid() {
        // Arrange
        ValidTaskGroupNumberValidator validTaskGroupNumberValidator = new ValidTaskGroupNumberValidator();
        ModifyJDBCTaskGroupDto modifyJDBCTaskGroupDto = new ModifyJDBCTaskGroupDto("1", "2", "3");

        // Act
        boolean result = validTaskGroupNumberValidator.isValid(modifyJDBCTaskGroupDto, null);

        // Assert
        assertTrue(result);
    }
}

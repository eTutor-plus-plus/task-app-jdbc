package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTask;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskDto;
import at.jku.dke.task_app.jdbc.services.JDBCTaskService;

import at.jku.dke.task_app.jdbc.services.JDBCTaskService;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JDBCTaskServiceTest {

    @Test
    void createTask() {
        // Arrange
        ModifyTaskDto<ModifyJDBCTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskDto(33));
        JDBCTaskService service = new JDBCTaskService(null, null, null);

        // Act
        JDBCTask task = service.createTask(3, dto);

        // Assert
        assertEquals(dto.additionalData().solution(), task.getSolution());
    }

    @Test
    void createTaskInvalidType() {
        // Arrange
        ModifyTaskDto<ModifyJDBCTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyJDBCTaskDto(33));
        JDBCTaskService service = new JDBCTaskService(null, null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTask(3, dto));
    }

    @Test
    void updateTask() {
        // Arrange
        ModifyTaskDto<ModifyJDBCTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskDto(33));
        JDBCTaskService service = new JDBCTaskService(null, null, null);
        JDBCTask task = new JDBCTask(3);

        // Act
        service.updateTask(task, dto);

        // Assert
        assertEquals(dto.additionalData().solution(), task.getSolution());
    }

    @Test
    void updateTaskInvalidType() {
        // Arrange
        ModifyTaskDto<ModifyJDBCTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyJDBCTaskDto(33));
        JDBCTaskService service = new JDBCTaskService(null, null, null);
        JDBCTask task = new JDBCTask(3);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTask(task, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        MessageSource ms = mock(MessageSource.class);
        JDBCTaskService service = new JDBCTaskService(null, null, ms);
        JDBCTask task = new JDBCTask(3);
        task.setSolution(33);

        // Act
        TaskModificationResponseDto result = service.mapToReturnData(task, true);

        // Assert
        assertNotNull(result);
        verify(ms).getMessage("defaultTaskDescription", null, Locale.GERMAN);
        verify(ms).getMessage("defaultTaskDescription", null, Locale.ENGLISH);
    }

}

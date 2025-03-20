package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;
import at.jku.dke.task_app.jdbc.services.JDBCTaskGroupService;

import at.jku.dke.task_app.jdbc.services.JDBCTaskGroupService;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JDBCTaskGroupServiceTest {

    @Test
    void createTaskGroup() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto(1, 2));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);

        // Act
        var taskGroup = service.createTaskGroup(3, dto);

        // Assert
        assertEquals(dto.additionalData().minNumber(), taskGroup.getMinNumber());
        assertEquals(dto.additionalData().maxNumber(), taskGroup.getMaxNumber());
    }

    @Test
    void createTaskGroupInvalidType() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("sql", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto(1, 2));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTaskGroup(3, dto));
    }

    @Test
    void updateTaskGroup() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto(1, 2));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);
        var taskGroup = new JDBCTaskGroup(3, 4);

        // Act
        service.updateTaskGroup(taskGroup, dto);

        // Assert
        assertEquals(dto.additionalData().minNumber(), taskGroup.getMinNumber());
        assertEquals(dto.additionalData().maxNumber(), taskGroup.getMaxNumber());
    }

    @Test
    void updateTaskGroupInvalidType() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("sql", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto(1, 2));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);
        var taskGroup = new JDBCTaskGroup(3, 4);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTaskGroup(taskGroup, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        MessageSource ms = mock(MessageSource.class);
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, ms);
        var taskGroup = new JDBCTaskGroup(3, 4);

        // Act
        var result = service.mapToReturnData(taskGroup, true);

        // Assert
        assertNotNull(result);
        verify(ms).getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getMinNumber(), taskGroup.getMaxNumber()}, Locale.GERMAN);
        verify(ms).getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getMinNumber(), taskGroup.getMaxNumber()}, Locale.ENGLISH);
    }

}

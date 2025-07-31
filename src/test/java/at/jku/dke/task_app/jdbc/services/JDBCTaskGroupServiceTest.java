package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;


import at.jku.dke.task_app.jdbc.evaluation.TableDump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;

class JDBCTaskGroupServiceTest {

    @Mock private MessageSource ms;
    private JDBCTaskGroupService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new JDBCTaskGroupService(null, ms);
    }

    @Test
    void createTaskGroup() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("jdbc", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto("1", "2", "3"));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);

        // Act
        var taskGroup = service.createTaskGroup(3, dto);

        // Assert
        assertEquals(dto.additionalData().createStatements(), taskGroup.getCreateStatements());
        assertEquals(dto.additionalData().insertStatementsDiagnose(), taskGroup.getInsertStatementsDiagnose());
        assertEquals(dto.additionalData().insertStatementsSubmission(), taskGroup.getInsertStatementsSubmission());
    }

    @Test
    void createTaskGroupInvalidType() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("sql", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto("1", "2", "3"));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTaskGroup(3, dto));
    }

    @Test
    void updateTaskGroup() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("jdbc", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto("1", "2", "3"));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);
        var taskGroup = new JDBCTaskGroup(TaskStatus.APPROVED, "1", "2", "3");

        // Act
        service.updateTaskGroup(taskGroup, dto);

        // Assert
        assertEquals(dto.additionalData().createStatements(), taskGroup.getCreateStatements());
        assertEquals(dto.additionalData().insertStatementsDiagnose(), taskGroup.getInsertStatementsDiagnose());
        assertEquals(dto.additionalData().insertStatementsSubmission(), taskGroup.getInsertStatementsSubmission());
    }

    @Test
    void updateTaskGroupInvalidType() {
        // Arrange
        ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> dto = new ModifyTaskGroupDto<>("sql", TaskStatus.APPROVED, new ModifyJDBCTaskGroupDto("1", "2", "3"));
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, null);
        var taskGroup =new JDBCTaskGroup(TaskStatus.APPROVED, "1", "2", "3");

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTaskGroup(taskGroup, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        MessageSource ms = mock(MessageSource.class);
        JDBCTaskGroupService service = new JDBCTaskGroupService(null, ms);
        var taskGroup = new JDBCTaskGroup(TaskStatus.APPROVED, "1", "2", "3");

        // Act
        var result = service.mapToReturnData(taskGroup, true);

        // Assert
        assertNotNull(result);
        verify(ms).getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getCreateStatements()}, Locale.GERMAN);
        verify(ms).getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getCreateStatements()}, Locale.ENGLISH);
    }

    @Test
    void loadSchemaTableDumps_simpleSchema_returnsCorrectDump() throws Exception {
        String ddl = "CREATE TABLE test(id INT PRIMARY KEY, name VARCHAR(255));";
        when(ms.getMessage("table.header.name", null, Locale.ENGLISH)).thenReturn("Name");
        when(ms.getMessage("table.header.type", null, Locale.ENGLISH)).thenReturn("Type");
        when(ms.getMessage("table.header.extra", null, Locale.ENGLISH)).thenReturn("Extra");
        when(ms.getMessage("table.extra.pk", null, Locale.ENGLISH)).thenReturn("PK");

        Method m = JDBCTaskGroupService.class.getDeclaredMethod("loadSchemaTableDumps", String.class, Locale.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<TableDump> dumps = (List<TableDump>) m.invoke(service, ddl, Locale.ENGLISH);

        assertNotNull(dumps);
        assertEquals(1, dumps.size());
        TableDump td = dumps.get(0);
        assertEquals("TEST", td.tableName());
        List<String> headers = td.columns();
        assertEquals(List.of("Name", "Type", "Extra"), headers);
        List<List<String>> rows = td.rows();
        assertEquals(2, rows.size());
        // Column ID
        List<String> idRow = rows.get(0);
        assertEquals("ID", idRow.get(0));
        assertNotNull(idRow.get(1));
        assertFalse(idRow.get(1).isBlank(), "Type name for ID column should not be blank");
        assertEquals("PK", idRow.get(2));
        // Column NAME
        List<String> nameRow = rows.get(1);
        assertEquals("NAME", nameRow.get(0));
        assertNotNull(nameRow.get(1));
        assertFalse(nameRow.get(1).isBlank(), "Type name for NAME column should not be blank");
        assertEquals("", nameRow.get(2));
    }

    @Test
    void renderTableDumps_emptyList_showsNoSchema() {
        String html = service.renderTableDumps(List.of(), Locale.ENGLISH);
        assertTrue(html.contains("<p>No schema tables found</p>"));
    }

    @Test
    void renderTableDumps_withOneDump_generatesHtml() {
        TableDump dump = new TableDump(
            "TestTable",
            List.of("Col1", "Col2"),
            List.of(List.of("v1", "v2"))
        );
        when(ms.getMessage("table.label", null, Locale.ENGLISH)).thenReturn("Table");

        String html = service.renderTableDumps(List.of(dump), Locale.ENGLISH);
        assertTrue(html.contains("<b>Table TestTable</b>"));
        assertTrue(html.contains("<th>Col1</th>"));
        assertTrue(html.contains("<th>Col2</th>"));
        assertTrue(html.contains("<td style='padding:4px;'>v1</td>"));
        assertTrue(html.contains("<td style='padding:4px;'>v2</td>"));
    }
}

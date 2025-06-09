package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TableDumpTest {

    @Test
    public void tableDumpConstructionAndAccess() {
        List<String> columns = List.of("id", "name");
        List<List<String>> rows = List.of(
            List.of("1", "Test1"),
            List.of("2", "Test2")
        );

        TableDump dump = new TableDump("users", columns, rows);

        assertEquals("users", dump.tableName());
        assertEquals(columns, dump.columns());
        assertEquals(rows, dump.rows());
    }

    @Test
    public void equalityOfTables() {
        TableDump d1 = new TableDump(
            "products",
            List.of("id", "title"),
            List.of(List.of("101", "Laptop"))
        );

        TableDump d2 = new TableDump(
            "products",
            List.of("id", "title"),
            List.of(List.of("101", "Laptop"))
        );

        TableDump d3 = new TableDump(
            "orders",
            List.of("id", "total"),
            List.of(List.of("1", "99.99"))
        );

        assertEquals(d1, d2);
        assertNotEquals(d1, d3);
    }

    @Test
    public void toStringContainsTableName() {
        TableDump dump = new TableDump("employees", List.of("id"), List.of(List.of("1")));

        String str = dump.toString();

        assertTrue(str.contains("employees"));
        assertTrue(str.contains("id"));
        assertTrue(str.contains("1"));
    }
}

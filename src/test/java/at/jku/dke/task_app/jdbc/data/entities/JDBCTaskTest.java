package at.jku.dke.task_app.jdbc.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class JDBCTaskTest {

    @Test
    void constructorWithSolutionAndTables() {
        String solution = "SELECT * FROM books;";
        String tables = "books";
        JDBCTask task = new JDBCTask(solution, tables);

        assertEquals(solution, task.getSolution());
        assertEquals(tables, task.getTables());
    }

    @Test
    void fullConstructor() {
        BigDecimal points = BigDecimal.TEN;
        TaskStatus status = TaskStatus.APPROVED;
        JDBCTaskGroup group = new JDBCTaskGroup();
        String solution = "SELECT 1;";
        String tables = "users";
        String vars = "int x = 1;";

        JDBCTask task = new JDBCTask(points, status, group, solution, tables, vars);

        assertEquals(points, task.getMaxPoints());
        assertEquals(status, task.getStatus());
        assertEquals(group, task.getTaskGroup());
        assertEquals(solution, task.getSolution());
        assertEquals(tables, task.getTables());
        assertEquals(vars, task.getVariables());
    }

    @Test
    void gettersAndSetters() {
        JDBCTask task = new JDBCTask();

        task.setSolution("SOLUTION");
        task.setTables("table1, table2");
        task.setWrongOutputPenalty(2);
        task.setExceptionHandlingPenalty(3);
        task.setWrongDbContentPenalty(4);
        task.setCheckAutocommit(true);
        task.setAutocommitPenalty(5);
        task.setVariables("int a = 5;");

        assertEquals("SOLUTION", task.getSolution());
        assertEquals("table1, table2", task.getTables());
        assertEquals(2, task.getWrongOutputPenalty());
        assertEquals(3, task.getExceptionHandlingPenalty());
        assertEquals(4, task.getWrongDbContentPenalty());
        assertTrue(task.isCheckAutocommit());
        assertEquals(5, task.getAutocommitPenalty());
        assertEquals("int a = 5;", task.getVariables());
    }

    @Test
    void jdbcTaskConstructor_initializesFieldsCorrectly() {
        BigDecimal points = BigDecimal.TEN;
        TaskStatus status = TaskStatus.DRAFT;
        JDBCTaskGroup group = new JDBCTaskGroup("CREATE TABLE books(id INT);", "INSERT INTO books VALUES (1);", "INSERT INTO books VALUES (1);");
        Long id = 1L;
        String solution = "SELECT * FROM books;";
        String tables = "books";
        String variables = "int x = 1;";

        JDBCTask task = new JDBCTask(id, points, status, group, solution, tables, variables);

        assertEquals(id, task.getId());
        assertEquals(points, task.getMaxPoints());
        assertEquals(status, task.getStatus());
        assertEquals(group, task.getTaskGroup());
        assertEquals(solution, task.getSolution());
        assertEquals(tables, task.getTables());
        assertEquals(variables, task.getVariables());
    }

}

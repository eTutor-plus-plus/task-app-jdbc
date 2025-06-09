package at.jku.dke.task_app.jdbc.data.entities;

import at.jku.dke.etutor.task_app.dto.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JDBCTaskGroupTest {

    @Test
    void defaultConstructor() {
        JDBCTaskGroup group = new JDBCTaskGroup();
        assertNotNull(group);
    }

    @Test
    void constructorWithCreateAndInsertStatements() {
        String createStmt = "CREATE TABLE test (id INT);";
        String insertDiag = "INSERT INTO test VALUES (1);";
        String insertSub = "INSERT INTO test VALUES (2);";

        JDBCTaskGroup group = new JDBCTaskGroup(createStmt, insertDiag, insertSub);

        assertEquals(createStmt, group.getCreateStatements());
        assertEquals(insertDiag, group.getInsertStatementsDiagnose());
        assertEquals(insertSub, group.getInsertStatementsSubmission());
    }

    @Test
    void constructorWithStatusAndStatements() {
        TaskStatus status = TaskStatus.APPROVED;
        String createStmt = "CREATE TABLE example (x INT);";
        String insertDiag = "INSERT INTO example VALUES (10);";
        String insertSub = "INSERT INTO example VALUES (20);";

        JDBCTaskGroup group = new JDBCTaskGroup(status, createStmt, insertDiag, insertSub);

        assertEquals(status, group.getStatus());
        assertEquals(createStmt, group.getCreateStatements());
        assertEquals(insertDiag, group.getInsertStatementsDiagnose());
        assertEquals(insertSub, group.getInsertStatementsSubmission());
    }

    @Test
    void constructorWithIdStatusAndStatements() {
        long id = 123L;
        TaskStatus status = TaskStatus.DRAFT;
        String createStmt = "CREATE TABLE t (a INT);";
        String insertDiag = "INSERT INTO t VALUES (5);";
        String insertSub = "INSERT INTO t VALUES (6);";

        JDBCTaskGroup group = new JDBCTaskGroup(id, status, createStmt, insertDiag, insertSub);

        assertEquals(id, group.getId());
        assertEquals(status, group.getStatus());
        assertEquals(createStmt, group.getCreateStatements());
        assertEquals(insertDiag, group.getInsertStatementsDiagnose());
        assertEquals(insertSub, group.getInsertStatementsSubmission());
    }

    @Test
    void settersAndGetters() {
        JDBCTaskGroup group = new JDBCTaskGroup();
        String createStmt = "CREATE TABLE demo (id INT);";
        String insertDiag = "INSERT INTO demo VALUES (1);";
        String insertSub = "INSERT INTO demo VALUES (2);";

        group.setCreateStatements(createStmt);
        group.setInsertStatementsDiagnose(insertDiag);
        group.setInsertStatementsSubmission(insertSub);

        assertEquals(createStmt, group.getCreateStatements());
        assertEquals(insertDiag, group.getInsertStatementsDiagnose());
        assertEquals(insertSub, group.getInsertStatementsSubmission());
    }
}

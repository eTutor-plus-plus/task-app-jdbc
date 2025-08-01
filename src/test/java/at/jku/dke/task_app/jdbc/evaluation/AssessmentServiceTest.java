package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentServiceTest {

    private static final String schema = """
        CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255));
        INSERT INTO users (id, name) VALUES (1, 'Michael');
        """;
    private static final String[] tables = {"users"};

    private static final String validCode = """
        try (var con = ds.getConnection()) {
            con.setAutoCommit(false);
            try (var st = con.createStatement()) {
                st.executeUpdate("INSERT INTO users (id, name) VALUES (99, 'Test')");
                con.commit();
            }
        } catch (Exception e) {
        }
        """;

    private static final String validCodeWithOutput = """
        try (var con = ds.getConnection()) {
            con.setAutoCommit(false);
            try (var st = con.createStatement()) {
                st.executeUpdate("INSERT INTO users (id, name) VALUES (99, 'Test')");
                Out.println("Inserted");
                con.commit();
            }
        } catch (Exception e) {
            Out.println("DB ERROR");
        }
        """;

    private static final String faultySyntaxCode = """
        try (var con = ds.getConnection()) {
            try (var st = con.createStatement()) {
                st.executeUpdate("INSERT INTO users (id, name) VALUES (99, 'Test')");
        """;

    private static final String codeWithNoAutocommit = """
        try (var con = ds.getConnection()) {
            try (var st = con.createStatement()) {
                st.executeUpdate("INSERT INTO users (id, name) VALUES (100, 'NoAuto')");
            }
        } catch (Exception e) {
            Out.println("DB ERROR");
        }
        """;

    private static final String codeWithNoChange = """
        try (var con = ds.getConnection()) {
            con.setAutoCommit(false);
            try (var st = con.createStatement()) {
                st.executeUpdate("DELETE FROM users WHERE id = 999");
                con.commit();
            }
        } catch (Exception e) {
            Out.println("DB ERROR");
        }
        """;

    @Test
    void assessTask_validCode() {
        String code = addUniqueClassName(validCode, "Valid" + UUID.randomUUID());
        Result result = AssessmentService.assessTask(code, schema, code, tables, "", true);

        assertNotNull(result);
        assertTrue(result.getSyntaxResult());
        assertTrue(result.getAutoCommitResult());
        assertTrue(result.getOutputComparisonResult());
        assertTrue(result.getDatabaseResult());
        assertTrue(result.getExceptionResult());
    }

    @Test
    void assessTask_syntaxError() {
        String code = addUniqueClassName(faultySyntaxCode, "Faulty" + UUID.randomUUID());
        Result result = AssessmentService.assessTask(code, schema, code, tables, "", true);

        assertNotNull(result);
        assertFalse(result.getSyntaxResult());
        assertNotNull(result.getSyntaxError());
    }

    @Test
    void assessTask_autocommitNotSet() {
        String code = addUniqueClassName(codeWithNoAutocommit, "NoAuto" + UUID.randomUUID());
        Result result = AssessmentService.assessTask(code, schema, code, tables, "", true);

        assertNotNull(result);
        assertTrue(result.getSyntaxResult());
        assertFalse(result.getAutoCommitResult());
    }

    @Test
    void assessTask_databaseMismatch() {
        String code = addUniqueClassName(codeWithNoChange, "DbMismatch" + UUID.randomUUID());
        String solution = addUniqueClassName(validCode, "Ref" + UUID.randomUUID());

        Result result = AssessmentService.assessTask(code, schema, solution, tables, "", true);

        assertNotNull(result);
        assertTrue(result.getSyntaxResult());
        assertFalse(result.getDatabaseResult());
        System.out.println("Missing: " + result.getMissingTuples().toString());
        System.out.println("Super: " + result.getSuperfluousTuples().toString());
        assertFalse(result.getMissingTuples().isEmpty());
    }

    @Test
    void assessTask_outputMismatch() {
        String studentCode = addUniqueClassName(validCodeWithOutput, "OutputMismatch" + UUID.randomUUID());
        String solutionCode = addUniqueClassName(validCode, "Ref" + UUID.randomUUID());

        Result result = AssessmentService.assessTask(studentCode, schema, solutionCode, tables, "", true);

        assertNotNull(result);
        assertFalse(result.getOutputComparisonResult());
        assertFalse(result.getSuperfluousOutputs().isEmpty());
    }

    private static String addUniqueClassName(String baseCode, String className) {
        return baseCode.replace("public class Template", "public class Template" + className);
    }

    @Test
    void assessTask_runMode_onlySyntaxCheck() {
        String code = addUniqueClassName(validCodeWithOutput, "RunOnly" + UUID.randomUUID());
        Result result = AssessmentService.assessTask(code, schema, "", tables, "", false);
        assertNotNull(result);
        assertTrue(result.getSyntaxResult());
        assertNotNull(result.getStudentOutput());
        assertNotNull(result.getDatabaseBefore());
        assertNotNull(result.getStudentQueryResult());
    }
}

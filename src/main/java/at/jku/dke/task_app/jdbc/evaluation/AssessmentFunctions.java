package at.jku.dke.task_app.jdbc.evaluation;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.h2.jdbcx.JdbcDataSource;

/**
 * Provides utility functions for dynamically compiling, executing,
 * and verifying student SQL-related Java code using an in-memory H2 database.
 *
 * The class provides methods for:
 * - Syntax checking by compiling generated code
 * - Comparing output from student and solution code
 * - Capturing and comparing database states
 */

public class AssessmentFunctions {

    private final JdbcDataSource ds;

    /**
     * Initializes the internal shared in-memory H2 database.
     * This datasource is reused for methods that do not receive a specific URL.
     */
    public AssessmentFunctions() {
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:shared_db;DB_CLOSE_DELAY=-1;MODE=Oracle");
        ds.setUser("sa");
        ds.setPassword("");
    }

    /**
     * Checks the syntax of the students Java code after embedding it into the
     * template and attempting to compile it.
     *
     * @param code   the Java code provided by the student
     * @param result the Result object to fill in the syntax check info
     * @return true if the syntax is correct, false if there are errors
     */
    public boolean checkSyntax(String code, Result result) {
        try {
            // Load the Template.java file from the classpath as a string
            String template = new String(getClass().getClassLoader().getResourceAsStream("Template.java").readAllBytes(), StandardCharsets.UTF_8);

            // Replace placeholders in the template:
            // - "public class Template" -> "public class SyntaxCheck" to give a unique class name
            // - "/*<DB_URL>*/" -> in-memory H2 database connection URL
            template = template.replace("public class Template", "public class SyntaxCheck")
                .replace("/*<DB_URL>*/", "jdbc:h2:mem:shared_db;DB_CLOSE_DELAY=-1;MODE=Oracle");

            // Integrate the student's code into the template
            String integrated = template.replace("/*<StudentInput> */", code);

            // Create a JavaFileObject for the integrated code, which will be compiled in-memory
            JavaFileObject file = new MemoryJavaFile("at.jku.dke.task_app.jdbc.SyntaxCheck", integrated);

            // Set the default locale to English for any error messages
            Locale.setDefault(Locale.ENGLISH);

            // Initialize the Java compiler and diagnostics collector
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            // Use the custom file manager to compile in-memory, instead of using the standard file manager
            try (StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null)) {
                // Initialize the custom file manager that handles in-memory compilation
                MemoryJavaFile.FileManagerMap fileManager = new MemoryJavaFile.FileManagerMap(stdManager);
                //Compilation options (clear warnings)

                List<String> options = List.of(
                    "-proc:none",
                    "-Xlint:-options"
                );
                // Compile the student's code
                JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fileManager, diagnostics, options, null, List.of(file)
                );

                // Call the compilation task
                boolean success = task.call();

                // If compilation fails, gather the error messages and set the result accordingly
                if (!success) {
                    StringBuilder errorMessage = new StringBuilder();
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        int lineNumberModifier = 25; //Lines before the template is inserted
                        long lineNumber = diagnostic.getLineNumber() - lineNumberModifier;
                        lineNumber = lineNumber >= 0 ? lineNumber : 0;

                        errorMessage
                            .append("Syntax error")
                            .append(": ")
                            .append(diagnostic.getMessage(null))
                            .append(" in Line ")
                            .append(lineNumber)
                            .append("\n");
                    }
                    String test = "";
                    test += diagnostics.getDiagnostics().getFirst().getLineNumber();

                    // Update the result with the error details
                    result.setSyntaxResult(false);
                    result.setSyntaxMessage("Syntax error");
                    result.setSyntaxError(errorMessage.toString());
                    return false;
                }

                // If no errors were found, set the result as successful
                result.setSyntaxResult(true);
                result.setSyntaxMessage("Syntax correct");
                result.setSyntaxError(null);
                return true;
            }
        } catch (Exception e) {
            // Handle any unexpected exceptions and set the result accordingly
            result.setSyntaxResult(false);
            result.setSyntaxMessage("Failed to compile");
            result.setSyntaxError(e.getMessage());
            return false;
        }
    }

    /**
     * Compares the output of student and solution executions.
     *
     * @param studentOutput  the output produced by the student code
     * @param solutionOutput the expected output from the solution
     * @return true if both outputs match exactly (ignoring leading/trailing
     *         whitespace)
     */
    public boolean checkOutput(String studentOutput, String solutionOutput) {
        return studentOutput.trim().equals(solutionOutput.trim());
    }

    /**
     * Returns a string representation of the current database state for the
     * specified H2 database connection URL.
     * Includes table names, column headers, and all row values.
     *
     * @param dbUrl the JDBC URL of the H2 database
     * @return the formatted database content
     */
    public String getCurrentDbState(String dbUrl, String[] tablesToCheck) {
        StringBuilder sb = new StringBuilder();

        try (Connection con = DriverManager.getConnection(dbUrl, "sa", "")) {
            DatabaseMetaData metaData = con.getMetaData();

            for (String tableName : tablesToCheck) {
                sb.append("Table: ").append(tableName).append("\n");

                // Check if the table actually exists
                try (ResultSet tableExists = metaData.getTables(null, "PUBLIC", tableName.toUpperCase(), new String[]{"TABLE"})) {
                    if (!tableExists.next()) {
                        sb.append("Table ").append(tableName).append(" does not exist.\n\n");
                        continue;
                    }
                }

                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY 1")) {

                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();

                    // Column headers
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(rsMeta.getColumnName(i)).append(", ");
                    }
                    sb.append("\n");

                    // Rows
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            sb.append(rs.getObject(i)).append(", ");
                        }
                        sb.append("\n");
                    }
                    sb.append("\n");
                } catch (SQLException e) {
                    sb.append("Error reading table ").append(tableName).append(": ").append(e.getMessage()).append("\n\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Compares two database state dumps after normalizing out timestamps.
     *
     * This avoids mismatches caused by differing dates/times.
     *
     * @param studentQueryResult  the database dump after student code execution
     * @param solutionQueryResult the database dump after solution execution
     * @return true if both states match, false otherwise
     */

    public boolean compareDbStates(String studentQueryResult, String solutionQueryResult) {
        // Normalize the student's query result by removing timestamps formatted as 'yyyy-MM-dd HH:mm:ss.SSS'
        String normalizedStudent = studentQueryResult.replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d", "");
        // Normalize the solution's query result by removing timestamps.
        String normalizedSolution = solutionQueryResult.replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d",
                "");
        // Compare the normalized strings (ignoring timestamp differences)
        return normalizedStudent.equals(normalizedSolution);
    }

    public void analyzeTupleDifferences(String studentDbUrl, String solutionDbUrl, Result result, String[] tables) {
        List<List<String>> studentTuples = getDatabaseContentAsTuples(studentDbUrl, tables);
        List<List<String>> solutionTuples = getDatabaseContentAsTuples(solutionDbUrl, tables);

        List<List<String>> missing = new ArrayList<>(solutionTuples);
        missing.removeAll(studentTuples);

        List<List<String>> superfluous = new ArrayList<>(studentTuples);
        superfluous.removeAll(solutionTuples);

        result.setMissingTuples(missing);
        result.setSuperfluousTuples(superfluous);

        StringBuilder msg = new StringBuilder();
        if (!missing.isEmpty())
            msg.append("Missing tuples (").append(missing.size()).append(")");
        if (!superfluous.isEmpty())
            msg.append(" Superfluous tuples (").append(superfluous.size()).append(")");

        result.setDatabaseMessage(msg.toString().trim());
    }

    public List<List<String>> getDatabaseContentAsTuples(String dbUrl, String[] tablesToCheck) {
        List<List<String>> tuples = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(dbUrl, "sa", "")) {
            for (String tableName : tablesToCheck) {
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY 1")) {

                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();

                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            Object val = rs.getObject(i);
                            row.add(val != null ? val.toString() : "NULL");
                        }
                        tuples.add(row);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tuples;
    }


    public List<TableDump> getDatabaseContent(String dbUrl, String[] tables) {
        List<TableDump> result = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(dbUrl, "sa", "")) {
            for (String tableName : tables) {
                List<String> columns = new ArrayList<>();
                List<List<String>> rows = new ArrayList<>();

                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " ORDER BY 1")) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    for (int i = 1; i <= colCount; i++)
                        columns.add(meta.getColumnName(i));

                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            row.add(val != null ? val.toString() : "NULL");
                        }
                        rows.add(row);
                    }
                    result.add(new TableDump(tableName, columns, rows));
                } catch (SQLException e) {
                    result.add(new TableDump(tableName, List.of(), List.of(List.of("Error: " + e.getMessage()))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB read error: " + e.getMessage(), e);
        }

        return result;
    }






}

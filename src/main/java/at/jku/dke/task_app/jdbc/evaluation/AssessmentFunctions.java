package at.jku.dke.task_app.jdbc.evaluation;

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
 * This class contains functions used for evaluating a student's JDBC task. It includes methods for:
 * - Checking syntax correctness
 * - Comparing the student's output with the reference solution
 * - Analyzing and comparing the database content between the student's and the solution's code
 * - Retrieving the content of database tables as tuples
 */
public class AssessmentFunctions {

    private final JdbcDataSource ds;

    /**
     * Initializes the AssessmentFunctions instance and sets up the H2 database connection.
     */
    public AssessmentFunctions() {
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:shared_db;DB_CLOSE_DELAY=-1;MODE=Oracle");
        ds.setUser("sa");
        ds.setPassword("");
    }

    /**
     * Checks the syntax of the student's code.
     * The method integrates the student's code into a template and attempts to compile it in memory.
     *
     * @param code The student's code to be checked.
     * @param variables Variables that are inserted into the code.
     * @param result A Result object that holds the outcome of the syntax check.
     * @return {@code true} if the code compiles successfully, {@code false} if there are syntax errors.
     */
    public boolean checkSyntax(String code, String variables, Result result) {
        try {
            // Load the Template from the Template class
            String template = new Template().getTemplate();

            // Count how many lines are before the students code to get the correct line number later
            int variableLineCount = variables.split("\n", -1).length;
            int linesBeforeStudentCode = countLinesBefore(template, "/*<StudentInput> */");
            int lineNumberModifier = linesBeforeStudentCode - 1 - 1 + variableLineCount;

            // Replace placeholders in the template:
            template = template.replace("public class Template", "public class SyntaxCheck")
                .replace("/*<DB_URL>*/", "jdbc:h2:mem:shared_db;DB_CLOSE_DELAY=-1;MODE=Oracle");

            // Integrate the student's code into the template
            String integrated = template
                .replace("/*<Variables>*/", variables)
                .replace("/*<StudentInput> */", code);
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
            result.setSyntaxResult(false);
            result.setSyntaxMessage("Failed to compile");
            result.setSyntaxError(e.getMessage());
            return false;
        }
    }

    /**
     * Compares the output of the student's code with the expected output of the solution.
     *
     * @param studentOutput The output generated by the student's code.
     * @param solutionOutput The expected output (reference solution output).
     * @return {@code true} if both outputs are the same, {@code false} otherwise.
     */
    public boolean checkOutput(String studentOutput, String solutionOutput) {
        return studentOutput.trim().equals(solutionOutput.trim());
    }

    /**
     * Analyzes the differences between the student's and the solution's database content.
     * It compares the rows of each table and identifies any missing or superfluous tuples.
     *
     * @param studentDbUrl The URL of the student's database.
     * @param solutionDbUrl The URL of the solution's database.
     * @param result A  Result object that holds the outcome of the analysis.
     * @param tables An array of table names to compare.
     */
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

    /**
     * Retrieves the content of the specified database tables as tuples.
     * Each tuple represents a row in the database.
     *
     * @param dbUrl The URL of the database.
     * @param tablesToCheck An array of table names to retrieve data from.
     * @return A list of tuples representing the rows of the specified tables.
     */
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

    /**
     * Retrieves the content of the specified database tables and returns it as a list of TableDump objects.
     * Each TableDump object represents a table with its columns and rows.
     *
     * @param dbUrl The URL of the database.
     * @param tables An array of table names to retrieve data from.
     * @return A list of TableDump objects containing the data from the specified tables.
     */
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

    /**
     * Counts the number of lines in the template before a given marker.
     */
    private int countLinesBefore(String template, String marker) {
        String[] lines = template.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(marker)) {
                return i + 1; // +1 to include the line with the marker itself
            }
        }
        return 0;
    }
}

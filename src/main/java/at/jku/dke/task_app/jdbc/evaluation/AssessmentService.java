package at.jku.dke.task_app.jdbc.evaluation;

import javax.tools.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * The AssessmentService class is responsible for compiling, executing,
 * and assessing student code against a provided solution.
 * This service evaluates the student's code by performing syntax checks,
 * comparing the output and database state with the solution, and checking
 * exception handling during execution.
 */
public class AssessmentService {
    /**
     * Main entry point for assessing a student's code.
     * This method performs the following:
     * - Checks the syntax of the student's code
     * - Compiles the student code and the solution code
     * - Executes both codes, capturing and comparing the output
     * - Compares the database states of the student and solution
     * - Verifies if exceptions are handled properly in the student's code
     *
     * @param studentInput The Java code provided by the student
     * @param dbSchema The schema for the H2 in-memory database
     * @param taskSolution The reference solution for comparison
     * @return A Result object containing evaluation results, including syntax, output, database state, and exception handling
     */
    public static Result assessTask(String studentInput, String dbSchema, String taskSolution, String[] tables, boolean analyze){
        //long startTimestamp = System.currentTimeMillis();
        DriverManager.setLoginTimeout(5);
        Result evalResult = new Result();
        AssessmentFunctions assessment = new AssessmentFunctions();
        String dbId = UUID.randomUUID().toString().replace("-", "_");
        String studentDbUrl = "jdbc:h2:mem:student_" + dbId + ";DB_CLOSE_DELAY=-1;MODE=Oracle";
        String solutionDbUrl = "jdbc:h2:mem:solution_" + dbId + ";DB_CLOSE_DELAY=-1;MODE=Oracle";

        // 1. Syntax Check
        if (!assessment.checkSyntax(studentInput, evalResult)) {
            System.out.println(evalResult.getSyntaxError());
            //System.out.println("Execution Time: " + (System.currentTimeMillis() - startTimestamp) + " ms");
            return evalResult;
        }

        evalResult.setSyntaxResult(true);
        evalResult.setSyntaxMessage("Syntax Correct");
        try {
            // 2. Compile and integrate student and solution code into templates
            ClassLoader classLoader = AssessmentService.class.getClassLoader();
            InputStream stream = classLoader.getResourceAsStream("Template.java");
            String template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            String integratedStudent = template
                .replace("/*<StudentInput> */", studentInput)
                .replace("public class Template", "public class TemplateStudent")
                .replace("/*<DB_URL>*/", studentDbUrl);

            String integratedSolution = template
                .replace("/*<StudentInput> */", taskSolution)
                .replace("public class Template", "public class TemplateSolution")
                .replace("/*<DB_URL>*/", solutionDbUrl);

            // 3. Compile student and solution code in memory
            Map<String, byte[]> compiledStudentClasses = compileJavaInMemory("at.jku.dke.task_app.jdbc.TemplateStudent", integratedStudent);
            Map<String, byte[]> compiledSolutionClasses = compileJavaInMemory("at.jku.dke.task_app.jdbc.TemplateSolution", integratedSolution);

            if (!analyze) {
                // Nur Syntax prüfen und ausführen. Keine Bewertung
                resetDatabase(studentDbUrl, dbSchema);
                String studentOutput = CodeRunner.runCode("at.jku.dke.task_app.jdbc.TemplateStudent", compiledStudentClasses);
                evalResult.setStudentOutput(studentOutput);
                evalResult.setStudentQueryResult(assessment.getDatabaseContent(studentDbUrl, tables));
                return evalResult;
            }

            // 4. Check for disabled autocommit in student's code
            // Remove comments and whitespaces
            String cleaned = studentInput
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)^\\s*//.*$", "")
                .replaceAll("\\s+", " ");

            // Find position of setAutoCommit(false)
            int autoCommitIndex = cleaned.indexOf(".setAutoCommit(false);");

            // Find database operations before disabling autocommit
            boolean hasWriteBefore = false;
            if (autoCommitIndex > 0) {
                String before = cleaned.substring(0, autoCommitIndex).toLowerCase();
                hasWriteBefore = before.contains("insert") || before.contains("update") || before.contains("delete");
            }
            boolean disabledAutoCommit = autoCommitIndex != -1 && !hasWriteBefore;

            evalResult.setAutoCommitResult(disabledAutoCommit);
            evalResult.setAutoCommitMessage(disabledAutoCommit ? "Autocommit disabled" : "Autocommit enabled");

            // 5. Execute student code and capture the output
            resetDatabase(studentDbUrl, dbSchema);
            String studentOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateStudent", compiledStudentClasses
            );
            //System.out.println("Student: " + studentOutput);
            //String studentDbState = assessment.getCurrentDbState(studentDbUrl, tables);
            //String studentDbContent = assessment.dbUrlToToString(studentDbUrl, tables);
            //evalResult.setStudentQueryResult(studentDbContent);

            List<TableDump> studentDbTables = assessment.getDatabaseContent(studentDbUrl, tables);
            evalResult.setStudentQueryResult(studentDbTables);

            // 6. Execute solution code and compare outputs
            resetDatabase(solutionDbUrl, dbSchema);
            String solutionOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateSolution", compiledSolutionClasses
            );
            //System.out.println("Solution: " + solutionOutput);
            boolean outputRes = assessment.checkOutput(studentOutput, solutionOutput);

            evalResult.setStudentOutput(studentOutput);

            evalResult.setOutputComparisionResult(outputRes);
            evalResult.setOutputComparisionMessage(outputRes ? "Output correct" : "Output incorrect");

            // Split lines
            List<String> studentLines = Arrays.stream(studentOutput.split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            List<String> solutionLines = Arrays.stream(solutionOutput.split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList();

            List<String> missing = new ArrayList<>(solutionLines);
            missing.removeAll(studentLines);

            List<String> extra = new ArrayList<>(studentLines);
            extra.removeAll(solutionLines);

            evalResult.setMissingOutputs(missing);
            evalResult.setSuperfluousOutputs(extra);


            // 7. Compare database states between student and solution
            resetDatabase(solutionDbUrl, dbSchema);
            solutionOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateSolution", compiledSolutionClasses
            );
            //String solutionDbState = assessment.getCurrentDbState(solutionDbUrl, tables);
            //assessment.analyzeTupleDifferences(studentDbState, solutionDbState, evalResult);

            //boolean dbResult = assessment.compareDbStates(studentDbState, solutionDbState);
            //evalResult.setDatabaseResult(dbResult);
            //evalResult.setDatabaseMessage(dbResult ? "Database content correct" : "Incorrect Database Content");

            assessment.analyzeTupleDifferences(studentDbUrl, solutionDbUrl, evalResult, tables);

            //assessment.analyzeTupleDifferences(studentDbState, solutionDbState, evalResult);

            boolean dbResult = evalResult.getMissingTuples().isEmpty() && evalResult.getSuperfluousTuples().isEmpty();
            evalResult.setDatabaseResult(dbResult);

            if (dbResult) {
                evalResult.setDatabaseMessage("Database content correct");
            } else {
                int missingCount = evalResult.getMissingTuples().size();
                int extraCount = evalResult.getSuperfluousTuples().size();
                evalResult.setDatabaseMessage("Incorrect Database Content: "
                    + missingCount + " missing, "
                    + extraCount + " extra tuples.\n" +
                    "Missing: " + evalResult.getMissingTuples() + "\n" +
                    "Extra: " + evalResult.getSuperfluousTuples());
            }

            // 8. Exception Handling Test

            String integratedStudentFaulty = template
                .replace("/*<StudentInput> */", studentInput)
                .replace("public class Template", "public class TemplateStudentFaulty")
                .replace("/*<DB_URL>*/", "jdbc:h2:tcp://10.255.255.1:4040/mem:test");

            Map<String, byte[]> compiledFaultyClasses = compileJavaInMemory(
                "at.jku.dke.task_app.jdbc.TemplateStudentFaulty",
                integratedStudentFaulty
            );
            String faultyOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateStudentFaulty",
                compiledFaultyClasses
            );

            System.out.println("Exception Test Output:\n" + faultyOutput);


            // 9. Get exception handling result
            if (faultyOutput.contains("DB ERROR")) {
                evalResult.setExceptionResult(true);
                evalResult.setExceptionMessage("Exceptions handled correctly");
            } else if (faultyOutput.contains("Student code crashed")) {
                evalResult.setExceptionResult(false);
                evalResult.setExceptionMessage("Exceptions caught by template");
            } else if (faultyOutput.contains("java.net.UnknownHostException") || faultyOutput.contains("java.sql.SQLException")) {
                evalResult.setExceptionResult(false);
                evalResult.setExceptionMessage("java.sql.SQLException thrown");
            } else if (faultyOutput.strip().isEmpty()) {
                evalResult.setExceptionResult(false);
                evalResult.setExceptionMessage("Unclear");
            } else {
                evalResult.setExceptionResult(false);
                evalResult.setExceptionMessage("Unrecognized");
            }

            //System.out.println("Execution Time: " + (System.currentTimeMillis() - startTimestamp) + " ms");
            return evalResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compiles the provided Java source code in-memory.
     *
     * @param className The name of the class to be compiled
     * @param javaSource The source code of the Java class
     * @return A map containing the compiled class bytecode
     */
    private static Map<String, byte[]> compileJavaInMemory(String className, String javaSource) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Java compiler not found.");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        MemoryJavaFile sourceFile = new MemoryJavaFile(className, javaSource);

        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        MemoryJavaFile.FileManagerMap fileManager = new MemoryJavaFile.FileManagerMap(stdFileManager);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(sourceFile));
        boolean success = task.call();

        if (!success) {
            diagnostics.getDiagnostics().forEach(d -> System.err.println(d.getMessage(null)));
            throw new RuntimeException("Compilation failed.");
        }

        return fileManager.getAllClassBytes();
    }

     /**
     * Resets the database schema for the given database URL by truncating all tables.
     * This method is faster than dropping and re-creating all database objects.
     *
     * @param dbUrl  The JDBC URL of the H2 in-memory database to reset
     * @param schema The schema definition and initial data as a semicolon-separated SQL string
     * @throws SQLException If a database access error occurs
     */
    private static void resetDatabase(String dbUrl, String schema) throws SQLException {
        try (Connection con = DriverManager.getConnection(dbUrl, "sa", ""); Statement stmt = con.createStatement()) {
            stmt.execute("DROP ALL OBJECTS;");
            for (String sql : schema.split(";")) {
                if (!sql.strip().isEmpty()) stmt.execute(sql.strip());
            }
        }
    }
}

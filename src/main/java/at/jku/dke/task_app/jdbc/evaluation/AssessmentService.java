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
    public static Result assessTask(String studentInput, String dbSchema, String taskSolution, String[] tables) {
        long startTimestamp = System.currentTimeMillis();
        Result evalResult = new Result();
        AssessmentFunctions assessment = new AssessmentFunctions();

        // 1. Syntax Check
        if (!assessment.checkSyntax(studentInput, evalResult)) {
            System.out.println(evalResult.getSyntaxError());
            System.out.println("Execution Time: " + (System.currentTimeMillis() - startTimestamp) + " ms");
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
                .replace("/*<DB_URL>*/", "jdbc:h2:mem:student_db;DB_CLOSE_DELAY=-1;MODE=Oracle");

            String integratedSolution = template
                .replace("/*<StudentInput> */", taskSolution)
                .replace("public class Template", "public class TemplateSolution")
                .replace("/*<DB_URL>*/", "jdbc:h2:mem:solution_db;DB_CLOSE_DELAY=-1;MODE=Oracle");

            // 3. Compile student and solution code in memory
            Map<String, byte[]> compiledStudentClasses = compileJavaInMemory("at.jku.dke.task_app.jdbc.TemplateStudent", integratedStudent);
            Map<String, byte[]> compiledSolutionClasses = compileJavaInMemory("at.jku.dke.task_app.jdbc.TemplateSolution", integratedSolution);

            // 4. Check for disabled autocommit in student's code
            String cleanedInput = studentInput
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)^\\s*//.*$", "");

            boolean disabledAutoCommit = cleanedInput.matches("(?s).*\\.setAutoCommit\\(\\s*false\\s*\\)\\s*;.*");
            evalResult.setAutoCommitResult(disabledAutoCommit);
            evalResult.setAutoCommitMessage(disabledAutoCommit ? "Autocommit disabled" : "Autocommit enabled");

            // 5. Execute student code and capture the output
            resetDatabase("jdbc:h2:mem:student_db;DB_CLOSE_DELAY=-1;MODE=Oracle", dbSchema);
            String studentOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateStudent", compiledStudentClasses
            );
            System.out.println("Student: " + studentOutput);
            String studentDbState = assessment.getCurrentDbState("jdbc:h2:mem:student_db;DB_CLOSE_DELAY=-1;MODE=Oracle", tables);
            String studentDbContent = assessment.dbUrlToToString("jdbc:h2:mem:student_db;DB_CLOSE_DELAY=-1;MODE=Oracle", tables);
            evalResult.setStudentQueryResult(studentDbContent);

            // 6. Execute solution code and compare outputs
            resetDatabase("jdbc:h2:mem:solution_db;DB_CLOSE_DELAY=-1;MODE=Oracle", dbSchema);
            String solutionOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateSolution", compiledSolutionClasses
            );
            System.out.println("Solution: " + solutionOutput);
            boolean outputRes = assessment.checkOutput(studentOutput, solutionOutput);

            evalResult.setOutputComparisionResult(outputRes);
            evalResult.setOutputComparisionMessage(outputRes ? "Output correct" : "Output incorrect");

            // 7. Compare database states between student and solution
            resetDatabase("jdbc:h2:mem:solution_db;DB_CLOSE_DELAY=-1;MODE=Oracle", dbSchema);
            solutionOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateSolution", compiledSolutionClasses
            );
            String solutionDbState = assessment.getCurrentDbState("jdbc:h2:mem:solution_db;DB_CLOSE_DELAY=-1;MODE=Oracle", tables);

            boolean dbResult = assessment.compareDbStates(studentDbState, solutionDbState);
            evalResult.setDatabaseResult(dbResult);
            evalResult.setDatabaseMessage(dbResult ? "Database content correct" : "Incorrect Database Content");
            // 8. Exception Handling Test
            String integratedStudentFaulty = template
                .replace("/*<StudentInput> */", studentInput)
                .replace("public class Template", "public class TemplateStudentFaulty")
                .replace("/*<DB_URL>*/", "jdbc:h2:tcp://nonexistentdb:4040/mem:test");


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
                evalResult.setExceptionMessage("Exceptions were not caught by student");
            } else if (faultyOutput.strip().isEmpty()) {
                evalResult.setExceptionResult(false);
                evalResult.setExceptionMessage("Unclear");
            } else {
                evalResult.setExceptionResult(false);
                evalResult.setExceptionMessage("Unrecognized");
            }

            System.out.println("Execution Time: " + (System.currentTimeMillis() - startTimestamp) + " ms");
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

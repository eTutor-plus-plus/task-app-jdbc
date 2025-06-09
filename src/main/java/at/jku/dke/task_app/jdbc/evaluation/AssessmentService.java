package at.jku.dke.task_app.jdbc.evaluation;

import javax.tools.*;
import java.sql.*;
import java.util.*;

/**
 * This class is responsible for evaluating a student's JDBC code against a predefined solution.
 * It checks for syntax correctness, autocommit settings, output correctness, and compares the database states
 * between the student's solution and the reference solution. It also performs exception handling tests for the student's code.
 */
public class AssessmentService {

    /**
     * Assesses the student's task based on the provided input, solution, and database schema.
     * It evaluates syntax, autocommit handling, output correctness, and compares database states.
     * Additionally, it runs exception handling tests on the student's code.
     *
     * @param studentInput The student's code input to be assessed.
     * @param dbSchema The schema to initialize the databases with.
     * @param taskSolution The reference solution code to compare against.
     * @param tables The name of the tables in the database to evaluate.
     * @param variables Variables that are inserted into the code.
     * @param analyze If true, performs full evaluation.
     *                if false only syntax will be checked.
     * @return The result of the assessment including syntax, autocommit, output comparison, database state, and exception handling results.
     */
    public static Result assessTask(String studentInput, String dbSchema, String taskSolution, String[] tables, String variables, boolean analyze){
        DriverManager.setLoginTimeout(5);
        Result evalResult = new Result();
        AssessmentFunctions assessment = new AssessmentFunctions();
        String dbId = UUID.randomUUID().toString().replace("-", "_");
        String studentDbUrl = "jdbc:h2:mem:student_" + dbId + ";DB_CLOSE_DELAY=-1;MODE=Oracle";
        String solutionDbUrl = "jdbc:h2:mem:solution_" + dbId + ";DB_CLOSE_DELAY=-1;MODE=Oracle";

        // 1. Syntax Check
        if (!assessment.checkSyntax(studentInput, variables, evalResult)) {
            return evalResult;
        }

        evalResult.setSyntaxResult(true);
        evalResult.setSyntaxMessage("Syntax Correct");
        try {
            // 2. Compile and integrate student and solution code into templates
            String template = new Template().getTemplate();

            String integratedStudent = template
                .replace("/*<Variables>*/", variables)
                .replace("/*<StudentInput> */", studentInput)
                .replace("public class Template", "public class TemplateStudent")
                .replace("/*<DB_URL>*/", studentDbUrl);

            String integratedSolution = template
                .replace("/*<Variables>*/", variables)
                .replace("/*<StudentInput> */", taskSolution)
                .replace("public class Template", "public class TemplateSolution")
                .replace("/*<DB_URL>*/", solutionDbUrl);

            // 3. Compile student and solution code in memory
            Map<String, byte[]> compiledStudentClasses = compileJavaInMemory("at.jku.dke.task_app.jdbc.TemplateStudent", integratedStudent);
            Map<String, byte[]> compiledSolutionClasses = compileJavaInMemory("at.jku.dke.task_app.jdbc.TemplateSolution", integratedSolution);

            if (!analyze) {
                // Only check Syntax without any further checks
                resetDatabase(studentDbUrl, dbSchema);
                evalResult.setDatabaseBefore(assessment.getDatabaseContent(studentDbUrl, tables));
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
            evalResult.setDatabaseBefore(assessment.getDatabaseContent(studentDbUrl, tables));
            String studentOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateStudent", compiledStudentClasses
            );
            List<TableDump> studentDbTables = assessment.getDatabaseContent(studentDbUrl, tables);
            evalResult.setStudentQueryResult(studentDbTables);

            // 6. Execute solution code and compare outputs
            resetDatabase(solutionDbUrl, dbSchema);
            String solutionOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateSolution", compiledSolutionClasses
            );
            boolean outputRes = assessment.checkOutput(studentOutput, solutionOutput);

            evalResult.setStudentOutput(studentOutput);

            evalResult.setOutputComparisonResult(outputRes);
            evalResult.setOutputComparisonMessage(outputRes ? "Output correct" : "Output incorrect");
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
            assessment.analyzeTupleDifferences(studentDbUrl, solutionDbUrl, evalResult, tables);

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
            DriverManager.setLoginTimeout(1);
            String integratedStudentFaulty = new Template().getTemplate()
                .replace("/*<Variables>*/", variables)
                .replace("/*<StudentInput> */", studentInput)
                .replace("public class Template", "public class TemplateStudentFaulty")
                .replace("/*<DB_URL>*/", "jdbc:h2:tcp://nonexistent.invalid/mem:test");

            Map<String, byte[]> compiledFaultyClasses = compileJavaInMemory(
                "at.jku.dke.task_app.jdbc.TemplateStudentFaulty",
                integratedStudentFaulty
            );
            String faultyOutput = CodeRunner.runCode(
                "at.jku.dke.task_app.jdbc.TemplateStudentFaulty",
                compiledFaultyClasses
            );

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
            return evalResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Compiles the given Java source code and returns the compiled classes as a map.
     *
     * @param className The fully qualified name of the class.
     * @param javaSource The Java source code to be compiled.
     * @return A map of class names to byte arrays of compiled classes.
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
     * Resets the database by clearing all objects and re-executing the provided schema.
     *
     * @param dbUrl The URL of the database to reset.
     * @param schema The schema to initialize the database with.
     * @throws SQLException If there is an error while resetting the database.
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

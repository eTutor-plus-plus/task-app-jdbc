package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import javax.tools.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CodeRunnerTest {

    private Map<String, byte[]> compileSource(String className, String sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MemoryJavaFile sourceFile = new MemoryJavaFile(className, sourceCode);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        MemoryJavaFile.FileManagerMap fileManager = new MemoryJavaFile.FileManagerMap(stdFileManager);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(sourceFile));
        boolean success = task.call();

        assertTrue(success, "Compilation failed: " + diagnostics.getDiagnostics());

        return fileManager.getAllClassBytes();
    }

    @Test
    public void runCodeReturnsExpectedOutput() {
        String className = "TestClass";
        String code = """
            public class TestClass {
                public String templateMethod() {
                    return "Hello World!";
                }
            }
            """;

        Map<String, byte[]> bytecode = compileSource(className, code);
        String result = CodeRunner.runCode(className, bytecode);

        assertEquals("Hello World!", result);
    }

    @Test
    public void runCodeTimeout() {
        String className = "TimeoutClass";
        String code = """
            public class TimeoutClass {
                public String templateMethod() {
                    try {
                        Thread.sleep(6000); // longer than 5s timeout
                    } catch (Exception e) {}
                    return "Finished";
                }
            }
            """;

        Map<String, byte[]> bytecode = compileSource(className, code);
        String result = CodeRunner.runCode(className, bytecode);

        assertEquals("TIMEOUT", result);
    }

    @Test
    public void runCodeThrowsException() {
        String className = "ExceptionClass";
        String code = """
            public class ExceptionClass {
                public String templateMethod() {
                    throw new RuntimeException("Failure");
                }
            }
            """;

        Map<String, byte[]> bytecode = compileSource(className, code);
        String result = CodeRunner.runCode(className, bytecode);
        assertEquals("", result);
    }
}

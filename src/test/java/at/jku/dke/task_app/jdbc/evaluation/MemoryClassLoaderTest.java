package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import javax.tools.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryClassLoaderTest {

    //Copy of the Compilation Function from AssessmentService

    private Map<String, byte[]> compileJava(String className, String sourceCode) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MemoryJavaFile sourceFile = new MemoryJavaFile(className, sourceCode);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
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


    @Test
    public void testLoadValidClassFromMemory() throws Exception {
        String className = "HelloWorld";
        String qualifiedName = "HelloWorld";
        String sourceCode = """
            public class HelloWorld {
                public String sayHi() {
                    return "Hi!";
                }
            }
            """;

        Map<String, byte[]> compiledClasses = compileJava(qualifiedName, sourceCode);
        MemoryClassLoader loader = new MemoryClassLoader(compiledClasses);

        Class<?> clazz = loader.loadClass(qualifiedName);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        String result = (String) clazz.getMethod("sayHi").invoke(instance);
        assertEquals("Hi!", result);
    }

    @Test
    public void testDelegationToParentClassLoader() throws Exception {
        MemoryClassLoader loader = new MemoryClassLoader(Map.of());

        Class<?> clazz = loader.loadClass("java.lang.String");

        assertNotNull(clazz);
        assertEquals("java.lang.String", clazz.getName());
    }

    @Test
    public void testClassNotFoundExceptionWhenClassIsMissing() {
        MemoryClassLoader loader = new MemoryClassLoader(Map.of());

        assertThrows(ClassNotFoundException.class, () -> {
            loader.loadClass("com.nonexistent.DoesNotExist");
        });
    }
}

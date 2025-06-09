package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import javax.tools.*;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryJavaFileTest {

    @Test
    public void constructor_SourceFile() {
        String className = "com.example.SourceClass";
        String source = "public class SourceClass {}";

        MemoryJavaFile file = new MemoryJavaFile(className, source);

        assertEquals(source, file.getCharContent(true));
        assertThrows(IllegalStateException.class, file::openOutputStream);
    }

    @Test
    public void constructor_BytecodeFile() throws Exception {
        String className = "com.example.BytecodeClass";

        MemoryJavaFile file = new MemoryJavaFile(className, JavaFileObject.Kind.CLASS);

        OutputStream os = file.openOutputStream();
        os.write("compiled".getBytes());
        os.close();

        byte[] bytes = file.getCompiledBytes();
        assertNotNull(bytes);
        assertArrayEquals("compiled".getBytes(), bytes);

        assertThrows(UnsupportedOperationException.class, () -> file.getCharContent(true));
    }

    @Test
    public void getCompiledBytes_ReturnsNullForSourceFile() {
        String className = "com.example.SourceClass";
        MemoryJavaFile file = new MemoryJavaFile(className, "public class SourceClass {}");

        assertNull(file.getCompiledBytes());
    }

    @Test
    public void fileManagerMap_StoresCompiledBytecode() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "JDK required for tests (javac)");

        String className = "Example";
        String qualifiedName = "Example";
        String source = """
            public class Example {
                public String hello() {
                    return "Hello!";
                }
            }
            """;

        MemoryJavaFile sourceFile = new MemoryJavaFile(qualifiedName, source);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
        MemoryJavaFile.FileManagerMap fileManager = new MemoryJavaFile.FileManagerMap(stdManager);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(sourceFile));
        boolean success = task.call();

        assertTrue(success, "Compilation failed: " + diagnostics.getDiagnostics());

        Map<String, byte[]> compiled = fileManager.getAllClassBytes();

        assertTrue(compiled.containsKey(qualifiedName));
        byte[] bytecode = compiled.get(qualifiedName);
        assertNotNull(bytecode);
        assertTrue(bytecode.length > 0);
    }

    @Test
    public void fileManagerMap_HandlesMultipleClasses() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);

        String className1 = "Test1";
        String className2 = "Test2";
        String src1 = "public class Test1 { public String foo() { return \"Test1\"; } }";
        String src2 = "public class Test2 { public String bar() { return \"Test2\"; } }";

        MemoryJavaFile f1 = new MemoryJavaFile(className1, src1);
        MemoryJavaFile f2 = new MemoryJavaFile(className2, src2);

        StandardJavaFileManager std = compiler.getStandardFileManager(null, null, null);
        MemoryJavaFile.FileManagerMap manager = new MemoryJavaFile.FileManagerMap(std);
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, null, null, List.of(f1, f2));

        assertTrue(task.call());

        Map<String, byte[]> compiled = manager.getAllClassBytes();
        assertEquals(2, compiled.size());
        assertTrue(compiled.containsKey(className1));
        assertTrue(compiled.containsKey(className2));
    }
}

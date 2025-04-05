package at.jku.dke.task_app.jdbc.evaluation;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utility class that dynamically loads and executes compiled Java classes.
 * It looks for classes inside the target/generated-classes/ directory,
 * instantiates them, and calls the templateMethod() method.
 */
public class CodeRunner {

    /**
     * Dynamically loads a class from compiled bytecode stored in memory, creates an instance of the class,
     * and invokes its 'templateMethod' to execute the code.
     *
     * @param className The fully qualified name of the class to load.
     * @param compiledClasses A map containing the compiled class bytecode, where the key is the class name
     *                        and the value is the bytecode as a byte array.
     * @return The result of invoking 'templateMethod', expected to be a 'String'.
     * @throws Exception If any error occurs during class loading, instantiation, method invocation, or reflection.
     */
    public static String runCode(String className, Map<String, byte[]> compiledClasses) throws Exception {
        // Create a new instance of MemoryClassLoader using the provided map of compiled classes (bytecode)
        MemoryClassLoader classLoader = new MemoryClassLoader(compiledClasses);

        // Load the class dynamically using the class name. The class is fetched from memory
        Class<?> clazz = classLoader.loadClass(className);

        // Create a new instance of the loaded class using reflection
        Object instance = clazz.getDeclaredConstructor().newInstance();

        // Get the 'templateMethod' from the class using reflection
        Method templateMethod = clazz.getMethod("templateMethod");

        // Invoke the 'templateMethod' on the created instance and return the result as a String
        return (String) templateMethod.invoke(instance);
    }

}

package at.jku.dke.task_app.jdbc.evaluation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class is responsible for executing dynamically compiled Java code.
 * It loads a class at runtime using the MemoryClassLoader,
 * invokes its templateMethod() method, and returns the result.
 *
 * The Execution happens in a separate thread with a fixed timeout
 * to prevent infinite loops or excessively long-running code.
 */
public class CodeRunner {

    /**
     * Time limit (in seconds) for code execution.
     * If the execution exceeds this limit, it will be aborted.
     */
    private static final long TIME_LIMIT_SECONDS = 5;

    /**
     * Runs a specified class by loading it from the provided bytecode map,
     * creating an instance, and invoking its {@code templateMethod()}.
     *
     * @param className       The fully qualified name of the class to be executed.
     * @param compiledClasses A map where the key is the class name and the value is the compiled bytecode.
     * @return The result of the {@code templateMethod()} as a String,
     *         or "TIMEOUT" if execution takes too long.
     */
    public static String runCode(String className, Map<String, byte[]> compiledClasses) {
        MemoryClassLoader classLoader = new MemoryClassLoader(compiledClasses);
        String result = "";

        // Execute code in a separate thread to enforce timeout
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<String> future = null;

            try {
                // Dynamically load and instantiate the class
                Class<?> clazz = classLoader.loadClass(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();

                // Get the method to execute
                Method templateMethod = clazz.getMethod("templateMethod");

                // Run the method asynchronously
                future = executor.submit(() -> (String) templateMethod.invoke(instance));

                // Try to get the result with time limit
                result = future.get(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

            } catch (TimeoutException e) {
                if (future != null) future.cancel(true);
                System.err.println("Execution took too long (Timeout).");
                result = "TIMEOUT";
            } catch (ExecutionException e) {
                System.err.println("Error during code execution: " + e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Execution was interrupted.");
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
            } finally {
                executor.shutdownNow();
            }
        }

        return result;
    }
}

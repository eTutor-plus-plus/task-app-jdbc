package at.jku.dke.task_app.jdbc.evaluation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

public class CodeRunner {

    private static final long TIME_LIMIT_SECONDS = 5; // Time limit (seconds)

    /**
     * Executes the code with a time limit
     *
     * @param className Name of the class to be executed.
     * @param compiledClasses Bytecode of the compiled classes.
     * @return Result of templateMethod() or an error message.
     */
    public static String runCode(String className, Map<String, byte[]> compiledClasses) {
        MemoryClassLoader classLoader = new MemoryClassLoader(compiledClasses);
        String result = "";

        try {
            Class<?> clazz = classLoader.loadClass(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method templateMethod = clazz.getMethod("templateMethod");

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                return (String) templateMethod.invoke(instance);
            });

            // Wait for the result with a time limit
            result = future.get(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            System.err.println("Execution took too long (Timeout).");
        } catch (ExecutionException e) {
            System.err.println("Error during code execution: " + e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution was interrupted.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }

        return result;
    }
}

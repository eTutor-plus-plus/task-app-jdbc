package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceTest {

    private static final String baseStudentCode = """
        try (var con = ds.getConnection()) {
            con.setAutoCommit(false);
            try (var st = con.createStatement()) {
                st.executeUpdate("INSERT INTO users (id, name) VALUES (99, 'Test')");
                con.commit();
            }
        } catch (Exception e) {
            Out.println("DB ERROR");
        }
    """;

    private static final String schema = "CREATE TABLE users (id INT, name VARCHAR); INSERT INTO users (id, name) VALUES (1, 'Michael');";
    private static final String[] tables = new String[]{"users"};

    @Test
    public void concurrentEvaluations() throws InterruptedException {
        int[] threadCounts = new int[]{1};

        for (int threads : threadCounts) {
            System.out.println("\n--- Running test with " + threads + " concurrent evaluations ---");

            // Warmup
            for (int i = 0; i < 3; i++) {
                String uniqueCode = addUniqueClassName(baseStudentCode, "Warmup" + i);
                AssessmentService.assessTask(uniqueCode, schema, uniqueCode, tables, "", true);
            }

            ExecutorService executor = Executors.newFixedThreadPool(Math.min(threads, 16));
            List<Future<Long>> futures = new ArrayList<>();

            long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long startTime = System.nanoTime();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    String studentCode = addUniqueClassName(baseStudentCode, "Student" + uuid);
                    String taskSolution = studentCode;

                    long start = System.nanoTime();
                    Result result = AssessmentService.assessTask(studentCode, schema, taskSolution, tables, "", true);
                    System.out.println(result.toString());
                    long end = System.nanoTime();

                    if (result == null)
                        throw new RuntimeException("Result was null");

                    assertTrue(result.getSyntaxResult());
                    return end - start;
                }));
            }

            executor.shutdown();
            boolean terminated = executor.awaitTermination(90, TimeUnit.SECONDS);
            long endTime = System.nanoTime();

            if (!terminated) {
                System.err.println("Executor did not terminate cleanly!");
            }

            List<Long> durations = new ArrayList<>();
            for (Future<Long> future : futures) {
                try {
                    durations.add(future.get(25, TimeUnit.SECONDS));
                } catch (ExecutionException e) {
                    System.err.println("Execution error: " + e.getCause());
                } catch (TimeoutException e) {
                    System.err.println("Timeout during task execution");
                }
            }

            long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = memoryAfter - memoryBefore;

            long totalDuration = endTime - startTime;
            double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);

            System.out.printf("Total Time for %d threads: %.2f s%n", threads, totalDuration / 1_000_000_000.0);
            System.out.printf("Average per evaluation: %.2f s%n", avg / 1_000_000_000.0);
            System.out.printf("Heap memory used: %.2f MB%n", memoryUsed / (1024.0 * 1024));
        }
    }

    /**
     * Adds unique class declaration to Template
     */
    private static String addUniqueClassName(String baseCode, String className) {
        return baseCode.replace("public class Template", "public class Template" + className);
    }
}

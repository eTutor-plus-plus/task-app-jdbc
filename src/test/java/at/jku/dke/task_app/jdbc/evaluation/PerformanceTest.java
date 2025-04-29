package at.jku.dke.task_app.jdbc.evaluation;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class PerformanceTest {

    private static final String studentCodeExample = """
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

    private static final String taskSolution = studentCodeExample;
    private static final String schema = "CREATE TABLE users (id INT, name VARCHAR); INSERT INTO users (id, name) VALUES (1, 'Michael');";
    private static final String[] tables = new String[]{"users"};

    @Test
    public void testConcurrentEvaluations() throws InterruptedException {
        int[] threadCounts = new int[]{1};

        for (int threads : threadCounts) {
            System.out.println("\n--- Running test with " + threads + " concurrent evaluations ---");

            ExecutorService executor = Executors.newFixedThreadPool(Math.min(threads, 16));
            List<Future<Long>> futures = new ArrayList<>();

            long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Instant start = Instant.now();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    Instant s = Instant.now();
                    Result result = AssessmentService.assessTask(studentCodeExample, schema, taskSolution, tables, true);
                    Instant e = Instant.now();

                    if (result == null) {
                        throw new RuntimeException("Result was null");
                    }

                    assertTrue(result.getSyntaxResult());
                    return Duration.between(s, e).toMillis();
                }));
            }

            executor.shutdown();
            boolean terminated = executor.awaitTermination(90, TimeUnit.SECONDS);
            Instant end = Instant.now();

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

            long totalDuration = Duration.between(start, end).toMillis();
            double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);

            System.out.printf("Total Time for %d threads: %dms\n", threads, totalDuration);
            System.out.printf("Average per evaluation: %.2fms\n", avg);
            System.out.printf("Heap memory used: %d KB\n", memoryUsed / 1024);
        }
    }
}

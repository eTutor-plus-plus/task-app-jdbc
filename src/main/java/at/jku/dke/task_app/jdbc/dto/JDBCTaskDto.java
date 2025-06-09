package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.jdbc.data.entities.JDBCTask}
 *
 * @param solution The solution.
 */
public record JDBCTaskDto(
    @NotNull String solution,
    @NotNull String tables,
    @NotNull int wrongOutputPenalty,
    @NotNull int exceptionHandlingPenalty,
    @NotNull int wrongDbContentPenalty,
    @NotNull boolean checkAutocommit,
    Integer autocommitPenalty,
    String variables
    ) implements Serializable {}

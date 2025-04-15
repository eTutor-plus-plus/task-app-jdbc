package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a JDBC task.
 *
 * @param solution The solution.
 */
public record ModifyJDBCTaskDto(
    @NotNull String solution,
    @NotNull String tables,
    @NotNull int wrongOutputPenalty,
    @NotNull int exceptionHandlingPenalty,
    @NotNull int wrongDbContentPenalty,
    @NotNull boolean checkAutocommit,
    Integer autocommitPenalty
) implements Serializable {
}

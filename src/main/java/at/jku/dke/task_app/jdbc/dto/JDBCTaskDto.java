package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.jdbc.data.entities.JDBCTask}
 *
 * @param solution The solution.
 */
public record JDBCTaskDto(@NotNull Integer solution) implements Serializable {
}

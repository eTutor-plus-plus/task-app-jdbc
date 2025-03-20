package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.jdbc.data.entities.jdbcTask}
 *
 * @param solution The solution.
 */
public record jdbcTaskDto(@NotNull Integer solution) implements Serializable {
}

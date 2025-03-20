package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * This class represents a data transfer object for modifying a jdbc task.
 *
 * @param solution The solution.
 */
public record ModifyjdbcTaskDto(@NotNull Integer solution) implements Serializable {
}

package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup}
 *
 * @param minNumber The minimum number.
 * @param maxNumber The maximum number.
 */
public record JDBCTaskGroupDto(@NotNull Integer minNumber, @NotNull Integer maxNumber) implements Serializable {
}

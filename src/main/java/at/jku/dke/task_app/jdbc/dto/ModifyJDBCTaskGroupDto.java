package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import at.jku.dke.task_app.jdbc.validation.ValidTaskGroupNumber;

/**
 * This class represents a data transfer object for modifying a JDBC task group.
 *
 * @param minNumber The minimum number.
 * @param maxNumber The maximum number.
 */
@ValidTaskGroupNumber
public record ModifyJDBCTaskGroupDto(@NotNull Integer minNumber, @NotNull Integer maxNumber) implements Serializable {
}

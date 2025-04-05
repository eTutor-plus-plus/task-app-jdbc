package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Contains a minimum and a maximum number.
 *
 * @param schema The database schema for a task.
 */
public record SchemaDto(@NotNull String schema) {
}

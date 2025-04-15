package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup}
 *
 * @param schema The database schema for a task.
 */
public record JDBCTaskGroupDto(
    @NotNull String createStatements,
    @NotNull String insertStatementsDiagnose,
    @NotNull String insertStatementsSubmission
) implements Serializable {}


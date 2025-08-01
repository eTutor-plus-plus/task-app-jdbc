package at.jku.dke.task_app.jdbc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO for {@link at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup}
 *
 * This DTO holds the SQL statements required to initialize the database schema and data
 * for both diagnose and submission mode.
 *
 * @param createStatements         SQL {@code CREATE TABLE} statements to define the schema.
 * @param insertStatementsDiagnose SQL {@code INSERT INTO} statements for diagnose mode.
 * @param insertStatementsSubmission SQL {@code INSERT INTO} statements for submission mode.
 */
public record JDBCTaskGroupDto(
    @NotNull String createStatements,
    @NotNull String insertStatementsDiagnose,
    @NotNull String insertStatementsSubmission
) implements Serializable {}


package at.jku.dke.task_app.jdbc.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTaskGroup;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a JDBC task group.
 * <p>
 * It is also possible to create tasks without task types. Tasks of type JDBC would not need a task group.
 * Here a task group is only used for demonstration purposes.
 */
@Entity
@Table(name = "task_group")
public class JDBCTaskGroup extends BaseTaskGroup {

    @NotNull
    @Column(name = "create_statements", nullable = false, columnDefinition = "TEXT")
    private String createStatements;

    @NotNull
    @Column(name = "insert_statements_diagnose", nullable = false, columnDefinition = "TEXT")
    private String insertStatementsDiagnose;

    @NotNull
    @Column(name = "insert_statements_submission", nullable = false, columnDefinition = "TEXT")
    private String insertStatementsSubmission;

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     */
    public JDBCTaskGroup() {
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param createStatements          The CREATE TABLE statements.
     * @param insertStatementsDiagnose  The INSERT INTO statements for diagnosis.
     * @param insertStatementsSubmission The INSERT INTO statements for submission.
     */
    public JDBCTaskGroup(String createStatements, String insertStatementsDiagnose, String insertStatementsSubmission) {
        this.createStatements = createStatements;
        this.insertStatementsDiagnose = insertStatementsDiagnose;
        this.insertStatementsSubmission = insertStatementsSubmission;
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param status                    The task group status.
     * @param createStatements          The CREATE TABLE statements.
     * @param insertStatementsDiagnose  The INSERT INTO statements for diagnosis.
     * @param insertStatementsSubmission The INSERT INTO statements for submission.
     */
    public JDBCTaskGroup(TaskStatus status, String createStatements, String insertStatementsDiagnose, String insertStatementsSubmission) {
        super(status);
        this.createStatements = createStatements;
        this.insertStatementsDiagnose = insertStatementsDiagnose;
        this.insertStatementsSubmission = insertStatementsSubmission;
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param id                        The identifier.
     * @param status                    The task group status.
     * @param createStatements          The CREATE TABLE statements.
     * @param insertStatementsDiagnose  The INSERT INTO statements for diagnosis.
     * @param insertStatementsSubmission The INSERT INTO statements for submission.
     */
    public JDBCTaskGroup(Long id, TaskStatus status, String createStatements, String insertStatementsDiagnose, String insertStatementsSubmission) {
        super(id, status);
        this.createStatements = createStatements;
        this.insertStatementsDiagnose = insertStatementsDiagnose;
        this.insertStatementsSubmission = insertStatementsSubmission;
    }

    /**
     * Gets the CREATE TABLE statements.
     *
     * @return The statements.
     */
    public String getCreateStatements() {
        return createStatements;
    }

    /**
     * Sets the CREATE TABLE statements.
     *
     * @param createStatements The statements.
     */
    public void setCreateStatements(String createStatements) {
        this.createStatements = createStatements;
    }

    /**
     * Gets the INSERT INTO statements for diagnosis.
     *
     * @return The statements.
     */
    public String getInsertStatementsDiagnose() {
        return insertStatementsDiagnose;
    }

    /**
     * Sets the INSERT INTO statements for diagnosis.
     *
     * @param insertStatementsDiagnose The statements.
     */
    public void setInsertStatementsDiagnose(String insertStatementsDiagnose) {
        this.insertStatementsDiagnose = insertStatementsDiagnose;
    }

    /**
     * Gets the INSERT INTO statements for submission.
     *
     * @return The statements.
     */
    public String getInsertStatementsSubmission() {
        return insertStatementsSubmission;
    }

    /**
     * Sets the INSERT INTO statements for submission.
     *
     * @param insertStatementsSubmission The statements.
     */
    public void setInsertStatementsSubmission(String insertStatementsSubmission) {
        this.insertStatementsSubmission = insertStatementsSubmission;
    }
}

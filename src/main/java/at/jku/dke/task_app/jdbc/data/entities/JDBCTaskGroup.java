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
    @Column(name = "schema", nullable = false)
    private String schema;

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     */
    public JDBCTaskGroup() {
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param schema The database schema for a task.
     */
    public JDBCTaskGroup(String schema) {
        this.schema = schema;
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param status    The status.
     * @param schema The database schema for a task.
     */
    public JDBCTaskGroup(TaskStatus status, String schema) {
        super(status);
        this.schema = schema;
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param id        The id.
     * @param status    The status.
     * @param schema The database schema for a task.
     */
    public JDBCTaskGroup(Long id, TaskStatus status, String schema) {
        super(id, status);
        this.schema = schema;
    }

    /**
     * Gets the minimum number.
     *
     * @return The database schema.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the minimum number.
     *
     * @param schema The database schema for a task.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }
}

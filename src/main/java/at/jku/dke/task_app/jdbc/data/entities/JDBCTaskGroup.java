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
    @Column(name = "min_number", nullable = false)
    private Integer minNumber;

    @NotNull
    @Column(name = "max_number", nullable = false)
    private Integer maxNumber;

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     */
    public JDBCTaskGroup() {
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param minNumber The minimum number.
     * @param maxNumber The maximum number.
     */
    public JDBCTaskGroup(Integer minNumber, Integer maxNumber) {
        this.minNumber = minNumber;
        this.maxNumber = maxNumber;
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param status    The status.
     * @param minNumber The minimum number.
     * @param maxNumber The maximum number.
     */
    public JDBCTaskGroup(TaskStatus status, Integer minNumber, Integer maxNumber) {
        super(status);
        this.minNumber = minNumber;
        this.maxNumber = maxNumber;
    }

    /**
     * Creates a new instance of class {@link JDBCTaskGroup}.
     *
     * @param id        The id.
     * @param status    The status.
     * @param minNumber The minimum number.
     * @param maxNumber The maximum number.
     */
    public JDBCTaskGroup(Long id, TaskStatus status, Integer minNumber, Integer maxNumber) {
        super(id, status);
        this.minNumber = minNumber;
        this.maxNumber = maxNumber;
    }

    /**
     * Gets the minimum number.
     *
     * @return The minimum number.
     */
    public Integer getMinNumber() {
        return minNumber;
    }

    /**
     * Sets the minimum number.
     *
     * @param minNumber The minimum number.
     */
    public void setMinNumber(Integer minNumber) {
        this.minNumber = minNumber;
    }

    /**
     * Gets the maximum number.
     *
     * @return The maximum number.
     */
    public Integer getMaxNumber() {
        return maxNumber;
    }

    /**
     * Sets the maximum number.
     *
     * @param maxNumber The maximum number.
     */
    public void setMaxNumber(Integer maxNumber) {
        this.maxNumber = maxNumber;
    }
}

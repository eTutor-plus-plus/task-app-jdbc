package at.jku.dke.task_app.jdbc.data.entities;

import at.jku.dke.etutor.task_app.data.entities.BaseTaskInGroup;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Represents a JDBC task.
 */
@Entity
@Table(name = "task")
public class JDBCTask extends BaseTaskInGroup<JDBCTaskGroup> {
    @NotNull
    @Column(name = "solution", nullable = false)
    private Integer solution;

    /**
     * Creates a new instance of class {@link JDBCTask}.
     */
    public JDBCTask() {
    }

    /**
     * Creates a new instance of class {@link JDBCTask}.
     *
     * @param solution The solution.
     */
    public JDBCTask(Integer solution) {
        this.solution = solution;
    }

    /**
     * Creates a new instance of class {@link JDBCTask}.
     *
     * @param maxPoints The maximum points.
     * @param status    The status.
     * @param taskGroup The task group.
     * @param solution  The solution.
     */
    public JDBCTask(BigDecimal maxPoints, TaskStatus status, JDBCTaskGroup taskGroup, Integer solution) {
        super(maxPoints, status, taskGroup);
        this.solution = solution;
    }

    /**
     * Creates a new instance of class {@link JDBCTask}.
     *
     * @param id        The identifier.
     * @param maxPoints The maximum points.
     * @param status    The status.
     * @param taskGroup The task group.
     * @param solution  The solution.
     */
    public JDBCTask(Long id, BigDecimal maxPoints, TaskStatus status, JDBCTaskGroup taskGroup, Integer solution) {
        super(id, maxPoints, status, taskGroup);
        this.solution = solution;
    }

    /**
     * Gets the solution.
     *
     * @return The solution.
     */
    public Integer getSolution() {
        return solution;
    }

    /**
     * Sets the solution.
     *
     * @param solution The solution.
     */
    public void setSolution(Integer solution) {
        this.solution = solution;
    }
}

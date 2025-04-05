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
    private String solution;

    @NotNull
    @Column(name = "tables", nullable = false)
    private String tables;

    /**
     * Creates a new instance of class {@link JDBCTask}.
     */
    public JDBCTask() {
    }

    /**
     * Creates a new instance of class {@link JDBCTask}.
     *
     * @param solution The solution.
     * @param tables   The table list.
     */
    public JDBCTask(String solution, String tables) {
        this.solution = solution;
        this.tables = tables;
    }

    /**
     * Creates a new instance of class {@link JDBCTask}.
     *
     * @param maxPoints The maximum points.
     * @param status    The status.
     * @param taskGroup The task group.
     * @param solution  The solution.
     * @param tables    The table list.
     */
    public JDBCTask(BigDecimal maxPoints, TaskStatus status, JDBCTaskGroup taskGroup, String solution, String tables) {
        super(maxPoints, status, taskGroup);
        this.solution = solution;
        this.tables = tables;
    }

    /**
     * Creates a new instance of class {@link JDBCTask}.
     *
     * @param id        The identifier.
     * @param maxPoints The maximum points.
     * @param status    The status.
     * @param taskGroup The task group.
     * @param solution  The solution.
     * @param tables    The table list.
     */
    public JDBCTask(Long id, BigDecimal maxPoints, TaskStatus status, JDBCTaskGroup taskGroup, String solution, String tables) {
        super(id, maxPoints, status, taskGroup);
        this.solution = solution;
        this.tables = tables;
    }

    /**
     * Gets the solution.
     *
     * @return The solution.
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets the solution.
     *
     * @param solution The solution.
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    /**
     * Gets the tables.
     *
     * @return The tables.
     */
    public String getTables() {
        return tables;
    }

    /**
     * Sets the tables.
     *
     * @param tables The tables.
     */
    public void setTables(String tables) {
        this.tables = tables;
    }
}

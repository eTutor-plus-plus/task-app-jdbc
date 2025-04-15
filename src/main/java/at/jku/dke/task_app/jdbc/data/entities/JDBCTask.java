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

    @Column(name = "wrong_output_penalty")
    private Integer wrongOutputPenalty;

    @Column(name = "exception_handling_penalty")
    private Integer exceptionHandlingPenalty;

    @Column(name = "wrong_db_content_penalty")
    private Integer wrongDbContentPenalty;

    @Column(name = "check_autocommit")
    private boolean checkAutocommit;

    @Column(name = "autocommit_penalty")
    private Integer autocommitPenalty;

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

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public Integer getWrongOutputPenalty() {
        return wrongOutputPenalty;
    }

    public void setWrongOutputPenalty(Integer wrongOutputPenalty) {
        this.wrongOutputPenalty = wrongOutputPenalty;
    }

    public Integer getExceptionHandlingPenalty() {
        return exceptionHandlingPenalty;
    }

    public void setExceptionHandlingPenalty(Integer exceptionHandlingPenalty) {
        this.exceptionHandlingPenalty = exceptionHandlingPenalty;
    }

    public Integer getWrongDbContentPenalty() {
        return wrongDbContentPenalty;
    }

    public void setWrongDbContentPenalty(Integer wrongDbContentPenalty) {
        this.wrongDbContentPenalty = wrongDbContentPenalty;
    }

    public boolean isCheckAutocommit() {
        return checkAutocommit;
    }

    public void setCheckAutocommit(boolean checkAutocommit) {
        this.checkAutocommit = checkAutocommit;
    }

    public Integer getAutocommitPenalty() {
        return autocommitPenalty;
    }

    public void setAutocommitPenalty(Integer autocommitPenalty) {
        this.autocommitPenalty = autocommitPenalty;
    }
}

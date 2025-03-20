package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.jdbc.data.entities.jdbcTask;
import at.jku.dke.task_app.jdbc.dto.jdbcTaskDto;
import at.jku.dke.task_app.jdbc.dto.ModifyjdbcTaskDto;
import at.jku.dke.task_app.jdbc.services.jdbcTaskService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link jdbcTask}s.
 */
@RestController
public class TaskController extends BaseTaskController<jdbcTask, jdbcTaskDto, ModifyjdbcTaskDto> {

    /**
     * Creates a new instance of class {@link TaskController}.
     *
     * @param taskService The task service.
     */
    public TaskController(jdbcTaskService taskService) {
        super(taskService);
    }

    @Override
    protected jdbcTaskDto mapToDto(jdbcTask task) {
        return new jdbcTaskDto(task.getSolution());
    }

}

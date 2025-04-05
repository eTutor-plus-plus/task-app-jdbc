package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTask;
import at.jku.dke.task_app.jdbc.dto.JDBCTaskDto;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskDto;
import at.jku.dke.task_app.jdbc.services.JDBCTaskService;

import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link JDBCTask}s.
 */
@RestController
public class TaskController extends BaseTaskController<JDBCTask, JDBCTaskDto, ModifyJDBCTaskDto> {

    /**
     * Creates a new instance of class {@link TaskController}.
     *
     * @param taskService The task service.
     */
    public TaskController(JDBCTaskService taskService) {
        super(taskService);
    }

    @Override
    protected JDBCTaskDto mapToDto(JDBCTask task) {
        return new JDBCTaskDto(task.getSolution(), task.getTables());
    }


}

package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskInGroupService;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTask;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskGroupRepository;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskRepository;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskDto;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * This class provides methods for managing {@link JDBCTask}s.
 */
@Service
public class JDBCTaskService extends BaseTaskInGroupService<JDBCTask, JDBCTaskGroup, ModifyJDBCTaskDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link JDBCTaskService}.
     *
     * @param repository          The task repository.
     * @param taskGroupRepository The task group repository.
     * @param messageSource       The message source.
     */
    public JDBCTaskService(JDBCTaskRepository repository, JDBCTaskGroupRepository taskGroupRepository, MessageSource messageSource) {
        super(repository, taskGroupRepository);
        this.messageSource = messageSource;
    }

    @Override
    protected JDBCTask createTask(long id, ModifyTaskDto<ModifyJDBCTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        var data = modifyTaskDto.additionalData();
        var task = new JDBCTask(data.solution(), data.tables());

        task.setWrongOutputPenalty(data.wrongOutputPenalty());
        task.setExceptionHandlingPenalty(data.exceptionHandlingPenalty());
        task.setWrongDbContentPenalty(data.wrongDbContentPenalty());
        task.setCheckAutocommit(data.checkAutocommit());
        task.setAutocommitPenalty(data.autocommitPenalty());

        return task;
    }

    @Override
    protected void updateTask(JDBCTask task, ModifyTaskDto<ModifyJDBCTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        var data = modifyTaskDto.additionalData();

        task.setSolution(data.solution());
        task.setTables(data.tables());
        task.setWrongOutputPenalty(data.wrongOutputPenalty());
        task.setExceptionHandlingPenalty(data.exceptionHandlingPenalty());
        task.setWrongDbContentPenalty(data.wrongDbContentPenalty());
        task.setCheckAutocommit(data.checkAutocommit());
        task.setAutocommitPenalty(data.autocommitPenalty());
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(JDBCTask task, boolean create) {
        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.ENGLISH)
        );
    }
}

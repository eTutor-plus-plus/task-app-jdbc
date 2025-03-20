package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskInGroupService;
import at.jku.dke.task_app.jdbc.data.entities.jdbcTask;
import at.jku.dke.task_app.jdbc.data.entities.jdbcTaskGroup;
import at.jku.dke.task_app.jdbc.data.repositories.jdbcTaskGroupRepository;
import at.jku.dke.task_app.jdbc.data.repositories.jdbcTaskRepository;
import at.jku.dke.task_app.jdbc.dto.ModifyjdbcTaskDto;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * This class provides methods for managing {@link jdbcTask}s.
 */
@Service
public class jdbcTaskService extends BaseTaskInGroupService<jdbcTask, jdbcTaskGroup, ModifyjdbcTaskDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link jdbcTaskService}.
     *
     * @param repository          The task repository.
     * @param taskGroupRepository The task group repository.
     * @param messageSource       The message source.
     */
    public jdbcTaskService(jdbcTaskRepository repository, jdbcTaskGroupRepository taskGroupRepository, MessageSource messageSource) {
        super(repository, taskGroupRepository);
        this.messageSource = messageSource;
    }

    @Override
    protected jdbcTask createTask(long id, ModifyTaskDto<ModifyjdbcTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");
        return new jdbcTask(modifyTaskDto.additionalData().solution());
    }

    @Override
    protected void updateTask(jdbcTask task, ModifyTaskDto<ModifyjdbcTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");
        task.setSolution(modifyTaskDto.additionalData().solution());
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(jdbcTask task, boolean create) {
        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.ENGLISH)
        );
    }
}

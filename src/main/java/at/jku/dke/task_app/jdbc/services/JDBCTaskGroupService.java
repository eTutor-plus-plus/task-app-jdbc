package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskGroupModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskGroupService;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskGroupRepository;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * This class provides methods for managing {@link JDBCTaskGroup}s.
 */
@Service
public class JDBCTaskGroupService extends BaseTaskGroupService<JDBCTaskGroup, ModifyJDBCTaskGroupDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link JDBCTaskGroupService}.
     *
     * @param repository    The task group repository.
     * @param messageSource The message source.
     */
    public JDBCTaskGroupService(JDBCTaskGroupRepository repository, MessageSource messageSource) {
        super(repository);
        this.messageSource = messageSource;
    }

    @Override
    protected JDBCTaskGroup createTaskGroup(long id, ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> modifyTaskGroupDto) {
        if (!modifyTaskGroupDto.taskGroupType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task group type.");

        var data = modifyTaskGroupDto.additionalData();
        return new JDBCTaskGroup(
            data.createStatements(),
            data.insertStatementsDiagnose(),
            data.insertStatementsSubmission()
        );
    }

    @Override
    protected void updateTaskGroup(JDBCTaskGroup taskGroup, ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> modifyTaskGroupDto) {
        if (!modifyTaskGroupDto.taskGroupType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task group type.");

        var data = modifyTaskGroupDto.additionalData();
        taskGroup.setCreateStatements(data.createStatements());
        taskGroup.setInsertStatementsDiagnose(data.insertStatementsDiagnose());
        taskGroup.setInsertStatementsSubmission(data.insertStatementsSubmission());
    }

    @Override
    protected TaskGroupModificationResponseDto mapToReturnData(JDBCTaskGroup taskGroup, boolean create) {
        return new TaskGroupModificationResponseDto(
            this.messageSource.getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getCreateStatements()}, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskGroupDescription", new Object[]{taskGroup.getCreateStatements()}, Locale.ENGLISH)
        );
    }
}

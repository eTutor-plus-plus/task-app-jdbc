package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.controllers.BaseTaskGroupController;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.dto.JDBCTaskGroupDto;
import at.jku.dke.task_app.jdbc.dto.SchemaDto;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;
import at.jku.dke.task_app.jdbc.services.JDBCTaskGroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * Controller for managing {@link JDBCTaskGroup}s.
 */
@RestController
public class TaskGroupController extends BaseTaskGroupController<JDBCTaskGroup, JDBCTaskGroupDto, ModifyJDBCTaskGroupDto> {

    /**
     * Creates a new instance of class {@link TaskGroupController}.
     *
     * @param taskGroupService The task group service.
     */
    public TaskGroupController(JDBCTaskGroupService taskGroupService) {
        super(taskGroupService);
    }

    @Override
    protected JDBCTaskGroupDto mapToDto(JDBCTaskGroup taskGroup) {
        return new JDBCTaskGroupDto(
            taskGroup.getCreateStatements(),
            taskGroup.getInsertStatementsDiagnose(),
            taskGroup.getInsertStatementsSubmission()
        );
    }
}

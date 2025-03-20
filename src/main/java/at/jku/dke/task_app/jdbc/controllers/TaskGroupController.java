package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.controllers.BaseTaskGroupController;
import at.jku.dke.task_app.jdbc.data.entities.jdbcTaskGroup;
import at.jku.dke.task_app.jdbc.dto.MinMaxDto;
import at.jku.dke.task_app.jdbc.dto.ModifyjdbcTaskGroupDto;
import at.jku.dke.task_app.jdbc.dto.jdbcTaskGroupDto;
import at.jku.dke.task_app.jdbc.services.jdbcTaskGroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * Controller for managing {@link jdbcTaskGroup}s.
 */
@RestController
public class TaskGroupController extends BaseTaskGroupController<jdbcTaskGroup, jdbcTaskGroupDto, ModifyjdbcTaskGroupDto> {

    /**
     * Creates a new instance of class {@link TaskGroupController}.
     *
     * @param taskGroupService The task group service.
     */
    public TaskGroupController(jdbcTaskGroupService taskGroupService) {
        super(taskGroupService);
    }

    @Override
    protected jdbcTaskGroupDto mapToDto(jdbcTaskGroup taskGroup) {
        return new jdbcTaskGroupDto(taskGroup.getMinNumber(), taskGroup.getMaxNumber());
    }

    /**
     * Returns two random numbers.
     * <p>
     * This method is used to demonstrate how additional endpoints can be used.
     *
     * @return Two random numbers.
     */
    @GetMapping(value = "/random", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize(AuthConstants.CRUD_AUTHORITY)
    @SecurityRequirement(name = AuthConstants.API_KEY_REQUIREMENT)
    public ResponseEntity<MinMaxDto> getRandomNumbers() {
        var rand = new Random();
        var min = rand.nextInt(100);
        return ResponseEntity.ok(new MinMaxDto(min,  rand.nextInt(min + 1, 1000)));
    }

}

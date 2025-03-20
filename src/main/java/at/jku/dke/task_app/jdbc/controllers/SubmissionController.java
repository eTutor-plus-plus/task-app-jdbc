package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseSubmissionController;
import at.jku.dke.task_app.jdbc.data.entities.jdbcSubmission;
import at.jku.dke.task_app.jdbc.dto.jdbcSubmissionDto;
import at.jku.dke.task_app.jdbc.services.jdbcSubmissionService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link jdbcSubmission}s.
 */
@RestController
public class SubmissionController extends BaseSubmissionController<jdbcSubmissionDto> {
    /**
     * Creates a new instance of class {@link SubmissionController}.
     *
     * @param submissionService The input service.
     */
    public SubmissionController(jdbcSubmissionService submissionService) {
        super(submissionService);
    }
}

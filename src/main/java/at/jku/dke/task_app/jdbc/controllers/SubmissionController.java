package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseSubmissionController;
import at.jku.dke.task_app.jdbc.data.entities.JDBCSubmission;
import at.jku.dke.task_app.jdbc.dto.JDBCSubmissionDto;
import at.jku.dke.task_app.jdbc.services.JDBCSubmissionService;

import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link JDBCSubmission}s.
 */
@RestController
public class SubmissionController extends BaseSubmissionController<JDBCSubmissionDto> {
    /**
     * Creates a new instance of class {@link SubmissionController}.
     *
     * @param submissionService The input service.
     */
    public SubmissionController(JDBCSubmissionService submissionService) {
        super(submissionService);
    }
}

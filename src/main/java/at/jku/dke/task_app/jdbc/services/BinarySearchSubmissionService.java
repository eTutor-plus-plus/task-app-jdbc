package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.services.BaseSubmissionService;
import at.jku.dke.task_app.jdbc.data.entities.jdbcSubmission;
import at.jku.dke.task_app.jdbc.data.entities.jdbcTask;
import at.jku.dke.task_app.jdbc.data.repositories.jdbcSubmissionRepository;
import at.jku.dke.task_app.jdbc.data.repositories.jdbcTaskRepository;
import at.jku.dke.task_app.jdbc.dto.jdbcSubmissionDto;
import at.jku.dke.task_app.jdbc.evaluation.EvaluationService;
import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link jdbcSubmission}s.
 */
@Service
public class jdbcSubmissionService extends BaseSubmissionService<jdbcTask, jdbcSubmission, jdbcSubmissionDto> {

    private final EvaluationService evaluationService;

    /**
     * Creates a new instance of class {@link jdbcSubmissionService}.
     *
     * @param submissionRepository The input repository.
     * @param taskRepository       The task repository.
     * @param evaluationService    The evaluation service.
     */
    public jdbcSubmissionService(jdbcSubmissionRepository submissionRepository, jdbcTaskRepository taskRepository, EvaluationService evaluationService) {
        super(submissionRepository, taskRepository);
        this.evaluationService = evaluationService;
    }

    @Override
    protected jdbcSubmission createSubmissionEntity(SubmitSubmissionDto<jdbcSubmissionDto> submitSubmissionDto) {
        return new jdbcSubmission(submitSubmissionDto.submission().input());
    }

    @Override
    protected GradingDto evaluate(SubmitSubmissionDto<jdbcSubmissionDto> submitSubmissionDto) {
        return this.evaluationService.evaluate(submitSubmissionDto);
    }

    @Override
    protected jdbcSubmissionDto mapSubmissionToSubmissionData(jdbcSubmission submission) {
        return new jdbcSubmissionDto(submission.getSubmission());
    }

}

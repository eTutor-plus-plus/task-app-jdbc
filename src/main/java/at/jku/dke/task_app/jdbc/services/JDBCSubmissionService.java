package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.GradingDto;
import at.jku.dke.etutor.task_app.dto.SubmitSubmissionDto;
import at.jku.dke.etutor.task_app.services.BaseSubmissionService;
import at.jku.dke.task_app.jdbc.data.entities.JDBCSubmission;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTask;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCSubmissionRepository;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskRepository;
import at.jku.dke.task_app.jdbc.dto.JDBCSubmissionDto;
import at.jku.dke.task_app.jdbc.evaluation.EvaluationService;

import org.springframework.stereotype.Service;

/**
 * This class provides methods for managing {@link JDBCSubmission}s.
 */
@Service
public class JDBCSubmissionService extends BaseSubmissionService<JDBCTask, JDBCSubmission, JDBCSubmissionDto> {

    private final EvaluationService evaluationService;

    /**
     * Creates a new instance of class {@link JDBCSubmissionService}.
     *
     * @param submissionRepository The input repository.
     * @param taskRepository       The task repository.
     * @param evaluationService    The evaluation service.
     */
    public JDBCSubmissionService(JDBCSubmissionRepository submissionRepository, JDBCTaskRepository taskRepository, EvaluationService evaluationService) {
        super(submissionRepository, taskRepository);
        this.evaluationService = evaluationService;
    }

    @Override
    protected JDBCSubmission createSubmissionEntity(SubmitSubmissionDto<JDBCSubmissionDto> submitSubmissionDto) {
        return new JDBCSubmission(submitSubmissionDto.submission().input());
    }

    @Override
    protected GradingDto evaluate(SubmitSubmissionDto<JDBCSubmissionDto> submitSubmissionDto) {
        return this.evaluationService.evaluate(submitSubmissionDto);
    }

    @Override
    protected JDBCSubmissionDto mapSubmissionToSubmissionData(JDBCSubmission submission) {
        return new JDBCSubmissionDto(submission.getSubmission());
    }

}

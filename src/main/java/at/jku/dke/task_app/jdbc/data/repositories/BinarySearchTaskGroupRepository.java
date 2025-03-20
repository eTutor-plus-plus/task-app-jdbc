package at.jku.dke.task_app.jdbc.data.repositories;

import at.jku.dke.etutor.task_app.data.repositories.TaskGroupRepository;
import at.jku.dke.task_app.jdbc.data.entities.jdbcTaskGroup;

import java.util.Optional;

/**
 * Repository for entity {@link jdbcTaskGroup}.
 */
public interface jdbcTaskGroupRepository extends TaskGroupRepository<jdbcTaskGroup> {
}

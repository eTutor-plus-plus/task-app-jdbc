package at.jku.dke.task_app.jdbc.data.repositories;

import at.jku.dke.etutor.task_app.data.repositories.TaskGroupRepository;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;

import java.util.Optional;

/**
 * Repository for entity {@link JDBCTaskGroup}.
 */
public interface JDBCTaskGroupRepository extends TaskGroupRepository<JDBCTaskGroup> {
}

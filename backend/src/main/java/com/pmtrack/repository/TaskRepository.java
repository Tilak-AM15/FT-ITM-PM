package com.pmtrack.repository;

import com.pmtrack.model.Project;
import com.pmtrack.model.Task;
import com.pmtrack.model.TaskStatus;
import com.pmtrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProject(Project project);

    List<Task> findByProjectId(Long projectId);

    List<Task> findByTaskOwner(User taskOwner);

    List<Task> findByStatus(TaskStatus status);

    Optional<Task> findByTaskCode(String taskCode);

    /**
     * Returns tasks where the user is:
     * 1. The task owner, OR
     * 2. One of the assigned users.
     *
     * Explicit JOIN is used instead of MEMBER OF so that
     * the assignee relationship is queried directly by ID.
     */
    @Query("""
        SELECT DISTINCT t
        FROM Task t
        LEFT JOIN t.assignees a
        WHERE t.taskOwner.id = :userId
           OR a.id = :userId
        ORDER BY t.createdAt DESC
        """)
    List<Task> findTasksAssignedToUser(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
          AND t.status = 'COMPLETED'
        """)
    long countCompletedTasksByProjectId(
            @Param("projectId") Long projectId
    );

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
        """)
    long countTotalTasksByProjectId(
            @Param("projectId") Long projectId
    );
}

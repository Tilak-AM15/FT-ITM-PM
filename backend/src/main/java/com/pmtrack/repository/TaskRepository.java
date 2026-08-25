package com.pmtrack.repository;

import com.pmtrack.model.Project;
import com.pmtrack.model.Task;
import com.pmtrack.model.TaskStatus;
import com.pmtrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByProjectId(Long projectId);
    List<Task> findByTaskOwner(User taskOwner);
    List<Task> findByStatus(TaskStatus status);
    Optional<Task> findByTaskCode(String taskCode);

    @Query("SELECT t FROM Task t WHERE t.taskOwner.id = :userId OR :user MEMBER OF t.assignees")
    List<Task> findTasksAssignedToUser(@Param("userId") Long userId, @Param("user") User user);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = 'COMPLETED'")
    long countCompletedTasksByProjectId(Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countTotalTasksByProjectId(Long projectId);
}

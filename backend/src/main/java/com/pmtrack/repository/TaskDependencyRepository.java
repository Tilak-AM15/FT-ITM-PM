package com.pmtrack.repository;

import com.pmtrack.model.Task;
import com.pmtrack.model.TaskComment;
import com.pmtrack.model.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    List<TaskDependency> findByTask(Task task);
    List<TaskDependency> findByTaskId(Long taskId);
    List<TaskDependency> findByDependsOnTaskId(Long dependsOnTaskId);
}

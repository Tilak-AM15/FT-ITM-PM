package com.pmtrack.repository;

import com.pmtrack.model.Task;
import com.pmtrack.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTaskOrderByCreatedAtAsc(Task task);
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}

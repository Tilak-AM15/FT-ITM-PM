package com.pmtrack.repository;

import com.pmtrack.model.SubTask;
import com.pmtrack.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findByTask(Task task);
    List<SubTask> findByTaskId(Long taskId);
}

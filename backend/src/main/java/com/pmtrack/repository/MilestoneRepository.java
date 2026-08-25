package com.pmtrack.repository;

import com.pmtrack.model.Milestone;
import com.pmtrack.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProject(Project project);
    List<Milestone> findByProjectId(Long projectId);
}

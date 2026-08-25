package com.pmtrack.repository;

import com.pmtrack.model.Project;
import com.pmtrack.model.ProjectRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRiskRepository extends JpaRepository<ProjectRisk, Long> {
    List<ProjectRisk> findByProject(Project project);
    List<ProjectRisk> findByProjectId(Long projectId);
}

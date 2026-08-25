package com.pmtrack.repository;

import com.pmtrack.model.Project;
import com.pmtrack.model.ProjectStatus;
import com.pmtrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectCode(String projectCode);
    List<Project> findByProjectManager(User projectManager);
    List<Project> findByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.projectManager.id = :userId OR p.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId)")
    List<Project> findProjectsForUser(Long userId);
}

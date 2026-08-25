package com.pmtrack.repository;

import com.pmtrack.model.Project;
import com.pmtrack.model.ProjectMember;
import com.pmtrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProject(Project project);
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByUser(User user);
    List<ProjectMember> findByUserId(Long userId);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}

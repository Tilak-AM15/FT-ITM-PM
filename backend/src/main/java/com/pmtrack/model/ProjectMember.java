package com.pmtrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_members", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String roleInProject = "Developer"; // Tech Lead, Developer, QA, Designer, DevOps

    private Integer allocationPercentage = 100; // 0 - 100%

    private LocalDate assignedDate = LocalDate.now();

    public ProjectMember() {}

    public ProjectMember(Project project, User user, String roleInProject, Integer allocationPercentage) {
        this.project = project;
        this.user = user;
        this.roleInProject = roleInProject;
        this.allocationPercentage = allocationPercentage;
        this.assignedDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRoleInProject() { return roleInProject; }
    public void setRoleInProject(String roleInProject) { this.roleInProject = roleInProject; }

    public Integer getAllocationPercentage() { return allocationPercentage; }
    public void setAllocationPercentage(Integer allocationPercentage) { this.allocationPercentage = allocationPercentage; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
}

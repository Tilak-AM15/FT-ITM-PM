package com.pmtrack.dto;

import com.pmtrack.model.ProjectPriority;
import com.pmtrack.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {

    public static class ProjectRequest {
        @NotBlank(message = "Project code is required")
        @Size(max = 50, message = "Project code must be 50 characters or fewer")
        private String projectCode;

        @NotBlank(message = "Project name is required")
        @Size(max = 150, message = "Project name must be 150 characters or fewer")
        private String name;

        private String clientName;
        private String description;
        private Long projectManagerId;
        private LocalDate startDate;
        private LocalDate endDate;
        private ProjectPriority priority = ProjectPriority.MEDIUM;
        private ProjectStatus status = ProjectStatus.ACTIVE;
        @PositiveOrZero(message = "Budget cannot be negative")
        private Double budgetAmount = 0.0;
        @PositiveOrZero(message = "Estimated hours cannot be negative")
        private Double estimatedHours = 0.0;
        private List<Long> memberUserIds;
        private List<String> documentUrls;

        public ProjectRequest() {}

        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getProjectManagerId() { return projectManagerId; }
        public void setProjectManagerId(Long projectManagerId) { this.projectManagerId = projectManagerId; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public ProjectPriority getPriority() { return priority; }
        public void setPriority(ProjectPriority priority) { this.priority = priority; }
        public ProjectStatus getStatus() { return status; }
        public void setStatus(ProjectStatus status) { this.status = status; }
        public Double getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        public List<Long> getMemberUserIds() { return memberUserIds; }
        public void setMemberUserIds(List<Long> memberUserIds) { this.memberUserIds = memberUserIds; }
        public List<String> getDocumentUrls() { return documentUrls; }
        public void setDocumentUrls(List<String> documentUrls) { this.documentUrls = documentUrls; }
    }

    public static class ProjectResponse {
        private Long id;
        private String projectCode;
        private String name;
        private String clientName;
        private String description;
        private AuthDto.UserProfileDto projectManager;
        private LocalDate startDate;
        private LocalDate endDate;
        private ProjectPriority priority;
        private ProjectStatus status;
        private Double budgetAmount;
        private Double estimatedHours;
        private Double actualHours;
        private Integer healthScore;
        private Integer completionPercentage = 0;
        private long totalTasks = 0;
        private long completedTasks = 0;
        private long activeTasks = 0;
        private int teamMemberCount = 0;
        private List<MemberInfo> members;
        private List<String> documentUrls;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        public ProjectResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public AuthDto.UserProfileDto getProjectManager() { return projectManager; }
        public void setProjectManager(AuthDto.UserProfileDto projectManager) { this.projectManager = projectManager; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public ProjectPriority getPriority() { return priority; }
        public void setPriority(ProjectPriority priority) { this.priority = priority; }
        public ProjectStatus getStatus() { return status; }
        public void setStatus(ProjectStatus status) { this.status = status; }
        public Double getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        public Double getActualHours() { return actualHours; }
        public void setActualHours(Double actualHours) { this.actualHours = actualHours; }
        public Integer getHealthScore() { return healthScore; }
        public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
        public Integer getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
        public long getActiveTasks() { return activeTasks; }
        public void setActiveTasks(long activeTasks) { this.activeTasks = activeTasks; }
        public int getTeamMemberCount() { return teamMemberCount; }
        public void setTeamMemberCount(int teamMemberCount) { this.teamMemberCount = teamMemberCount; }
        public List<MemberInfo> getMembers() { return members; }
        public void setMembers(List<MemberInfo> members) { this.members = members; }
        public List<String> getDocumentUrls() { return documentUrls; }
        public void setDocumentUrls(List<String> documentUrls) { this.documentUrls = documentUrls; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
        public LocalDateTime getUpdatedDate() { return updatedDate; }
        public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    }

    public static class MemberInfo {
        private Long id;
        private AuthDto.UserProfileDto user;
        private String roleInProject;
        private Integer allocationPercentage;
        private LocalDate assignedDate;

        public MemberInfo() {}
        public MemberInfo(Long id, AuthDto.UserProfileDto user, String roleInProject, Integer allocationPercentage, LocalDate assignedDate) {
            this.id = id;
            this.user = user;
            this.roleInProject = roleInProject;
            this.allocationPercentage = allocationPercentage;
            this.assignedDate = assignedDate;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public AuthDto.UserProfileDto getUser() { return user; }
        public void setUser(AuthDto.UserProfileDto user) { this.user = user; }
        public String getRoleInProject() { return roleInProject; }
        public void setRoleInProject(String roleInProject) { this.roleInProject = roleInProject; }
        public Integer getAllocationPercentage() { return allocationPercentage; }
        public void setAllocationPercentage(Integer allocationPercentage) { this.allocationPercentage = allocationPercentage; }
        public LocalDate getAssignedDate() { return assignedDate; }
        public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    }

    public static class MilestoneDto {
        private Long id;
        private Long projectId;
        private String title;
        private String description;
        private LocalDate targetDate;
        private String status;
        private String deliverables;

        public MilestoneDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDeliverables() { return deliverables; }
        public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
    }

    public static class RiskDto {
        private Long id;
        private Long projectId;
        private String title;
        private String description;
        private String severity;
        private String status;
        private String mitigationPlan;

        public RiskDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMitigationPlan() { return mitigationPlan; }
        public void setMitigationPlan(String mitigationPlan) { this.mitigationPlan = mitigationPlan; }
    }
}

package com.pmtrack.dto;

import com.pmtrack.model.TaskPriority;
import com.pmtrack.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskDto {

    public static class TaskRequest {
        @NotNull(message = "Project ID is required")
        private Long projectId;

        private String taskCode;

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must be 200 characters or fewer")
        private String title;

        private String description;
        private String moduleName;
        private Long taskOwnerId;
        private List<Long> assigneeIds;
        private TaskPriority priority = TaskPriority.MEDIUM;
        private TaskStatus status = TaskStatus.TO_DO;
        private LocalDate startDate;
        private LocalDate dueDate;
        @PositiveOrZero(message = "Estimated hours cannot be negative")
        private Double estimatedHours = 0.0;
        private Long parentTaskId;
        private List<Long> dependsOnTaskIds;
        private List<String> attachmentUrls;

        public TaskRequest() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getTaskCode() { return taskCode; }
        public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getModuleName() { return moduleName; }
        public void setModuleName(String moduleName) { this.moduleName = moduleName; }
        public Long getTaskOwnerId() { return taskOwnerId; }
        public void setTaskOwnerId(Long taskOwnerId) { this.taskOwnerId = taskOwnerId; }
        public List<Long> getAssigneeIds() { return assigneeIds; }
        public void setAssigneeIds(List<Long> assigneeIds) { this.assigneeIds = assigneeIds; }
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        public Long getParentTaskId() { return parentTaskId; }
        public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }
        public List<Long> getDependsOnTaskIds() { return dependsOnTaskIds; }
        public void setDependsOnTaskIds(List<Long> dependsOnTaskIds) { this.dependsOnTaskIds = dependsOnTaskIds; }
        public List<String> getAttachmentUrls() { return attachmentUrls; }
        public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls; }
    }

    public static class TaskResponse {
        private Long id;
        private Long projectId;
        private String projectName;
        private String projectCode;
        private String taskCode;
        private String title;
        private String description;
        private String moduleName;
        private AuthDto.UserProfileDto taskOwner;
        private List<AuthDto.UserProfileDto> assignees;
        private TaskPriority priority;
        private TaskStatus status;
        private LocalDate startDate;
        private LocalDate dueDate;
        private Double estimatedHours;
        private Double actualHours;
        private Integer progressPercentage;
        private Long parentTaskId;
        private boolean isOverdue;
        private List<SubTaskDto> subTasks;
        private List<TaskCommentDto> comments;
        private List<DependencyDto> dependencies;
        private List<String> attachmentUrls;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TaskResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getTaskCode() { return taskCode; }
        public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getModuleName() { return moduleName; }
        public void setModuleName(String moduleName) { this.moduleName = moduleName; }
        public AuthDto.UserProfileDto getTaskOwner() { return taskOwner; }
        public void setTaskOwner(AuthDto.UserProfileDto taskOwner) { this.taskOwner = taskOwner; }
        public List<AuthDto.UserProfileDto> getAssignees() { return assignees; }
        public void setAssignees(List<AuthDto.UserProfileDto> assignees) { this.assignees = assignees; }
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        public Double getActualHours() { return actualHours; }
        public void setActualHours(Double actualHours) { this.actualHours = actualHours; }
        public Integer getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
        public Long getParentTaskId() { return parentTaskId; }
        public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }
        public boolean isOverdue() { return isOverdue; }
        public void setOverdue(boolean overdue) { isOverdue = overdue; }
        public List<SubTaskDto> getSubTasks() { return subTasks; }
        public void setSubTasks(List<SubTaskDto> subTasks) { this.subTasks = subTasks; }
        public List<TaskCommentDto> getComments() { return comments; }
        public void setComments(List<TaskCommentDto> comments) { this.comments = comments; }
        public List<DependencyDto> getDependencies() { return dependencies; }
        public void setDependencies(List<DependencyDto> dependencies) { this.dependencies = dependencies; }
        public List<String> getAttachmentUrls() { return attachmentUrls; }
        public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class SubTaskDto {
        private Long id;
        private Long taskId;
        private String title;
        private boolean completed;
        private AuthDto.UserProfileDto assignedTo;

        public SubTaskDto() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public AuthDto.UserProfileDto getAssignedTo() { return assignedTo; }
        public void setAssignedTo(AuthDto.UserProfileDto assignedTo) { this.assignedTo = assignedTo; }
    }

    public static class TaskCommentDto {
        private Long id;
        private Long taskId;
        private AuthDto.UserProfileDto author;
        private String content;
        private LocalDateTime createdAt;

        public TaskCommentDto() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public AuthDto.UserProfileDto getAuthor() { return author; }
        public void setAuthor(AuthDto.UserProfileDto author) { this.author = author; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class DependencyDto {
        private Long id;
        private Long taskId;
        private Long dependsOnTaskId;
        private String dependsOnTaskTitle;
        private String dependsOnTaskCode;
        private TaskStatus dependsOnTaskStatus;
        private String dependencyType;

        public DependencyDto() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Long getDependsOnTaskId() { return dependsOnTaskId; }
        public void setDependsOnTaskId(Long dependsOnTaskId) { this.dependsOnTaskId = dependsOnTaskId; }
        public String getDependsOnTaskTitle() { return dependsOnTaskTitle; }
        public void setDependsOnTaskTitle(String dependsOnTaskTitle) { this.dependsOnTaskTitle = dependsOnTaskTitle; }
        public String getDependsOnTaskCode() { return dependsOnTaskCode; }
        public void setDependsOnTaskCode(String dependsOnTaskCode) { this.dependsOnTaskCode = dependsOnTaskCode; }
        public TaskStatus getDependsOnTaskStatus() { return dependsOnTaskStatus; }
        public void setDependsOnTaskStatus(TaskStatus dependsOnTaskStatus) { this.dependsOnTaskStatus = dependsOnTaskStatus; }
        public String getDependencyType() { return dependencyType; }
        public void setDependencyType(String dependencyType) { this.dependencyType = dependencyType; }
    }

    public static class StatusUpdateDto {
        private TaskStatus status;
        private Integer progressPercentage;

        public StatusUpdateDto() {}
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
        public Integer getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    }
}

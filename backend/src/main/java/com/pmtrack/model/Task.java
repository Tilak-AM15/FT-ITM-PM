package com.pmtrack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = "task_code"))
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 50)
    private String taskCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String moduleName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_owner_id")
    private User taskOwner;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "task_assignees",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status = TaskStatus.TO_DO;

    private LocalDate startDate;
    private LocalDate dueDate;

    private Double estimatedHours = 0.0;
    private Double actualHours = 0.0;
    private Integer progressPercentage = 0; // 0 - 100%

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_attachments", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "attachment_url", length = 500)
    private List<String> attachmentUrls = new ArrayList<>();

    public Task() {}

    public Task(Project project, String taskCode, String title, String description, String moduleName, User taskOwner, TaskPriority priority, TaskStatus status, LocalDate startDate, LocalDate dueDate, Double estimatedHours) {
        this.project = project;
        this.taskCode = taskCode;
        this.title = title;
        this.description = description;
        this.moduleName = moduleName;
        this.taskOwner = taskOwner;
        this.priority = priority;
        this.status = status;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.estimatedHours = estimatedHours;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls != null ? attachmentUrls : new ArrayList<>(); }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public User getTaskOwner() { return taskOwner; }
    public void setTaskOwner(User taskOwner) { this.taskOwner = taskOwner; }

    public Set<User> getAssignees() { return assignees; }
    public void setAssignees(Set<User> assignees) { this.assignees = assignees; }

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

    public Task getParentTask() { return parentTask; }
    public void setParentTask(Task parentTask) { this.parentTask = parentTask; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

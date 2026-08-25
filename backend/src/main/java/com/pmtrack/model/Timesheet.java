package com.pmtrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private Double hoursWorked;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 20)
    private String startTime;

    @Column(length = 20)
    private String endTime;

    private Boolean billable = true;

    @Column(length = 255)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDateTime submittedAt;
    private LocalDateTime approvedOrRejectedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Timesheet() {}

    public Timesheet(User user, Project project, Task task, LocalDate workDate, Double hoursWorked, String description, Boolean billable, TimesheetStatus status) {
        this.user = user;
        this.project = project;
        this.task = task;
        this.workDate = workDate;
        this.hoursWorked = hoursWorked;
        this.description = description;
        this.billable = billable != null ? billable : true;
        this.status = status != null ? status : TimesheetStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Boolean getBillable() { return billable; }
    public void setBillable(Boolean billable) { this.billable = billable; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getApprovedOrRejectedAt() { return approvedOrRejectedAt; }
    public void setApprovedOrRejectedAt(LocalDateTime approvedOrRejectedAt) { this.approvedOrRejectedAt = approvedOrRejectedAt; }

    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

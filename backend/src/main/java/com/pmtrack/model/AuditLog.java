package com.pmtrack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String username;

    @Column(nullable = false, length = 100)
    private String action; // e.g. "PROJECT_CREATED", "TIMESHEET_APPROVED", "TASK_STATUS_CHANGED"

    @Column(nullable = false, length = 50)
    private String entityName; // "Project", "Task", "Timesheet", "User"

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String previousValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 50)
    private String ipAddress;

    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(User user, String username, String action, String entityName, Long entityId, String previousValue, String newValue, String details) {
        this.user = user;
        this.username = username != null ? username : (user != null ? user.getUsername() : "SYSTEM");
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

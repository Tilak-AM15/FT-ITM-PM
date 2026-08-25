package com.pmtrack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String projectCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String clientName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    private Double budgetAmount = 0.0;
    private Double estimatedHours = 0.0;
    private Double actualHours = 0.0;
    private Integer healthScore = 95; // 0-100%

    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime updatedDate = LocalDateTime.now();

    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_documents", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "document_url", length = 500)
    private List<String> documentUrls = new ArrayList<>();

    public Project() {}

    public Project(String projectCode, String name, String clientName, String description, User projectManager, LocalDate startDate, LocalDate endDate, ProjectPriority priority, ProjectStatus status, Double budgetAmount, Double estimatedHours) {
        this.projectCode = projectCode;
        this.name = name;
        this.clientName = clientName;
        this.description = description;
        this.projectManager = projectManager;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.status = status;
        this.budgetAmount = budgetAmount;
        this.estimatedHours = estimatedHours;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public List<String> getDocumentUrls() { return documentUrls; }
    public void setDocumentUrls(List<String> documentUrls) { this.documentUrls = documentUrls != null ? documentUrls : new ArrayList<>(); }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // Getters & Setters
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

    public User getProjectManager() { return projectManager; }
    public void setProjectManager(User projectManager) { this.projectManager = projectManager; }

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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}

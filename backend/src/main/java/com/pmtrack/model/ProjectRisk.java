package com.pmtrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "project_risks")
public class ProjectRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String severity = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(length = 20)
    private String status = "OPEN"; // OPEN, MITIGATED, CLOSED

    @Column(columnDefinition = "TEXT")
    private String mitigationPlan;

    public ProjectRisk() {}

    public ProjectRisk(Project project, String title, String description, String severity, String status, String mitigationPlan) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.severity = severity != null ? severity : "MEDIUM";
        this.status = status != null ? status : "OPEN";
        this.mitigationPlan = mitigationPlan;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

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

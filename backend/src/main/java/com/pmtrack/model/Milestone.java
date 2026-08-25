package com.pmtrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "milestones")
public class Milestone {

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

    private LocalDate targetDate;

    @Column(length = 30)
    private String status = "PENDING"; // PENDING, ACHIEVED, DELAYED

    @Column(columnDefinition = "TEXT")
    private String deliverables;

    public Milestone() {}

    public Milestone(Project project, String title, String description, LocalDate targetDate, String status, String deliverables) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.targetDate = targetDate;
        this.status = status != null ? status : "PENDING";
        this.deliverables = deliverables;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

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

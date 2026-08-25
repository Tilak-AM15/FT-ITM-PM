package com.pmtrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "task_dependencies", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"task_id", "depends_on_task_id"}))
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOnTask;

    @Column(length = 50)
    private String dependencyType = "FINISH_TO_START"; // FINISH_TO_START, START_TO_START

    public TaskDependency() {}

    public TaskDependency(Task task, Task dependsOnTask, String dependencyType) {
        this.task = task;
        this.dependsOnTask = dependsOnTask;
        this.dependencyType = dependencyType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Task getDependsOnTask() { return dependsOnTask; }
    public void setDependsOnTask(Task dependsOnTask) { this.dependsOnTask = dependsOnTask; }

    public String getDependencyType() { return dependencyType; }
    public void setDependencyType(String dependencyType) { this.dependencyType = dependencyType; }
}

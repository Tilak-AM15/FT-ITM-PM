package com.pmtrack.dto;

import java.time.LocalDate;

public class ReportDto {

    public static class ProjectReportRow {
        private Long projectId;
        private String projectCode;
        private String projectName;
        private String clientName;
        private String projectManagerName;
        private String status;
        private String priority;
        private LocalDate startDate;
        private LocalDate endDate;
        private Double budgetAmount;
        private Double estimatedHours;
        private Double actualHours;
        private Double varianceHours;
        private Integer completionPercentage;
        private Long delayedTasksCount;

        public ProjectReportRow() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public String getProjectManagerName() { return projectManagerName; }
        public void setProjectManagerName(String projectManagerName) { this.projectManagerName = projectManagerName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Double getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }
        public Double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
        public Double getActualHours() { return actualHours; }
        public void setActualHours(Double actualHours) { this.actualHours = actualHours; }
        public Double getVarianceHours() { return varianceHours; }
        public void setVarianceHours(Double varianceHours) { this.varianceHours = varianceHours; }
        public Integer getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
        public Long getDelayedTasksCount() { return delayedTasksCount; }
        public void setDelayedTasksCount(Long delayedTasksCount) { this.delayedTasksCount = delayedTasksCount; }
    }

    public static class ResourceReportRow {
        private Long userId;
        private String employeeName;
        private String email;
        private String designation;
        private String department;
        private Double totalHours;
        private Double billableHours;
        private Double nonBillableHours;
        private Double utilizationPercentage;
        private Integer activeProjectsCount;
        private Integer activeTasksCount;

        public ResourceReportRow() {}

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Double getTotalHours() { return totalHours; }
        public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }
        public Double getBillableHours() { return billableHours; }
        public void setBillableHours(Double billableHours) { this.billableHours = billableHours; }
        public Double getNonBillableHours() { return nonBillableHours; }
        public void setNonBillableHours(Double nonBillableHours) { this.nonBillableHours = nonBillableHours; }
        public Double getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(Double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
        public Integer getActiveProjectsCount() { return activeProjectsCount; }
        public void setActiveProjectsCount(Integer activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }
        public Integer getActiveTasksCount() { return activeTasksCount; }
        public void setActiveTasksCount(Integer activeTasksCount) { this.activeTasksCount = activeTasksCount; }
    }

    public static class TimesheetReportRow {
        private Long timesheetId;
        private LocalDate workDate;
        private String employeeName;
        private String projectCode;
        private String projectName;
        private String taskTitle;
        private Double hoursWorked;
        private String description;
        private Boolean billable;
        private String status;
        private String reviewerName;

        public TimesheetReportRow() {}

        public Long getTimesheetId() { return timesheetId; }
        public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }
        public LocalDate getWorkDate() { return workDate; }
        public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public Double getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Boolean getBillable() { return billable; }
        public void setBillable(Boolean billable) { this.billable = billable; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReviewerName() { return reviewerName; }
        public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    }
}

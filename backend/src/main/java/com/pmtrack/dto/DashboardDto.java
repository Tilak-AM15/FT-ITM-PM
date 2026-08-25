package com.pmtrack.dto;

import java.util.List;
import java.util.Map;

public class DashboardDto {

    public static class ManagementDashboardResponse {
        private long totalProjects;
        private long activeProjects;
        private long completedProjects;
        private long delayedProjects;
        private long projectsAtRisk;
        private long totalEmployees;
        private long utilizedEmployees;
        private long availableEmployees;
        private double totalPlannedHours;
        private double totalActualHours;
        private double totalBillableHours;
        private double totalNonBillableHours;
        private double overallUtilizationPercentage;
        private long pendingTimesheetsCount;
        private long pendingApprovalsCount;
        private List<PortfolioProjectItem> portfolioProjects;
        private TaskOverviewItem taskOverview;
        private List<TeamWorkloadItem> teamWorkloads;
        private ProjectHealthSummary projectHealth;

        public ManagementDashboardResponse() {}

        public long getTotalProjects() { return totalProjects; }
        public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }
        public long getActiveProjects() { return activeProjects; }
        public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }
        public long getCompletedProjects() { return completedProjects; }
        public void setCompletedProjects(long completedProjects) { this.completedProjects = completedProjects; }
        public long getDelayedProjects() { return delayedProjects; }
        public void setDelayedProjects(long delayedProjects) { this.delayedProjects = delayedProjects; }
        public long getProjectsAtRisk() { return projectsAtRisk; }
        public void setProjectsAtRisk(long projectsAtRisk) { this.projectsAtRisk = projectsAtRisk; }
        public long getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
        public long getUtilizedEmployees() { return utilizedEmployees; }
        public void setUtilizedEmployees(long utilizedEmployees) { this.utilizedEmployees = utilizedEmployees; }
        public long getAvailableEmployees() { return availableEmployees; }
        public void setAvailableEmployees(long availableEmployees) { this.availableEmployees = availableEmployees; }
        public double getTotalPlannedHours() { return totalPlannedHours; }
        public void setTotalPlannedHours(double totalPlannedHours) { this.totalPlannedHours = totalPlannedHours; }
        public double getTotalActualHours() { return totalActualHours; }
        public void setTotalActualHours(double totalActualHours) { this.totalActualHours = totalActualHours; }
        public double getTotalBillableHours() { return totalBillableHours; }
        public void setTotalBillableHours(double totalBillableHours) { this.totalBillableHours = totalBillableHours; }
        public double getTotalNonBillableHours() { return totalNonBillableHours; }
        public void setTotalNonBillableHours(double totalNonBillableHours) { this.totalNonBillableHours = totalNonBillableHours; }
        public double getOverallUtilizationPercentage() { return overallUtilizationPercentage; }
        public void setOverallUtilizationPercentage(double overallUtilizationPercentage) { this.overallUtilizationPercentage = overallUtilizationPercentage; }
        public long getPendingTimesheetsCount() { return pendingTimesheetsCount; }
        public void setPendingTimesheetsCount(long pendingTimesheetsCount) { this.pendingTimesheetsCount = pendingTimesheetsCount; }
        public long getPendingApprovalsCount() { return pendingApprovalsCount; }
        public void setPendingApprovalsCount(long pendingApprovalsCount) { this.pendingApprovalsCount = pendingApprovalsCount; }
        public List<PortfolioProjectItem> getPortfolioProjects() { return portfolioProjects; }
        public void setPortfolioProjects(List<PortfolioProjectItem> portfolioProjects) { this.portfolioProjects = portfolioProjects; }
        public TaskOverviewItem getTaskOverview() { return taskOverview; }
        public void setTaskOverview(TaskOverviewItem taskOverview) { this.taskOverview = taskOverview; }
        public List<TeamWorkloadItem> getTeamWorkloads() { return teamWorkloads; }
        public void setTeamWorkloads(List<TeamWorkloadItem> teamWorkloads) { this.teamWorkloads = teamWorkloads; }
        public ProjectHealthSummary getProjectHealth() { return projectHealth; }
        public void setProjectHealth(ProjectHealthSummary projectHealth) { this.projectHealth = projectHealth; }
    }

    public static class PortfolioProjectItem {
        private Long id;
        private String name;
        private String projectCode;
        private String clientName;
        private int progressPercentage;
        private long totalTasks;
        private long openTasks;
        private double loggedHours;
        private double estimatedHours;
        private double billablePercentage;
        private String status;
        private String endDate;

        public PortfolioProjectItem() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }
        public int getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
        public long getOpenTasks() { return openTasks; }
        public void setOpenTasks(long openTasks) { this.openTasks = openTasks; }
        public double getLoggedHours() { return loggedHours; }
        public void setLoggedHours(double loggedHours) { this.loggedHours = loggedHours; }
        public double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }
        public double getBillablePercentage() { return billablePercentage; }
        public void setBillablePercentage(double billablePercentage) { this.billablePercentage = billablePercentage; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    public static class TaskOverviewItem {
        private long totalTasks;
        private long todoCount;
        private long inProgressCount;
        private long blockedCount;
        private long completedCount;

        public TaskOverviewItem() {}
        public TaskOverviewItem(long totalTasks, long todoCount, long inProgressCount, long blockedCount, long completedCount) {
            this.totalTasks = totalTasks;
            this.todoCount = todoCount;
            this.inProgressCount = inProgressCount;
            this.blockedCount = blockedCount;
            this.completedCount = completedCount;
        }

        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
        public long getTodoCount() { return todoCount; }
        public void setTodoCount(long todoCount) { this.todoCount = todoCount; }
        public long getInProgressCount() { return inProgressCount; }
        public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }
        public long getBlockedCount() { return blockedCount; }
        public void setBlockedCount(long blockedCount) { this.blockedCount = blockedCount; }
        public long getCompletedCount() { return completedCount; }
        public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
    }

    public static class TeamWorkloadItem {
        private Long userId;
        private String name;
        private String role;
        private String avatarUrl;
        private int activeProjectsCount;
        private int activeTasksCount;
        private int utilizationPercentage;
        private double totalHoursLogged;

        public TeamWorkloadItem() {}

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public int getActiveProjectsCount() { return activeProjectsCount; }
        public void setActiveProjectsCount(int activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }
        public int getActiveTasksCount() { return activeTasksCount; }
        public void setActiveTasksCount(int activeTasksCount) { this.activeTasksCount = activeTasksCount; }
        public int getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(int utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
        public double getTotalHoursLogged() { return totalHoursLogged; }
        public void setTotalHoursLogged(double totalHoursLogged) { this.totalHoursLogged = totalHoursLogged; }
    }

    public static class ProjectHealthSummary {
        private int budgetVsActual; // e.g. 78%
        private int schedulePerformance; // e.g. 84%
        private int qualityScore; // e.g. 90%
        private int riskLevel; // e.g. 5 open risks
        private int resourceUtilization; // e.g. 78%

        public ProjectHealthSummary() {}
        public ProjectHealthSummary(int budgetVsActual, int schedulePerformance, int qualityScore, int riskLevel, int resourceUtilization) {
            this.budgetVsActual = budgetVsActual;
            this.schedulePerformance = schedulePerformance;
            this.qualityScore = qualityScore;
            this.riskLevel = riskLevel;
            this.resourceUtilization = resourceUtilization;
        }

        public int getBudgetVsActual() { return budgetVsActual; }
        public void setBudgetVsActual(int budgetVsActual) { this.budgetVsActual = budgetVsActual; }
        public int getSchedulePerformance() { return schedulePerformance; }
        public void setSchedulePerformance(int schedulePerformance) { this.schedulePerformance = schedulePerformance; }
        public int getQualityScore() { return qualityScore; }
        public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }
        public int getRiskLevel() { return riskLevel; }
        public void setRiskLevel(int riskLevel) { this.riskLevel = riskLevel; }
        public int getResourceUtilization() { return resourceUtilization; }
        public void setResourceUtilization(int resourceUtilization) { this.resourceUtilization = resourceUtilization; }
    }

    public static class EmployeeDashboardResponse {
        private long myProjectsCount;
        private long myTasksCount;
        private long todayTasksCount;
        private long pendingTasksCount;
        private long completedTasksCount;
        private double totalHoursLogged;
        private double thisWeekHours;
        private double thisWeekBillableHours;
        private double thisWeekNonBillableHours;
        private long pendingTimesheetsCount;
        private long approvedTimesheetsCount;
        private long rejectedTimesheetsCount;
        private List<TaskDto.TaskResponse> assignedTasks;
        private List<TimesheetDto.TimesheetResponse> recentTimesheets;

        public EmployeeDashboardResponse() {}

        public long getMyProjectsCount() { return myProjectsCount; }
        public void setMyProjectsCount(long myProjectsCount) { this.myProjectsCount = myProjectsCount; }
        public long getMyTasksCount() { return myTasksCount; }
        public void setMyTasksCount(long myTasksCount) { this.myTasksCount = myTasksCount; }
        public long getTodayTasksCount() { return todayTasksCount; }
        public void setTodayTasksCount(long todayTasksCount) { this.todayTasksCount = todayTasksCount; }
        public long getPendingTasksCount() { return pendingTasksCount; }
        public void setPendingTasksCount(long pendingTasksCount) { this.pendingTasksCount = pendingTasksCount; }
        public long getCompletedTasksCount() { return completedTasksCount; }
        public void setCompletedTasksCount(long completedTasksCount) { this.completedTasksCount = completedTasksCount; }
        public double getTotalHoursLogged() { return totalHoursLogged; }
        public void setTotalHoursLogged(double totalHoursLogged) { this.totalHoursLogged = totalHoursLogged; }
        public double getThisWeekHours() { return thisWeekHours; }
        public void setThisWeekHours(double thisWeekHours) { this.thisWeekHours = thisWeekHours; }
        public double getThisWeekBillableHours() { return thisWeekBillableHours; }
        public void setThisWeekBillableHours(double thisWeekBillableHours) { this.thisWeekBillableHours = thisWeekBillableHours; }
        public double getThisWeekNonBillableHours() { return thisWeekNonBillableHours; }
        public void setThisWeekNonBillableHours(double thisWeekNonBillableHours) { this.thisWeekNonBillableHours = thisWeekNonBillableHours; }
        public long getPendingTimesheetsCount() { return pendingTimesheetsCount; }
        public void setPendingTimesheetsCount(long pendingTimesheetsCount) { this.pendingTimesheetsCount = pendingTimesheetsCount; }
        public long getApprovedTimesheetsCount() { return approvedTimesheetsCount; }
        public void setApprovedTimesheetsCount(long approvedTimesheetsCount) { this.approvedTimesheetsCount = approvedTimesheetsCount; }
        public long getRejectedTimesheetsCount() { return rejectedTimesheetsCount; }
        public void setRejectedTimesheetsCount(long rejectedTimesheetsCount) { this.rejectedTimesheetsCount = rejectedTimesheetsCount; }
        public List<TaskDto.TaskResponse> getAssignedTasks() { return assignedTasks; }
        public void setAssignedTasks(List<TaskDto.TaskResponse> assignedTasks) { this.assignedTasks = assignedTasks; }
        public List<TimesheetDto.TimesheetResponse> getRecentTimesheets() { return recentTimesheets; }
        public void setRecentTimesheets(List<TimesheetDto.TimesheetResponse> recentTimesheets) { this.recentTimesheets = recentTimesheets; }
    }
}

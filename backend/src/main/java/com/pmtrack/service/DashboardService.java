package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.dto.DashboardDto;
import com.pmtrack.dto.TaskDto;
import com.pmtrack.dto.TimesheetDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskService taskService;
    private final TimesheetService timesheetService;
    private final ProjectRiskRepository projectRiskRepository;

    public DashboardService(ProjectRepository projectRepository, TaskRepository taskRepository,
                            TimesheetRepository timesheetRepository, UserRepository userRepository,
                            ProjectMemberRepository projectMemberRepository, TaskService taskService,
                            TimesheetService timesheetService, ProjectRiskRepository projectRiskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timesheetRepository = timesheetRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskService = taskService;
        this.timesheetService = timesheetService;
        this.projectRiskRepository = projectRiskRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDto.ManagementDashboardResponse getManagementDashboard(User viewer) {
        DashboardDto.ManagementDashboardResponse resp = new DashboardDto.ManagementDashboardResponse();

        List<Project> allProjects = projectRepository.findAll();
        if (viewer.getRole() == Role.PROJECT_MANAGER || viewer.getRole() == Role.TEAM_LEAD) {
            allProjects = allProjects.stream().filter(p -> projectServiceCanAccess(p, viewer)).collect(Collectors.toList());
        }
        final List<Project> scopedProjects = allProjects;
        List<Task> allTasks = taskRepository.findAll().stream().filter(t -> scopedProjects.stream().anyMatch(p -> p.getId().equals(t.getProject().getId()))).collect(Collectors.toList());
        List<User> allUsers = userRepository.findByActiveTrue();
        List<Timesheet> allTimesheets = timesheetRepository.findAll().stream().filter(t -> scopedProjects.stream().anyMatch(p -> p.getId().equals(t.getProject().getId()))).collect(Collectors.toList());

        long totalProjects = allProjects.size();
        long activeProjects = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
        long completedProjects = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();

        LocalDate now = LocalDate.now();
        long delayedProjects = allProjects.stream()
                .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(now) && p.getStatus() != ProjectStatus.COMPLETED)
                .count();

        long projectsAtRisk = allProjects.stream()
                .filter(p -> p.getHealthScore() != null && p.getHealthScore() < 70)
                .count();

        resp.setTotalProjects(totalProjects);
        resp.setActiveProjects(activeProjects);
        resp.setCompletedProjects(completedProjects);
        resp.setDelayedProjects(delayedProjects);
        resp.setProjectsAtRisk(projectsAtRisk);

        // Employee allocation stats
        List<User> workforce = allUsers.stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE || u.getRole() == Role.TEAM_LEAD || u.getRole() == Role.PROJECT_MANAGER)
                .collect(Collectors.toList());
        long totalEmp = workforce.size();
        long utilizedEmp = workforce.stream().filter(u -> projectMemberRepository.findByUserId(u.getId()).stream()
                .anyMatch(pm -> scopedProjects.stream().anyMatch(p -> p.getId().equals(pm.getProject().getId())))).count();
        resp.setTotalEmployees(totalEmp);
        resp.setUtilizedEmployees(utilizedEmp);
        resp.setAvailableEmployees(Math.max(0, totalEmp - utilizedEmp));

        // Hours & utilization
        double totalPlanned = allProjects.stream().mapToDouble(p -> p.getEstimatedHours() != null ? p.getEstimatedHours() : 0.0).sum();
        double totalActual = allTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED).mapToDouble(Timesheet::getHoursWorked).sum();
        double totalBillable = allTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED && Boolean.TRUE.equals(t.getBillable())).mapToDouble(Timesheet::getHoursWorked).sum();
        double totalNonBillable = allTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED && Boolean.FALSE.equals(t.getBillable())).mapToDouble(Timesheet::getHoursWorked).sum();

        resp.setTotalPlannedHours(totalPlanned);
        resp.setTotalActualHours(totalActual);
        resp.setTotalBillableHours(totalBillable);
        resp.setTotalNonBillableHours(totalNonBillable);

        double utilPct = totalPlanned > 0 ? (totalActual / totalPlanned) * 100.0 : 0.0;
        resp.setOverallUtilizationPercentage(Math.min(100.0, Math.round(utilPct * 10.0) / 10.0));

        // Pending counts
        long pendingTs = allTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.DRAFT).count();
        long pendingApp = allTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.SUBMITTED || t.getStatus() == TimesheetStatus.RESUBMITTED).count();
        resp.setPendingTimesheetsCount(pendingTs);
        resp.setPendingApprovalsCount(pendingApp);

        // Portfolio Projects
        List<DashboardDto.PortfolioProjectItem> portfolio = allProjects.stream().map(p -> {
            DashboardDto.PortfolioProjectItem item = new DashboardDto.PortfolioProjectItem();
            item.setId(p.getId());
            item.setName(p.getName());
            item.setProjectCode(p.getProjectCode());
            item.setClientName(p.getClientName());

            long pTotalTasks = taskRepository.countTotalTasksByProjectId(p.getId());
            long pCompletedTasks = taskRepository.countCompletedTasksByProjectId(p.getId());
            int prog = pTotalTasks > 0 ? (int) Math.round(((double) pCompletedTasks / pTotalTasks) * 100) : 0;
            item.setProgressPercentage(prog);
            item.setTotalTasks(pTotalTasks);
            item.setOpenTasks(pTotalTasks - pCompletedTasks);

            Double logged = timesheetRepository.sumApprovedHoursByProjectId(p.getId());
            item.setLoggedHours(logged != null ? logged : 0.0);
            item.setEstimatedHours(p.getEstimatedHours() != null ? p.getEstimatedHours() : 0.0);
            List<Timesheet> projectSheets = timesheetRepository.findByProjectId(p.getId()).stream()
                    .filter(t -> t.getStatus() == TimesheetStatus.APPROVED).collect(Collectors.toList());
            double projectHours = projectSheets.stream().mapToDouble(Timesheet::getHoursWorked).sum();
            double projectBillable = projectSheets.stream().filter(t -> Boolean.TRUE.equals(t.getBillable()))
                    .mapToDouble(Timesheet::getHoursWorked).sum();
            item.setBillablePercentage(projectHours > 0 ? Math.round((projectBillable / projectHours) * 1000.0) / 10.0 : 0.0);
            item.setStatus(p.getStatus().name());
            item.setEndDate(p.getEndDate() != null ? p.getEndDate().toString() : "TBD");
            return item;
        }).collect(Collectors.toList());
        resp.setPortfolioProjects(portfolio);

        // Task Overview
        long todoCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TO_DO).count();
        long inProgCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long blockedCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();
        long completedCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        resp.setTaskOverview(new DashboardDto.TaskOverviewItem(allTasks.size(), todoCount, inProgCount, blockedCount, completedCount));

        // Team Workloads
        List<DashboardDto.TeamWorkloadItem> teamWorkloads = allUsers.stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE || u.getRole() == Role.TEAM_LEAD || u.getRole() == Role.PROJECT_MANAGER)
                .filter(u -> viewer.getRole() != Role.PROJECT_MANAGER && viewer.getRole() != Role.TEAM_LEAD
                        || u.getId().equals(viewer.getId())
                        || projectMemberRepository.findByUserId(u.getId()).stream()
                            .anyMatch(pm -> scopedProjects.stream().anyMatch(p -> p.getId().equals(pm.getProject().getId()))))
                .map(u -> {
                    DashboardDto.TeamWorkloadItem item = new DashboardDto.TeamWorkloadItem();
                    item.setUserId(u.getId());
                    item.setName(u.getFullName());
                    item.setRole(u.getDesignation() != null ? u.getDesignation() : u.getRole().name());
                    item.setAvatarUrl(u.getAvatarUrl());

                    List<ProjectMember> memberships = projectMemberRepository.findByUserId(u.getId());
                    item.setActiveProjectsCount(memberships.size());

                    List<Task> assigned = taskRepository.findTasksAssignedToUser(u.getId(), u);
                    item.setActiveTasksCount((int) assigned.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED).count());

                    Double userHours = timesheetRepository.sumApprovedHoursByUserId(u.getId());
                    item.setTotalHoursLogged(userHours != null ? userHours : 0.0);
                    double plannedForUser = memberships.stream()
                            .filter(pm -> scopedProjects.stream().anyMatch(p -> p.getId().equals(pm.getProject().getId())))
                            .mapToDouble(pm -> pm.getAllocationPercentage() != null ? pm.getAllocationPercentage() : 0).sum();
                    double userUtil = plannedForUser > 0 ? (userHours != null ? userHours : 0.0) / (plannedForUser * 40.0 / 100.0) * 100.0 : 0.0;
                    item.setUtilizationPercentage((int) Math.round(Math.min(100.0, userUtil)));
                    return item;
                }).collect(Collectors.toList());
        resp.setTeamWorkloads(teamWorkloads);

        // Project Health — derived from current transactional data.
        int budgetVsActual = 0;
        double budgetTotal = allProjects.stream().mapToDouble(p -> p.getBudgetAmount() != null ? p.getBudgetAmount() : 0.0).sum();
        double actualCost = allTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED)
                .mapToDouble(t -> t.getHoursWorked() * (t.getUser().getHourlyRate() != null ? t.getUser().getHourlyRate() : 0.0)).sum();
        if (budgetTotal > 0) {
            budgetVsActual = (int) Math.round(Math.max(0.0, Math.min(100.0, (1.0 - actualCost / budgetTotal) * 100.0)));
        }
        int schedulePerformance = totalProjects > 0 ? (int) Math.round(((totalProjects - delayedProjects) * 100.0) / totalProjects) : 0;
        long totalTaskCount = allTasks.size();
        long completedTaskCount = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        int qualityScore = totalTaskCount > 0 ? (int) Math.round((completedTaskCount * 100.0) / totalTaskCount) : 0;
        long openRisks = scopedProjects.stream().flatMap(p -> projectRiskRepository.findByProjectId(p.getId()).stream())
                .filter(r -> !"CLOSED".equalsIgnoreCase(r.getStatus())).count();
        int resourceUtilization = totalPlanned > 0 ? (int) Math.round(Math.min(100.0, (totalActual / totalPlanned) * 100.0)) : 0;
        resp.setProjectHealth(new DashboardDto.ProjectHealthSummary(
                budgetVsActual, schedulePerformance, qualityScore, (int) openRisks, resourceUtilization));

        return resp;
    }

    private boolean projectServiceCanAccess(Project project, User user) {
        if (project.getProjectManager() != null && project.getProjectManager().getId().equals(user.getId())) return true;
        return projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId()).isPresent();
    }

    @Transactional(readOnly = true)
    public DashboardDto.EmployeeDashboardResponse getEmployeeDashboard(User user) {
        DashboardDto.EmployeeDashboardResponse resp = new DashboardDto.EmployeeDashboardResponse();

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());
        resp.setMyProjectsCount(memberships.size());

        List<Task> assignedTasks = taskRepository.findTasksAssignedToUser(user.getId(), user);
        resp.setMyTasksCount(assignedTasks.size());

        LocalDate today = LocalDate.now();
        long todayCount = assignedTasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today)).count();
        long pendingCount = assignedTasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED).count();
        long completedCount = assignedTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

        resp.setTodayTasksCount(todayCount);
        resp.setPendingTasksCount(pendingCount);
        resp.setCompletedTasksCount(completedCount);

        // Timesheets
        List<Timesheet> userTimesheets = timesheetRepository.findByUserId(user.getId());
        double totalLogged = userTimesheets.stream().mapToDouble(Timesheet::getHoursWorked).sum();
        resp.setTotalHoursLogged(totalLogged);

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Timesheet> thisWeekEntries = timesheetRepository.findByUserIdAndWorkDateBetween(user.getId(), startOfWeek, endOfWeek);

        double weekHours = thisWeekEntries.stream().mapToDouble(Timesheet::getHoursWorked).sum();
        double weekBillable = thisWeekEntries.stream().filter(t -> Boolean.TRUE.equals(t.getBillable())).mapToDouble(Timesheet::getHoursWorked).sum();
        double weekNonBillable = thisWeekEntries.stream().filter(t -> Boolean.FALSE.equals(t.getBillable())).mapToDouble(Timesheet::getHoursWorked).sum();

        resp.setThisWeekHours(weekHours);
        resp.setThisWeekBillableHours(weekBillable);
        resp.setThisWeekNonBillableHours(weekNonBillable);

        resp.setPendingTimesheetsCount(userTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.DRAFT || t.getStatus() == TimesheetStatus.SUBMITTED).count());
        resp.setApprovedTimesheetsCount(userTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED).count());
        resp.setRejectedTimesheetsCount(userTimesheets.stream().filter(t -> t.getStatus() == TimesheetStatus.REJECTED).count());

        resp.setAssignedTasks(assignedTasks.stream().map(taskService::mapToTaskResponse).collect(Collectors.toList()));
        resp.setRecentTimesheets(userTimesheets.stream().limit(10).map(timesheetService::mapToTimesheetResponse).collect(Collectors.toList()));

        return resp;
    }
}

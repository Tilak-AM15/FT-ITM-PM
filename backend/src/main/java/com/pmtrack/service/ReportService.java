package com.pmtrack.service;

import com.pmtrack.dto.ReportDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ReportService(ProjectRepository projectRepository, TaskRepository taskRepository,
                         TimesheetRepository timesheetRepository, UserRepository userRepository,
                         ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timesheetRepository = timesheetRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<ReportDto.ProjectReportRow> getProjectReports(Long projectId, ProjectStatus status, User viewer) {
        List<Project> projects = projectRepository.findAll();
        if (viewer.getRole() == Role.PROJECT_MANAGER) {
            projects = projects.stream().filter(p -> p.getProjectManager() != null && p.getProjectManager().getId().equals(viewer.getId())).collect(Collectors.toList());
        } else if (viewer.getRole() == Role.FINANCE_HR) {
            // Finance/HR receives organization-level effort reports, but not project administration.
            projects = projects.stream().filter(p -> p.getStatus() != ProjectStatus.CANCELLED).collect(Collectors.toList());
        }

        if (projectId != null) {
            projects = projects.stream().filter(p -> p.getId().equals(projectId)).collect(Collectors.toList());
        }
        if (status != null) {
            projects = projects.stream().filter(p -> p.getStatus() == status).collect(Collectors.toList());
        }

        return projects.stream().map(p -> {
            ReportDto.ProjectReportRow row = new ReportDto.ProjectReportRow();
            row.setProjectId(p.getId());
            row.setProjectCode(p.getProjectCode());
            row.setProjectName(p.getName());
            row.setClientName(p.getClientName());
            row.setProjectManagerName(p.getProjectManager() != null ? p.getProjectManager().getFullName() : "Unassigned");
            row.setStatus(p.getStatus().name());
            row.setPriority(p.getPriority().name());
            row.setStartDate(p.getStartDate());
            row.setEndDate(p.getEndDate());
            row.setBudgetAmount(p.getBudgetAmount());
            row.setEstimatedHours(p.getEstimatedHours());

            Double actual = timesheetRepository.sumApprovedHoursByProjectId(p.getId());
            double actualHrs = actual != null ? actual : 0.0;
            row.setActualHours(actualHrs);
            row.setVarianceHours((p.getEstimatedHours() != null ? p.getEstimatedHours() : 0.0) - actualHrs);

            long totalTasks = taskRepository.countTotalTasksByProjectId(p.getId());
            long compTasks = taskRepository.countCompletedTasksByProjectId(p.getId());
            int compPct = totalTasks > 0 ? (int) Math.round(((double) compTasks / totalTasks) * 100) : 0;
            row.setCompletionPercentage(compPct);

            List<Task> tasks = taskRepository.findByProjectId(p.getId());
            long delayedCount = tasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.COMPLETED)
                    .count();
            row.setDelayedTasksCount(delayedCount);

            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto.ResourceReportRow> getResourceReports(Long userId, String department, User viewer) {
        List<User> users = userRepository.findByActiveTrue();
        if (viewer.getRole() == Role.PROJECT_MANAGER) {
            java.util.Set<Long> memberIds = projectMemberRepository.findByUserId(viewer.getId()).stream()
                    .map(pm -> pm.getUser().getId()).collect(Collectors.toSet());
            users = users.stream().filter(u -> memberIds.contains(u.getId()) || u.getId().equals(viewer.getId())).collect(Collectors.toList());
        }

        if (userId != null) {
            users = users.stream().filter(u -> u.getId().equals(userId)).collect(Collectors.toList());
        }
        if (department != null && !department.isBlank()) {
            users = users.stream().filter(u -> department.equalsIgnoreCase(u.getDepartment())).collect(Collectors.toList());
        }

        return users.stream().map(u -> {
            ReportDto.ResourceReportRow row = new ReportDto.ResourceReportRow();
            row.setUserId(u.getId());
            row.setEmployeeName(u.getFullName());
            row.setEmail(u.getEmail());
            row.setDesignation(u.getDesignation());
            row.setDepartment(u.getDepartment());

            List<Timesheet> userSheets = timesheetRepository.findByUserId(u.getId());
            double totalHours = userSheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED).mapToDouble(Timesheet::getHoursWorked).sum();
            double billableHours = userSheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED && Boolean.TRUE.equals(t.getBillable())).mapToDouble(Timesheet::getHoursWorked).sum();
            double nonBillableHours = userSheets.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED && Boolean.FALSE.equals(t.getBillable())).mapToDouble(Timesheet::getHoursWorked).sum();

            row.setTotalHours(totalHours);
            row.setBillableHours(billableHours);
            row.setNonBillableHours(nonBillableHours);

            double utilPct = totalHours > 0 ? (billableHours / totalHours) * 100.0 : 80.0;
            row.setUtilizationPercentage(Math.round(utilPct * 10.0) / 10.0);

            List<ProjectMember> memberships = projectMemberRepository.findByUserId(u.getId());
            row.setActiveProjectsCount(memberships.size());

            List<Task> assigned = taskRepository.findTasksAssignedToUser(u.getId(), u);
            row.setActiveTasksCount((int) assigned.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED).count());

            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDto.TimesheetReportRow> getTimesheetReports(Long projectId, Long userId, TimesheetStatus status, LocalDate start, LocalDate end, User viewer) {
        List<Timesheet> list = timesheetRepository.findAll();
        if (viewer.getRole() == Role.PROJECT_MANAGER) {
            list = list.stream().filter(t -> t.getProject().getProjectManager() != null
                    && t.getProject().getProjectManager().getId().equals(viewer.getId())).collect(Collectors.toList());
        } else if (viewer.getRole() == Role.FINANCE_HR) {
            list = list.stream().filter(t -> t.getStatus() == TimesheetStatus.APPROVED).collect(Collectors.toList());
        }

        if (projectId != null) {
            list = list.stream().filter(t -> t.getProject().getId().equals(projectId)).collect(Collectors.toList());
        }
        if (userId != null) {
            list = list.stream().filter(t -> t.getUser().getId().equals(userId)).collect(Collectors.toList());
        }
        if (status != null) {
            list = list.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        if (start != null) {
            list = list.stream().filter(t -> !t.getWorkDate().isBefore(start)).collect(Collectors.toList());
        }
        if (end != null) {
            list = list.stream().filter(t -> !t.getWorkDate().isAfter(end)).collect(Collectors.toList());
        }

        return list.stream().map(t -> {
            ReportDto.TimesheetReportRow row = new ReportDto.TimesheetReportRow();
            row.setTimesheetId(t.getId());
            row.setWorkDate(t.getWorkDate());
            row.setEmployeeName(t.getUser().getFullName());
            row.setProjectCode(t.getProject().getProjectCode());
            row.setProjectName(t.getProject().getName());
            row.setTaskTitle(t.getTask().getTitle());
            row.setHoursWorked(t.getHoursWorked());
            row.setDescription(t.getDescription());
            row.setBillable(t.getBillable());
            row.setStatus(t.getStatus().name());
            row.setReviewerName(t.getReviewer() != null ? t.getReviewer().getFullName() : "-");
            return row;
        }).collect(Collectors.toList());
    }

    public String generateTimesheetsCsv(List<ReportDto.TimesheetReportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("Timesheet ID,Date,Employee,Project Code,Project Name,Task,Hours,Billable,Status,Reviewer,Description\n");
        for (ReportDto.TimesheetReportRow r : rows) {
            sb.append(r.getTimesheetId()).append(",")
              .append(r.getWorkDate()).append(",")
              .append("\"").append(r.getEmployeeName()).append("\",")
              .append(r.getProjectCode()).append(",")
              .append("\"").append(r.getProjectName()).append("\",")
              .append("\"").append(r.getTaskTitle()).append("\",")
              .append(r.getHoursWorked()).append(",")
              .append(r.getBillable() ? "Yes" : "No").append(",")
              .append(r.getStatus()).append(",")
              .append("\"").append(r.getReviewerName()).append("\",")
              .append("\"").append(r.getDescription() != null ? r.getDescription().replace("\"", "\"\"") : "").append("\"\n");
        }
        return sb.toString();
    }
}

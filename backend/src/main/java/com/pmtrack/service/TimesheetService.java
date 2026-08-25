package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.dto.TimesheetDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.ProjectRepository;
import com.pmtrack.repository.TaskRepository;
import com.pmtrack.repository.TimesheetRepository;
import com.pmtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    public TimesheetService(TimesheetRepository timesheetRepository, ProjectRepository projectRepository,
                            TaskRepository taskRepository, UserRepository userRepository,
                            AuditService auditService, NotificationService notificationService, ProjectService projectService) {
        this.timesheetRepository = timesheetRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<TimesheetDto.TimesheetResponse> getUserTimesheets(User user, LocalDate startDate, LocalDate endDate, TimesheetStatus status) {
        List<Timesheet> timesheets;

        if (startDate != null && endDate != null) {
            timesheets = timesheetRepository.findByUserIdAndWorkDateBetween(user.getId(), startDate, endDate);
        } else {
            timesheets = timesheetRepository.findByUserId(user.getId());
        }

        if (status != null) {
            timesheets = timesheets.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }

        return timesheets.stream().map(this::mapToTimesheetResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TimesheetDto.TimesheetResponse getTimesheetById(Long id, User viewer) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found with id: " + id));
        assertCanView(timesheet, viewer);
        return mapToTimesheetResponse(timesheet);
    }

    @Transactional
    public TimesheetDto.TimesheetResponse saveOrSubmitTimesheet(TimesheetDto.TimesheetEntryRequest request, User user) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + request.getProjectId()));
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found: " + request.getTaskId()));

        if (!task.getProject().getId().equals(project.getId())) {
            throw new RuntimeException("Task does not belong to the selected project.");
        }
        projectService.assertCanAccessProject(project, user);
        boolean assigned = task.getTaskOwner() != null && task.getTaskOwner().getId().equals(user.getId())
                || task.getAssignees().stream().anyMatch(a -> a.getId().equals(user.getId()));
        if (!assigned && user.getRole() != Role.SUPER_ADMIN && user.getRole() != Role.ADMIN && user.getRole() != Role.PROJECT_MANAGER) {
            throw new RuntimeException("You can only log time against tasks assigned to you.");
        }
        if (request.getHoursWorked() == null || request.getHoursWorked() <= 0 || request.getHoursWorked() > 24) {
            throw new RuntimeException("Hours worked must be greater than 0 and no more than 24 hours per entry.");
        }

        Timesheet timesheet;
        String action = "TIMESHEET_CREATED";
        String previousValue = null;

        if (request.getId() != null) {
            timesheet = timesheetRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Timesheet not found: " + request.getId()));
            if (!timesheet.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("You can only edit your own timesheets.");
            }
            if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED && timesheet.getStatus() != TimesheetStatus.UNDER_REVIEW) {
                throw new RuntimeException("Only Draft or Rejected timesheets can be edited.");
            }
            previousValue = "Hours: " + timesheet.getHoursWorked() + ", Status: " + timesheet.getStatus();
            action = "TIMESHEET_UPDATED";
        } else {
            timesheet = new Timesheet();
            timesheet.setUser(user);
        }

        timesheet.setProject(project);
        timesheet.setTask(task);
        timesheet.setWorkDate(request.getWorkDate());
        timesheet.setHoursWorked(request.getHoursWorked());
        timesheet.setDescription(request.getDescription() != null ? request.getDescription() : "Work logged");
        timesheet.setStartTime(request.getStartTime());
        timesheet.setEndTime(request.getEndTime());
        timesheet.setBillable(request.getBillable() != null ? request.getBillable() : true);
        timesheet.setRemarks(request.getRemarks());

        TimesheetStatus requestedStatus = request.getStatus() != null ? request.getStatus() : TimesheetStatus.DRAFT;
        if (requestedStatus != TimesheetStatus.DRAFT && requestedStatus != TimesheetStatus.SUBMITTED
                && requestedStatus != TimesheetStatus.RESUBMITTED) {
            throw new RuntimeException("Employees may only save Draft or submit a timesheet for review.");
        }
        TimesheetStatus newStatus = requestedStatus;
        if (timesheet.getId() != null && (timesheet.getStatus() == TimesheetStatus.REJECTED || timesheet.getStatus() == TimesheetStatus.UNDER_REVIEW)
                && requestedStatus == TimesheetStatus.SUBMITTED) {
            newStatus = TimesheetStatus.RESUBMITTED;
        }
        timesheet.setStatus(newStatus);

        if (newStatus == TimesheetStatus.SUBMITTED || newStatus == TimesheetStatus.RESUBMITTED) {
            timesheet.setSubmittedAt(LocalDateTime.now());

            // Notify Project Manager
            if (project.getProjectManager() != null) {
                notificationService.createNotification(
                        project.getProjectManager(),
                        "Timesheet Submitted by " + user.getFullName(),
                        user.getFullName() + " submitted " + timesheet.getHoursWorked() + "h for " + task.getTitle() + " on " + timesheet.getWorkDate(),
                        NotificationType.TIMESHEET_SUBMITTED,
                        "/approvals"
                );
            }
        }

        Timesheet saved = timesheetRepository.save(timesheet);

        auditService.logAction(
                user,
                action,
                "Timesheet",
                saved.getId(),
                previousValue,
                "Hours: " + saved.getHoursWorked() + ", Status: " + saved.getStatus(),
                "Timesheet logged for task " + task.getTaskCode()
        );

        return mapToTimesheetResponse(saved);
    }

    @Transactional
    public List<TimesheetDto.TimesheetResponse> saveWeeklyTimesheet(TimesheetDto.WeeklySubmissionRequest request, User user) {
        LocalDate startOfWeek = request.getWeekStartDate();
        List<Timesheet> savedTimesheets = new ArrayList<>();

        for (TimesheetDto.WeeklyTimesheetRow row : request.getRows()) {
            if (row.getProjectId() == null || row.getTaskId() == null) continue;

            Project project = projectRepository.findById(row.getProjectId()).orElse(null);
            Task task = taskRepository.findById(row.getTaskId()).orElse(null);
            if (project == null || task == null) {
                throw new RuntimeException("Invalid project or task in weekly timesheet.");
            }
            if (!task.getProject().getId().equals(project.getId())) {
                throw new RuntimeException("Weekly timesheet task does not belong to the selected project.");
            }
            projectService.assertCanAccessProject(project, user);
            boolean assigned = task.getTaskOwner() != null && task.getTaskOwner().getId().equals(user.getId())
                    || task.getAssignees().stream().anyMatch(a -> a.getId().equals(user.getId()));
            if (!assigned && user.getRole() != Role.SUPER_ADMIN && user.getRole() != Role.ADMIN && user.getRole() != Role.PROJECT_MANAGER) {
                throw new RuntimeException("You can only log time against tasks assigned to you.");
            }

            Double[] dailyHours = new Double[]{
                    row.getMondayHours(),
                    row.getTuesdayHours(),
                    row.getWednesdayHours(),
                    row.getThursdayHours(),
                    row.getFridayHours(),
                    row.getSaturdayHours(),
                    row.getSundayHours()
            };

            for (int i = 0; i < 7; i++) {
                Double hrs = dailyHours[i];
                if (hrs != null && hrs > 0) {
                    if (hrs > 24) throw new RuntimeException("A daily timesheet entry cannot exceed 24 hours.");
                    LocalDate entryDate = startOfWeek.plusDays(i);

                    Timesheet ts = timesheetRepository.findByUserIdAndProjectIdAndTaskIdAndWorkDate(
                            user.getId(), project.getId(), task.getId(), entryDate).orElseGet(Timesheet::new);
                    if (ts.getId() != null && ts.getUser() != null && !ts.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Invalid timesheet owner.");
                    }
                    if (ts.getId() != null && ts.getStatus() != TimesheetStatus.DRAFT && ts.getStatus() != TimesheetStatus.REJECTED) {
                        continue;
                    }
                    ts.setUser(user);
                    ts.setProject(project);
                    ts.setTask(task);
                    ts.setWorkDate(entryDate);
                    ts.setHoursWorked(hrs);
                    ts.setDescription(row.getDescription() != null && !row.getDescription().isBlank() ? row.getDescription() : "Weekly effort log");
                    ts.setBillable(row.getBillable() != null ? row.getBillable() : true);
                    ts.setStatus(request.isSubmitForApproval() ? (ts.getId() != null && ts.getStatus() == TimesheetStatus.REJECTED ? TimesheetStatus.RESUBMITTED : TimesheetStatus.SUBMITTED) : TimesheetStatus.DRAFT);
                    if (request.isSubmitForApproval()) {
                        ts.setSubmittedAt(LocalDateTime.now());
                    }

                    Timesheet saved = timesheetRepository.save(ts);
                    auditService.logAction(
                            user,
                            "TIMESHEET_" + (request.isSubmitForApproval() ? "SUBMITTED" : "DRAFT_SAVED"),
                            "Timesheet",
                            saved.getId(),
                            null,
                            "Hours: " + saved.getHoursWorked() + ", Status: " + saved.getStatus(),
                            "Weekly timesheet entry processed"
                    );
                    savedTimesheets.add(saved);
                }
            }
        }

        if (request.isSubmitForApproval() && !savedTimesheets.isEmpty()) {
            auditService.logAction(
                    user,
                    "TIMESHEET_WEEKLY_SUBMITTED",
                    "Timesheet",
                    null,
                    null,
                    "Total entries: " + savedTimesheets.size(),
                    "Weekly timesheet batch submitted"
            );
        }

        return savedTimesheets.stream().map(this::mapToTimesheetResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTimesheet(Long id, User user) {
        Timesheet ts = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found: " + id));

        if (!ts.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own timesheets.");
        }
        if (ts.getStatus() != TimesheetStatus.DRAFT && ts.getStatus() != TimesheetStatus.REJECTED) {
            throw new RuntimeException("Only Draft or Rejected timesheets can be deleted.");
        }

        timesheetRepository.delete(ts);

        auditService.logAction(
                user,
                "TIMESHEET_DELETED",
                "Timesheet",
                id,
                "Hours: " + ts.getHoursWorked(),
                null,
                "Draft timesheet deleted"
        );
    }

    private void assertCanView(Timesheet timesheet, User viewer) {
        if (viewer == null) throw new RuntimeException("Authenticated user is required.");
        boolean privileged = viewer.getRole() == Role.SUPER_ADMIN || viewer.getRole() == Role.ADMIN
                || viewer.getRole() == Role.MANAGEMENT || viewer.getRole() == Role.FINANCE_HR;
        boolean owner = timesheet.getUser().getId().equals(viewer.getId());
        boolean projectManager = timesheet.getProject().getProjectManager() != null
                && timesheet.getProject().getProjectManager().getId().equals(viewer.getId());
        if (!privileged && !owner && !projectManager) {
            throw new RuntimeException("You are not authorized to view this timesheet.");
        }
    }

    public TimesheetDto.TimesheetResponse mapToTimesheetResponse(Timesheet ts) {
        TimesheetDto.TimesheetResponse dto = new TimesheetDto.TimesheetResponse();
        dto.setId(ts.getId());
        dto.setUser(new AuthDto.UserProfileDto(ts.getUser()));
        dto.setProjectId(ts.getProject().getId());
        dto.setProjectName(ts.getProject().getName());
        dto.setProjectCode(ts.getProject().getProjectCode());
        dto.setTaskId(ts.getTask().getId());
        dto.setTaskTitle(ts.getTask().getTitle());
        dto.setTaskCode(ts.getTask().getTaskCode());
        dto.setWorkDate(ts.getWorkDate());
        dto.setHoursWorked(ts.getHoursWorked());
        dto.setDescription(ts.getDescription());
        dto.setStartTime(ts.getStartTime());
        dto.setEndTime(ts.getEndTime());
        dto.setBillable(ts.getBillable());
        dto.setRemarks(ts.getRemarks());
        dto.setStatus(ts.getStatus());
        dto.setRejectionReason(ts.getRejectionReason());
        dto.setSubmittedAt(ts.getSubmittedAt());
        dto.setApprovedOrRejectedAt(ts.getApprovedOrRejectedAt());
        if (ts.getReviewer() != null) {
            dto.setReviewer(new AuthDto.UserProfileDto(ts.getReviewer()));
        }
        dto.setCreatedAt(ts.getCreatedAt());
        return dto;
    }
}

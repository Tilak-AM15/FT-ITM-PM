package com.pmtrack.config;

import com.pmtrack.model.*;
import com.pmtrack.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationScheduler(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            TimesheetRepository timesheetRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.timesheetRepository = timesheetRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Daily automation for due dates, overdue work, project delays and pending approvals.
     * The checks are idempotent for each recipient/type/day.
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "${app.scheduler.zone:Asia/Kolkata}")
    @Transactional
    public void generateDailyNotifications() {
        LocalDate today = LocalDate.now();
        for (Task task : taskRepository.findAll()) {
            if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
                continue;
            }

            if (task.getDueDate() != null && task.getDueDate().isEqual(today.plusDays(1))) {
                notifyAssignees(task, NotificationType.TASK_DUE_SOON,
                        "Task Due Tomorrow",
                        "Task " + task.getTaskCode() + " is due tomorrow.");
            }

            if (task.getDueDate() != null && task.getDueDate().isBefore(today)) {
                notifyAssignees(task, NotificationType.TASK_OVERDUE,
                        "Task Overdue",
                        "Task " + task.getTaskCode() + " is overdue.");
            }
        }

        for (Project project : projectRepository.findAll()) {
            if (project.getProjectManager() == null || project.getStatus() == ProjectStatus.COMPLETED
                    || project.getStatus() == ProjectStatus.CANCELLED) {
                continue;
            }

            if (project.getEndDate() != null && project.getEndDate().isBefore(today)) {
                notify(project.getProjectManager(), NotificationType.PROJECT_DELAY,
                        "Project Delayed",
                        project.getName() + " has passed its target end date.");
            } else if (project.getEndDate() != null && !project.getEndDate().isAfter(today.plusDays(7))) {
                notify(project.getProjectManager(), NotificationType.PROJECT_MILESTONE,
                        "Project Deadline Approaching",
                        project.getName() + " reaches its target end date within 7 days.");
            }
        }

        List<Timesheet> pending = timesheetRepository.findByStatus(TimesheetStatus.SUBMITTED);
        pending.addAll(timesheetRepository.findByStatus(TimesheetStatus.RESUBMITTED));
        for (User manager : userRepository.findByRole(Role.PROJECT_MANAGER)) {
            long count = pending.stream()
                    .filter(ts -> ts.getProject().getProjectManager() != null
                            && ts.getProject().getProjectManager().getId().equals(manager.getId()))
                    .count();
            if (count > 0) {
                notify(manager, NotificationType.PENDING_APPROVAL_REMINDER,
                        "Pending Timesheet Approvals",
                        count + " timesheet submission(s) are waiting for your review.");
            }
        }
    }

    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "${app.scheduler.zone:Asia/Kolkata}")
    @Transactional
    public void remindMissingTimesheets() {
        LocalDate today = LocalDate.now();
        for (User employee : userRepository.findByActiveTrue()) {
            if (employee.getRole() != Role.EMPLOYEE && employee.getRole() != Role.TEAM_LEAD) {
                continue;
            }
            boolean hasEntry = timesheetRepository.findByUserIdAndWorkDateBetween(
                    employee.getId(), today, today).stream().findAny().isPresent();
            boolean hasActiveTask = taskRepository.findTasksAssignedToUser(employee.getId(), employee).stream()
                    .anyMatch(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED);
            if (hasActiveTask && !hasEntry) {
                notify(employee, NotificationType.SYSTEM,
                        "Timesheet Reminder",
                        "You have active work but no timesheet entry for today.");
            }
        }
    }

    private void notifyAssignees(Task task, NotificationType type, String title, String message) {
        if (task.getTaskOwner() != null) {
            notify(task.getTaskOwner(), type, title, message);
        }
        for (User assignee : task.getAssignees()) {
            notify(assignee, type, title, message);
        }
    }

    private void notify(User recipient, NotificationType type, String title, String message) {
        LocalDate today = LocalDate.now();
        if (notificationRepository.existsByRecipientIdAndTypeAndCreatedAtBetween(
                recipient.getId(), type, today.atStartOfDay(), today.atTime(LocalTime.MAX))) {
            return;
        }
        notificationRepository.save(new Notification(recipient, title, message, type, "/notifications"));
    }
}

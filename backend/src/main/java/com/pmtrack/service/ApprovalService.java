package com.pmtrack.service;

import com.pmtrack.dto.TimesheetDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.TimesheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetService timesheetService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ApprovalService(TimesheetRepository timesheetRepository, TimesheetService timesheetService,
                           NotificationService notificationService, AuditService auditService) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetService = timesheetService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TimesheetDto.TimesheetResponse> getPendingApprovals(User manager) {
        List<Timesheet> pending;

        if (manager.getRole() == Role.SUPER_ADMIN || manager.getRole() == Role.ADMIN) {
            pending = timesheetRepository.findByStatus(TimesheetStatus.SUBMITTED);
            pending.addAll(timesheetRepository.findByStatus(TimesheetStatus.RESUBMITTED));
            pending.addAll(timesheetRepository.findByStatus(TimesheetStatus.UNDER_REVIEW));
        } else {
            pending = timesheetRepository.findPendingForManager(manager.getId());
        }

        return pending.stream().map(timesheetService::mapToTimesheetResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<TimesheetDto.TimesheetResponse> processApprovals(TimesheetDto.ApprovalActionRequest request, User manager) {
        List<Timesheet> updatedList = new ArrayList<>();
        String action = request.getAction(); // APPROVE, REJECT, REQUEST_CORRECTION
        String comment = request.getComment();

        if (request.getTimesheetIds() == null || request.getTimesheetIds().isEmpty()) {
            throw new RuntimeException("At least one timesheet must be selected.");
        }
        if (action == null || (!"APPROVE".equalsIgnoreCase(action) && !"REJECT".equalsIgnoreCase(action)
                && !"REQUEST_CORRECTION".equalsIgnoreCase(action))) {
            throw new RuntimeException("Invalid approval action.");
        }
        if (("REJECT".equalsIgnoreCase(action) || "REQUEST_CORRECTION".equalsIgnoreCase(action))
                && (comment == null || comment.isBlank())) {
            throw new RuntimeException("A reviewer comment is required for rejection or correction requests.");
        }

        for (Long tsId : request.getTimesheetIds()) {
            Timesheet ts = timesheetRepository.findById(tsId)
                    .orElseThrow(() -> new RuntimeException("Timesheet not found: " + tsId));
            boolean privileged = manager.getRole() == Role.SUPER_ADMIN || manager.getRole() == Role.ADMIN;
            boolean ownsProject = ts.getProject().getProjectManager() != null
                    && ts.getProject().getProjectManager().getId().equals(manager.getId());
            if (!privileged && !ownsProject) {
                throw new RuntimeException("You can only approve timesheets for projects you manage.");
            }
            if (ts.getStatus() != TimesheetStatus.SUBMITTED && ts.getStatus() != TimesheetStatus.RESUBMITTED
                    && ts.getStatus() != TimesheetStatus.UNDER_REVIEW) {
                throw new RuntimeException("Timesheet " + tsId + " is not awaiting approval.");
            }

            {
                TimesheetStatus prevStatus = ts.getStatus();
                ts.setReviewer(manager);
                ts.setApprovedOrRejectedAt(LocalDateTime.now());

                if ("APPROVE".equalsIgnoreCase(action)) {
                    ts.setStatus(TimesheetStatus.APPROVED);
                    ts.setRejectionReason(null);

                    notificationService.createNotification(
                            ts.getUser(),
                            "Timesheet Approved (" + ts.getHoursWorked() + "h)",
                            "Your timesheet entry for " + ts.getTask().getTitle() + " on " + ts.getWorkDate() + " was approved by " + manager.getFullName(),
                            NotificationType.TIMESHEET_APPROVED,
                            "/timesheets"
                    );

                    auditService.logAction(
                            manager,
                            "TIMESHEET_APPROVED",
                            "Timesheet",
                            ts.getId(),
                            prevStatus.name(),
                            "APPROVED",
                            "Approved " + ts.getHoursWorked() + "h on " + ts.getTask().getTaskCode()
                    );

                } else if ("REJECT".equalsIgnoreCase(action)) {
                    ts.setStatus(TimesheetStatus.REJECTED);
                    ts.setRejectionReason(comment != null ? comment : "Rejected by manager");

                    notificationService.createNotification(
                            ts.getUser(),
                            "Timesheet Rejected (" + ts.getHoursWorked() + "h)",
                            "Reason: " + (comment != null ? comment : "Please review and resubmit"),
                            NotificationType.TIMESHEET_REJECTED,
                            "/timesheets"
                    );

                    auditService.logAction(
                            manager,
                            "TIMESHEET_REJECTED",
                            "Timesheet",
                            ts.getId(),
                            prevStatus.name(),
                            "REJECTED",
                            "Reason: " + comment
                    );

                } else if ("REQUEST_CORRECTION".equalsIgnoreCase(action)) {
                    ts.setStatus(TimesheetStatus.UNDER_REVIEW);
                    ts.setRejectionReason(comment != null ? comment : "Correction requested");

                    notificationService.createNotification(
                            ts.getUser(),
                            "Timesheet Correction Requested",
                            "Manager " + manager.getFullName() + " requested correction: " + comment,
                            NotificationType.TIMESHEET_REJECTED,
                            "/timesheets"
                    );

                    auditService.logAction(
                            manager,
                            "TIMESHEET_CORRECTION_REQUESTED",
                            "Timesheet",
                            ts.getId(),
                            prevStatus.name(),
                            "UNDER_REVIEW",
                            "Note: " + comment
                    );
                }

                updatedList.add(timesheetRepository.save(ts));
            }
        }

        return updatedList.stream().map(timesheetService::mapToTimesheetResponse).collect(Collectors.toList());
    }
}

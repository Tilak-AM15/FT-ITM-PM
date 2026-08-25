package com.pmtrack.service;

import com.pmtrack.model.AuditLog;
import com.pmtrack.model.Role;
import com.pmtrack.model.Task;
import com.pmtrack.model.Timesheet;
import com.pmtrack.model.User;
import com.pmtrack.repository.AuditLogRepository;
import com.pmtrack.repository.ProjectRepository;
import com.pmtrack.repository.TaskRepository;
import com.pmtrack.repository.TimesheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimesheetRepository timesheetRepository;

    public AuditService(AuditLogRepository auditLogRepository, ProjectRepository projectRepository,
                        TaskRepository taskRepository, TimesheetRepository timesheetRepository) {
        this.auditLogRepository = auditLogRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timesheetRepository = timesheetRepository;
    }

    @Transactional
    public AuditLog logAction(User user, String action, String entityName, Long entityId, String previousValue, String newValue, String details) {
        String username = user != null ? user.getUsername() : "SYSTEM";
        AuditLog log = new AuditLog(user, username, action, entityName, entityId, previousValue, newValue, details);
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityName, User viewer) {
        List<AuditLog> logs = auditLogRepository.findByEntityNameOrderByTimestampDesc(entityName);
        return filterForViewer(logs, viewer);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntityAndId(String entityName, Long entityId, User viewer) {
        List<AuditLog> logs = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
        return filterForViewer(logs, viewer);
    }

    private List<AuditLog> filterForViewer(List<AuditLog> logs, User viewer) {
        if (viewer == null || viewer.getRole() != Role.PROJECT_MANAGER) {
            return logs;
        }

        return logs.stream().filter(log -> {
            if (log.getEntityId() == null) return false;
            try {
                if ("Project".equalsIgnoreCase(log.getEntityName())) {
                    return projectRepository.findById(log.getEntityId())
                            .map(p -> p.getProjectManager() != null && p.getProjectManager().getId().equals(viewer.getId()))
                            .orElse(false);
                }
                if ("Task".equalsIgnoreCase(log.getEntityName())) {
                    return taskRepository.findById(log.getEntityId())
                            .map(t -> t.getProject().getProjectManager() != null
                                    && t.getProject().getProjectManager().getId().equals(viewer.getId()))
                            .orElse(false);
                }
                if ("Timesheet".equalsIgnoreCase(log.getEntityName())) {
                    return timesheetRepository.findById(log.getEntityId())
                            .map(t -> t.getProject().getProjectManager() != null
                                    && t.getProject().getProjectManager().getId().equals(viewer.getId()))
                            .orElse(false);
                }
                return false;
            } catch (RuntimeException ex) {
                return false;
            }
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }
}

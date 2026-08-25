package com.pmtrack.controller;

import com.pmtrack.model.AuditLog;
import com.pmtrack.service.AuditService;
import com.pmtrack.service.UserService;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Immutable audit trail explorer for compliance and security")
public class AuditController {

    private final AuditService auditService;
    private final UserService userService;

    public AuditController(AuditService auditService, UserService userService) {
        this.auditService = auditService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGEMENT')")
    @Operation(summary = "Get complete system audit log trail")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.getAllAuditLogs());
    }

    @GetMapping("/entity/{entityName}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGEMENT', 'PROJECT_MANAGER')")
    @Operation(summary = "Get audit logs filtered by entity name (Project, Task, Timesheet, User)")
    public ResponseEntity<List<AuditLog>> getLogsByEntity(@PathVariable String entityName, @AuthenticationPrincipal UserPrincipal principal) {
        User viewer = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(auditService.getLogsByEntity(entityName, viewer));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGEMENT', 'PROJECT_MANAGER')")
    @Operation(summary = "Get audit logs for a specific entity ID")
    public ResponseEntity<List<AuditLog>> getLogsByEntityAndId(@PathVariable String entityName, @PathVariable Long entityId, @AuthenticationPrincipal UserPrincipal principal) {
        User viewer = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(auditService.getLogsByEntityAndId(entityName, entityId, viewer));
    }
}

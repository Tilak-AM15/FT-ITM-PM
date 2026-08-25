package com.pmtrack.controller;

import com.pmtrack.dto.TimesheetDto;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.ApprovalService;
import com.pmtrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@Tag(name = "Approvals", description = "Project Manager timesheet approval and rejection desk")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserService userService;

    public ApprovalController(ApprovalService approvalService, UserService userService) {
        this.approvalService = approvalService;
        this.userService = userService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Get pending timesheets awaiting manager review")
    public ResponseEntity<List<TimesheetDto.TimesheetResponse>> getPendingApprovals(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User manager = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(approvalService.getPendingApprovals(manager));
    }

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Process timesheets: Approve, Reject, or Request Correction")
    public ResponseEntity<List<TimesheetDto.TimesheetResponse>> processApprovals(
            @Valid @RequestBody TimesheetDto.ApprovalActionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User manager = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(approvalService.processApprovals(request, manager));
    }
}

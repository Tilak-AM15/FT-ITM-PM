package com.pmtrack.controller;

import com.pmtrack.dto.DashboardDto;
import com.pmtrack.model.User;
import com.pmtrack.service.DashboardService;
import com.pmtrack.service.UserService;
import com.pmtrack.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/dashboards")
@Tag(name = "Dashboards", description = "Role-based executive, project manager, and employee dashboard metrics")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/management")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGEMENT','FINANCE_HR')")
    @Operation(summary = "Get organization-wide executive metrics and portfolio health")
    public ResponseEntity<DashboardDto.ManagementDashboardResponse> getManagementDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(dashboardService.getManagementDashboard(currentUser));
    }

    @GetMapping("/project-manager")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD')")
    @Operation(summary = "Get project-manager/team-lead scoped dashboard")
    public ResponseEntity<DashboardDto.ManagementDashboardResponse> getProjectManagerDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(dashboardService.getManagementDashboard(currentUser));
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employee personal workspace stats, my tasks, and timesheet counts")
    public ResponseEntity<DashboardDto.EmployeeDashboardResponse> getEmployeeDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(dashboardService.getEmployeeDashboard(currentUser));
    }
}

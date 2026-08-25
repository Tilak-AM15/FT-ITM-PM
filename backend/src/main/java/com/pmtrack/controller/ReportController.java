package com.pmtrack.controller;

import com.pmtrack.dto.ReportDto;
import com.pmtrack.model.ProjectStatus;
import com.pmtrack.model.TimesheetStatus;
import com.pmtrack.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.UserService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Project, Resource, and Timesheet reports with CSV export")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/projects")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT','FINANCE_HR')")
    @Operation(summary = "Get project status and effort reports")
    public ResponseEntity<List<ReportDto.ProjectReportRow>> getProjectReports(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ProjectStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        User viewer = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(reportService.getProjectReports(projectId, status, viewer));
    }

    @GetMapping("/resources")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT','FINANCE_HR')")
    @Operation(summary = "Get employee utilization and workload allocation reports")
    public ResponseEntity<List<ReportDto.ResourceReportRow>> getResourceReports(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String department,
            @AuthenticationPrincipal UserPrincipal principal) {
        User viewer = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(reportService.getResourceReports(userId, department, viewer));
    }

    @GetMapping("/timesheets")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT','FINANCE_HR')")
    @Operation(summary = "Get granular timesheet records with multi-criteria filters")
    public ResponseEntity<List<ReportDto.TimesheetReportRow>> getTimesheetReports(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TimesheetStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        User viewer = userService.getUserEntityById(principal.getId());
        return ResponseEntity.ok(reportService.getTimesheetReports(projectId, userId, status, startDate, endDate, viewer));
    }

    @GetMapping(value = "/timesheets/csv", produces = "text/csv")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT','FINANCE_HR')")
    @Operation(summary = "Download timesheet report as CSV file")
    public ResponseEntity<String> exportTimesheetsCsv(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) TimesheetStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        User viewer = userService.getUserEntityById(principal.getId());
        List<ReportDto.TimesheetReportRow> rows = reportService.getTimesheetReports(projectId, userId, status, startDate, endDate, viewer);
        String csv = reportService.generateTimesheetsCsv(rows);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"timesheet-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}

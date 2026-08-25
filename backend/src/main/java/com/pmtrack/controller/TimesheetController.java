package com.pmtrack.controller;

import com.pmtrack.dto.TimesheetDto;
import com.pmtrack.model.TimesheetStatus;
import com.pmtrack.model.User;
import com.pmtrack.security.UserPrincipal;
import com.pmtrack.service.TimesheetService;
import com.pmtrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timesheets")
@Tag(name = "Timesheets", description = "Daily and Weekly Timesheet Logging and History")
public class TimesheetController {

    private final TimesheetService timesheetService;
    private final UserService userService;

    public TimesheetController(TimesheetService timesheetService, UserService userService) {
        this.timesheetService = timesheetService;
        this.userService = userService;
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user timesheets with optional date range and status filters")
    public ResponseEntity<List<TimesheetDto.TimesheetResponse>> getMyTimesheets(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TimesheetStatus status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(timesheetService.getUserTimesheets(currentUser, startDate, endDate, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get timesheet entry by ID")
    public ResponseEntity<TimesheetDto.TimesheetResponse> getTimesheetById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User viewer = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(timesheetService.getTimesheetById(id, viewer));
    }

    @PostMapping
    @Operation(summary = "Create or update daily timesheet entry (Draft or Submitted)")
    public ResponseEntity<TimesheetDto.TimesheetResponse> saveTimesheet(
            @Valid @RequestBody TimesheetDto.TimesheetEntryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(timesheetService.saveOrSubmitTimesheet(request, currentUser));
    }

    @PostMapping("/weekly")
    @Operation(summary = "Save or submit weekly timesheet grid (7-day matrix)")
    public ResponseEntity<List<TimesheetDto.TimesheetResponse>> saveWeeklyTimesheet(
            @Valid @RequestBody TimesheetDto.WeeklySubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        return ResponseEntity.ok(timesheetService.saveWeeklyTimesheet(request, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete draft or rejected timesheet")
    public ResponseEntity<?> deleteTimesheet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userService.getUserEntityById(userPrincipal.getId());
        timesheetService.deleteTimesheet(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Timesheet deleted successfully"));
    }
}

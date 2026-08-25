package com.pmtrack.dto;

import com.pmtrack.model.TimesheetStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TimesheetDto {

    public static class TimesheetEntryRequest {
        private Long id; // optional for edit

        @NotNull(message = "Project ID is required")
        private Long projectId;

        @NotNull(message = "Task ID is required")
        private Long taskId;

        @NotNull(message = "Work date is required")
        private LocalDate workDate;

        @NotNull(message = "Hours worked is required")
        @Positive(message = "Hours worked must be greater than zero")
        private Double hoursWorked;

        private String description;
        private String startTime;
        private String endTime;
        private Boolean billable = true;
        private String remarks;
        private TimesheetStatus status = TimesheetStatus.DRAFT;

        public TimesheetEntryRequest() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public LocalDate getWorkDate() { return workDate; }
        public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
        public Double getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Boolean getBillable() { return billable; }
        public void setBillable(Boolean billable) { this.billable = billable; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public TimesheetStatus getStatus() { return status; }
        public void setStatus(TimesheetStatus status) { this.status = status; }
    }

    public static class TimesheetResponse {
        private Long id;
        private AuthDto.UserProfileDto user;
        private Long projectId;
        private String projectName;
        private String projectCode;
        private Long taskId;
        private String taskTitle;
        private String taskCode;
        private LocalDate workDate;
        private Double hoursWorked;
        private String description;
        private String startTime;
        private String endTime;
        private Boolean billable;
        private String remarks;
        private TimesheetStatus status;
        private String rejectionReason;
        private LocalDateTime submittedAt;
        private LocalDateTime approvedOrRejectedAt;
        private AuthDto.UserProfileDto reviewer;
        private LocalDateTime createdAt;

        public TimesheetResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public AuthDto.UserProfileDto getUser() { return user; }
        public void setUser(AuthDto.UserProfileDto user) { this.user = user; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public String getTaskCode() { return taskCode; }
        public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
        public LocalDate getWorkDate() { return workDate; }
        public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
        public Double getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Boolean getBillable() { return billable; }
        public void setBillable(Boolean billable) { this.billable = billable; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public TimesheetStatus getStatus() { return status; }
        public void setStatus(TimesheetStatus status) { this.status = status; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
        public LocalDateTime getApprovedOrRejectedAt() { return approvedOrRejectedAt; }
        public void setApprovedOrRejectedAt(LocalDateTime approvedOrRejectedAt) { this.approvedOrRejectedAt = approvedOrRejectedAt; }
        public AuthDto.UserProfileDto getReviewer() { return reviewer; }
        public void setReviewer(AuthDto.UserProfileDto reviewer) { this.reviewer = reviewer; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class ApprovalActionRequest {
        private List<Long> timesheetIds;
        private String action; // "APPROVE", "REJECT", "REQUEST_CORRECTION"
        private String comment;

        public ApprovalActionRequest() {}

        public List<Long> getTimesheetIds() { return timesheetIds; }
        public void setTimesheetIds(List<Long> timesheetIds) { this.timesheetIds = timesheetIds; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class WeeklyTimesheetRow {
        private Long projectId;
        private String projectName;
        private Long taskId;
        private String taskTitle;
        private Boolean billable;
        private String description;
        // 7 days Monday to Sunday
        private Double mondayHours = 0.0;
        private Double tuesdayHours = 0.0;
        private Double wednesdayHours = 0.0;
        private Double thursdayHours = 0.0;
        private Double fridayHours = 0.0;
        private Double saturdayHours = 0.0;
        private Double sundayHours = 0.0;

        public WeeklyTimesheetRow() {}

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public Boolean getBillable() { return billable; }
        public void setBillable(Boolean billable) { this.billable = billable; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getMondayHours() { return mondayHours; }
        public void setMondayHours(Double mondayHours) { this.mondayHours = mondayHours; }
        public Double getTuesdayHours() { return tuesdayHours; }
        public void setTuesdayHours(Double tuesdayHours) { this.tuesdayHours = tuesdayHours; }
        public Double getWednesdayHours() { return wednesdayHours; }
        public void setWednesdayHours(Double wednesdayHours) { this.wednesdayHours = wednesdayHours; }
        public Double getThursdayHours() { return thursdayHours; }
        public void setThursdayHours(Double thursdayHours) { this.thursdayHours = thursdayHours; }
        public Double getFridayHours() { return fridayHours; }
        public void setFridayHours(Double fridayHours) { this.fridayHours = fridayHours; }
        public Double getSaturdayHours() { return saturdayHours; }
        public void setSaturdayHours(Double saturdayHours) { this.saturdayHours = saturdayHours; }
        public Double getSundayHours() { return sundayHours; }
        public void setSundayHours(Double sundayHours) { this.sundayHours = sundayHours; }
    }

    public static class WeeklySubmissionRequest {
        private LocalDate weekStartDate;
        private List<WeeklyTimesheetRow> rows;
        private boolean submitForApproval = false;

        public WeeklySubmissionRequest() {}

        public LocalDate getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }
        public List<WeeklyTimesheetRow> getRows() { return rows; }
        public void setRows(List<WeeklyTimesheetRow> rows) { this.rows = rows; }
        public boolean isSubmitForApproval() { return submitForApproval; }
        public void setSubmitForApproval(boolean submitForApproval) { this.submitForApproval = submitForApproval; }
    }
}

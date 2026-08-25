package com.pmtrack;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.dto.ProjectDto;
import com.pmtrack.dto.TaskDto;
import com.pmtrack.dto.TimesheetDto;
import com.pmtrack.model.*;
import com.pmtrack.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PmTrackApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private AuditService auditService;

    @Test
    @DisplayName("Application Context Loads and Seed Data Exists")
    void contextLoads() {
        List<AuthDto.UserProfileDto> users = userService.getAllUsers();
        assertNotNull(users);
        assertTrue(users.size() >= 5);
    }

    @Test
    @DisplayName("Test User Authentication Login")
    void testAuthLogin() {
        AuthDto.LoginRequest loginReq = new AuthDto.LoginRequest("prasanna", "password123");
        AuthDto.AuthResponse authResp = authService.login(loginReq);

        assertNotNull(authResp);
        assertNotNull(authResp.getToken());
        assertEquals("prasanna", authResp.getUser().getUsername());
        assertEquals(Role.PROJECT_MANAGER, authResp.getUser().getRole());
    }

    @Test
    @Transactional
    @DisplayName("End-to-End Workflow: Project -> Task -> Timesheet -> Approval -> Audit")
    void testEndToEndWorkflow() {
        User pm = userService.getUserEntityByUsername("prasanna");
        User developer = userService.getUserEntityByUsername("rahul");

        // 1. Create Project
        ProjectDto.ProjectRequest prjReq = new ProjectDto.ProjectRequest();
        prjReq.setProjectCode("TEST-PRJ-99");
        prjReq.setName("Automated Testing Platform");
        prjReq.setClientName("Future Labs");
        prjReq.setProjectManagerId(pm.getId());
        prjReq.setStartDate(LocalDate.now());
        prjReq.setEndDate(LocalDate.now().plusMonths(3));
        prjReq.setPriority(ProjectPriority.HIGH);
        prjReq.setStatus(ProjectStatus.ACTIVE);
        prjReq.setEstimatedHours(100.0);

        ProjectDto.ProjectResponse prjResp = projectService.createProject(prjReq, pm);
        assertNotNull(prjResp.getId());
        assertEquals("TEST-PRJ-99", prjResp.getProjectCode());

        // 2. Create Task
        TaskDto.TaskRequest taskReq = new TaskDto.TaskRequest();
        taskReq.setProjectId(prjResp.getId());
        taskReq.setTitle("Develop Automation Scripts");
        taskReq.setTaskOwnerId(pm.getId());
        taskReq.setAssigneeIds(List.of(developer.getId()));
        taskReq.setEstimatedHours(16.0);
        taskReq.setPriority(TaskPriority.HIGH);
        taskReq.setStatus(TaskStatus.TO_DO);

        TaskDto.TaskResponse taskResp = taskService.createTask(taskReq, pm);
        assertNotNull(taskResp.getId());
        assertEquals(TaskStatus.TO_DO, taskResp.getStatus());

        // 3. Employee updates task status to IN_PROGRESS and submits timesheet
        taskService.updateTaskStatus(taskResp.getId(), TaskStatus.IN_PROGRESS, 50, developer);

        TimesheetDto.TimesheetEntryRequest tsReq = new TimesheetDto.TimesheetEntryRequest();
        tsReq.setProjectId(prjResp.getId());
        tsReq.setTaskId(taskResp.getId());
        tsReq.setWorkDate(LocalDate.now());
        tsReq.setHoursWorked(8.0);
        tsReq.setDescription("Automated integration test execution");
        tsReq.setBillable(true);
        tsReq.setStatus(TimesheetStatus.SUBMITTED);

        TimesheetDto.TimesheetResponse tsResp = timesheetService.saveOrSubmitTimesheet(tsReq, developer);
        assertNotNull(tsResp.getId());
        assertEquals(TimesheetStatus.SUBMITTED, tsResp.getStatus());

        // 4. Project Manager reviews and approves timesheet
        TimesheetDto.ApprovalActionRequest approvalReq = new TimesheetDto.ApprovalActionRequest();
        approvalReq.setTimesheetIds(List.of(tsResp.getId()));
        approvalReq.setAction("APPROVE");
        approvalReq.setComment("Great job, approved!");

        List<TimesheetDto.TimesheetResponse> approvedList = approvalService.processApprovals(approvalReq, pm);
        assertNotNull(approvedList);
        assertEquals(1, approvedList.size());
        assertEquals(TimesheetStatus.APPROVED, approvedList.get(0).getStatus());

        // 5. Verify Audit Trail captured the transactions
        List<AuditLog> auditLogs = auditService.getLogsByEntity("Timesheet", pm);
        assertFalse(auditLogs.isEmpty());
        assertTrue(auditLogs.stream().anyMatch(l -> "TIMESHEET_APPROVED".equals(l.getAction())));
    }
}

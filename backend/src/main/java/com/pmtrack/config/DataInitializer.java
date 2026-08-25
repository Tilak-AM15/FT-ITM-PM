package com.pmtrack.config;

import com.pmtrack.model.*;
import com.pmtrack.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TimesheetRepository timesheetRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRiskRepository projectRiskRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    @Value("${app.bootstrap-admin.username:admin}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap-admin.email:admin@pmtrack.local}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap-admin.password:admin123}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap-admin.full-name:System Administrator}")
    private String bootstrapAdminFullName;

    public DataInitializer(UserRepository userRepository, ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository, TaskRepository taskRepository,
                           SubTaskRepository subTaskRepository, TaskCommentRepository taskCommentRepository,
                           TimesheetRepository timesheetRepository, MilestoneRepository milestoneRepository,
                           ProjectRiskRepository projectRiskRepository, NotificationRepository notificationRepository,
                           AuditLogRepository auditLogRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.timesheetRepository = timesheetRepository;
        this.milestoneRepository = milestoneRepository;
        this.projectRiskRepository = projectRiskRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // A production deployment can disable demo data while still receiving
        // one secure bootstrap administrator from environment variables.
        ensureBootstrapAdmin();

        if (!seedDemoData || userRepository.count() > 1) {
            return;
        }

        System.out.println(">>> Initializing PMTrack Enterprise Demo Seed Data...");

        // 1. Create Users
        User admin = userRepository.findByUsername(bootstrapAdminUsername).orElseGet(() ->
                createUser(bootstrapAdminUsername, bootstrapAdminEmail, bootstrapAdminPassword, bootstrapAdminFullName,
                        Role.SUPER_ADMIN, "IT Operations", "Principal Administrator", null));
        User prasanna = createUser("prasanna", "prasanna.lohar@futuretransformation.com", "password123", "Prasanna Lohar", Role.PROJECT_MANAGER, "Engineering Management", "Head of Engineering / PM", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150");
        User rahul = createUser("rahul", "rahul.sharma@pmtrack.io", "password123", "Rahul Sharma", Role.EMPLOYEE, "Engineering", "Senior Full Stack Developer", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150");
        User amit = createUser("amit", "amit.verma@pmtrack.io", "password123", "Amit Verma", Role.EMPLOYEE, "Engineering", "Backend Java Engineer", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150");
        User priya = createUser("priya", "priya.singh@pmtrack.io", "password123", "Priya Singh", Role.EMPLOYEE, "Product & UI/UX", "UI/UX Designer & React Dev", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150");
        User neha = createUser("neha", "neha.patel@pmtrack.io", "password123", "Neha Patel", Role.TEAM_LEAD, "QA & Release", "Lead QA Automation Engineer", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150");
        User executive = createUser("executive", "vikram.mehta@pmtrack.io", "password123", "Vikram Mehta", Role.MANAGEMENT, "Executive Leadership", "VP Operations / Executive", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150");
        User finance = createUser("finance", "ananya.roy@pmtrack.io", "password123", "Ananya Roy", Role.FINANCE_HR, "Finance & HR", "Finance Controller & Billing Lead", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150");

        // 2. Create Projects matching mockups
        Project p1 = createProject("PRJ-OL-01", "OpenLayer Digital Transformation", "OpenLayer Technologies",
                "Digital transformation initiative for OpenLayer platform including API integration, workflow automation, and dashboard development.",
                prasanna, LocalDate.now().minusWeeks(4), LocalDate.now().plusWeeks(12), ProjectPriority.HIGH, ProjectStatus.ACTIVE, 150000.0, 520.0);

        Project p2 = createProject("PRJ-FRC-02", "FRC 2026 Event & Research Conclave", "Future Research Council",
                "Flagship annual technology research conference portal, sponsor management, paper submission, and registration workflows.",
                prasanna, LocalDate.now().minusWeeks(6), LocalDate.now().plusWeeks(8), ProjectPriority.HIGH, ProjectStatus.ACTIVE, 90000.0, 380.0);

        Project p3 = createProject("PRJ-OFX-03", "OFX Platform API Marketplace", "OFX Global Financial",
                "Multi-tenant fintech API integration layer with zero-trust token authentication and sandbox testing portal.",
                prasanna, LocalDate.now().minusWeeks(2), LocalDate.now().plusWeeks(16), ProjectPriority.MEDIUM, ProjectStatus.ACTIVE, 120000.0, 420.0);

        Project p4 = createProject("PRJ-SEC-04", "SecureSetu Security Platform", "National Cyber Hub",
                "Next-generation enterprise threat modeling, zero-trust audit compliance dashboard, and IAM orchestration.",
                prasanna, LocalDate.now().minusWeeks(8), LocalDate.now().plusWeeks(10), ProjectPriority.CRITICAL, ProjectStatus.ACTIVE, 200000.0, 600.0);

        Project p5 = createProject("PRJ-OPS-05", "Internal Operations Tools", "Internal Ops",
                "Continuous automation tooling, pipeline orchestrators, and employee internal knowledge base.",
                prasanna, LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(6), ProjectPriority.LOW, ProjectStatus.ACTIVE, 40000.0, 160.0);

        // 3. Assign Members
        addProjectMember(p1, rahul, "Senior Developer", 100);
        addProjectMember(p1, amit, "Backend Developer", 100);
        addProjectMember(p1, priya, "Frontend Designer", 100);
        addProjectMember(p1, neha, "QA Engineer", 50);

        addProjectMember(p2, amit, "Backend Developer", 80);
        addProjectMember(p2, priya, "Frontend Developer", 80);

        addProjectMember(p3, rahul, "Lead Architect", 80);
        addProjectMember(p3, amit, "Backend Developer", 60);

        addProjectMember(p4, neha, "Security QA Lead", 100);
        addProjectMember(p4, rahul, "Integration Specialist", 50);

        addProjectMember(p5, rahul, "Developer", 20);

        // 4. Create Tasks
        Task t1 = createTask(p1, "PRJ-OL-01-T1", "API Gateway & OAuth2 Integration", "Build secure API gateway proxies and JWT validation middleware.", "Backend API", rahul, List.of(rahul), TaskPriority.HIGH, TaskStatus.IN_PROGRESS, LocalDate.now().minusDays(10), LocalDate.now().plusDays(4), 40.0);
        Task t2 = createTask(p1, "PRJ-OL-01-T2", "Dashboard Charts & Modern UI Layout", "Implement responsive glassmorphic widgets and real-time project metrics.", "Frontend UI", priya, List.of(priya), TaskPriority.HIGH, TaskStatus.IN_PROGRESS, LocalDate.now().minusDays(8), LocalDate.now().plusDays(2), 35.0);
        Task t3 = createTask(p1, "PRJ-OL-01-T3", "KYC Automated Verification Workflow", "Design asynchronous verification queue with document OCR integration.", "Workflow Engine", amit, List.of(amit), TaskPriority.MEDIUM, TaskStatus.TO_DO, LocalDate.now().minusDays(2), LocalDate.now().plusDays(14), 50.0);
        Task t4 = createTask(p1, "PRJ-OL-01-T4", "End-to-End Test Suite Automation", "Write automated Cypress and Playwright tests for onboarding journey.", "QA Automation", neha, List.of(neha), TaskPriority.MEDIUM, TaskStatus.TO_DO, LocalDate.now().plusDays(2), LocalDate.now().plusDays(20), 30.0);

        Task t5 = createTask(p2, "PRJ-FRC-02-T1", "Sponsor & Exhibitor Database Design", "PostgreSQL database schemas for sponsorship packages and booth allocations.", "Database Architecture", amit, List.of(amit), TaskPriority.HIGH, TaskStatus.IN_PROGRESS, LocalDate.now().minusDays(14), LocalDate.now().plusDays(3), 45.0);
        Task t6 = createTask(p2, "PRJ-FRC-02-T2", "Speaker Portal & Agenda Builder", "Interactive conference timetable builder with speaker bio modals.", "Frontend UI", priya, List.of(priya), TaskPriority.MEDIUM, TaskStatus.COMPLETED, LocalDate.now().minusDays(20), LocalDate.now().minusDays(2), 30.0);

        Task t7 = createTask(p3, "PRJ-OFX-03-T1", "OFX API Documentation & Swagger Hub", "Generate interactive Swagger 3.0 specs and developer sample code.", "API Marketplace", rahul, List.of(rahul), TaskPriority.MEDIUM, TaskStatus.COMPLETED, LocalDate.now().minusDays(12), LocalDate.now().minusDays(1), 25.0);

        Task t8 = createTask(p4, "PRJ-SEC-04-T1", "Zero-Trust Access Gateway & Firewall Rules", "Configure packet filtering and identity-aware proxies.", "Infrastructure", neha, List.of(neha), TaskPriority.CRITICAL, TaskStatus.BLOCKED, LocalDate.now().minusDays(15), LocalDate.now().plusDays(1), 60.0);

        // 5. Subtasks & Comments
        SubTask st1 = new SubTask(t1, "Generate RSA key pairs for token signing", rahul);
        st1.setCompleted(true);
        subTaskRepository.save(st1);

        SubTask st2 = new SubTask(t1, "Implement Spring Security 6 filter chain", rahul);
        st2.setCompleted(true);
        subTaskRepository.save(st2);

        SubTask st3 = new SubTask(t1, "Rate limiting and IP throttling rules", rahul);
        subTaskRepository.save(st3);

        taskCommentRepository.save(new TaskComment(t1, prasanna, "Make sure to enable Redis caching for token blacklist checking."));
        taskCommentRepository.save(new TaskComment(t1, rahul, "Added Redis configuration in application-prod.yml!"));

        // 6. Milestones & Risks
        milestoneRepository.save(new Milestone(p1, "Phase 1 - Architecture & Gateway", "Complete core gateway and token service", LocalDate.now().minusDays(5), "ACHIEVED", "Gateway repo, Swagger docs"));
        milestoneRepository.save(new Milestone(p1, "Phase 2 - UI Dashboard & Workflows", "Release operational dashboard to client", LocalDate.now().plusWeeks(3), "PENDING", "Dashboard release candidate"));

        projectRiskRepository.save(new ProjectRisk(p4, "Third-party HSM Certificate Delay", "Hardware Security Module delivery delayed by vendor", "HIGH", "OPEN", "Use software-based KMS emulator during phase 1 testing."));

        // 7. Timesheets (Approved, Submitted for Approval, Draft, Rejected)
        createTimesheet(rahul, p1, t1, LocalDate.now().minusDays(1), 8.0, "Completed OAuth2 JWT token provider and security filter integration.", true, TimesheetStatus.SUBMITTED, null, null, LocalDateTime.now().minusHours(4));
        createTimesheet(rahul, p1, t1, LocalDate.now().minusDays(2), 7.5, "Wrote unit tests for authentication endpoints and CORS filter.", true, TimesheetStatus.APPROVED, prasanna, null, LocalDateTime.now().minusDays(2));
        createTimesheet(rahul, p3, t7, LocalDate.now().minusDays(3), 6.0, "Authored OpenAPI 3.0 documentation and payload samples for OFX endpoint.", true, TimesheetStatus.APPROVED, prasanna, null, LocalDateTime.now().minusDays(3));

        createTimesheet(amit, p1, t3, LocalDate.now().minusDays(1), 6.5, "Designed async queue processor schema for KYC identity records.", true, TimesheetStatus.SUBMITTED, null, null, LocalDateTime.now().minusHours(3));
        createTimesheet(amit, p2, t5, LocalDate.now().minusDays(2), 8.0, "Database migration scripts and foreign key constraints for sponsor entities.", true, TimesheetStatus.APPROVED, prasanna, null, LocalDateTime.now().minusDays(2));

        createTimesheet(priya, p1, t2, LocalDate.now().minusDays(1), 7.0, "Engineered glassmorphic stat cards, SVG sparklines, and responsive sidebar navigation.", true, TimesheetStatus.SUBMITTED, null, null, LocalDateTime.now().minusHours(2));
        createTimesheet(priya, p2, t6, LocalDate.now().minusDays(3), 8.0, "Implemented interactive conference agenda builder and speaker modal.", true, TimesheetStatus.APPROVED, prasanna, null, LocalDateTime.now().minusDays(3));

        createTimesheet(neha, p4, t8, LocalDate.now().minusDays(1), 4.0, "Diagnosed network handshake bottleneck on staging firewall.", true, TimesheetStatus.REJECTED, prasanna, "Please provide more specific ticket references for firewall configurations.", LocalDateTime.now().minusDays(1));

        // 8. In-App Notifications
        notificationRepository.save(new Notification(prasanna, "Pending Timesheets Awaiting Review", "3 new timesheet submissions from Rahul, Amit, and Priya require your approval.", NotificationType.PENDING_APPROVAL_REMINDER, "/approvals"));
        notificationRepository.save(new Notification(rahul, "Timesheet Approved", "Your 7.5h timesheet entry for OpenLayer Platform was approved by Prasanna Lohar.", NotificationType.TIMESHEET_APPROVED, "/timesheets"));
        notificationRepository.save(new Notification(neha, "Timesheet Correction Requested", "Prasanna Lohar requested clarification on your 4.0h entry for SecureSetu Platform.", NotificationType.TIMESHEET_REJECTED, "/timesheets"));

        // 9. Audit Logs
        auditLogRepository.save(new AuditLog(admin, "admin", "SYSTEM_BOOTSTRAP", "System", 1L, null, "INITIALIZED", "System master data pre-seeded successfully."));
        auditLogRepository.save(new AuditLog(prasanna, "prasanna", "PROJECT_CREATED", "Project", p1.getId(), null, "OpenLayer Digital Transformation (PRJ-OL-01)", "Project initiated with $150k budget."));

        System.out.println(">>> PMTrack Enterprise Seed Data Successfully Initialized!");
    }

    private void ensureBootstrapAdmin() {
        if (userRepository.findByUsername(bootstrapAdminUsername).isPresent()) {
            return;
        }
        if (bootstrapAdminUsername == null || bootstrapAdminUsername.isBlank()
                || bootstrapAdminEmail == null || bootstrapAdminEmail.isBlank()
                || bootstrapAdminPassword == null || bootstrapAdminPassword.length() < 12) {
            throw new IllegalStateException(
                    "Bootstrap admin credentials are required when no administrator exists. "
                            + "Set APP_BOOTSTRAP_ADMIN_USERNAME, APP_BOOTSTRAP_ADMIN_EMAIL and "
                            + "APP_BOOTSTRAP_ADMIN_PASSWORD (minimum 12 characters)."
            );
        }

        User admin = new User(
                bootstrapAdminUsername,
                bootstrapAdminEmail,
                passwordEncoder.encode(bootstrapAdminPassword),
                bootstrapAdminFullName,
                Role.SUPER_ADMIN,
                "IT Operations",
                "Platform Administrator"
        );
        admin.setActive(true);
        userRepository.save(admin);
        System.out.println(">>> Bootstrap administrator created: " + bootstrapAdminUsername);
    }

    private User createUser(String username, String email, String password, String fullName, Role role, String department, String designation, String avatarUrl) {
        User user = new User(username, email, passwordEncoder.encode(password), fullName, role, department, designation);
        user.setAvatarUrl(avatarUrl != null ? avatarUrl : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150");
        return userRepository.save(user);
    }

    private Project createProject(String code, String name, String client, String desc, User pm, LocalDate start, LocalDate end, ProjectPriority priority, ProjectStatus status, Double budget, Double estHours) {
        Project p = new Project(code, name, client, desc, pm, start, end, priority, status, budget, estHours);
        p.setActualHours(0.0);
        return projectRepository.save(p);
    }

    private void addProjectMember(Project project, User user, String roleInProject, Integer allocation) {
        ProjectMember pm = new ProjectMember(project, user, roleInProject, allocation);
        projectMemberRepository.save(pm);
    }

    private Task createTask(Project project, String code, String title, String desc, String module, User owner, List<User> assignees, TaskPriority priority, TaskStatus status, LocalDate start, LocalDate due, Double estHours) {
        Task t = new Task(project, code, title, desc, module, owner, priority, status, start, due, estHours);
        t.setAssignees(new HashSet<>(assignees));
        return taskRepository.save(t);
    }

    private void createTimesheet(User user, Project project, Task task, LocalDate date, Double hours, String desc, boolean billable, TimesheetStatus status, User reviewer, String rejectionReason, LocalDateTime submittedAt) {
        Timesheet ts = new Timesheet(user, project, task, date, hours, desc, billable, status);
        ts.setReviewer(reviewer);
        ts.setRejectionReason(rejectionReason);
        ts.setSubmittedAt(submittedAt);
        if (status == TimesheetStatus.APPROVED || status == TimesheetStatus.REJECTED) {
            ts.setApprovedOrRejectedAt(LocalDateTime.now().minusDays(1));
        }
        timesheetRepository.save(ts);
    }
}

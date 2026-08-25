package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.dto.ProjectDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final TimesheetRepository timesheetRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRiskRepository projectRiskRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
                          TaskRepository taskRepository, TimesheetRepository timesheetRepository,
                          MilestoneRepository milestoneRepository, ProjectRiskRepository projectRiskRepository,
                          UserRepository userRepository, AuditService auditService, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.timesheetRepository = timesheetRepository;
        this.milestoneRepository = milestoneRepository;
        this.projectRiskRepository = projectRiskRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto.ProjectResponse> getAllProjects(User currentUser) {
        List<Project> projects;

        if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.ADMIN ||
            currentUser.getRole() == Role.MANAGEMENT || currentUser.getRole() == Role.FINANCE_HR) {
            projects = projectRepository.findAll();
        } else if (currentUser.getRole() == Role.PROJECT_MANAGER) {
            projects = projectRepository.findByProjectManager(currentUser);
        } else {
            projects = projectRepository.findProjectsForUser(currentUser.getId());
        }

        return projects.stream().map(this::mapToProjectResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDto.ProjectResponse getProjectById(Long id, User currentUser) {
        Project project = getProjectEntityById(id);
        assertCanAccessProject(project, currentUser);
        return mapToProjectResponse(project);
    }

    public boolean canAccessProject(Project project, User user) {
        if (user == null) return false;
        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN
                || user.getRole() == Role.MANAGEMENT || user.getRole() == Role.FINANCE_HR) {
            return true;
        }
        if (project.getProjectManager() != null && project.getProjectManager().getId().equals(user.getId())) {
            return true;
        }
        return projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId()).isPresent();
    }

    public void assertCanAccessProject(Project project, User user) {
        if (!canAccessProject(project, user)) {
            throw new RuntimeException("You are not authorized to access this project.");
        }
    }

    public void assertCanManageProject(Project project, User user) {
        if (user == null) {
            throw new RuntimeException("Authenticated user is required.");
        }
        boolean roleAllowed = user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN
                || user.getRole() == Role.PROJECT_MANAGER;
        if (!roleAllowed || (user.getRole() == Role.PROJECT_MANAGER
                && (project.getProjectManager() == null || !project.getProjectManager().getId().equals(user.getId())))) {
            throw new RuntimeException("You are not authorized to manage this project.");
        }
    }

    @Transactional(readOnly = true)
    public void assertCanManageTasks(Project project, User user) {
        if (user == null) {
            throw new RuntimeException("Authenticated user is required.");
        }
        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.PROJECT_MANAGER) {
            if (project.getProjectManager() != null && project.getProjectManager().getId().equals(user.getId())) {
                return;
            }
            throw new RuntimeException("You are not authorized to manage tasks for this project.");
        }
        if (user.getRole() == Role.TEAM_LEAD
                && projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId()).isPresent()) {
            return;
        }
        throw new RuntimeException("You are not authorized to manage tasks for this project.");
    }

    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    @Transactional
    public ProjectDto.ProjectResponse createProject(ProjectDto.ProjectRequest request, User creator) {
        if (projectRepository.findByProjectCode(request.getProjectCode()).isPresent()) {
            throw new RuntimeException("Project code already exists: " + request.getProjectCode());
        }
        if (request.getStartDate() != null && request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Project end date cannot be before start date.");
        }
        if (request.getBudgetAmount() != null && request.getBudgetAmount() < 0) {
            throw new RuntimeException("Project budget cannot be negative.");
        }
        if (request.getEstimatedHours() != null && request.getEstimatedHours() < 0) {
            throw new RuntimeException("Estimated hours cannot be negative.");
        }

        Project project = new Project();
        project.setProjectCode(request.getProjectCode());
        project.setName(request.getName());
        project.setClientName(request.getClientName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setPriority(request.getPriority() != null ? request.getPriority() : ProjectPriority.MEDIUM);
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE);
        project.setBudgetAmount(request.getBudgetAmount() != null ? request.getBudgetAmount() : 0.0);
        project.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : 0.0);
        project.setDocumentUrls(validateUrls(request.getDocumentUrls(), "document"));
        project.setActualHours(0.0);
        project.setHealthScore(95);

        if (request.getProjectManagerId() != null) {
            User pm = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new RuntimeException("Project Manager not found with id: " + request.getProjectManagerId()));
            if (pm.getRole() != Role.PROJECT_MANAGER && pm.getRole() != Role.ADMIN && pm.getRole() != Role.SUPER_ADMIN) {
                throw new RuntimeException("Selected project manager must have a Project Manager, Admin, or Super Admin role.");
            }
            project.setProjectManager(pm);
        }

        Project savedProject = projectRepository.save(project);

        if (savedProject.getProjectManager() != null) {
            notificationService.createNotification(
                    savedProject.getProjectManager(),
                    "New Project Assigned",
                    "You have been assigned as Project Manager for " + savedProject.getName() + " (" + savedProject.getProjectCode() + ")",
                    NotificationType.SYSTEM,
                    "/projects/" + savedProject.getId()
            );
        }

        // Assign initial members if provided
        if (request.getMemberUserIds() != null && !request.getMemberUserIds().isEmpty()) {
            for (Long memberId : request.getMemberUserIds()) {
                userRepository.findById(memberId).ifPresent(user -> {
                    ProjectMember member = new ProjectMember(savedProject, user, "Developer", 100);
                    projectMemberRepository.save(member);
                });
            }
        }

        auditService.logAction(
                creator,
                "PROJECT_CREATED",
                "Project",
                savedProject.getId(),
                null,
                savedProject.getName() + " (" + savedProject.getProjectCode() + ")",
                "Project created successfully"
        );

        return mapToProjectResponse(savedProject);
    }

    @Transactional
    public ProjectDto.ProjectResponse updateProject(Long id, ProjectDto.ProjectRequest request, User modifier) {
        Project project = getProjectEntityById(id);
        assertCanManageProject(project, modifier);
        if (request.getStartDate() != null && request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Project end date cannot be before start date.");
        }
        if (request.getBudgetAmount() != null && request.getBudgetAmount() < 0) {
            throw new RuntimeException("Project budget cannot be negative.");
        }
        if (request.getEstimatedHours() != null && request.getEstimatedHours() < 0) {
            throw new RuntimeException("Estimated hours cannot be negative.");
        }
        String oldValues = "Status: " + project.getStatus() + ", EndDate: " + project.getEndDate();

        project.setName(request.getName());
        project.setClientName(request.getClientName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getBudgetAmount() != null) project.setBudgetAmount(request.getBudgetAmount());
        if (request.getEstimatedHours() != null) project.setEstimatedHours(request.getEstimatedHours());
        if (request.getDocumentUrls() != null) project.setDocumentUrls(validateUrls(request.getDocumentUrls(), "document"));

        if (request.getProjectManagerId() != null) {
            User pm = userRepository.findById(request.getProjectManagerId())
                    .orElseThrow(() -> new RuntimeException("Project Manager not found"));
            if (pm.getRole() != Role.PROJECT_MANAGER && pm.getRole() != Role.ADMIN && pm.getRole() != Role.SUPER_ADMIN) {
                throw new RuntimeException("Selected project manager must have a Project Manager, Admin, or Super Admin role.");
            }
            project.setProjectManager(pm);
        }

        Project saved = projectRepository.save(project);

        String newValues = "Status: " + saved.getStatus() + ", EndDate: " + saved.getEndDate();
        auditService.logAction(modifier, "PROJECT_UPDATED", "Project", saved.getId(), oldValues, newValues, "Project updated");

        return mapToProjectResponse(saved);
    }

    @Transactional
    public void addMember(Long projectId, Long userId, String roleInProject, Integer allocation, User actor) {
        Project project = getProjectEntityById(projectId);
        assertCanManageProject(project, actor);
        if (allocation == null || allocation < 0 || allocation > 100) {
            throw new RuntimeException("Allocation percentage must be between 0 and 100.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
            ProjectMember member = new ProjectMember(project, user, roleInProject != null ? roleInProject : "Developer", allocation != null ? allocation : 100);
            projectMemberRepository.save(member);
            auditService.logAction(actor, "PROJECT_MEMBER_ASSIGNED", "Project", projectId, null,
                    user.getUsername() + " (" + allocation + "%)", "Project team assignment changed");

            notificationService.createNotification(
                    user,
                    "Added to Project Team",
                    "You have been assigned to project " + project.getName(),
                    NotificationType.TASK_ASSIGNED,
                    "/projects/" + projectId
            );
        }
    }

    @Transactional
    public void removeMember(Long projectId, Long userId, User actor) {
        Project project = getProjectEntityById(projectId);
        assertCanManageProject(project, actor);
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        auditService.logAction(actor, "PROJECT_MEMBER_REMOVED", "Project", projectId,
                String.valueOf(userId), null, "Project team member removed");
    }

    @Transactional
    public Milestone addMilestone(Long projectId, ProjectDto.MilestoneDto dto, User actor) {
        Project project = getProjectEntityById(projectId);
        assertCanManageProject(project, actor);
        if (dto.getTitle() == null || dto.getTitle().isBlank()) throw new RuntimeException("Milestone title is required.");
        Milestone milestone = new Milestone(project, dto.getTitle(), dto.getDescription(), dto.getTargetDate(), dto.getStatus(), dto.getDeliverables());
        Milestone saved = milestoneRepository.save(milestone);
        auditService.logAction(actor, "MILESTONE_CREATED", "Project", projectId, null, saved.getTitle(), "Project milestone created");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Milestone> getMilestones(Long projectId, User actor) {
        Project project = getProjectEntityById(projectId);
        assertCanAccessProject(project, actor);
        return milestoneRepository.findByProjectId(projectId);
    }

    @Transactional
    public ProjectRisk addRisk(Long projectId, ProjectDto.RiskDto dto, User actor) {
        Project project = getProjectEntityById(projectId);
        assertCanManageProject(project, actor);
        if (dto.getTitle() == null || dto.getTitle().isBlank()) throw new RuntimeException("Risk title is required.");
        ProjectRisk risk = new ProjectRisk(project, dto.getTitle(), dto.getDescription(), dto.getSeverity(), dto.getStatus(), dto.getMitigationPlan());
        ProjectRisk saved = projectRiskRepository.save(risk);
        auditService.logAction(actor, "PROJECT_RISK_CREATED", "Project", projectId, null, saved.getTitle(), "Project risk/issue created");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ProjectRisk> getRisks(Long projectId, User actor) {
        Project project = getProjectEntityById(projectId);
        assertCanAccessProject(project, actor);
        return projectRiskRepository.findByProjectId(projectId);
    }

    private List<String> validateUrls(List<String> urls, String label) {
        if (urls == null) return new ArrayList<>();
        if (urls.size() > 20) throw new RuntimeException("A project cannot contain more than 20 " + label + " links.");
        return urls.stream().map(String::trim).filter(u -> !u.isBlank()).peek(u -> {
            if (!(u.startsWith("https://") || u.startsWith("http://"))) {
                throw new RuntimeException("Each " + label + " link must start with http:// or https://.");
            }
            if (u.length() > 500) throw new RuntimeException("A " + label + " link is too long.");
        }).distinct().collect(Collectors.toList());
    }

    public ProjectDto.ProjectResponse mapToProjectResponse(Project project) {
        ProjectDto.ProjectResponse dto = new ProjectDto.ProjectResponse();
        dto.setId(project.getId());
        dto.setProjectCode(project.getProjectCode());
        dto.setName(project.getName());
        dto.setClientName(project.getClientName());
        dto.setDescription(project.getDescription());
        if (project.getProjectManager() != null) {
            dto.setProjectManager(new AuthDto.UserProfileDto(project.getProjectManager()));
        }
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setPriority(project.getPriority());
        dto.setStatus(project.getStatus());
        dto.setBudgetAmount(project.getBudgetAmount());
        dto.setEstimatedHours(project.getEstimatedHours());

        // Actual hours from approved timesheets
        Double actualHrs = timesheetRepository.sumApprovedHoursByProjectId(project.getId());
        dto.setActualHours(actualHrs != null ? actualHrs : 0.0);

        // Task stats
        long totalTasks = taskRepository.countTotalTasksByProjectId(project.getId());
        long completedTasks = taskRepository.countCompletedTasksByProjectId(project.getId());
        dto.setTotalTasks(totalTasks);
        dto.setCompletedTasks(completedTasks);
        dto.setActiveTasks(totalTasks - completedTasks);

        int compPercent = totalTasks > 0 ? (int) Math.round(((double) completedTasks / totalTasks) * 100) : 0;
        dto.setCompletionPercentage(compPercent);

        // Dynamic health score based on delay and budget
        int health = 95;
        if (project.getEndDate() != null && project.getEndDate().isBefore(LocalDate.now()) && project.getStatus() != ProjectStatus.COMPLETED) {
            health -= 30;
        }
        if (project.getEstimatedHours() > 0 && actualHrs > project.getEstimatedHours()) {
            health -= 25;
        }
        dto.setHealthScore(Math.max(10, health));

        // Members
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        dto.setTeamMemberCount(members.size());
        dto.setMembers(members.stream().map(m -> new ProjectDto.MemberInfo(
                m.getId(),
                new AuthDto.UserProfileDto(m.getUser()),
                m.getRoleInProject(),
                m.getAllocationPercentage(),
                m.getAssignedDate()
        )).collect(Collectors.toList()));

        dto.setCreatedDate(project.getCreatedDate());
        dto.setUpdatedDate(project.getUpdatedDate());
        Hibernate.initialize(project.getDocumentUrls());
        dto.setDocumentUrls(project.getDocumentUrls() == null ? new ArrayList<>() : new ArrayList<>(project.getDocumentUrls()));
        return dto;
    }
}

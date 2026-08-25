package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.dto.TaskDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.ProjectRepository;
import com.pmtrack.repository.TaskRepository;
import com.pmtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectService projectService,
            AuditService auditService,
            NotificationService notificationService) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    /**
     * Get tasks visible to the current user.
     *
     * Admin/Management users:
     *     See all tasks.
     *
     * Project Managers:
     *     See tasks belonging to projects they manage.
     *
     * Employees / Team Leads:
     *     See tasks they own or are assigned to.
     */
    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksForUser(User user) {

        if (user == null) {
            return new ArrayList<>();
        }

        if (user.getRole() == Role.SUPER_ADMIN
                || user.getRole() == Role.ADMIN
                || user.getRole() == Role.MANAGEMENT
                || user.getRole() == Role.FINANCE_HR) {

            return taskRepository.findAll()
                    .stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }

        if (user.getRole() == Role.PROJECT_MANAGER) {

            return projectRepository.findByProjectManager(user)
                    .stream()
                    .flatMap(project ->
                            taskRepository.findByProject(project).stream()
                    )
                    .distinct()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }

        /*
         * Employees and Team Leads.
         *
         * This query checks:
         * 1. taskOwner
         * 2. assignees
         *
         * Therefore a task created by a Project Manager and assigned
         * to Rahul will appear in Rahul's My Tasks.
         */
        return taskRepository
                .findTasksAssignedToUser(user.getId(), user)
                .stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all tasks for a project.
     */
    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksByProjectId(
            Long projectId,
            User user) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        if (!projectService.canAccessProject(project, user)) {
            throw new RuntimeException(
                    "You do not have access to this project");
        }

        return taskRepository.findByProject(project)
                .stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get one task.
     */
    @Transactional(readOnly = true)
    public TaskDto.TaskResponse getTaskById(
            Long taskId,
            User user) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        if (!canAccessTask(task, user)) {
            throw new RuntimeException(
                    "You do not have access to this task");
        }

        return mapToTaskResponse(task);
    }

    /**
     * Create a task.
     */
    @Transactional
    public TaskDto.TaskResponse createTask(
            TaskDto.TaskRequest request,
            User creator) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() ->
                        new RuntimeException("Project not found"));

        if (!projectService.canAccessProject(project, creator)) {
            throw new RuntimeException(
                    "You do not have access to this project");
        }

        Task task = new Task();

        task.setProject(project);

        /*
         * Generate task code if the frontend does not provide one.
         */
        if (request.getTaskCode() != null
                && !request.getTaskCode().trim().isEmpty()) {

            task.setTaskCode(request.getTaskCode());

        } else {
            task.setTaskCode(generateTaskCode(project));
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setModuleName(request.getModuleName());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        } else {
            task.setPriority(TaskPriority.MEDIUM);
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        } else {
            task.setStatus(TaskStatus.TO_DO);
        }

        task.setEstimatedHours(
                request.getEstimatedHours() != null
                        ? request.getEstimatedHours()
                        : 0.0
        );

        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());

        task.setProgressPercentage(0);

        /*
         * Task owner.
         *
         * If no owner is supplied, creator becomes owner.
         */
        User taskOwner = creator;

        if (request.getTaskOwnerId() != null) {

            taskOwner = userRepository.findById(
                    request.getTaskOwnerId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Task owner not found: "
                                    + request.getTaskOwnerId()
                    )
            );

            if (!projectService.canAccessProject(
                    project,
                    taskOwner)) {

                throw new RuntimeException(
                        "Task owner "
                                + taskOwner.getUsername()
                                + " is not assigned to the project"
                );
            }
        }

        task.setTaskOwner(taskOwner);

        /*
         * Assign employees.
         */
        Set<User> assignees = new HashSet<>();

        if (request.getAssigneeIds() != null
                && !request.getAssigneeIds().isEmpty()) {

            for (Long userId : request.getAssigneeIds()) {

                if (userId == null) {
                    continue;
                }

                User assignee = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignee not found: "
                                                + userId
                                )
                        );

                /*
                 * Only project members / users with project access
                 * can be assigned to the task.
                 */
                if (!projectService.canAccessProject(
                        project,
                        assignee)) {

                    throw new RuntimeException(
                            "Assignee "
                                    + assignee.getUsername()
                                    + " is not assigned to the project"
                    );
                }

                assignees.add(assignee);
            }
        }

        task.setAssignees(assignees);

        /*
         * Attachments.
         */
        if (request.getAttachmentUrls() != null) {
            task.setAttachmentUrls(
                    new ArrayList<>(request.getAttachmentUrls())
            );
        }

        Task savedTask = taskRepository.save(task);

        /*
         * Audit.
         */
        auditService.logAction(
                creator,
                "TASK_CREATED",
                "Task",
                savedTask.getId(),
                null,
                savedTask.getTitle(),
                "Task created: " + savedTask.getTitle()
        );

        /*
         * Notify assigned users.
         */
        for (User assignee : assignees) {

            if (assignee.getId() != null
                    && creator.getId() != null
                    && assignee.getId().equals(creator.getId())) {
                continue;
            }

            notificationService.createNotification(
                    assignee,
                    "New Task Assigned",
                    "You have been assigned task: "
                            + savedTask.getTitle(),
                    NotificationType.TASK_ASSIGNED,
                    "/tasks/" + savedTask.getId()
            );
        }

        return mapToTaskResponse(savedTask);
    }

    /**
     * Update task details and assignees.
     */
    @Transactional
    public TaskDto.TaskResponse updateTask(
            Long taskId,
            TaskDto.TaskRequest request,
            User modifier) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        if (!canModifyTask(task, modifier)) {
            throw new RuntimeException(
                    "You do not have permission to update this task");
        }

        String previousTitle = task.getTitle();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getModuleName() != null) {
            task.setModuleName(request.getModuleName());
        }

        if (request.getTaskCode() != null
                && !request.getTaskCode().trim().isEmpty()) {
            task.setTaskCode(request.getTaskCode());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }

        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getAttachmentUrls() != null) {
            task.setAttachmentUrls(
                    new ArrayList<>(request.getAttachmentUrls())
            );
        }

        /*
         * Update task owner.
         */
        if (request.getTaskOwnerId() != null) {

            User owner = userRepository.findById(
                    request.getTaskOwnerId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Task owner not found")
            );

            if (!projectService.canAccessProject(
                    task.getProject(),
                    owner)) {

                throw new RuntimeException(
                        "Task owner is not assigned to the project");
            }

            task.setTaskOwner(owner);
        }

        /*
         * Update assignees.
         */
        if (request.getAssigneeIds() != null) {

            Set<User> newAssignees = new HashSet<>();

            for (Long userId : request.getAssigneeIds()) {

                if (userId == null) {
                    continue;
                }

                User assignee = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignee not found: "
                                                + userId
                                )
                        );

                if (!projectService.canAccessProject(
                        task.getProject(),
                        assignee)) {

                    throw new RuntimeException(
                            "Assignee "
                                    + assignee.getUsername()
                                    + " is not assigned to the project"
                    );
                }

                newAssignees.add(assignee);
            }

            task.setAssignees(newAssignees);
        }

        Task savedTask = taskRepository.save(task);

        auditService.logAction(
                modifier,
                "TASK_UPDATED",
                "Task",
                savedTask.getId(),
                previousTitle,
                savedTask.getTitle(),
                "Task updated"
        );

        return mapToTaskResponse(savedTask);
    }

    /**
     * Update task status and progress.
     */
    @Transactional
    public TaskDto.TaskResponse updateTaskStatus(
            Long taskId,
            TaskStatus status,
            Integer progressPercentage,
            User modifier) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        if (!canAccessTask(task, modifier)) {
            throw new RuntimeException(
                    "You do not have access to this task");
        }

        TaskStatus previousStatus = task.getStatus();

        if (status != null) {
            task.setStatus(status);
        }

        if (progressPercentage != null) {

            int progress = Math.max(
                    0,
                    Math.min(100, progressPercentage)
            );

            task.setProgressPercentage(progress);
        }

        if (status == TaskStatus.COMPLETED) {
            task.setProgressPercentage(100);
        }

        Task savedTask = taskRepository.save(task);

        auditService.logAction(
                modifier,
                "TASK_STATUS_UPDATED",
                "Task",
                savedTask.getId(),
                previousStatus != null
                        ? previousStatus.name()
                        : null,
                status != null
                        ? status.name()
                        : null,
                "Task status updated"
        );

        /*
         * Notify owner and assignees.
         */
        Set<User> recipients = new HashSet<>();

        if (task.getTaskOwner() != null) {
            recipients.add(task.getTaskOwner());
        }

        if (task.getAssignees() != null) {
            recipients.addAll(task.getAssignees());
        }

        for (User recipient : recipients) {

            if (recipient.getId() != null
                    && modifier.getId() != null
                    && recipient.getId().equals(modifier.getId())) {
                continue;
            }

            notificationService.createNotification(
                    recipient,
                    "Task Status Updated",
                    "Task \"" + task.getTitle()
                            + "\" is now "
                            + status,
                    NotificationType.TASK_UPDATED,
                    "/tasks/" + task.getId()
            );
        }

        return mapToTaskResponse(savedTask);
    }

    /**
     * Add subtask.
     */
    @Transactional
    public TaskDto.SubTaskDto addSubTask(
            Long taskId,
            String title,
            Long assigneeId,
            User actor) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        if (!canModifyTask(task, actor)) {
            throw new RuntimeException(
                    "You do not have permission to modify this task");
        }

        User assignee = null;

        if (assigneeId != null) {

            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Sub-task assignee not found"));

            if (!projectService.canAccessProject(
                    task.getProject(),
                    assignee)) {

                throw new RuntimeException(
                        "Sub-task assignee is not assigned to the project");
            }
        }

        SubTask subTask = new SubTask();

        subTask.setTask(task);
        subTask.setTitle(title);
        subTask.setAssignedTo(assignee);
        subTask.setCompleted(false);

        if (task.getSubTasks() == null) {
            task.setSubTasks(new ArrayList<>());
        }

        task.getSubTasks().add(subTask);

        taskRepository.save(task);

        return mapToSubTaskResponse(subTask);
    }

    /**
     * Toggle subtask completion.
     */
    @Transactional
    public void toggleSubTask(
            Long subTaskId,
            User actor) {

        Task task = taskRepository.findAll()
                .stream()
                .filter(t ->
                        t.getSubTasks() != null
                                && t.getSubTasks()
                                .stream()
                                .anyMatch(
                                        s -> s.getId() != null
                                                && s.getId()
                                                .equals(subTaskId)
                                ))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sub-task not found"));

        if (!canAccessTask(task, actor)) {
            throw new RuntimeException(
                    "You do not have access to this task");
        }

        task.getSubTasks()
                .stream()
                .filter(s ->
                        s.getId() != null
                                && s.getId().equals(subTaskId))
                .findFirst()
                .ifPresent(s ->
                        s.setCompleted(!s.isCompleted()));

        taskRepository.save(task);
    }

    /**
     * Add comment.
     */
    @Transactional
    public TaskDto.TaskCommentDto addComment(
            Long taskId,
            String content,
            User author) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        if (!canAccessTask(task, author)) {
            throw new RuntimeException(
                    "You do not have access to this task");
        }

        TaskComment comment = new TaskComment();

        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        if (task.getComments() == null) {
            task.setComments(new ArrayList<>());
        }

        task.getComments().add(comment);

        taskRepository.save(task);

        return mapToCommentResponse(comment);
    }

    /**
     * Check whether user can access task.
     */
    private boolean canAccessTask(
            Task task,
            User user) {

        if (task == null || user == null) {
            return false;
        }

        if (user.getRole() == Role.SUPER_ADMIN
                || user.getRole() == Role.ADMIN
                || user.getRole() == Role.MANAGEMENT
                || user.getRole() == Role.FINANCE_HR) {

            return true;
        }

        /*
         * Task owner.
         */
        if (task.getTaskOwner() != null
                && task.getTaskOwner().getId() != null
                && task.getTaskOwner()
                .getId()
                .equals(user.getId())) {

            return true;
        }

        /*
         * Explicit assignee.
         */
        if (task.getAssignees() != null
                && task.getAssignees()
                .stream()
                .anyMatch(a ->
                        a.getId() != null
                                && a.getId().equals(user.getId()))) {

            return true;
        }

        /*
         * Project access.
         */
        return task.getProject() != null
                && projectService.canAccessProject(
                task.getProject(),
                user
        );
    }

    /**
     * Check whether user can modify task.
     */
    private boolean canModifyTask(
            Task task,
            User user) {

        if (task == null || user == null) {
            return false;
        }

        if (user.getRole() == Role.SUPER_ADMIN
                || user.getRole() == Role.ADMIN) {

            return true;
        }

        /*
         * Project Manager of the task's project.
         */
        if (task.getProject() != null
                && task.getProject().getProjectManager() != null
                && task.getProject()
                .getProjectManager()
                .getId() != null
                && task.getProject()
                .getProjectManager()
                .getId()
                .equals(user.getId())) {

            return true;
        }

        /*
         * Task owner.
         */
        if (task.getTaskOwner() != null
                && task.getTaskOwner().getId() != null
                && task.getTaskOwner()
                .getId()
                .equals(user.getId())) {

            return true;
        }

        return false;
    }

    /**
     * Generate a unique task code.
     */
    private String generateTaskCode(Project project) {

        String prefix = project.getProjectCode() != null
                ? project.getProjectCode()
                : "TASK";

        long count =
                taskRepository.countTotalTasksByProjectId(
                        project.getId());

        return prefix + "-TASK-" + (count + 1);
    }

    /**
     * Convert Task entity to TaskResponse.
     *
     * PUBLIC because DashboardService / other services
     * may need to use this mapper.
     */
    @Transactional(readOnly = true)
    public TaskDto.TaskResponse mapToTaskResponse(Task task) {

        TaskDto.TaskResponse dto =
                new TaskDto.TaskResponse();

        dto.setId(task.getId());
        dto.setTaskCode(task.getTaskCode());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setModuleName(task.getModuleName());

        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setActualHours(task.getActualHours());
        dto.setProgressPercentage(
                task.getProgressPercentage()
        );

        dto.setParentTaskId(
                task.getParentTaskId()
        );

        dto.setStartDate(task.getStartDate());
        dto.setDueDate(task.getDueDate());

        /*
         * Project.
         */
        if (task.getProject() != null) {

            dto.setProjectId(
                    task.getProject().getId()
            );

            dto.setProjectName(
                    task.getProject().getName()
            );

            dto.setProjectCode(
                    task.getProject().getProjectCode()
            );
        }

        /*
         * Task owner.
         */
        if (task.getTaskOwner() != null) {

            dto.setTaskOwner(
                    mapToUserProfile(
                            task.getTaskOwner()
                    )
            );
        }

        /*
         * Assignees.
         */
        if (task.getAssignees() != null) {

            dto.setAssignees(
                    task.getAssignees()
                            .stream()
                            .map(this::mapToUserProfile)
                            .collect(Collectors.toList())
            );

        } else {

            dto.setAssignees(
                    new ArrayList<>()
            );
        }

        /*
         * Subtasks.
         */
        if (task.getSubTasks() != null) {

            dto.setSubTasks(
                    task.getSubTasks()
                            .stream()
                            .map(this::mapToSubTaskResponse)
                            .collect(Collectors.toList())
            );

        } else {

            dto.setSubTasks(
                    new ArrayList<>()
            );
        }

        /*
         * Comments.
         */
        if (task.getComments() != null) {

            dto.setComments(
                    task.getComments()
                            .stream()
                            .map(this::mapToCommentResponse)
                            .collect(Collectors.toList())
            );

        } else {

            dto.setComments(
                    new ArrayList<>()
            );
        }

        /*
         * Attachments.
         *
         * Copy the collection so Hibernate's lazy collection
         * is not returned directly to Jackson.
         */
        if (task.getAttachmentUrls() != null) {

            dto.setAttachmentUrls(
                    new ArrayList<>(
                            task.getAttachmentUrls()
                    )
            );

        } else {

            dto.setAttachmentUrls(
                    new ArrayList<>()
            );
        }

        /*
         * Dependencies.
         *
         * If your Task entity contains dependencies, map them here.
         * Otherwise return an empty list.
         */
        dto.setDependencies(
                new ArrayList<>()
        );

        /*
         * Overdue.
         */
        boolean overdue = false;

        if (task.getDueDate() != null
                && task.getStatus() != TaskStatus.COMPLETED) {

            overdue = task.getDueDate()
                    .isBefore(
                            java.time.LocalDate.now()
                    );
        }

        dto.setOverdue(overdue);

        /*
         * Timestamps.
         *
         * These are populated only if the Task entity exposes
         * these getters.
         */
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }

    /**
     * Convert User to the project's existing
     * AuthDto.UserProfileDto.
     */
    private AuthDto.UserProfileDto mapToUserProfile(User user) {

        if (user == null) {
            return null;
        }

        AuthDto.UserProfileDto dto =
                new AuthDto.UserProfileDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setDepartment(user.getDepartment());
        dto.setDesignation(user.getDesignation());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setActive(user.isActive());
        dto.setHourlyRate(user.getHourlyRate());

        return dto;
    }

    /**
     * Map SubTask entity to DTO.
     */
    private TaskDto.SubTaskDto mapToSubTaskResponse(
            SubTask subTask) {

        TaskDto.SubTaskDto dto =
                new TaskDto.SubTaskDto();

        dto.setId(subTask.getId());

        if (subTask.getTask() != null) {
            dto.setTaskId(
                    subTask.getTask().getId()
            );
        }

        dto.setTitle(subTask.getTitle());
        dto.setCompleted(subTask.isCompleted());

        if (subTask.getAssignedTo() != null) {

            dto.setAssignedTo(
                    mapToUserProfile(
                            subTask.getAssignedTo()
                    )
            );
        }

        return dto;
    }

    /**
     * Map TaskComment entity to DTO.
     */
    private TaskDto.TaskCommentDto mapToCommentResponse(
            TaskComment comment) {

        TaskDto.TaskCommentDto dto =
                new TaskDto.TaskCommentDto();

        dto.setId(comment.getId());

        if (comment.getTask() != null) {
            dto.setTaskId(
                    comment.getTask().getId()
            );
        }

        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getAuthor() != null) {

            dto.setAuthor(
                    mapToUserProfile(
                            comment.getAuthor()
                    )
            );
        }

        return dto;
    }
}

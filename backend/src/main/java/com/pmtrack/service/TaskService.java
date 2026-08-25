package com.pmtrack.service;

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
     * Employees see:
     * - Tasks they own
     * - Tasks assigned to them
     *
     * Project Managers see:
     * - Tasks belonging to projects they manage
     */
    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksForUser(User user) {

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

        // Employee / Team Lead / other users
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
     * Get a single task.
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
     * Create a new task.
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
        task.setTaskCode(generateTaskCode(project));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        } else {
            task.setPriority(TaskPriority.MEDIUM);
        }

        task.setEstimatedHours(
                request.getEstimatedHours() != null
                        ? request.getEstimatedHours()
                        : 0.0
        );

        task.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : TaskStatus.TO_DO
        );

        task.setProgressPercentage(0);

        /*
         * The creator becomes the task owner when no explicit
         * task owner is supplied.
         */
        User taskOwner = creator;

        if (request.getTaskOwnerId() != null) {
            taskOwner = userRepository.findById(
                    request.getTaskOwnerId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Task owner not found: "
                                    + request.getTaskOwnerId()
                    ));

            if (!projectService.canAccessProject(project, taskOwner)) {
                throw new RuntimeException(
                        "Task owner " + taskOwner.getUsername()
                                + " is not assigned to the project."
                );
            }
        }

        task.setTaskOwner(taskOwner);

        /*
         * Assign employees to the task.
         */
        Set<User> assignees = new HashSet<>();

        if (request.getAssigneeIds() != null
                && !request.getAssigneeIds().isEmpty()) {

            for (Long userId : request.getAssigneeIds()) {

                User assignee = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignee not found: "
                                                + userId
                                ));

                /*
                 * Only users who have access to the project
                 * can be assigned to its tasks.
                 */
                if (!projectService.canAccessProject(
                        project,
                        assignee)) {

                    throw new RuntimeException(
                            "Assignee "
                                    + assignee.getUsername()
                                    + " is not assigned to the project."
                    );
                }

                assignees.add(assignee);
            }
        }

        task.setAssignees(assignees);

        /*
         * Billable defaults to true.
         */
        task.setBillable(
                request.getBillable() == null
                        || request.getBillable()
        );

        Task savedTask = taskRepository.save(task);

        /*
         * Audit the task creation.
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

            if (!assignee.getId().equals(creator.getId())) {

                notificationService.createNotification(
                        assignee,
                        "New Task Assigned",
                        "You have been assigned task: "
                                + savedTask.getTitle(),
                        NotificationType.TASK_ASSIGNED,
                        "/tasks/" + savedTask.getId()
                );
            }
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

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getBillable() != null) {
            task.setBillable(request.getBillable());
        }

        /*
         * Update task owner.
         */
        if (request.getTaskOwnerId() != null) {

            User owner = userRepository.findById(
                    request.getTaskOwnerId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Task owner not found"));

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

                User assignee = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignee not found: "
                                                + userId
                                ));

                if (!projectService.canAccessProject(
                        task.getProject(),
                        assignee)) {

                    throw new RuntimeException(
                            "Assignee "
                                    + assignee.getUsername()
                                    + " is not assigned to the project."
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

        task.setStatus(status);

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
         * Notify task owner and assignees when status changes.
         */
        Set<User> recipients = new HashSet<>();

        if (task.getTaskOwner() != null) {
            recipients.add(task.getTaskOwner());
        }

        if (task.getAssignees() != null) {
            recipients.addAll(task.getAssignees());
        }

        for (User recipient : recipients) {

            if (recipient.getId().equals(modifier.getId())) {
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
     * Add a sub-task.
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

        task.getSubTasks().add(subTask);

        taskRepository.save(task);

        return mapToSubTaskResponse(subTask);
    }

    /**
     * Toggle sub-task completion.
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
                                        s -> s.getId()
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
                        s.getId().equals(subTaskId))
                .findFirst()
                .ifPresent(s ->
                        s.setCompleted(!s.isCompleted()));

        taskRepository.save(task);
    }

    /**
     * Add a comment to a task.
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

        task.getComments().add(comment);

        taskRepository.save(task);

        return mapToCommentResponse(comment);
    }

    /**
     * Check whether a user can access a task.
     */
    private boolean canAccessTask(
            Task task,
            User user) {

        if (user == null) {
            return false;
        }

        if (user.getRole() == Role.SUPER_ADMIN
                || user.getRole() == Role.ADMIN
                || user.getRole() == Role.MANAGEMENT
                || user.getRole() == Role.FINANCE_HR) {

            return true;
        }

        if (task.getTaskOwner() != null
                && task.getTaskOwner()
                .getId()
                .equals(user.getId())) {

            return true;
        }

        if (task.getAssignees() != null
                && task.getAssignees()
                .stream()
                .anyMatch(a ->
                        a.getId().equals(user.getId()))) {

            return true;
        }

        return task.getProject() != null
                && projectService.canAccessProject(
                        task.getProject(),
                        user);
    }

    /**
     * Check whether a user can modify a task.
     */
    private boolean canModifyTask(
            Task task,
            User user) {

        if (user == null) {
            return false;
        }

        if (user.getRole() == Role.SUPER_ADMIN
                || user.getRole() == Role.ADMIN) {

            return true;
        }

        if (task.getProject() != null
                && task.getProject()
                .getProjectManager() != null
                && task.getProject()
                .getProjectManager()
                .getId()
                .equals(user.getId())) {

            return true;
        }

        if (task.getTaskOwner() != null
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
     * Convert Task entity to response DTO.
     */
    private TaskDto.TaskResponse mapToTaskResponse(Task task) {

        TaskDto.TaskResponse dto =
                new TaskDto.TaskResponse();

        dto.setId(task.getId());
        dto.setTaskCode(task.getTaskCode());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());

        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setBillable(task.isBillable());
        dto.setProgressPercentage(
                task.getProgressPercentage());

        if (task.getProject() != null) {

            dto.setProjectId(
                    task.getProject().getId());

            dto.setProjectName(
                    task.getProject().getName());

            dto.setProjectCode(
                    task.getProject().getProjectCode());
        }

        if (task.getTaskOwner() != null) {

            dto.setTaskOwnerId(
                    task.getTaskOwner().getId());

            dto.setTaskOwnerName(
                    task.getTaskOwner().getFullName());
        }

        /*
         * Map assignees.
         */
        if (task.getAssignees() != null) {

            dto.setAssignees(
                    task.getAssignees()
                            .stream()
                            .map(this::mapToUserSummary)
                            .collect(Collectors.toList())
            );
        } else {

            dto.setAssignees(
                    new ArrayList<>()
            );
        }

        /*
         * Map subtasks.
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
         * Map comments.
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
         * Avoid returning Hibernate lazy proxies directly.
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

        return dto;
    }

    /**
     * Map user to a lightweight user summary.
     */
    private TaskDto.UserSummaryDto mapToUserSummary(
            User user) {

        TaskDto.UserSummaryDto dto =
                new TaskDto.UserSummaryDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    /**
     * Map sub-task entity to DTO.
     */
    private TaskDto.SubTaskDto mapToSubTaskResponse(
            SubTask subTask) {

        TaskDto.SubTaskDto dto =
                new TaskDto.SubTaskDto();

        dto.setId(subTask.getId());
        dto.setTitle(subTask.getTitle());
        dto.setCompleted(subTask.isCompleted());

        if (subTask.getAssignedTo() != null) {

            dto.setAssignedToId(
                    subTask.getAssignedTo().getId());

            dto.setAssignedToName(
                    subTask.getAssignedTo().getFullName());
        }

        return dto;
    }

    /**
     * Map comment entity to DTO.
     */
    private TaskDto.TaskCommentDto mapToCommentResponse(
            TaskComment comment) {

        TaskDto.TaskCommentDto dto =
                new TaskDto.TaskCommentDto();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getAuthor() != null) {

            dto.setAuthorId(
                    comment.getAuthor().getId());

            dto.setAuthorName(
                    comment.getAuthor().getFullName());
        }

        return dto;
    }
}

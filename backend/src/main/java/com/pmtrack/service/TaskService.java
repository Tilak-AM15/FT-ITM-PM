package com.pmtrack.service;

import com.pmtrack.dto.AuthDto;
import com.pmtrack.dto.TaskDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SubTaskRepository subTaskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TimesheetRepository timesheetRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository, SubTaskRepository subTaskRepository,
                       TaskDependencyRepository taskDependencyRepository, TaskCommentRepository taskCommentRepository,
                       TimesheetRepository timesheetRepository, AuditService auditService,
                       NotificationService notificationService, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.subTaskRepository = subTaskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.timesheetRepository = timesheetRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksByProjectId(Long projectId, User user) {
        Project project = projectService.getProjectEntityById(projectId);
        projectService.assertCanAccessProject(project, user);
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDto.TaskResponse> getTasksForUser(User user) {
        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN ||
            user.getRole() == Role.MANAGEMENT || user.getRole() == Role.FINANCE_HR) {
            return taskRepository.findAll().stream().map(this::mapToTaskResponse).collect(Collectors.toList());
        }

        if (user.getRole() == Role.PROJECT_MANAGER) {
            return projectRepository.findByProjectManager(user).stream()
                    .flatMap(project -> taskRepository.findByProject(project).stream())
                    .distinct()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }

        return taskRepository.findTasksAssignedToUser(user.getId(), user).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDto.TaskResponse getTaskById(Long id, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        projectService.assertCanAccessProject(task.getProject(), user);
        return mapToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public Task getTaskEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    @Transactional
    public TaskDto.TaskResponse createTask(TaskDto.TaskRequest request, User creator) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + request.getProjectId()));
        projectService.assertCanManageTasks(project, creator);
        if (request.getStartDate() != null && request.getDueDate() != null && request.getDueDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Task due date cannot be before start date.");
        }
        if (request.getEstimatedHours() != null && request.getEstimatedHours() < 0) {
            throw new RuntimeException("Estimated hours cannot be negative.");
        }

        Task task = new Task();
        task.setProject(project);

        String taskCode = request.getTaskCode();
        if (taskCode == null || taskCode.isBlank()) {
            taskCode = project.getProjectCode() + "-T" + (taskRepository.countTotalTasksByProjectId(project.getId()) + 1);
        }
        if (taskRepository.findByTaskCode(taskCode).isPresent()) {
            throw new RuntimeException("Task code already exists: " + taskCode);
        }
        task.setTaskCode(taskCode);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setModuleName(request.getModuleName() != null ? request.getModuleName() : "General");
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TO_DO);
        task.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        task.setDueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusWeeks(1));
        task.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : 8.0);
        task.setAttachmentUrls(validateUrls(request.getAttachmentUrls()));
        task.setActualHours(0.0);
        task.setProgressPercentage(0);

        if (request.getTaskOwnerId() != null) {
            User owner = userRepository.findById(request.getTaskOwnerId())
                    .orElseThrow(() -> new RuntimeException("Task owner not found: " + request.getTaskOwnerId()));
            if (!projectService.canAccessProject(project, owner)) {
                throw new RuntimeException("Task owner must have access to the project.");
            }
            task.setTaskOwner(owner);
        }

        Set<User> assignees = new HashSet<>();
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            for (Long uid : request.getAssigneeIds()) {
                User assignee = userRepository.findById(uid)
                        .orElseThrow(() -> new RuntimeException("Assignee not found: " + uid));
                if (!projectService.canAccessProject(project, assignee)) {
                    throw new RuntimeException("Assignee " + assignee.getUsername() + " is not assigned to the project.");
                }
                assignees.add(assignee);
            }
        }
        task.setAssignees(assignees);

        if (request.getParentTaskId() != null) {
            Task parent = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> new RuntimeException("Parent task not found: " + request.getParentTaskId()));
            if (!parent.getProject().getId().equals(project.getId())) {
                throw new RuntimeException("Parent task must belong to the same project.");
            }
            task.setParentTask(parent);
        }

        Task savedTask = taskRepository.save(task);

        // Add dependencies if any
        if (request.getDependsOnTaskIds() != null) {
            for (Long depId : request.getDependsOnTaskIds()) {
                Task depTask = taskRepository.findById(depId)
                        .orElseThrow(() -> new RuntimeException("Dependency task not found: " + depId));
                if (!depTask.getProject().getId().equals(project.getId()) || depTask.getId().equals(savedTask.getId())) {
                    throw new RuntimeException("Dependencies must reference a different task in the same project.");
                }
                TaskDependency dep = new TaskDependency(savedTask, depTask, "FINISH_TO_START");
                taskDependencyRepository.save(dep);
            }
        }

        // Notify assignees
        for (User assignee : assignees) {
            notificationService.createNotification(
                    assignee,
                    "New Task Assigned: " + savedTask.getTitle(),
                    "You have been assigned to task " + savedTask.getTaskCode() + " in " + project.getName() + ". Due: " + savedTask.getDueDate(),
                    NotificationType.TASK_ASSIGNED,
                    "/tasks"
            );
        }

        auditService.logAction(
                creator,
                "TASK_CREATED",
                "Task",
                savedTask.getId(),
                null,
                savedTask.getTitle() + " (" + savedTask.getStatus() + ")",
                "Task created in project " + project.getProjectCode()
        );

        return mapToTaskResponse(savedTask);
    }

    @Transactional
    public TaskDto.TaskResponse updateTask(Long id, TaskDto.TaskRequest request, User modifier) {
        Task task = getTaskEntityById(id);
        projectService.assertCanManageTasks(task.getProject(), modifier);
        if (request.getStartDate() != null && request.getDueDate() != null && request.getDueDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Task due date cannot be before start date.");
        }
        String oldValues = "Status: " + task.getStatus() + ", Priority: " + task.getPriority() + ", EstHours: " + task.getEstimatedHours();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getModuleName() != null) task.setModuleName(request.getModuleName());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getStartDate() != null) task.setStartDate(request.getStartDate());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getEstimatedHours() != null) task.setEstimatedHours(request.getEstimatedHours());
        if (request.getAttachmentUrls() != null) task.setAttachmentUrls(validateUrls(request.getAttachmentUrls()));

        if (request.getTaskOwnerId() != null) {
            User owner = userRepository.findById(request.getTaskOwnerId())
                    .orElseThrow(() -> new RuntimeException("Task owner not found: " + request.getTaskOwnerId()));
            if (!projectService.canAccessProject(task.getProject(), owner)) {
                throw new RuntimeException("Task owner must have access to the project.");
            }
            task.setTaskOwner(owner);
        }

        if (request.getAssigneeIds() != null) {
            Set<User> assignees = new HashSet<>();
            for (Long uid : request.getAssigneeIds()) {
                User assignee = userRepository.findById(uid)
                        .orElseThrow(() -> new RuntimeException("Assignee not found: " + uid));
                if (!projectService.canAccessProject(task.getProject(), assignee)) {
                    throw new RuntimeException("Assignee " + assignee.getUsername() + " is not assigned to the project.");
                }
                assignees.add(assignee);
            }
            task.setAssignees(assignees);
        }

        Task saved = taskRepository.save(task);

        String newValues = "Status: " + saved.getStatus() + ", Priority: " + saved.getPriority() + ", EstHours: " + saved.getEstimatedHours();
        auditService.logAction(modifier, "TASK_UPDATED", "Task", saved.getId(), oldValues, newValues, "Task details updated");

        return mapToTaskResponse(saved);
    }

    @Transactional
    public TaskDto.TaskResponse updateTaskStatus(Long id, TaskStatus status, Integer progressPercentage, User modifier) {
        Task task = getTaskEntityById(id);
        if (status == null) {
            throw new RuntimeException("Task status is required.");
        }
        boolean manager = modifier.getRole() == Role.SUPER_ADMIN || modifier.getRole() == Role.ADMIN
                || modifier.getRole() == Role.PROJECT_MANAGER || modifier.getRole() == Role.TEAM_LEAD;
        boolean assignee = task.getTaskOwner() != null && task.getTaskOwner().getId().equals(modifier.getId())
                || task.getAssignees().stream().anyMatch(u -> u.getId().equals(modifier.getId()));
        if (manager) {
            projectService.assertCanManageTasks(task.getProject(), modifier);
        } else if (!assignee) {
            throw new RuntimeException("Only the task owner, assignees, or authorized managers can update task status.");
        }
        if (progressPercentage != null && (progressPercentage < 0 || progressPercentage > 100)) {
            throw new RuntimeException("Progress percentage must be between 0 and 100.");
        }
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);

        if (progressPercentage != null) {
            task.setProgressPercentage(progressPercentage);
        } else if (status == TaskStatus.COMPLETED) {
            task.setProgressPercentage(100);
        }

        Task saved = taskRepository.save(task);

        auditService.logAction(
                modifier,
                "TASK_STATUS_CHANGED",
                "Task",
                saved.getId(),
                oldStatus.name(),
                status.name(),
                "Task status updated to " + status.name()
        );

        return mapToTaskResponse(saved);
    }

    @Transactional
    public TaskDto.SubTaskDto addSubTask(Long taskId, String title, Long assigneeId, User actor) {
        Task task = getTaskEntityById(taskId);
        projectService.assertCanManageTasks(task.getProject(), actor);
        if (title == null || title.isBlank()) throw new RuntimeException("Sub-task title is required.");
        User assignedTo = assigneeId != null ? userRepository.findById(assigneeId).orElseThrow(() -> new RuntimeException("Assignee not found: " + assigneeId)) : null;
        if (assignedTo != null && !projectService.canAccessProject(task.getProject(), assignedTo)) {
            throw new RuntimeException("Sub-task assignee must have access to the project.");
        }
        SubTask subTask = new SubTask(task, title, assignedTo);
        SubTask saved = subTaskRepository.save(subTask);
        auditService.logAction(actor, "SUBTASK_CREATED", "Task", taskId, null, title, "Sub-task created");

        TaskDto.SubTaskDto dto = new TaskDto.SubTaskDto();
        dto.setId(saved.getId());
        dto.setTaskId(taskId);
        dto.setTitle(saved.getTitle());
        dto.setCompleted(saved.isCompleted());
        if (saved.getAssignedTo() != null) {
            dto.setAssignedTo(new AuthDto.UserProfileDto(saved.getAssignedTo()));
        }
        return dto;
    }

    @Transactional
    public void toggleSubTask(Long subTaskId, User actor) {
        SubTask st = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("Sub-task not found: " + subTaskId));
        boolean allowed = st.getAssignedTo() != null && st.getAssignedTo().getId().equals(actor.getId());
        if (!allowed) {
            projectService.assertCanManageTasks(st.getTask().getProject(), actor);
        }
        boolean oldCompleted = st.isCompleted();
        st.setCompleted(!st.isCompleted());
        subTaskRepository.save(st);
        auditService.logAction(actor, "SUBTASK_STATUS_CHANGED", "Task", st.getTask().getId(), String.valueOf(oldCompleted), String.valueOf(st.isCompleted()), "Sub-task completion updated");
    }

    @Transactional
    public TaskDto.TaskCommentDto addComment(Long taskId, String content, User author) {
        Task task = getTaskEntityById(taskId);
        projectService.assertCanAccessProject(task.getProject(), author);
        if (content == null || content.isBlank()) throw new RuntimeException("Comment content is required.");
        TaskComment comment = new TaskComment(task, author, content.trim());
        TaskComment saved = taskCommentRepository.save(comment);
        auditService.logAction(author, "TASK_COMMENT_ADDED", "Task", taskId, null, content.trim(), "Task comment added");

        TaskDto.TaskCommentDto dto = new TaskDto.TaskCommentDto();
        dto.setId(saved.getId());
        dto.setTaskId(taskId);
        dto.setAuthor(new AuthDto.UserProfileDto(author));
        dto.setContent(saved.getContent());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    private List<String> validateUrls(List<String> urls) {
        if (urls == null) return new ArrayList<>();
        if (urls.size() > 20) throw new RuntimeException("A task cannot contain more than 20 attachment links.");
        return urls.stream().map(String::trim).filter(u -> !u.isBlank()).peek(u -> {
            if (!(u.startsWith("https://") || u.startsWith("http://"))) {
                throw new RuntimeException("Attachment links must start with http:// or https://.");
            }
            if (u.length() > 500) throw new RuntimeException("An attachment link is too long.");
        }).distinct().collect(Collectors.toList());
    }

    public TaskDto.TaskResponse mapToTaskResponse(Task task) {
        TaskDto.TaskResponse dto = new TaskDto.TaskResponse();
        dto.setId(task.getId());
        dto.setProjectId(task.getProject().getId());
        dto.setProjectName(task.getProject().getName());
        dto.setProjectCode(task.getProject().getProjectCode());
        dto.setTaskCode(task.getTaskCode());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setModuleName(task.getModuleName());
        if (task.getTaskOwner() != null) {
            dto.setTaskOwner(new AuthDto.UserProfileDto(task.getTaskOwner()));
        }
        dto.setAssignees(task.getAssignees().stream().map(AuthDto.UserProfileDto::new).collect(Collectors.toList()));
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setStartDate(task.getStartDate());
        dto.setDueDate(task.getDueDate());
        dto.setEstimatedHours(task.getEstimatedHours());

        // Calculate actual hours logged and approved for this task
        Double actualHrs = timesheetRepository.sumApprovedHoursByTaskId(task.getId());
        dto.setActualHours(actualHrs != null ? actualHrs : 0.0);

        dto.setProgressPercentage(task.getProgressPercentage());
        if (task.getParentTask() != null) {
            dto.setParentTaskId(task.getParentTask().getId());
        }

        boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED;
        dto.setOverdue(isOverdue);

        // Subtasks
        List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());
        dto.setSubTasks(subTasks.stream().map(st -> {
            TaskDto.SubTaskDto sd = new TaskDto.SubTaskDto();
            sd.setId(st.getId());
            sd.setTaskId(task.getId());
            sd.setTitle(st.getTitle());
            sd.setCompleted(st.isCompleted());
            if (st.getAssignedTo() != null) sd.setAssignedTo(new AuthDto.UserProfileDto(st.getAssignedTo()));
            return sd;
        }).collect(Collectors.toList()));

        // Comments
        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());
        dto.setComments(comments.stream().map(c -> {
            TaskDto.TaskCommentDto cd = new TaskDto.TaskCommentDto();
            cd.setId(c.getId());
            cd.setTaskId(task.getId());
            cd.setAuthor(new AuthDto.UserProfileDto(c.getAuthor()));
            cd.setContent(c.getContent());
            cd.setCreatedAt(c.getCreatedAt());
            return cd;
        }).collect(Collectors.toList()));

        // Dependencies
        List<TaskDependency> deps = taskDependencyRepository.findByTaskId(task.getId());
        dto.setAttachmentUrls(task.getAttachmentUrls());
        dto.setDependencies(deps.stream().map(d -> {
            TaskDto.DependencyDto dd = new TaskDto.DependencyDto();
            dd.setId(d.getId());
            dd.setTaskId(task.getId());
            dd.setDependsOnTaskId(d.getDependsOnTask().getId());
            dd.setDependsOnTaskTitle(d.getDependsOnTask().getTitle());
            dd.setDependsOnTaskCode(d.getDependsOnTask().getTaskCode());
            dd.setDependsOnTaskStatus(d.getDependsOnTask().getStatus());
            dd.setDependencyType(d.getDependencyType());
            return dd;
        }).collect(Collectors.toList()));

        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}

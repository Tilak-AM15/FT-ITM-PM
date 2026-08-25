import React, { useState, useEffect } from 'react';
import { taskApi, projectApi, userApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import { Modal } from '../../components/Modal';
import { useAuth } from '../../context/AuthContext';
import {
  CheckSquare,
  Search,
  Plus,
  ListTodo,
  Users,
  X,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const TaskList = () => {
  const { user, hasRole } = useAuth();

  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [employees, setEmployees] = useState([]);

  const assignableEmployees = employees;
  
  const [loading, setLoading] = useState(true);
  const [loadingFormData, setLoadingFormData] = useState(false);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');

  // Create Task Modal
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [projectId, setProjectId] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('MEDIUM');
  const [estimatedHours, setEstimatedHours] = useState(16);
  const [dueDate, setDueDate] = useState('');

  // Multiple employee IDs
  const [assigneeIds, setAssigneeIds] = useState([]);

  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate();

  /*
   * ---------------------------------------------------------
   * LOAD TASKS
   * ---------------------------------------------------------
   */
  const loadTasks = async () => {
    setLoading(true);

    const [taskResult, projectResult] = await Promise.allSettled([
      taskApi.getMyTasks(),
      projectApi.getAll(),
    ]);

    const taskData =
      taskResult.status === 'fulfilled' && Array.isArray(taskResult.value?.data)
        ? taskResult.value.data
        : [];
    const projectData =
      projectResult.status === 'fulfilled' && Array.isArray(projectResult.value?.data)
        ? projectResult.value.data
        : [];

    if (taskResult.status === 'rejected') console.error('Failed to load tasks:', taskResult.reason);
    if (projectResult.status === 'rejected') console.error('Failed to load projects for task form:', projectResult.reason);

    setTasks(taskData);

    const projectMap = new Map();
    projectData.forEach((project) => {
      if (project?.id != null) projectMap.set(String(project.id), project);
    });
    taskData.forEach((task) => {
      if (task?.projectId != null && !projectMap.has(String(task.projectId))) {
        projectMap.set(String(task.projectId), {
          id: task.projectId,
          name: task.projectName || `Project ${task.projectId}`,
          projectCode: task.projectCode || '',
        });
      }
    });

    const finalProjects = Array.from(projectMap.values());
    setProjects(finalProjects);

    if (finalProjects.length > 0) {
      setProjectId((currentProjectId) => {
        const valid = finalProjects.some(
          (project) => String(project.id) === String(currentProjectId)
        );
        return valid ? currentProjectId : String(finalProjects[0].id);
      });
    } else {
      setProjectId('');
    }

    console.log('Task page projects:', finalProjects);
    console.log('Task page tasks:', taskData);
    setLoading(false);
  };

  /*
   * ---------------------------------------------------------
   * LOAD EMPLOYEES
   * ---------------------------------------------------------
   */
  const loadEmployees = async () => {
    try {
      setLoadingFormData(true);

      const response =
        await userApi.getAll();

      const users = Array.isArray(response.data)
        ? response.data
        : [];

      /*
       * Only active users should normally be assignable.
       *
       * We don't restrict this to a particular role because
       * your backend exposes multiple valid roles and the
       * exact assignable-role policy is not defined here.
       */
      const activeUsers = users.filter(
        (employee) =>
          employee &&
          employee.id != null &&
          employee.active !== false
      );

      setEmployees(activeUsers);

      console.log(
        'Available employees:',
        activeUsers
      );
    } catch (err) {
      console.error(
        'Failed to load employees:',
        err
      );

      setEmployees([]);
    } finally {
      setLoadingFormData(false);
    }
  };

  /*
   * ---------------------------------------------------------
   * INITIAL LOAD
   * ---------------------------------------------------------
   */
  useEffect(() => {
    loadTasks();
    loadEmployees();
  }, []);

  /*
   * ---------------------------------------------------------
   * OPEN CREATE TASK MODAL
   * ---------------------------------------------------------
   */
  const openCreateModal = async () => {
    setIsModalOpen(true);

    /*
     * Refresh the project and employee lists whenever
     * the modal opens. This prevents stale dropdown data.
     */
    await Promise.all([
      loadTasks(),
      loadEmployees(),
    ]);
  };

  /*
   * ---------------------------------------------------------
   * CLOSE CREATE TASK MODAL
   * ---------------------------------------------------------
   */
  const closeCreateModal = () => {
    if (submitting) {
      return;
    }

    setIsModalOpen(false);
  };

  /*
   * ---------------------------------------------------------
   * ASSIGNEE SELECTION
   * ---------------------------------------------------------
   */
  const handleAssigneeChange = (e) => {
    const selectedIds = Array.from(
      e.target.selectedOptions,
      (option) => Number(option.value)
    );

    setAssigneeIds(selectedIds);
  };

  /*
   * ---------------------------------------------------------
   * REMOVE SELECTED ASSIGNEE
   * ---------------------------------------------------------
   */
  const removeAssignee = (id) => {
    setAssigneeIds((current) =>
      current.filter(
        (employeeId) =>
          Number(employeeId) !== Number(id)
      )
    );
  };

  /*
   * ---------------------------------------------------------
   * RESET CREATE FORM
   * ---------------------------------------------------------
   */
  const resetForm = () => {
    setTitle('');
    setDescription('');
    setPriority('MEDIUM');
    setEstimatedHours(16);
    setDueDate('');
    setAssigneeIds([]);

    /*
     * Keep the first project selected if available.
     */
    if (projects.length > 0) {
      setProjectId(
        String(projects[0].id)
      );
    } else {
      setProjectId('');
    }
  };

  /*
   * ---------------------------------------------------------
   * CREATE TASK
   * ---------------------------------------------------------
   */
  const handleCreateTask = async (e) => {
    e.preventDefault();

    if (!projectId) {
      console.error(
        'Cannot create task: project is missing'
      );
      return;
    }

    if (!title.trim()) {
      console.error(
        'Cannot create task: title is missing'
      );
      return;
    }

    setSubmitting(true);

    try {
      const payload = {
        projectId: Number(projectId),
        taskOwnerId: user?.id ? Number(user.id) : undefined,

        title: title.trim(),

        description:
          description.trim(),

        priority,

        estimatedHours:
          Number(estimatedHours),

        dueDate:
          dueDate || undefined,

        /*
         * IMPORTANT:
         * This is what assigns employees to the task.
         */
        assigneeIds:
          assigneeIds.map(Number),
      };

      console.log(
        'Creating task with payload:',
        payload
      );

      await taskApi.create(payload);

      setIsModalOpen(false);

      resetForm();

      /*
       * Refresh task/project data so the new task
       * immediately appears in the table.
       */
      await loadTasks();
    } catch (err) {
      console.error(
        'Failed to create task:',
        err
      );

      console.error(
        'Backend response:',
        err?.response?.data
      );
    } finally {
      setSubmitting(false);
    }
  };

  /*
   * ---------------------------------------------------------
   * STATUS CHANGE
   * ---------------------------------------------------------
   */
  const handleStatusChange = async (
    taskId,
    newStatus
  ) => {
    try {
      await taskApi.updateStatus(
        taskId,
        {
          status: newStatus,
          progressPercentage:
            newStatus === 'COMPLETED'
              ? 100
              : newStatus === 'IN_PROGRESS'
                ? 50
                : 0,
        }
      );

      await loadTasks();
    } catch (err) {
      console.error(
        'Failed to update status:',
        err
      );
    }
  };

  /*
   * ---------------------------------------------------------
   * FILTER TASKS
   * ---------------------------------------------------------
   */
  /*
   * ---------------------------------------------------------
   * PROJECT MEMBERS / ASSIGNABLE EMPLOYEES
   * ---------------------------------------------------------
   *
   * These values must be component-scoped because the create-task
   * modal uses them. Previously they were declared inside the
   * filteredTasks.filter() callback, which caused:
   *
   * ReferenceError: assignableEmployees is not defined
   */
  const selectedProject = projects.find(
    (project) => String(project.id) === String(projectId)
  );

  const projectMemberIds = Array.isArray(selectedProject?.members)
    ? selectedProject.members
        .map((member) => member?.user?.id)
        .filter((id) => id != null)
    : [];

  const assignableEmployees =
    projectMemberIds.length > 0
      ? employees.filter(
          (employee) =>
            projectMemberIds.some(
              (id) => Number(id) === Number(employee.id)
            ) ||
            Number(employee.id) ===
              Number(selectedProject?.projectManager?.id)
        )
      : employees;

  const filteredTasks = tasks.filter(
    (task) => {
      const search =
        searchTerm.toLowerCase();

      const matchSearch =
        task.title
          ?.toLowerCase()
          .includes(search) ||
        task.taskCode
          ?.toLowerCase()
          .includes(search) ||
        task.projectName
          ?.toLowerCase()
          .includes(search);

      const matchStatus =
        statusFilter === 'ALL' ||
        task.status === statusFilter;

      const matchPriority =
        priorityFilter === 'ALL' ||
        task.priority === priorityFilter;

  return (
        matchSearch &&
        matchStatus &&
        matchPriority
      );
    }
  );

  /*
   * ---------------------------------------------------------
   * GET EMPLOYEE NAME
   * ---------------------------------------------------------
   */
  const getEmployeeById = (id) => {
    return employees.find(
      (employee) =>
        Number(employee.id) === Number(id)
    );
  };

  return (
    <div className="space-y-6">

      {/* =====================================================
          HEADER
      ====================================================== */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">

        <div>
          <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">
            Tasks Management
          </h1>

          <p className="text-xs text-slate-400 mt-0.5">
            Track work items, assignees,
            deadlines, and execution progress.
          </p>
        </div>

        <div className="flex items-center gap-3">

          <button
            onClick={() =>
              navigate('/kanban')
            }
            className="btn btn-secondary btn-sm"
          >
            <ListTodo className="w-4 h-4 text-cyan-400" />
            <span>Kanban Board</span>
          </button>

          {hasRole(
            'SUPER_ADMIN',
            'ADMIN',
            'PROJECT_MANAGER',
            'TEAM_LEAD'
          ) && (
            <button
              onClick={openCreateModal}
              className="btn btn-primary btn-sm"
            >
              <Plus className="w-4 h-4" />
              <span>Create Task</span>
            </button>
          )}

        </div>
      </div>

      {/* =====================================================
          FILTERS
      ====================================================== */}
      <div className="glass-panel p-4 flex flex-col md:flex-row items-center justify-between gap-3">

        <div className="flex items-center gap-2 bg-slate-900 border border-white/10 rounded-lg px-3 py-1.5 w-full md:w-80 text-xs">

          <Search className="w-4 h-4 text-slate-400" />

          <input
            type="text"
            placeholder="Search tasks, codes, or projects..."
            value={searchTerm}
            onChange={(e) =>
              setSearchTerm(
                e.target.value
              )
            }
            className="bg-transparent border-none outline-none text-slate-200 w-full"
          />

        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">

          <select
            value={statusFilter}
            onChange={(e) =>
              setStatusFilter(
                e.target.value
              )
            }
            className="form-select text-xs py-1.5"
          >
            <option value="ALL">
              All Statuses
            </option>

            <option value="TO_DO">
              To Do
            </option>

            <option value="IN_PROGRESS">
              In Progress
            </option>

            <option value="BLOCKED">
              Blocked
            </option>

            <option value="COMPLETED">
              Completed
            </option>

            <option value="CANCELLED">
              Cancelled
            </option>
          </select>

          <select
            value={priorityFilter}
            onChange={(e) =>
              setPriorityFilter(
                e.target.value
              )
            }
            className="form-select text-xs py-1.5"
          >
            <option value="ALL">
              All Priorities
            </option>

            <option value="CRITICAL">
              Critical
            </option>

            <option value="HIGH">
              High
            </option>

            <option value="MEDIUM">
              Medium
            </option>

            <option value="LOW">
              Low
            </option>
          </select>

        </div>
      </div>

      {/* =====================================================
          TASK TABLE
      ====================================================== */}
      <div className="glass-card p-6 space-y-4">

        {loading ? (
          <div className="flex items-center justify-center py-20">

            <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />

          </div>
        ) : filteredTasks.length === 0 ? (
          <div className="text-center py-12 text-slate-400 text-xs">

            <CheckSquare className="w-8 h-8 mx-auto mb-3 opacity-40" />

            <p>
              No tasks found matching
              your filter criteria.
            </p>

          </div>
        ) : (
          <div className="table-container">

            <table className="modern-table">

              <thead>
                <tr>

                  <th>Code</th>

                  <th>Task Title</th>

                  <th>Project</th>

                  <th>Assignees</th>

                  <th>Priority</th>

                  <th>Hours (Act/Est)</th>

                  <th>Due Date</th>

                  <th>Status</th>

                  <th>Action</th>

                </tr>
              </thead>

              <tbody>

                {filteredTasks.map(
                  (task) => (
                    <tr key={task.id}>

                      <td className="font-mono text-xs font-bold text-indigo-400">
                        {task.taskCode}
                      </td>

                      <td>

                        <div className="font-semibold text-slate-200 text-xs">
                          {task.title}
                        </div>

                        <div className="text-[10px] text-slate-400">
                          {task.moduleName ||
                            'General'}
                        </div>

                      </td>

                      <td>

                        <span className="text-xs text-slate-300 font-medium block truncate max-w-xs">
                          {task.projectName ||
                            '—'}
                        </span>

                      </td>

                      <td>

                        <div className="flex flex-wrap items-center gap-1">

                          {task.assignees &&
                          task.assignees.length >
                            0 ? (
                            task.assignees.map(
                              (assignee) => (
                                <span
                                  key={
                                    assignee.id
                                  }
                                  className="inline-flex items-center gap-1 px-2 py-1 rounded-md bg-indigo-500/10 border border-indigo-500/20 text-xs text-slate-200"
                                >
                                  <Users className="w-3 h-3 text-indigo-400" />

                                  {assignee.fullName ||
                                    assignee.username}
                                </span>
                              )
                            )
                          ) : (
                            <span className="text-xs text-slate-500">
                              Unassigned
                            </span>
                          )}

                        </div>

                      </td>

                      <td>
                        <StatusBadge
                          status={
                            task.priority
                          }
                        />
                      </td>

                      <td className="text-xs font-semibold text-slate-200">
                        {task.actualHours ||
                          0}
                        h /{' '}
                        {task.estimatedHours ||
                          0}
                        h
                      </td>

                      <td className="text-xs text-slate-400">

                        <span
                          className={
                            task.isOverdue
                              ? 'text-rose-400 font-bold'
                              : ''
                          }
                        >
                          {task.dueDate ||
                            'No date'}
                        </span>

                      </td>

                      <td>
                        <StatusBadge
                          status={
                            task.status
                          }
                        />
                      </td>

                      <td>

                        <select
                          value={
                            task.status
                          }
                          onChange={(e) =>
                            handleStatusChange(
                              task.id,
                              e.target.value
                            )
                          }
                          className="form-select text-[11px] py-1 px-2 w-28 bg-slate-900"
                        >

                          <option value="TO_DO">
                            To Do
                          </option>

                          <option value="IN_PROGRESS">
                            In Progress
                          </option>

                          <option value="BLOCKED">
                            Blocked
                          </option>

                          <option value="COMPLETED">
                            Completed
                          </option>

                          <option value="CANCELLED">
                            Cancelled
                          </option>

                        </select>

                      </td>

                    </tr>
                  )
                )}

              </tbody>

            </table>

          </div>
        )}

      </div>

      {/* =====================================================
          CREATE TASK MODAL
      ====================================================== */}
      <Modal
        isOpen={isModalOpen}
        onClose={closeCreateModal}
        title="Create New Task"
      >

        <form
          onSubmit={
            handleCreateTask
          }
          className="space-y-4"
        >

          {/* -------------------------------------------------
              PROJECT
          -------------------------------------------------- */}
          <div className="form-group">

            <label className="form-label">
              Project *
            </label>

            <select
              value={projectId}
              onChange={(e) =>
                setProjectId(
                  e.target.value
                )
              }
              className="form-select text-xs"
              required
              disabled={
                loadingFormData &&
                projects.length === 0
              }
            >

              <option value="">
                {projects.length === 0
                  ? 'No projects available'
                  : 'Select Project'}
              </option>

              {projects.map(
                (project) => (
                  <option
                    key={project.id}
                    value={project.id}
                  >
                    {project.name ||
                      'Unnamed Project'}

                    {project.projectCode
                      ? ` (${project.projectCode})`
                      : ''}
                  </option>
                )
              )}

            </select>

            {projects.length === 0 && (
              <p className="text-[11px] text-amber-400 mt-1">
                No projects were returned
                by the backend for this
                account.
              </p>
            )}

          </div>

          {/* -------------------------------------------------
              TASK TITLE
          -------------------------------------------------- */}
          <div className="form-group">

            <label className="form-label">
              Task Title *
            </label>

            <input
              type="text"
              required
              placeholder="e.g. Implement OAuth2 API Gateway proxy"
              value={title}
              onChange={(e) =>
                setTitle(
                  e.target.value
                )
              }
              className="form-input text-xs"
            />

          </div>

          {/* -------------------------------------------------
              ASSIGNEES
          -------------------------------------------------- */}
          <div className="form-group">

            <label className="form-label flex items-center gap-1.5">

              <Users className="w-3.5 h-3.5 text-indigo-400" />

              Assign Employee(s)

            </label>

            <select
              multiple
              value={assigneeIds.map(
                String
              )}
              onChange={
                handleAssigneeChange
              }
              className="form-select text-xs min-h-[120px]"
              disabled={
                loadingFormData
              }
            >

              {assignableEmployees.length === 0 ? (
                <option
                  value=""
                  disabled
                >
                  {loadingFormData
                    ? 'Loading employees...'
                    : projectMemberIds.length > 0
                      ? 'No active members found for this project'
                      : 'No active employees found'}
                </option>
              ) : (
                assignableEmployees.map(
                  (employee) => (
                    <option
                      key={
                        employee.id
                      }
                      value={
                        employee.id
                      }
                    >
                      {employee.fullName ||
                        employee.username}

                      {employee.username &&
                      employee.fullName
                        ? ` (${employee.username})`
                        : ''}
                    </option>
                  )
                )
              )}

            </select>

            <p className="text-[10px] text-slate-500 mt-1">
              Hold Ctrl (Windows) or
              Command (Mac) to select
              multiple employees.
            </p>

            {/* Selected employees */}
            {assigneeIds.length >
              0 && (
              <div className="flex flex-wrap gap-2 mt-2">

                {assigneeIds.map(
                  (id) => {
                    const employee =
                      getEmployeeById(
                        id
                      );

                    return (
                      <span
                        key={id}
                        className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-indigo-500/10 border border-indigo-500/20 text-xs text-indigo-200"
                      >

                        {employee?.fullName ||
                          employee?.username ||
                          `Employee ${id}`}

                        <button
                          type="button"
                          onClick={() =>
                            removeAssignee(
                              id
                            )
                          }
                          className="text-slate-400 hover:text-rose-400"
                        >
                          <X className="w-3 h-3" />
                        </button>

                      </span>
                    );
                  }
                )}

              </div>
            )}

          </div>

          {/* -------------------------------------------------
              PRIORITY + ESTIMATED HOURS
          -------------------------------------------------- */}
          <div className="grid grid-cols-2 gap-3">

            <div className="form-group">

              <label className="form-label">
                Priority
              </label>

              <select
                value={priority}
                onChange={(e) =>
                  setPriority(
                    e.target.value
                  )
                }
                className="form-select text-xs"
              >

                <option value="LOW">
                  Low
                </option>

                <option value="MEDIUM">
                  Medium
                </option>

                <option value="HIGH">
                  High
                </option>

                <option value="CRITICAL">
                  Critical
                </option>

              </select>

            </div>

            <div className="form-group">

              <label className="form-label">
                Estimated Hours
              </label>

              <input
                type="number"
                min="0"
                step="0.5"
                value={
                  estimatedHours
                }
                onChange={(e) =>
                  setEstimatedHours(
                    e.target.value
                  )
                }
                className="form-input text-xs"
              />

            </div>

          </div>

          {/* -------------------------------------------------
              DUE DATE
          -------------------------------------------------- */}
          <div className="form-group">

            <label className="form-label">
              Due Date
            </label>

            <input
              type="date"
              value={dueDate}
              onChange={(e) =>
                setDueDate(
                  e.target.value
                )
              }
              className="form-input text-xs"
            />

          </div>

          {/* -------------------------------------------------
              DESCRIPTION
          -------------------------------------------------- */}
          <div className="form-group">

            <label className="form-label">
              Task Description
            </label>

            <textarea
              rows="3"
              placeholder="Task details, requirements, acceptance criteria..."
              value={description}
              onChange={(e) =>
                setDescription(
                  e.target.value
                )
              }
              className="form-textarea text-xs"
            />

          </div>

          {/* -------------------------------------------------
              BUTTONS
          -------------------------------------------------- */}
          <div className="flex justify-end gap-2 pt-2">

            <button
              type="button"
              onClick={
                closeCreateModal
              }
              className="btn btn-secondary btn-sm"
              disabled={submitting}
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={
                submitting ||
                !projectId ||
                !title.trim()
              }
              className="btn btn-primary btn-sm"
            >

              {submitting
                ? 'Creating...'
                : 'Create Task'}

            </button>

          </div>

        </form>

      </Modal>

    </div>
  );
};

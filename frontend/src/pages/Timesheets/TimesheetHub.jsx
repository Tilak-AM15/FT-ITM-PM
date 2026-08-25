import React, { useState, useEffect } from 'react';
import { timesheetApi, projectApi, taskApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import {
  Clock,
  Plus,
  Send,
  FileText,
  Grid3X3,
  Check,
} from 'lucide-react';

export const TimesheetHub = () => {
  const [timesheets, setTimesheets] = useState([]);
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  const [activeView, setActiveView] = useState('daily');

  // Daily Entry State
  const [projectId, setProjectId] = useState('');
  const [taskId, setTaskId] = useState('');
  const [workDate, setWorkDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [hours, setHours] = useState(8.0);
  const [billable, setBillable] = useState(true);
  const [desc, setDesc] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [toastMsg, setToastMsg] = useState('');
  const [editingId, setEditingId] = useState(null);

  // Weekly Matrix State
  const [weekStart, setWeekStart] = useState(() => {
    const today = new Date();
    const day = today.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    const monday = new Date(today);
    monday.setDate(today.getDate() + diff);
    return monday.toISOString().split('T')[0];
  });

  const [weeklyRows, setWeeklyRows] = useState([
    {
      projectId: '',
      taskId: '',
      billable: true,
      description: '',
      mondayHours: 0,
      tuesdayHours: 0,
      wednesdayHours: 0,
      thursdayHours: 0,
      fridayHours: 0,
      saturdayHours: 0,
      sundayHours: 0,
    },
  ]);

  /*
   * Load timesheets, projects and assigned tasks.
   *
   * IMPORTANT:
   * Some employees may have an assigned task even when the
   * /api/projects endpoint does not return that project directly.
   *
   * Therefore we build the project list from BOTH:
   *
   * 1. Projects returned by /api/projects
   * 2. Projects referenced by the employee's assigned tasks
   */
  const loadData = async () => {
    setLoading(true);

    const [timesheetResult, projectResult, taskResult] = await Promise.allSettled([
      timesheetApi.getMy(),
      projectApi.getAll(),
      taskApi.getMyTasks(),
    ]);

    const timesheetData =
      timesheetResult.status === 'fulfilled' && Array.isArray(timesheetResult.value?.data)
        ? timesheetResult.value.data
        : [];
    const projectData =
      projectResult.status === 'fulfilled' && Array.isArray(projectResult.value?.data)
        ? projectResult.value.data
        : [];
    const taskData =
      taskResult.status === 'fulfilled' && Array.isArray(taskResult.value?.data)
        ? taskResult.value.data
        : [];

    if (timesheetResult.status === 'rejected') console.error('Failed to load timesheets:', timesheetResult.reason);
    if (projectResult.status === 'rejected') console.error('Failed to load projects:', projectResult.reason);
    if (taskResult.status === 'rejected') console.error('Failed to load assigned tasks:', taskResult.reason);

    setTimesheets(timesheetData);
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

    const accessibleProjects = Array.from(projectMap.values());
    setProjects(accessibleProjects);

    console.log('Timesheet projects:', accessibleProjects);
    console.log('Timesheet tasks:', taskData);

    if (accessibleProjects.length > 0) {
      const firstProjectId = accessibleProjects[0].id;

      setProjectId((currentProjectId) => {
        const valid = accessibleProjects.some(
          (project) => String(project.id) === String(currentProjectId)
        );
        return valid ? currentProjectId : firstProjectId;
      });

      setWeeklyRows((currentRows) => {
        if (!currentRows?.length) {
          return [{
            projectId: firstProjectId, taskId: '', billable: true, description: '',
            mondayHours: 0, tuesdayHours: 0, wednesdayHours: 0, thursdayHours: 0,
            fridayHours: 0, saturdayHours: 0, sundayHours: 0,
          }];
        }
        if (currentRows[0]?.projectId) return currentRows;
        return [{ ...currentRows[0], projectId: firstProjectId }];
      });
    } else {
      setProjectId('');
      setTaskId('');
      setWeeklyRows((rows) => rows?.length ? rows : [{
        projectId: '', taskId: '', billable: true, description: '',
        mondayHours: 0, tuesdayHours: 0, wednesdayHours: 0, thursdayHours: 0,
        fridayHours: 0, saturdayHours: 0, sundayHours: 0,
      }]);
    }

    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  /*
   * Tasks belonging to the currently selected project.
   */
  const availableTasks = tasks.filter(
    (task) =>
      !projectId ||
      String(task.projectId) === String(projectId)
  );

  /*
   * Automatically select the first available task
   * whenever the selected project changes.
   */
  useEffect(() => {
    if (availableTasks.length > 0) {
      setTaskId(
        String(availableTasks[0].id)
      );
    } else {
      setTaskId('');
    }
  }, [projectId, tasks]);

  /*
   * ---------------------------------------------------------
   * DAILY TIMESHEET
   * ---------------------------------------------------------
   */
  const handleSaveDaily = async (
    submitNow = false
  ) => {
    if (
      !projectId ||
      !taskId ||
      !hours
    ) {
      return;
    }

    setSubmitting(true);

    try {
      await timesheetApi.save({
        ...(editingId
          ? { id: editingId }
          : {}),

        projectId: Number(projectId),
        taskId: Number(taskId),

        workDate,

        hoursWorked: Number(hours),

        billable,

        description:
          desc ||
          'Engineering execution',

        remarks,

        status: submitNow
          ? 'SUBMITTED'
          : 'DRAFT',
      });

      setToastMsg(
        submitNow
          ? 'Timesheet submitted for review!'
          : 'Saved as Draft.'
      );

      setDesc('');
      setRemarks('');
      setEditingId(null);

      setTimeout(() => {
        setToastMsg('');
      }, 3000);

      await loadData();
    } catch (err) {
      console.error(
        'Failed to save daily timesheet:',
        err
      );
    } finally {
      setSubmitting(false);
    }
  };

  /*
   * ---------------------------------------------------------
   * WEEKLY TIMESHEET
   * ---------------------------------------------------------
   */
  const handleSaveWeekly = async (
    submitNow = false
  ) => {
    setSubmitting(true);

    try {
      await timesheetApi.saveWeekly({
        weekStartDate: weekStart,
        rows: weeklyRows,
        submitForApproval: submitNow,
      });

      setToastMsg(
        submitNow
          ? 'Weekly timesheet submitted for approval!'
          : 'Weekly grid saved.'
      );

      setTimeout(() => {
        setToastMsg('');
      }, 3000);

      await loadData();
    } catch (err) {
      console.error(
        'Failed to save weekly grid:',
        err
      );
    } finally {
      setSubmitting(false);
    }
  };

  /*
   * Add another row to the weekly grid.
   */
  const handleAddWeeklyRow = () => {
    setWeeklyRows([
      ...weeklyRows,
      {
        projectId:
          projects[0]?.id || '',
        taskId:
          tasks[0]?.id || '',
        billable: true,
        description: '',
        mondayHours: 0,
        tuesdayHours: 0,
        wednesdayHours: 0,
        thursdayHours: 0,
        fridayHours: 0,
        saturdayHours: 0,
        sundayHours: 0,
      },
    ]);
  };

  /*
   * Edit an existing timesheet.
   */
  const handleEdit = (ts) => {
    setEditingId(ts.id);

    setProjectId(
      ts.projectId != null
        ? String(ts.projectId)
        : ''
    );

    setTaskId(
      ts.taskId != null
        ? String(ts.taskId)
        : ''
    );

    setWorkDate(
      ts.workDate ||
      new Date()
        .toISOString()
        .split('T')[0]
    );

    setHours(
      ts.hoursWorked ?? 8
    );

    setBillable(
      ts.billable !== false
    );

    setDesc(
      ts.description || ''
    );

    setRemarks(
      ts.remarks || ''
    );

    setActiveView('daily');
  };

  /*
   * Delete a timesheet.
   */
  const handleDelete = async (id) => {
    try {
      await timesheetApi.delete(id);

      await loadData();
    } catch (err) {
      console.error(
        'Failed to delete entry:',
        err
      );
    }
  };

  return (
    <div className="space-y-6">

      {/* =====================================================
          HEADER
      ====================================================== */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">

        <div>
          <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">
            Timesheet Management
          </h1>

          <p className="text-xs text-slate-400 mt-0.5">
            Capture billable/non-billable hours,
            review historical logs, and submit
            for approvals.
          </p>
        </div>

        {/* View Switcher */}
        <div className="flex items-center gap-1.5 p-1 rounded-xl bg-slate-900 border border-white/5">

          <button
            onClick={() =>
              setActiveView('daily')
            }
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
              activeView === 'daily'
                ? 'bg-indigo-600 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Clock className="w-3.5 h-3.5" />
            <span>Daily Entry</span>
          </button>

          <button
            onClick={() =>
              setActiveView('weekly')
            }
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
              activeView === 'weekly'
                ? 'bg-indigo-600 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Grid3X3 className="w-3.5 h-3.5" />
            <span>Weekly Grid</span>
          </button>

          <button
            onClick={() =>
              setActiveView('history')
            }
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
              activeView === 'history'
                ? 'bg-indigo-600 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <FileText className="w-3.5 h-3.5" />
            <span>History & Status</span>
          </button>

        </div>
      </div>

      {/* =====================================================
          TOAST
      ====================================================== */}
      {toastMsg && (
        <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs font-semibold flex items-center gap-2 animate-in fade-in">
          <Check className="w-4 h-4" />
          <span>{toastMsg}</span>
        </div>
      )}

      {/* =====================================================
          LOADING
      ====================================================== */}
      {loading && (
        <div className="glass-card p-4 text-center">
          <p className="text-xs text-slate-400">
            Loading timesheet data...
          </p>
        </div>
      )}

      {/* =====================================================
          DAILY ENTRY
      ====================================================== */}
      {!loading &&
        activeView === 'daily' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">

            {/* Daily Form */}
            <div className="lg:col-span-6 glass-card p-6 space-y-4">

              <h2 className="text-base font-bold text-white font-heading">
                {editingId
                  ? 'Edit Timesheet Entry'
                  : 'Log Daily Effort'}
              </h2>

              <div className="space-y-4">

                {/* Project */}
                <div className="form-group">

                  <label className="form-label">
                    Project *
                  </label>

                  <select
                    value={projectId}
                    onChange={(e) => {
                      setProjectId(
                        e.target.value
                      );
                    }}
                    className="form-select text-xs"
                    disabled={
                      projects.length === 0
                    }
                  >

                    <option value="">
                      {projects.length === 0
                        ? 'No projects available'
                        : 'Select Project'}
                    </option>

                    {projects.map((project) => (
                      <option
                        key={project.id}
                        value={project.id}
                      >
                        {project.name}
                        {project.projectCode
                          ? ` (${project.projectCode})`
                          : ''}
                      </option>
                    ))}

                  </select>

                  {projects.length === 0 && (
                    <p className="text-[11px] text-amber-400 mt-1">
                      No projects are available
                      for timesheet entry.
                      Make sure a task is assigned
                      to you.
                    </p>
                  )}

                </div>

                {/* Task */}
                <div className="form-group">

                  <label className="form-label">
                    Task *
                  </label>

                  <select
                    value={taskId}
                    onChange={(e) =>
                      setTaskId(
                        e.target.value
                      )
                    }
                    className="form-select text-xs"
                    disabled={
                      availableTasks.length === 0
                    }
                  >

                    <option value="">
                      {availableTasks.length === 0
                        ? 'No tasks assigned'
                        : 'Select Task'}
                    </option>

                    {availableTasks.map((task) => (
                      <option
                        key={task.id}
                        value={task.id}
                      >
                        {task.title}
                        {task.taskCode
                          ? ` (${task.taskCode})`
                          : ''}
                      </option>
                    ))}

                  </select>

                </div>

                {/* Date / Hours / Billable */}
                <div className="grid grid-cols-3 gap-3">

                  <div className="form-group">

                    <label className="form-label">
                      Date *
                    </label>

                    <input
                      type="date"
                      value={workDate}
                      onChange={(e) =>
                        setWorkDate(
                          e.target.value
                        )
                      }
                      className="form-input text-xs"
                    />

                  </div>

                  <div className="form-group">

                    <label className="form-label">
                      Hours *
                    </label>

                    <input
                      type="number"
                      step="0.5"
                      min="0.5"
                      max="24"
                      value={hours}
                      onChange={(e) =>
                        setHours(
                          e.target.value
                        )
                      }
                      className="form-input text-xs"
                    />

                  </div>

                  <div className="form-group">

                    <label className="form-label">
                      Billable
                    </label>

                    <select
                      value={
                        billable
                          ? 'true'
                          : 'false'
                      }
                      onChange={(e) =>
                        setBillable(
                          e.target.value ===
                            'true'
                        )
                      }
                      className="form-select text-xs"
                    >
                      <option value="true">
                        Yes
                      </option>

                      <option value="false">
                        No
                      </option>
                    </select>

                  </div>

                </div>

                {/* Description */}
                <div className="form-group">

                  <label className="form-label">
                    Work Description *
                  </label>

                  <textarea
                    rows="3"
                    placeholder="Details on what activities and deliverables were completed..."
                    value={desc}
                    onChange={(e) =>
                      setDesc(
                        e.target.value
                      )
                    }
                    className="form-textarea text-xs"
                  />

                </div>

                {/* Remarks */}
                <div className="form-group">

                  <label className="form-label">
                    Remarks / Blockers
                    (Optional)
                  </label>

                  <input
                    type="text"
                    placeholder="e.g. Dependent on staging environment refresh"
                    value={remarks}
                    onChange={(e) =>
                      setRemarks(
                        e.target.value
                      )
                    }
                    className="form-input text-xs"
                  />

                </div>

                {/* Buttons */}
                <div className="flex justify-end gap-2 pt-2">

                  <button
                    type="button"
                    disabled={
                      submitting ||
                      !projectId ||
                      !taskId
                    }
                    onClick={() =>
                      handleSaveDaily(false)
                    }
                    className="btn btn-secondary btn-sm"
                  >
                    Save Draft
                  </button>

                  <button
                    type="button"
                    disabled={
                      submitting ||
                      !projectId ||
                      !taskId
                    }
                    onClick={() =>
                      handleSaveDaily(true)
                    }
                    className="btn btn-primary btn-sm"
                  >

                    <Send className="w-3.5 h-3.5" />

                    <span>
                      {editingId
                        ? 'Resubmit for Review'
                        : 'Submit for Review'}
                    </span>

                  </button>

                </div>

              </div>
            </div>

            {/* Recent Activity */}
            <div className="lg:col-span-6 glass-card p-6 space-y-4">

              <h2 className="text-base font-bold text-white font-heading">
                Recent Timesheet Entries
              </h2>

              <div className="space-y-3 max-h-[460px] overflow-y-auto">

                {timesheets.length === 0 ? (
                  <p className="text-xs text-slate-500 py-6 text-center">
                    No timesheets recorded.
                  </p>
                ) : (
                  timesheets.map((ts) => (
                    <div
                      key={ts.id}
                      className="p-3.5 rounded-xl bg-slate-900/60 border border-white/5 space-y-2"
                    >

                      <div className="flex items-start justify-between">

                        <div>

                          <span className="text-xs font-bold text-white">
                            {ts.taskTitle}
                          </span>

                          <p className="text-[10px] text-slate-400">
                            {ts.projectName}
                            {' • '}
                            {ts.workDate}
                          </p>

                        </div>

                        <StatusBadge
                          status={ts.status}
                        />

                      </div>

                      <p className="text-xs text-slate-300 line-clamp-2">
                        {ts.description}
                      </p>

                      {ts.rejectionReason && (
                        <div className="p-2 rounded bg-rose-500/10 border border-rose-500/20 text-rose-300 text-[11px]">
                          <strong>
                            Reviewer note:
                          </strong>{' '}
                          {ts.rejectionReason}
                        </div>
                      )}

                      <div className="flex items-center justify-between pt-2 border-t border-white/5 text-[11px] text-slate-400">

                        <span className="font-bold text-white">
                          {ts.hoursWorked}h
                          {' '}
                          (
                          {ts.billable
                            ? 'Billable'
                            : 'Non-Billable'}
                          )
                        </span>

                        <div className="flex items-center gap-3">

                          {(
                            ts.status ===
                              'DRAFT' ||
                            ts.status ===
                              'REJECTED' ||
                            ts.status ===
                              'UNDER_REVIEW'
                          ) && (
                            <button
                              onClick={() =>
                                handleEdit(ts)
                              }
                              className="text-indigo-400 hover:text-indigo-300 font-semibold text-xs"
                            >
                              {ts.status ===
                              'DRAFT'
                                ? 'Edit Draft'
                                : 'Edit & Resubmit'}
                            </button>
                          )}

                          {(
                            ts.status ===
                              'DRAFT' ||
                            ts.status ===
                              'REJECTED'
                          ) && (
                            <button
                              onClick={() =>
                                handleDelete(
                                  ts.id
                                )
                              }
                              className="text-rose-400 hover:text-rose-300 font-semibold text-xs"
                            >
                              Delete
                            </button>
                          )}

                        </div>

                      </div>

                    </div>
                  ))
                )}

              </div>

            </div>

          </div>
        )}

      {/* =====================================================
          WEEKLY GRID
      ====================================================== */}
      {!loading &&
        activeView === 'weekly' && (
          <div className="glass-card p-6 space-y-4">

            <div className="flex items-center justify-between pb-3 border-b border-white/5">

              <div>

                <h2 className="text-base font-bold text-white font-heading">
                  Weekly Timesheet Grid
                  (7-Day Matrix)
                </h2>

                <p className="text-xs text-slate-400">
                  Log hours across Monday
                  through Sunday in a single
                  high-speed matrix view.
                </p>

              </div>

              <div className="flex items-center gap-3">

                <input
                  type="date"
                  value={weekStart}
                  onChange={(e) =>
                    setWeekStart(
                      e.target.value
                    )
                  }
                  className="form-input text-xs py-1"
                />

                <button
                  onClick={
                    handleAddWeeklyRow
                  }
                  className="btn btn-secondary btn-sm"
                >

                  <Plus className="w-3.5 h-3.5" />

                  <span>
                    Add Row
                  </span>

                </button>

              </div>

            </div>

            <div className="table-container">

              <table className="modern-table">

                <thead>
                  <tr>

                    <th className="w-48">
                      Project
                    </th>

                    <th className="w-48">
                      Task
                    </th>

                    <th>Mon</th>
                    <th>Tue</th>
                    <th>Wed</th>
                    <th>Thu</th>
                    <th>Fri</th>
                    <th>Sat</th>
                    <th>Sun</th>

                    <th>Total</th>

                  </tr>
                </thead>

                <tbody>

                  {weeklyRows.map(
                    (row, idx) => {

                      const rowTotal =
                        (Number(
                          row.mondayHours
                        ) || 0) +
                        (Number(
                          row.tuesdayHours
                        ) || 0) +
                        (Number(
                          row.wednesdayHours
                        ) || 0) +
                        (Number(
                          row.thursdayHours
                        ) || 0) +
                        (Number(
                          row.fridayHours
                        ) || 0) +
                        (Number(
                          row.saturdayHours
                        ) || 0) +
                        (Number(
                          row.sundayHours
                        ) || 0);

                      const rowTasks =
                        tasks.filter(
                          (task) =>
                            !row.projectId ||
                            String(
                              task.projectId
                            ) ===
                              String(
                                row.projectId
                              )
                        );

                      return (
                        <tr key={idx}>

                          {/* Project */}
                          <td>

                            <select
                              value={
                                row.projectId
                              }
                              onChange={(e) => {

                                const updated =
                                  [
                                    ...weeklyRows,
                                  ];

                                updated[
                                  idx
                                ] = {
                                  ...updated[
                                    idx
                                  ],
                                  projectId:
                                    e.target
                                      .value,
                                  taskId:
                                    '',
                                };

                                setWeeklyRows(
                                  updated
                                );
                              }}
                              className="form-select text-xs py-1"
                            >

                              <option value="">
                                Select Project
                              </option>

                              {projects.map(
                                (project) => (
                                  <option
                                    key={
                                      project.id
                                    }
                                    value={
                                      project.id
                                    }
                                  >
                                    {
                                      project.name
                                    }
                                    {project.projectCode
                                      ? ` (${project.projectCode})`
                                      : ''}
                                  </option>
                                )
                              )}

                            </select>

                          </td>

                          {/* Task */}
                          <td>

                            <select
                              value={
                                row.taskId
                              }
                              onChange={(e) => {

                                const updated =
                                  [
                                    ...weeklyRows,
                                  ];

                                updated[
                                  idx
                                ] = {
                                  ...updated[
                                    idx
                                  ],
                                  taskId:
                                    e.target
                                      .value,
                                };

                                setWeeklyRows(
                                  updated
                                );
                              }}
                              className="form-select text-xs py-1"
                              disabled={
                                rowTasks.length ===
                                0
                              }
                            >

                              <option value="">
                                {rowTasks.length ===
                                0
                                  ? 'No tasks assigned'
                                  : 'Select Task'}
                              </option>

                              {rowTasks.map(
                                (task) => (
                                  <option
                                    key={
                                      task.id
                                    }
                                    value={
                                      task.id
                                    }
                                  >
                                    {
                                      task.title
                                    }
                                  </option>
                                )
                              )}

                            </select>

                          </td>

                          {/* Daily Hours */}
                          {[
                            'mondayHours',
                            'tuesdayHours',
                            'wednesdayHours',
                            'thursdayHours',
                            'fridayHours',
                            'saturdayHours',
                            'sundayHours',
                          ].map(
                            (dayKey) => (
                              <td
                                key={
                                  dayKey
                                }
                              >

                                <input
                                  type="number"
                                  step="0.5"
                                  min="0"
                                  max="24"
                                  value={
                                    row[
                                      dayKey
                                    ]
                                  }
                                  onChange={(
                                    e
                                  ) => {

                                    const updated =
                                      [
                                        ...weeklyRows,
                                      ];

                                    updated[
                                      idx
                                    ] = {
                                      ...updated[
                                        idx
                                      ],
                                      [dayKey]:
                                        parseFloat(
                                          e
                                            .target
                                            .value
                                        ) ||
                                        0,
                                    };

                                    setWeeklyRows(
                                      updated
                                    );
                                  }}
                                  className="form-input text-xs text-center py-1 px-1 w-14"
                                />

                              </td>
                            )
                          )}

                          <td className="font-bold text-indigo-400 text-xs">
                            {rowTotal}h
                          </td>

                        </tr>
                      );
                    }
                  )}

                </tbody>

              </table>

            </div>

            <div className="flex justify-end gap-2 pt-4 border-t border-white/5">

              <button
                onClick={() =>
                  handleSaveWeekly(false)
                }
                disabled={submitting}
                className="btn btn-secondary btn-sm"
              >
                Save Weekly Draft
              </button>

              <button
                onClick={() =>
                  handleSaveWeekly(true)
                }
                disabled={submitting}
                className="btn btn-primary btn-sm"
              >

                <Send className="w-3.5 h-3.5" />

                <span>
                  Submit Week For Approval
                </span>

              </button>

            </div>

          </div>
        )}

      {/* =====================================================
          HISTORY
      ====================================================== */}
      {!loading &&
        activeView === 'history' && (
          <div className="glass-card p-6 space-y-4">

            <div className="flex items-center justify-between">

              <h2 className="text-base font-bold text-white font-heading">
                Historical Timesheet
                Submissions
              </h2>

              <span className="text-xs text-slate-400 font-semibold">
                {timesheets.length}{' '}
                Total Records
              </span>

            </div>

            <div className="table-container">

              <table className="modern-table">

                <thead>

                  <tr>
                    <th>Date</th>
                    <th>Project</th>
                    <th>Task</th>
                    <th>Hours</th>
                    <th>Billable</th>
                    <th>Status</th>
                    <th>Reviewer</th>
                    <th>Notes</th>
                  </tr>

                </thead>

                <tbody>

                  {timesheets.map(
                    (ts) => (
                      <tr key={ts.id}>

                        <td className="font-mono text-xs text-slate-300">
                          {ts.workDate}
                        </td>

                        <td className="font-semibold text-xs text-slate-200">
                          {ts.projectName}
                        </td>

                        <td className="text-xs text-slate-300">
                          {ts.taskTitle}
                        </td>

                        <td className="text-xs font-bold text-white">
                          {ts.hoursWorked}h
                        </td>

                        <td>

                          <span
                            className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                              ts.billable
                                ? 'bg-emerald-500/20 text-emerald-300'
                                : 'bg-slate-700 text-slate-300'
                            }`}
                          >
                            {ts.billable
                              ? 'Yes'
                              : 'No'}
                          </span>

                        </td>

                        <td>
                          <StatusBadge
                            status={
                              ts.status
                            }
                          />
                        </td>

                        <td className="text-xs text-slate-400">
                          {ts.reviewer
                            ?.fullName ||
                            '—'}
                        </td>

                        <td className="text-xs text-slate-400 max-w-xs truncate">
                          {ts.description}
                        </td>

                      </tr>
                    )
                  )}

                </tbody>

              </table>

            </div>

          </div>
        )}

    </div>
  );
};

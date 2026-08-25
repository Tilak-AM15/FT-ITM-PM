import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dashboardApi, projectApi, taskApi, timesheetApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import {
  Clock,
  Briefcase,
  CheckCircle2,
  AlertCircle,
  Plus,
  Send,
  Trash2,
  Edit2,
  Calendar,
  Check,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const EmployeeDashboard = () => {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  // Time Entry Form State
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [selectedTaskId, setSelectedTaskId] = useState('');
  const [workDate, setWorkDate] = useState(new Date().toISOString().split('T')[0]);
  const [hoursWorked, setHoursWorked] = useState(4.0);
  const [billable, setBillable] = useState(true);
  const [workDescription, setWorkDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const navigate = useNavigate();

  const loadData = async () => {
    try {
      const [dashRes, prjRes, taskRes] = await Promise.all([
        dashboardApi.getEmployee(),
        projectApi.getAll(),
        taskApi.getMyTasks(),
      ]);
      setData(dashRes.data);
      setProjects(prjRes.data || []);
      setTasks(taskRes.data || []);

      if (prjRes.data && prjRes.data.length > 0) {
        setSelectedProjectId(prjRes.data[0].id);
      }
    } catch (err) {
      console.error('Failed to load employee dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [user?.id]);

  // Filter tasks based on selected project
  const availableTasks = tasks.filter((t) => !selectedProjectId || String(t.projectId) === String(selectedProjectId));

  useEffect(() => {
    if (availableTasks.length > 0) {
      setSelectedTaskId(availableTasks[0].id);
    } else {
      setSelectedTaskId('');
    }
  }, [selectedProjectId, tasks]);

  const handleAddEntry = async (e) => {
    e.preventDefault();
    if (!selectedProjectId || !selectedTaskId || !hoursWorked) return;

    setSubmitting(true);
    try {
      await timesheetApi.save({
        projectId: selectedProjectId,
        taskId: selectedTaskId,
        workDate,
        hoursWorked: parseFloat(hoursWorked),
        billable,
        description: workDescription || 'General engineering work',
        status: 'DRAFT',
      });
      setSuccessMsg('Timesheet entry saved to Draft!');
      setWorkDescription('');
      setTimeout(() => setSuccessMsg(''), 3000);
      loadData();
    } catch (err) {
      console.error('Failed to save timesheet entry:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await timesheetApi.delete(id);
      loadData();
    } catch (err) {
      console.error('Failed to delete entry:', err);
    }
  };

  const handleSubmitAllDrafts = async () => {
    const drafts = (data?.recentTimesheets || []).filter((t) => t.status === 'DRAFT');
    for (const d of drafts) {
      await timesheetApi.save({
        id: d.id,
        projectId: d.projectId,
        taskId: d.taskId,
        workDate: d.workDate,
        hoursWorked: d.hoursWorked,
        billable: d.billable,
        description: d.description,
        status: 'SUBMITTED',
      });
    }
    setSuccessMsg('All drafts submitted for Project Manager review!');
    setTimeout(() => setSuccessMsg(''), 4000);
    loadData();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
      </div>
    );
  }

  const entries = data?.recentTimesheets || [];
  const totalEntriesHours = entries.reduce((acc, curr) => acc + (curr.hoursWorked || 0), 0);

  return (
    <div className="space-y-6">
      {/* Header matching OCR page 9 */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">My Timesheet</h1>
          <p className="text-xs text-slate-400 mt-0.5">Log your daily work hours and manage assigned tasks.</p>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs text-slate-300">
            <Calendar className="w-3.5 h-3.5 text-indigo-400" />
            <span>{new Date().toLocaleDateString(undefined, { day: '2-digit', month: 'short', year: 'numeric' })}</span>
          </div>

          <button
            onClick={handleSubmitAllDrafts}
            className="btn btn-primary btn-sm"
          >
            <Send className="w-3.5 h-3.5" />
            <span>Submit Timesheet</span>
          </button>
        </div>
      </div>

      {successMsg && (
        <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs font-semibold flex items-center gap-2 animate-in fade-in">
          <Check className="w-4 h-4" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* 4 Summary Stat Cards matching OCR Page 9 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-500/15 border border-indigo-500/30 text-indigo-400 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Total Hours</span>
            <span className="text-2xl font-black text-white">{data?.thisWeekHours ?? 0}h</span>
          </div>
        </div>

        <div className="glass-card p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Billable Hours</span>
            <span className="text-2xl font-black text-emerald-400">{data?.thisWeekBillableHours ?? 0}h</span>
          </div>
        </div>

        <div className="glass-card p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-amber-500/15 border border-amber-500/30 text-amber-400 flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Non-Billable</span>
            <span className="text-2xl font-black text-amber-400">{data?.thisWeekNonBillableHours ?? 0}h</span>
          </div>
        </div>

        <div className="glass-card p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-500/15 border border-cyan-500/30 text-cyan-400 flex items-center justify-center">
            <Briefcase className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Timesheet Status</span>
            <span className="text-xl font-bold text-white">Draft / Review</span>
          </div>
        </div>
      </div>

      {/* Add Time Entry Form Component matching OCR Page 9 */}
      <div className="glass-card p-6 space-y-4">
        <h2 className="text-base font-bold text-white font-heading">Add Time Entry</h2>

        <form onSubmit={handleAddEntry} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
            <div>
              <label className="form-label">Project *</label>
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="form-select text-xs"
                required
              >
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="form-label">Task *</label>
              <select
                value={selectedTaskId}
                onChange={(e) => setSelectedTaskId(e.target.value)}
                className="form-select text-xs"
                required
              >
                {availableTasks.length === 0 ? (
                  <option value="">No tasks assigned</option>
                ) : (
                  availableTasks.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.title}
                    </option>
                  ))
                )}
              </select>
            </div>

            <div>
              <label className="form-label">Date *</label>
              <input
                type="date"
                value={workDate}
                onChange={(e) => setWorkDate(e.target.value)}
                className="form-input text-xs"
                required
              />
            </div>

            <div>
              <label className="form-label">Hours *</label>
              <input
                type="number"
                step="0.5"
                min="0.5"
                max="24"
                value={hoursWorked}
                onChange={(e) => setHoursWorked(e.target.value)}
                className="form-input text-xs"
                required
              />
            </div>

            <div>
              <label className="form-label">Billable *</label>
              <select
                value={billable ? 'true' : 'false'}
                onChange={(e) => setBillable(e.target.value === 'true')}
                className="form-select text-xs"
              >
                <option value="true">Yes (Billable)</option>
                <option value="false">No (Internal / Non-billable)</option>
              </select>
            </div>
          </div>

          <div>
            <label className="form-label">Work Description *</label>
            <input
              type="text"
              placeholder="e.g. Worked on API mapping, authentication module and data validation."
              value={workDescription}
              onChange={(e) => setWorkDescription(e.target.value)}
              className="form-input text-xs"
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => { setWorkDescription(''); setHoursWorked(4); }}
              className="btn btn-secondary btn-sm"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || !selectedTaskId}
              className="btn btn-primary btn-sm"
            >
              <Plus className="w-4 h-4" />
              <span>{submitting ? 'Adding...' : 'Add Entry'}</span>
            </button>
          </div>
        </form>
      </div>

      {/* This Week Entries Table matching OCR Page 9 */}
      <div className="glass-card p-6 space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-white font-heading">This Week Entries</h2>
          <span className="text-xs text-slate-400 font-semibold">{entries.length} Entries Logged</span>
        </div>

        <div className="table-container">
          <table className="modern-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Project</th>
                <th>Task</th>
                <th>Work Description</th>
                <th>Hours</th>
                <th>Billable</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {entries.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center text-slate-500 py-6 text-xs">
                    No timesheets logged yet this week. Use the form above to add your first entry!
                  </td>
                </tr>
              ) : (
                entries.map((item) => (
                  <tr key={item.id}>
                    <td className="text-xs text-slate-300 font-mono">{item.workDate}</td>
                    <td className="text-xs font-semibold text-slate-200">{item.projectName}</td>
                    <td className="text-xs text-slate-300">{item.taskTitle}</td>
                    <td className="text-xs text-slate-400 max-w-xs truncate">{item.description}</td>
                    <td className="text-xs font-bold text-white">{item.hoursWorked}h</td>
                    <td>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${item.billable ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-700 text-slate-300'}`}>
                        {item.billable ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td>
                      <StatusBadge status={item.status} />
                    </td>
                    <td>
                      <div className="flex items-center gap-1.5">
                        {item.status === 'DRAFT' && (
                          <button
                            onClick={() => handleDelete(item.id)}
                            className="p-1 rounded hover:bg-rose-950/50 text-rose-400 hover:text-rose-300 transition-colors"
                            title="Delete entry"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
            {entries.length > 0 && (
              <tfoot>
                <tr className="border-t-2 border-white/10 font-bold">
                  <td colSpan="4" className="text-right text-xs text-slate-300">Total Logged:</td>
                  <td className="text-xs text-indigo-400 font-extrabold">{totalEntriesHours}h</td>
                  <td colSpan="3"></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      </div>
    </div>
  );
};

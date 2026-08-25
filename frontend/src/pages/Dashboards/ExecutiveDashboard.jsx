import React, { useEffect, useState } from 'react';
import { dashboardApi } from '../../api/endpoints';
import { useAuth } from '../../context/AuthContext';
import { StatCard } from '../../components/StatCard';
import { StatusBadge } from '../../components/StatusBadge';
import {
  FolderKanban,
  CheckSquare,
  Clock,
  AlertOctagon,
  Users,
  TrendingUp,
  Plus,
  Calendar,
  ChevronRight,
  ShieldAlert,
  ArrowUpRight,
  Sparkles,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ExecutiveDashboard = ({ onOpenProjectModal, onOpenTimeModal, scope = 'management' }) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    const load = async () => {
      try {
        const res = await (scope === 'pm' ? dashboardApi.getProjectManager() : dashboardApi.getManagement());
        setData(res.data);
      } catch (err) {
        console.error('Failed to load dashboard:', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [scope]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
      </div>
    );
  }

  const taskOverview = data?.taskOverview || { totalTasks: 0, todoCount: 0, inProgressCount: 0, blockedCount: 0, completedCount: 0 };
  const portfolio = data?.portfolioProjects || [];
  const workloads = data?.teamWorkloads || [];

  return (
    <div className="space-y-6">
      {/* Top Banner / Greeting matching OCR mockup */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">Good Morning, {user?.fullName || 'there'} 👋</h1>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">{scope === 'pm' ? 'Here is an overview of your assigned projects, team workload, and delivery health.' : 'Here is an overview of your organization’s projects and resource allocation.'}</p>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs text-slate-300">
            <Calendar className="w-3.5 h-3.5 text-indigo-400" />
            <span>{new Date().toLocaleDateString(undefined, { day: '2-digit', month: 'short', year: 'numeric' })}</span>
          </div>

          {onOpenProjectModal && (
            <button onClick={onOpenProjectModal} className="btn btn-primary btn-sm">
              <Plus className="w-4 h-4" />
              <span>New Project</span>
            </button>
          )}
        </div>
      </div>

      {/* Top Stat Cards matching OCR Page 3 & 7 */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard
          title="Total Projects"
          value={data?.totalProjects ?? 0}
          subtitle={`${data?.activeProjects ?? 0} Active`}
          icon={FolderKanban}
          color="#6366f1"
          progress={data?.totalProjects ? Math.round((data.activeProjects / data.totalProjects) * 100) : 0}
        />
        <StatCard
          title="Total Tasks"
          value={taskOverview.totalTasks ?? 0}
          subtitle={`${taskOverview.todoCount + taskOverview.inProgressCount} Open`}
          icon={CheckSquare}
          color="#06b6d4"
          progress={taskOverview.totalTasks ? Math.round(((taskOverview.inProgressCount + taskOverview.completedCount) / taskOverview.totalTasks) * 100) : 0}
        />
        <StatCard
          title="Total Hours"
          value={`${data?.totalActualHours ?? 0}h`}
          subtitle={`${data?.totalActualHours ? Math.round((data.totalBillableHours / data.totalActualHours) * 100) : 0}% Billable`}
          icon={Clock}
          color="#8b5cf6"
          trend="+12.4%"
        />
        <StatCard
          title="Overdue Tasks"
          value={data?.delayedProjects ?? 0}
          subtitle="High Priority"
          icon={AlertOctagon}
          color="#ef4444"
        />
        <StatCard
          title="Team Utilization"
          value={`${data?.overallUtilizationPercentage ?? 0}%`}
          subtitle="Good Health"
          icon={Users}
          color="#10b981"
          progress={data?.overallUtilizationPercentage ?? 0}
        />
      </div>

      {/* Main 2-Column Section: Project Portfolio & Task Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Col (8): Project Portfolio Table */}
        <div className="lg:col-span-8 glass-card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-bold text-white font-heading">Project Portfolio</h2>
              <p className="text-xs text-slate-400">Live delivery metrics, logged hours, and end dates.</p>
            </div>
            <button onClick={() => navigate('/projects')} className="text-xs text-indigo-400 hover:text-indigo-300 font-semibold flex items-center gap-1">
              <span>View All</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Project</th>
                  <th>Progress</th>
                  <th>Tasks</th>
                  <th>Hours</th>
                  <th>Status</th>
                  <th>End Date</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.slice(0, 5).map((p) => (
                  <tr key={p.id} className="cursor-pointer hover:bg-slate-800/40" onClick={() => navigate(`/projects/${p.id}`)}>
                    <td>
                      <div className="font-semibold text-slate-200 text-xs">{p.name}</div>
                      <div className="text-[10px] text-slate-400">{p.clientName} • {p.projectCode}</div>
                    </td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="w-16 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div className="bg-indigo-500 h-full rounded-full" style={{ width: `${p.progressPercentage}%` }} />
                        </div>
                        <span className="text-xs font-semibold text-slate-300">{p.progressPercentage}%</span>
                      </div>
                    </td>
                    <td>
                      <span className="text-xs text-slate-200">{p.totalTasks}</span>
                      <span className="text-[10px] text-slate-400 block">{p.openTasks} Open</span>
                    </td>
                    <td>
                      <span className="text-xs text-slate-200">{p.loggedHours}h</span>
                      <span className="text-[10px] text-slate-400 block">{p.billablePercentage}% Billable</span>
                    </td>
                    <td>
                      <StatusBadge status={p.status} />
                    </td>
                    <td className="text-xs text-slate-400">
                      {p.endDate}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right Col (4): Task Overview Donut & Workload */}
        <div className="lg:col-span-4 space-y-6">
          {/* Task Overview Breakdown */}
          <div className="glass-card p-5 space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-bold text-white font-heading">Tasks Overview</h2>
              <span className="text-xs font-bold text-slate-400">{taskOverview.totalTasks} Total</span>
            </div>

            {/* Visual Task Distribution */}
            <div className="flex h-4 w-full rounded-full overflow-hidden gap-1 my-2 bg-slate-900 p-0.5 border border-white/5">
              <div style={{ width: `${(taskOverview.todoCount / taskOverview.totalTasks) * 100}%` }} className="bg-slate-500 rounded-sm" title="To Do" />
              <div style={{ width: `${(taskOverview.inProgressCount / taskOverview.totalTasks) * 100}%` }} className="bg-cyan-500 rounded-sm" title="In Progress" />
              <div style={{ width: `${(taskOverview.completedCount / taskOverview.totalTasks) * 100}%` }} className="bg-emerald-500 rounded-sm" title="Completed" />
              <div style={{ width: `${(taskOverview.blockedCount / taskOverview.totalTasks) * 100}%` }} className="bg-rose-500 rounded-sm" title="Blocked" />
            </div>

            <div className="grid grid-cols-2 gap-2 pt-2 text-xs">
              <div className="p-2 rounded-lg bg-slate-900/60 border border-white/5 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-slate-400" />
                  <span className="text-slate-300">To Do</span>
                </div>
                <span className="font-bold text-white">{taskOverview.todoCount}</span>
              </div>
              <div className="p-2 rounded-lg bg-slate-900/60 border border-white/5 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-cyan-400" />
                  <span className="text-slate-300">In Progress</span>
                </div>
                <span className="font-bold text-white">{taskOverview.inProgressCount}</span>
              </div>
              <div className="p-2 rounded-lg bg-slate-900/60 border border-white/5 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-emerald-400" />
                  <span className="text-slate-300">Completed</span>
                </div>
                <span className="font-bold text-white">{taskOverview.completedCount}</span>
              </div>
              <div className="p-2 rounded-lg bg-slate-900/60 border border-white/5 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-rose-400" />
                  <span className="text-slate-300">Blocked</span>
                </div>
                <span className="font-bold text-white">{taskOverview.blockedCount}</span>
              </div>
            </div>
          </div>

          {/* Project Health Index */}
          <div className="glass-card p-5 space-y-3">
            <h2 className="text-base font-bold text-white font-heading">Project Health Summary</h2>
            <div className="space-y-2.5 text-xs">
              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>Budget vs Actual</span>
                  <span className="font-semibold text-emerald-400">{data?.projectHealth?.budgetVsActual ?? 0}%</span>
                </div>
                <div className="progress-track"><div className="progress-fill bg-emerald-500" style={{ width: `${data?.projectHealth?.budgetVsActual ?? 0}%` }} /></div>
              </div>
              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>Schedule Performance</span>
                  <span className="font-semibold text-cyan-400">{data?.projectHealth?.qualityScore ?? 0}%</span>
                </div>
                <div className="progress-track"><div className="progress-fill bg-cyan-500" style={{ width: `${data?.projectHealth?.qualityScore ?? 0}%` }} /></div>
              </div>
              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>Resource Utilization</span>
                  <span className="font-semibold text-indigo-400">{data?.projectHealth?.schedulePerformance ?? 0}%</span>
                </div>
                <div className="progress-track"><div className="progress-fill bg-indigo-500" style={{ width: `${data?.projectHealth?.budgetVsActual ?? 0}%` }} /></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Section: Team Workload & Timesheet Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Team Workload */}
        <div className="lg:col-span-8 glass-card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white font-heading">Team Workload & Utilization</h2>
            <span className="text-xs text-slate-400">Average: <strong className="text-emerald-400 font-bold">{data?.projectHealth?.resourceUtilization ?? 0}%</strong></span>
          </div>

          <div className="space-y-3">
            {workloads.map((m) => (
              <div key={m.userId} className="p-3 rounded-xl bg-slate-900/50 border border-white/5 flex items-center justify-between gap-4">
                <div className="flex items-center gap-3 w-44">
                  <img src={m.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'} alt={m.name} className="w-8 h-8 rounded-full object-cover" />
                  <div>
                    <p className="text-xs font-bold text-slate-200">{m.name}</p>
                    <p className="text-[10px] text-slate-400">{m.role}</p>
                  </div>
                </div>

                <div className="hidden sm:flex items-center gap-6 text-xs text-slate-300">
                  <span>Projects: <strong className="text-white">{m.activeProjectsCount}</strong></span>
                  <span>Tasks: <strong className="text-white">{m.activeTasksCount}</strong></span>
                </div>

                <div className="flex items-center gap-3 flex-1 max-w-xs">
                  <div className="progress-track flex-1">
                    <div className="progress-fill bg-indigo-500" style={{ width: `${m.utilizationPercentage}%` }} />
                  </div>
                  <span className="text-xs font-bold text-slate-200 w-9 text-right">{m.utilizationPercentage}%</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* This Week Timesheet Summary Widget */}
        <div className="lg:col-span-4 glass-card p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-3 border-b border-white/5">
              <h2 className="text-base font-bold text-white font-heading">Timesheet Summary</h2>
              <span className="text-xs font-bold text-indigo-400">This Week</span>
            </div>

            <div className="my-5 text-center">
              <div className="inline-flex items-center justify-center w-24 h-24 rounded-full border-4 border-indigo-500/30 bg-indigo-500/10">
                <div>
                  <span className="text-2xl font-black text-white">36.5h</span>
                  <p className="text-[10px] text-slate-400">Total Logged</p>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2 text-center text-xs">
              <div className="p-2.5 rounded-lg bg-slate-900/60 border border-white/5">
                <span className="text-[10px] text-slate-400 block">Billable Hours</span>
                <span className="text-sm font-bold text-emerald-400">29.0h (79%)</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-900/60 border border-white/5">
                <span className="text-[10px] text-slate-400 block">Non-Billable</span>
                <span className="text-sm font-bold text-slate-300">7.5h (21%)</span>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-white/5 flex gap-2">
            <button onClick={onOpenTimeModal} className="btn btn-secondary btn-sm flex-1">
              <Plus className="w-3.5 h-3.5" />
              <span>Log Time</span>
            </button>
            <button onClick={() => navigate('/timesheets')} className="btn btn-primary btn-sm flex-1">
              <span>View Timesheet</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { reportApi, projectApi, userApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import { ExportButtons } from '../../components/ExportButtons';
import {
  BarChart3,
  FolderKanban,
  Users,
  Clock,
  Download,
  Filter,
  Search,
  Calendar,
} from 'lucide-react';

export const ReportsHub = () => {
  const [activeTab, setActiveTab] = useState('timesheets'); // 'timesheets', 'projects', 'resources'
  const [timesheetRows, setTimesheetRows] = useState([]);
  const [projectRows, setProjectRows] = useState([]);
  const [resourceRows, setResourceRows] = useState([]);
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadReports = async () => {
    setLoading(true);
    try {
      const [tsRes, prjRes, resRes, pList, uList] = await Promise.all([
        reportApi.getTimesheets({
          projectId: selectedProjectId || undefined,
          userId: selectedUserId || undefined,
          status: selectedStatus || undefined,
          startDate: startDate || undefined,
          endDate: endDate || undefined,
        }),
        reportApi.getProjects({
          projectId: selectedProjectId || undefined,
        }),
        reportApi.getResources({
          userId: selectedUserId || undefined,
        }),
        projectApi.getAll(),
        userApi.getAll(),
      ]);

      setTimesheetRows(tsRes.data || []);
      setProjectRows(prjRes.data || []);
      setResourceRows(resRes.data || []);
      setProjects(pList.data || []);
      setUsers(uList.data || []);
    } catch (err) {
      console.error('Failed to load reports:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReports();
  }, [selectedProjectId, selectedUserId, selectedStatus, startDate, endDate]);

  const handleExportCsv = async () => {
    try {
      const response = await reportApi.downloadCsv({
        projectId: selectedProjectId || undefined,
        userId: selectedUserId || undefined,
        status: selectedStatus || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      });

      const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'timesheet-report.csv';
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to export CSV:', err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">Reports & Analytics</h1>
          <p className="text-xs text-slate-400 mt-0.5">Generate exportable project health, employee utilization, and timesheet billing statements.</p>
        </div>

        <ExportButtons onExportCsv={handleExportCsv} />
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-white/10 pb-2">
        <button
          onClick={() => setActiveTab('timesheets')}
          className={`px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 transition-all ${
            activeTab === 'timesheets'
              ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Clock className="w-3.5 h-3.5" />
          <span>Timesheet Breakdown</span>
        </button>

        <button
          onClick={() => setActiveTab('projects')}
          className={`px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 transition-all ${
            activeTab === 'projects'
              ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <FolderKanban className="w-3.5 h-3.5" />
          <span>Project Delivery & Effort</span>
        </button>

        <button
          onClick={() => setActiveTab('resources')}
          className={`px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 transition-all ${
            activeTab === 'resources'
              ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Users className="w-3.5 h-3.5" />
          <span>Resource Utilization</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="glass-panel p-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
        <div>
          <label className="form-label">Project</label>
          <select
            value={selectedProjectId}
            onChange={(e) => setSelectedProjectId(e.target.value)}
            className="form-select text-xs py-1.5"
          >
            <option value="">All Projects</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="form-label">Employee</label>
          <select
            value={selectedUserId}
            onChange={(e) => setSelectedUserId(e.target.value)}
            className="form-select text-xs py-1.5"
          >
            <option value="">All Employees</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>{u.fullName}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="form-label">Status</label>
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="form-select text-xs py-1.5"
          >
            <option value="">All Statuses</option>
            <option value="APPROVED">Approved</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="DRAFT">Draft</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>

        <div>
          <label className="form-label">Start Date</label>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="form-input text-xs py-1.5"
          />
        </div>

        <div>
          <label className="form-label">End Date</label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="form-input text-xs py-1.5"
          />
        </div>
      </div>

      {/* Tab 1: Timesheets Report */}
      {activeTab === 'timesheets' && (
        <div className="glass-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white font-heading">Timesheet Detailed Log ({timesheetRows.length} records)</h2>
          </div>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Employee</th>
                  <th>Project Code</th>
                  <th>Task Title</th>
                  <th>Hours</th>
                  <th>Billable</th>
                  <th>Status</th>
                  <th>Reviewer</th>
                </tr>
              </thead>
              <tbody>
                {timesheetRows.map((r) => (
                  <tr key={r.timesheetId}>
                    <td className="font-mono text-xs text-slate-300">{r.workDate}</td>
                    <td className="font-bold text-xs text-slate-200">{r.employeeName}</td>
                    <td className="font-mono text-xs text-indigo-400">{r.projectCode}</td>
                    <td className="text-xs text-slate-300">{r.taskTitle}</td>
                    <td className="text-xs font-black text-white">{r.hoursWorked}h</td>
                    <td>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${r.billable ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-700 text-slate-300'}`}>
                        {r.billable ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td><StatusBadge status={r.status} /></td>
                    <td className="text-xs text-slate-400">{r.reviewerName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: Project Reports */}
      {activeTab === 'projects' && (
        <div className="glass-card p-6 space-y-4">
          <h2 className="text-base font-bold text-white font-heading">Project Progress & Variance Report</h2>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Project</th>
                  <th>PM</th>
                  <th>Est. Hours</th>
                  <th>Actual Hours</th>
                  <th>Variance</th>
                  <th>Progress</th>
                  <th>Delayed Tasks</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {projectRows.map((p) => (
                  <tr key={p.projectId}>
                    <td>
                      <div className="font-bold text-xs text-slate-200">{p.projectName}</div>
                      <div className="text-[10px] text-slate-400 font-mono">{p.projectCode} • {p.clientName}</div>
                    </td>
                    <td className="text-xs text-slate-300">{p.projectManagerName}</td>
                    <td className="text-xs text-slate-300 font-semibold">{p.estimatedHours}h</td>
                    <td className="text-xs text-white font-bold">{p.actualHours}h</td>
                    <td className={`text-xs font-bold ${p.varianceHours < 0 ? 'text-rose-400' : 'text-emerald-400'}`}>
                      {p.varianceHours > 0 ? `+${p.varianceHours}h` : `${p.varianceHours}h`}
                    </td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="w-16 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div className="bg-indigo-500 h-full rounded-full" style={{ width: `${p.completionPercentage}%` }} />
                        </div>
                        <span className="text-xs font-semibold text-slate-300">{p.completionPercentage}%</span>
                      </div>
                    </td>
                    <td className="text-xs font-bold text-slate-300">{p.delayedTasksCount}</td>
                    <td><StatusBadge status={p.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Resource Reports */}
      {activeTab === 'resources' && (
        <div className="glass-card p-6 space-y-4">
          <h2 className="text-base font-bold text-white font-heading">Employee Utilization & Allocation</h2>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Department</th>
                  <th>Designation</th>
                  <th>Total Hours</th>
                  <th>Billable</th>
                  <th>Non-Billable</th>
                  <th>Utilization %</th>
                </tr>
              </thead>
              <tbody>
                {resourceRows.map((u) => (
                  <tr key={u.userId}>
                    <td className="font-bold text-xs text-slate-200">{u.employeeName}</td>
                    <td className="text-xs text-slate-300">{u.department}</td>
                    <td className="text-xs text-slate-400">{u.designation}</td>
                    <td className="text-xs font-black text-white">{u.totalHours}h</td>
                    <td className="text-xs font-semibold text-emerald-400">{u.billableHours}h</td>
                    <td className="text-xs font-semibold text-slate-400">{u.nonBillableHours}h</td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="w-16 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div className="bg-emerald-500 h-full rounded-full" style={{ width: `${u.utilizationPercentage}%` }} />
                        </div>
                        <span className="text-xs font-bold text-white">{u.utilizationPercentage}%</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

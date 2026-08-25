import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectApi, taskApi, userApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import { Modal } from '../../components/Modal';
import { useAuth } from '../../context/AuthContext';
import {
  FolderKanban,
  ArrowLeft,
  Users,
  CheckSquare,
  Clock,
  Calendar,
  DollarSign,
  Plus,
  Trash2,
  Flag,
  AlertTriangle,
  Shield,
} from 'lucide-react';

export const ProjectDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const canManageProject = hasRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER');

  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [milestones, setMilestones] = useState([]);
  const [risks, setRisks] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('tasks');

  // Modals
  const [isMemberModalOpen, setIsMemberModalOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [roleInProject, setRoleInProject] = useState('Developer');
  const [allocation, setAllocation] = useState(100);

  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDesc, setTaskDesc] = useState('');
  const [taskPriority, setTaskPriority] = useState('MEDIUM');
  const [taskModule, setTaskModule] = useState('General');
  const [taskOwnerId, setTaskOwnerId] = useState('');
  const [taskAssigneeIds, setTaskAssigneeIds] = useState([]);
  const [taskEstHours, setTaskEstHours] = useState(16);
  const [taskDueDate, setTaskDueDate] = useState('');
  const [taskAttachmentUrls, setTaskAttachmentUrls] = useState('');

  const loadDetails = async () => {
    try {
      const [pRes, tRes, mRes, rRes] = await Promise.all([
        projectApi.getById(id),
        taskApi.getByProject(id),
        projectApi.getMilestones(id),
        projectApi.getRisks(id),
      ]);
      setProject(pRes.data);
      setTasks(tRes.data || []);
      setMilestones(mRes.data || []);
      setRisks(rRes.data || []);

      if (canManageProject) {
        const uRes = await userApi.getAll();
        setAllUsers(uRes.data || []);
        if (uRes.data?.length > 0) setSelectedUserId(uRes.data[0].id);
      }
    } catch (err) {
      console.error('Failed to load project details:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetails();
  }, [id, canManageProject]);

  const handleAddMember = async (e) => {
    e.preventDefault();
    if (!selectedUserId) return;
    try {
      await projectApi.addMember(id, {
        userId: selectedUserId,
        roleInProject,
        allocationPercentage: Number(allocation),
      });
      setIsMemberModalOpen(false);
      loadDetails();
    } catch (err) {
      console.error('Failed to add member:', err);
    }
  };

  const handleRemoveMember = async (userId) => {
    try {
      await projectApi.removeMember(id, userId);
      loadDetails();
    } catch (err) {
      console.error('Failed to remove member:', err);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    try {
      await taskApi.create({
        projectId: Number(id),
        title: taskTitle,
        description: taskDesc,
        priority: taskPriority,
        moduleName: taskModule,
        taskOwnerId: taskOwnerId ? Number(taskOwnerId) : undefined,
        assigneeIds: taskAssigneeIds.map(Number),
        attachmentUrls: taskAttachmentUrls.split('\n').map((u) => u.trim()).filter(Boolean),
        estimatedHours: Number(taskEstHours),
        dueDate: taskDueDate || undefined,
      });
      setIsTaskModalOpen(false);
      setTaskTitle('');
      setTaskDesc('');
      setTaskModule('General');
      setTaskOwnerId('');
      setTaskAssigneeIds([]);
      setTaskAttachmentUrls('');
      loadDetails();
    } catch (err) {
      console.error('Failed to create task:', err);
    }
  };

  if (loading || !project) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Back Button & Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/projects')}
          className="p-1.5 rounded-lg bg-slate-900 border border-white/10 text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <div className="flex items-center gap-2">
            <span className="font-mono text-xs font-bold text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded border border-indigo-500/20">
              {project.projectCode}
            </span>
            <h1 className="text-xl lg:text-2xl font-extrabold text-white font-heading">{project.name}</h1>
            <StatusBadge status={project.status} />
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Client: <strong className="text-slate-200">{project.clientName}</strong> • PM: <strong className="text-slate-200">{project.projectManager?.fullName}</strong></p>
        </div>
      </div>

      {/* KPI Cards Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-card p-4">
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Effort Logged</span>
          <span className="text-2xl font-black text-white mt-1 block">{project.actualHours}h / {project.estimatedHours}h</span>
          <div className="progress-track mt-2">
            <div className="progress-fill bg-indigo-500" style={{ width: `${project.completionPercentage}%` }} />
          </div>
        </div>

        <div className="glass-card p-4">
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Project Timeline</span>
          <span className="text-xs font-bold text-slate-200 mt-1.5 block">{project.startDate} → {project.endDate}</span>
          <span className="text-[10px] text-emerald-400 block mt-1">Health: {project.healthScore}%</span>
        </div>

        <div className="glass-card p-4">
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Allocated Budget</span>
          <span className="text-2xl font-black text-emerald-400 mt-1 block">${(project.budgetAmount || 0).toLocaleString()}</span>
          <span className="text-[10px] text-slate-400 block mt-1">Priority: {project.priority}</span>
        </div>

        <div className="glass-card p-4">
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">Team & Tasks</span>
          <span className="text-2xl font-black text-cyan-400 mt-1 block">{project.teamMemberCount} Members</span>
          <span className="text-[10px] text-slate-400 block mt-1">{project.totalTasks} Total Tasks ({project.completedTasks} Done)</span>
        </div>
      </div>

      {project.documentUrls?.length > 0 && (
        <div className="glass-card p-5">
          <h2 className="text-sm font-bold text-white mb-3">Project Documents</h2>
          <div className="flex flex-wrap gap-2">
            {project.documentUrls.map((url) => (
              <a key={url} href={url} target="_blank" rel="noreferrer"
                className="text-xs text-indigo-300 hover:text-white underline break-all">
                {url}
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-white/10 pb-2">
        {['tasks', 'team', 'milestones', 'risks'].map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all ${
              activeTab === tab
                ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Tab 1: Tasks */}
      {activeTab === 'tasks' && (
        <div className="glass-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white font-heading">Project Tasks ({tasks.length})</h2>
            {hasRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'TEAM_LEAD') && (
              <button onClick={() => setIsTaskModalOpen(true)} className="btn btn-primary btn-sm">
                <Plus className="w-3.5 h-3.5" />
                <span>Add Task</span>
              </button>
            )}
          </div>

          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Task Title</th>
                  <th>Module</th>
                  <th>Assignee</th>
                  <th>Priority</th>
                  <th>Est. / Actual</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {tasks.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center text-slate-500 py-6 text-xs">No tasks created yet.</td>
                  </tr>
                ) : (
                  tasks.map((t) => (
                    <tr key={t.id}>
                      <td className="font-mono text-xs text-indigo-400 font-bold">{t.taskCode}</td>
                      <td>
                        <div className="font-semibold text-slate-200 text-xs">{t.title}</div>
                        <div className="text-[10px] text-slate-400">Due: {t.dueDate}</div>
                        {t.attachmentUrls?.length > 0 && (
                          <div className="flex gap-2 mt-1">
                            {t.attachmentUrls.map((url) => (
                              <a key={url} href={url} target="_blank" rel="noreferrer" className="text-[10px] text-indigo-400 underline">Attachment</a>
                            ))}
                          </div>
                        )}
                      </td>
                      <td className="text-xs text-slate-300">{t.moduleName || 'General'}</td>
                      <td>
                        <div className="flex items-center gap-1.5">
                          {t.assignees?.map((a) => (
                            <span key={a.id} className="text-xs text-slate-200 font-medium">{a.fullName}</span>
                          ))}
                          {(!t.assignees || t.assignees.length === 0) && <span className="text-xs text-slate-500">Unassigned</span>}
                        </div>
                      </td>
                      <td><StatusBadge status={t.priority} /></td>
                      <td className="text-xs font-semibold text-slate-200">{t.actualHours}h / {t.estimatedHours}h</td>
                      <td><StatusBadge status={t.status} /></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: Team Members */}
      {activeTab === 'team' && (
        <div className="glass-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white font-heading">Project Team ({project.members?.length || 0})</h2>
            {hasRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER') && (
              <button onClick={() => setIsMemberModalOpen(true)} className="btn btn-primary btn-sm">
                <Plus className="w-3.5 h-3.5" />
                <span>Assign Team Member</span>
              </button>
            )}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {project.members?.map((m) => (
              <div key={m.id} className="p-4 rounded-xl bg-slate-900/60 border border-white/5 flex items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <img
                    src={m.user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                    alt={m.user?.fullName}
                    className="w-10 h-10 rounded-full object-cover ring-1 ring-white/10"
                  />
                  <div>
                    <p className="text-xs font-bold text-white">{m.user?.fullName}</p>
                    <p className="text-[11px] text-indigo-400">{m.roleInProject} • {m.allocationPercentage}%</p>
                    <p className="text-[10px] text-slate-500">{m.user?.email}</p>
                  </div>
                </div>

                {hasRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER') && (
                  <button
                    onClick={() => handleRemoveMember(m.user?.id)}
                    className="p-1.5 rounded-lg text-rose-400 hover:bg-rose-950/50 transition-colors"
                    title="Remove from project"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab 3: Milestones */}
      {activeTab === 'milestones' && (
        <div className="glass-card p-6 space-y-4">
          <h2 className="text-base font-bold text-white font-heading">Key Milestones</h2>
          <div className="space-y-3">
            {milestones.length === 0 ? (
              <p className="text-xs text-slate-500">No milestones registered.</p>
            ) : (
              milestones.map((m) => (
                <div key={m.id} className="p-4 rounded-xl bg-slate-900/60 border border-white/5 flex items-center justify-between">
                  <div>
                    <h4 className="text-xs font-bold text-white">{m.title}</h4>
                    <p className="text-[11px] text-slate-400">{m.description}</p>
                    <span className="text-[10px] text-slate-500 mt-1 block">Target: {m.targetDate} • Deliverables: {m.deliverables}</span>
                  </div>
                  <span className={`badge ${m.status === 'ACHIEVED' ? 'badge-approved' : 'badge-submitted'}`}>{m.status}</span>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Tab 4: Risks */}
      {activeTab === 'risks' && (
        <div className="glass-card p-6 space-y-4">
          <h2 className="text-base font-bold text-white font-heading">Risks & Issues</h2>
          <div className="space-y-3">
            {risks.length === 0 ? (
              <p className="text-xs text-slate-500">No open risks registered.</p>
            ) : (
              risks.map((r) => (
                <div key={r.id} className="p-4 rounded-xl bg-slate-900/60 border border-white/5 flex items-start justify-between">
                  <div>
                    <h4 className="text-xs font-bold text-white flex items-center gap-2">
                      <AlertTriangle className="w-4 h-4 text-amber-400" />
                      <span>{r.title}</span>
                    </h4>
                    <p className="text-[11px] text-slate-400 mt-1">{r.description}</p>
                    <p className="text-[10px] text-indigo-300 mt-1">Mitigation: {r.mitigationPlan}</p>
                  </div>
                  <StatusBadge status={r.severity} />
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Add Member Modal */}
      <Modal isOpen={isMemberModalOpen} onClose={() => setIsMemberModalOpen(false)} title="Assign Team Member">
        <form onSubmit={handleAddMember} className="space-y-4">
          <div className="form-group">
            <label className="form-label">Select Employee</label>
            <select
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              className="form-select text-xs"
              required
            >
              {allUsers.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.fullName} ({u.department} - {u.designation})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="form-group">
              <label className="form-label">Role in Project</label>
              <input
                type="text"
                value={roleInProject}
                onChange={(e) => setRoleInProject(e.target.value)}
                className="form-input text-xs"
                placeholder="e.g. Lead Architect"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Allocation (%)</label>
              <input
                type="number"
                value={allocation}
                onChange={(e) => setAllocation(e.target.value)}
                className="form-input text-xs"
                min="10"
                max="100"
              />
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsMemberModalOpen(false)} className="btn btn-secondary btn-sm">Cancel</button>
            <button type="submit" className="btn btn-primary btn-sm">Add Member</button>
          </div>
        </form>
      </Modal>

      {/* Add Task Modal */}
      <Modal isOpen={isTaskModalOpen} onClose={() => setIsTaskModalOpen(false)} title="Add Task">
        <form onSubmit={handleCreateTask} className="space-y-4">
          <div className="form-group">
            <label className="form-label">Task Title *</label>
            <input
              type="text"
              required
              value={taskTitle}
              onChange={(e) => setTaskTitle(e.target.value)}
              className="form-input text-xs"
              placeholder="e.g. Implement OAuth2 API Gateway proxy"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="form-group">
              <label className="form-label">Module</label>
              <input
                type="text"
                value={taskModule}
                onChange={(e) => setTaskModule(e.target.value)}
                className="form-input text-xs"
                placeholder="e.g. Backend API"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Task Owner</label>
              <select
                value={taskOwnerId}
                onChange={(e) => setTaskOwnerId(e.target.value)}
                className="form-select text-xs"
              >
                <option value="">Unassigned</option>
                {project.members?.map((m) => (
                  <option key={m.user.id} value={m.user.id}>{m.user.fullName} ({m.roleInProject})</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Assign Team Members</label>
            <select
              multiple
              value={taskAssigneeIds.map(String)}
              onChange={(e) => setTaskAssigneeIds(Array.from(e.target.selectedOptions, (o) => o.value))}
              className="form-select text-xs min-h-24"
            >
              {project.members?.map((m) => (
                <option key={m.user.id} value={m.user.id}>{m.user.fullName} — {m.roleInProject}</option>
              ))}
            </select>
            <p className="text-[10px] text-slate-500 mt-1">Use Ctrl/Cmd-click to select multiple members.</p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="form-group">
              <label className="form-label">Estimated Hours</label>
              <input
                type="number"
                value={taskEstHours}
                onChange={(e) => setTaskEstHours(e.target.value)}
                className="form-input text-xs"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Due Date</label>
              <input
                type="date"
                value={taskDueDate}
                onChange={(e) => setTaskDueDate(e.target.value)}
                className="form-input text-xs"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Attachments (optional links)</label>
            <textarea
              rows="2"
              value={taskAttachmentUrls}
              onChange={(e) => setTaskAttachmentUrls(e.target.value)}
              className="form-textarea text-xs"
              placeholder="One attachment URL per line"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              rows="2"
              value={taskDesc}
              onChange={(e) => setTaskDesc(e.target.value)}
              className="form-textarea text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsTaskModalOpen(false)} className="btn btn-secondary btn-sm">Cancel</button>
            <button type="submit" className="btn btn-primary btn-sm">Create Task</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

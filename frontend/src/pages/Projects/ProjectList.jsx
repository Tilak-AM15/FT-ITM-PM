import React, { useState, useEffect } from 'react';
import { projectApi, userApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import { Modal } from '../../components/Modal';
import { useAuth } from '../../context/AuthContext';
import {
  FolderKanban,
  Plus,
  Search,
  Filter,
  Users,
  Clock,
  Calendar,
  DollarSign,
  ChevronRight,
  Sparkles,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ProjectList = ({ isModalOpen, setIsModalOpen }) => {
  const { hasRole } = useAuth();
  const canManageProjects = hasRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER');
  const [projects, setProjects] = useState([]);
  const [managers, setManagers] = useState([]);
  const [availableUsers, setAvailableUsers] = useState([]);
  const [memberUserIds, setMemberUserIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const navigate = useNavigate();

  // Create Project Form State matching OCR Page 4
  const [projectName, setProjectName] = useState('');
  const [projectCode, setProjectCode] = useState('');
  const [clientName, setClientName] = useState('');
  const [projectManagerId, setProjectManagerId] = useState('');
  const [startDate, setStartDate] = useState(new Date().toISOString().slice(0, 10));
  const [endDate, setEndDate] = useState(new Date(Date.now() + 90 * 86400000).toISOString().slice(0, 10));
  const [description, setDescription] = useState('');
  const [documentUrls, setDocumentUrls] = useState('');
  const [projectStatus, setProjectStatus] = useState('ACTIVE');
  const [projectPriority, setProjectPriority] = useState('MEDIUM');
  const [budgetAmount, setBudgetAmount] = useState(100000);
  const [estimatedHours, setEstimatedHours] = useState(400);
  const [submitting, setSubmitting] = useState(false);

  const loadProjects = async () => {
    setLoading(true);

    const [projectResult, managerResult, usersResult] = await Promise.allSettled([
      projectApi.getAll(),
      canManageProjects ? userApi.getByRole('PROJECT_MANAGER') : Promise.resolve({ data: [] }),
      canManageProjects ? userApi.getAll() : Promise.resolve({ data: [] }),
    ]);

    if (projectResult.status === 'fulfilled') {
      setProjects(Array.isArray(projectResult.value?.data) ? projectResult.value.data : []);
    } else {
      console.error('Failed to load projects:', projectResult.reason);
      setProjects([]);
    }

    if (canManageProjects) {
      let managerData =
        managerResult.status === 'fulfilled' && Array.isArray(managerResult.value?.data)
          ? managerResult.value.data.filter((u) => u?.active !== false)
          : [];

      const users =
        usersResult.status === 'fulfilled' && Array.isArray(usersResult.value?.data)
          ? usersResult.value.data
          : [];

      if (!managerData.length) {
        managerData = users.filter((u) =>
          u?.active !== false &&
          ['PROJECT_MANAGER', 'ADMIN', 'SUPER_ADMIN'].includes(u?.role)
        );
      }

      setManagers(managerData);
      setAvailableUsers(users.filter((u) => u?.active !== false && u?.id != null));

      setProjectManagerId((currentId) => {
        const exists = managerData.some((m) => String(m.id) === String(currentId));
        return exists ? currentId : (managerData[0]?.id ? String(managerData[0].id) : '');
      });
    }

    setLoading(false);
  };

  useEffect(() => {
    loadProjects();
  }, [canManageProjects]);

  const handleCreateProject = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const code = projectCode || `PRJ-${projectName.slice(0, 3).toUpperCase()}-${Math.floor(10 + Math.random() * 90)}`;
      await projectApi.create({
        name: projectName,
        projectCode: code,
        clientName,
        projectManagerId: projectManagerId ? Number(projectManagerId) : null,
        startDate,
        endDate,
        description,
        status: projectStatus,
        priority: projectPriority,
        budgetAmount: Number(budgetAmount),
        estimatedHours: Number(estimatedHours),
        memberUserIds: memberUserIds.map(Number),
        documentUrls: documentUrls.split('\n').map((u) => u.trim()).filter(Boolean),
      });

      setIsModalOpen(false);
      // Reset form
      setProjectName('');
      setProjectCode('');
      setClientName('');
      setDescription('');
      setDocumentUrls('');
      setMemberUserIds([]);
      loadProjects();
    } catch (err) {
      console.error('Failed to create project:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredProjects = projects.filter((p) => {
    const matchSearch =
      p.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.projectCode?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.clientName?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'ALL' || p.status === statusFilter;
    const matchPriority = priorityFilter === 'ALL' || p.priority === priorityFilter;
    return matchSearch && matchStatus && matchPriority;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">Project Portfolio</h1>
          <p className="text-xs text-slate-400 mt-0.5">Manage organizational initiatives, timelines, budgets, and team allocations.</p>
        </div>

        {hasRole('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER') && (
          <button onClick={() => setIsModalOpen(true)} className="btn btn-primary btn-sm">
            <Plus className="w-4 h-4" />
            <span>Create Project</span>
          </button>
        )}
      </div>

      {/* Filters Bar */}
      <div className="glass-panel p-4 flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-2 bg-slate-900 border border-white/10 rounded-lg px-3 py-1.5 w-full md:w-80 text-xs">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search by name, code, or client..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="bg-transparent border-none outline-none text-slate-200 w-full"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="form-select text-xs py-1.5"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="DRAFT">Draft</option>
            <option value="ON_HOLD">On Hold</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="form-select text-xs py-1.5"
          >
            <option value="ALL">All Priorities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
      </div>

      {/* Project Cards Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
        </div>
      ) : filteredProjects.length === 0 ? (
        <div className="glass-card p-12 text-center text-slate-400 text-xs">
          No projects matched your criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredProjects.map((p) => (
            <div
              key={p.id}
              onClick={() => navigate(`/projects/${p.id}`)}
              className="glass-card p-5 cursor-pointer hover:border-indigo-500/40 hover:shadow-xl hover:shadow-indigo-500/5 transition-all flex flex-col justify-between group"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <span className="font-mono text-[11px] font-bold text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded border border-indigo-500/20">
                    {p.projectCode}
                  </span>
                  <StatusBadge status={p.status} />
                </div>

                <h3 className="text-base font-bold text-white font-heading group-hover:text-indigo-300 transition-colors line-clamp-1">
                  {p.name}
                </h3>
                <p className="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed">
                  {p.description || `Client: ${p.clientName}`}
                </p>
              </div>

              <div className="mt-4 pt-4 border-t border-white/5 space-y-3">
                {/* Progress Bar */}
                <div>
                  <div className="flex justify-between text-[11px] text-slate-400 mb-1">
                    <span>Task Completion</span>
                    <span className="font-bold text-white">{p.completionPercentage}%</span>
                  </div>
                  <div className="progress-track">
                    <div className="progress-fill bg-indigo-500" style={{ width: `${p.completionPercentage}%` }} />
                  </div>
                </div>

                {/* Metadata Row */}
                <div className="grid grid-cols-2 gap-2 text-[11px] text-slate-300 pt-1">
                  <div className="flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-cyan-400" />
                    <span>{p.actualHours}h / {p.estimatedHours}h</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Users className="w-3.5 h-3.5 text-emerald-400" />
                    <span>{p.teamMemberCount} Team Members</span>
                  </div>
                </div>

                {/* Footer Info */}
                <div className="flex items-center justify-between text-[11px] text-slate-400 pt-2 border-t border-white/5">
                  <span>PM: <strong className="text-slate-200">{p.projectManager?.fullName || 'Unassigned'}</strong></span>
                  <div className="flex items-center gap-1 text-indigo-400 font-semibold group-hover:translate-x-0.5 transition-transform">
                    <span>Details</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Project Modal matching OCR Page 4 */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create New Project"
        maxWidth="max-w-2xl"
      >
        <form onSubmit={handleCreateProject} className="space-y-4">
          <div className="form-group">
            <label className="form-label">Project Name *</label>
            <input
              type="text"
              required
              placeholder="e.g. OpenLayer Digital Transformation"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
              className="form-input text-xs"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Client / Business Unit *</label>
              <input
                type="text"
                required
                placeholder="e.g. OpenLayer Technologies"
                value={clientName}
                onChange={(e) => setClientName(e.target.value)}
                className="form-input text-xs"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Project Manager *</label>
              <select
                value={projectManagerId}
                onChange={(e) => setProjectManagerId(e.target.value)}
                className="form-select text-xs"
                required
              >
                <option value="">Select Project Manager</option>
                {managers.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.fullName || m.username} ({m.role?.replaceAll('_', ' ') || 'USER'})
                  </option>
                ))}
              </select>
              {managers.length === 0 && (
                <p className="text-[11px] text-amber-400 mt-1">
                  No active project managers were returned by the backend.
                </p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Start Date *</label>
              <input
                type="date"
                required
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="form-input text-xs"
              />
            </div>

            <div className="form-group">
              <label className="form-label">End Date *</label>
              <input
                type="date"
                required
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="form-input text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Estimated Effort (Hours)</label>
              <input
                type="number"
                value={estimatedHours}
                onChange={(e) => setEstimatedHours(e.target.value)}
                className="form-input text-xs"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Budget ($)</label>
              <input
                type="number"
                value={budgetAmount}
                onChange={(e) => setBudgetAmount(e.target.value)}
                className="form-input text-xs"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Initial Team Members (optional)</label>
            <select
              multiple
              value={memberUserIds.map(String)}
              onChange={(e) =>
                setMemberUserIds(
                  Array.from(e.target.selectedOptions, (option) => Number(option.value))
                )
              }
              className="form-select text-xs min-h-[120px]"
            >
              {availableUsers.length === 0 ? (
                <option value="" disabled>No active users available</option>
              ) : (
                availableUsers.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.fullName || u.username} ({u.role?.replaceAll('_', ' ') || 'USER'})
                  </option>
                ))
              )}
            </select>
            <p className="text-[10px] text-slate-500 mt-1">
              Hold Ctrl (Windows) or Command (Mac) to select multiple team members.
            </p>
          </div>

          <div className="form-group">
            <label className="form-label">Project Description</label>
            <textarea
              rows="3"
              placeholder="Digital transformation initiative for platform including API integration..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="form-textarea text-xs"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Project Priority</label>
            <select
              value={projectPriority}
              onChange={(e) => setProjectPriority(e.target.value)}
              className="form-select text-xs"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Project Documents (optional links)</label>
            <textarea
              rows="2"
              value={documentUrls}
              onChange={(e) => setDocumentUrls(e.target.value)}
              className="form-textarea text-xs"
              placeholder="One document URL per line"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Project Status</label>
            <div className="flex items-center gap-6 mt-1">
              <label className="flex items-center gap-2 text-xs text-slate-300 cursor-pointer">
                <input
                  type="radio"
                  name="status"
                  value="ACTIVE"
                  checked={projectStatus === 'ACTIVE'}
                  onChange={() => setProjectStatus('ACTIVE')}
                  className="accent-indigo-500"
                />
                <span>Active</span>
              </label>
              <label className="flex items-center gap-2 text-xs text-slate-300 cursor-pointer">
                <input
                  type="radio"
                  name="status"
                  value="DRAFT"
                  checked={projectStatus === 'DRAFT'}
                  onChange={() => setProjectStatus('DRAFT')}
                  className="accent-indigo-500"
                />
                <span>Draft</span>
              </label>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-4 border-t border-white/10">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="btn btn-secondary btn-sm"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="btn btn-primary btn-sm"
            >
              <Plus className="w-4 h-4" />
              <span>{submitting ? 'Creating...' : '+ Create Project'}</span>
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

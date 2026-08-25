import React, { useState, useEffect } from 'react';
import { userApi } from '../../api/endpoints';
import { Modal } from '../../components/Modal';
import {
  Users,
  Plus,
  Search,
  Shield,
  Briefcase,
  Mail,
  DollarSign,
  CheckCircle2,
} from 'lucide-react';

const ROLES = [
  { value: 'SUPER_ADMIN', label: 'Super Admin' },
  { value: 'ADMIN', label: 'Admin / PMO' },
  { value: 'PROJECT_MANAGER', label: 'Project Manager' },
  { value: 'TEAM_LEAD', label: 'Team Lead' },
  { value: 'EMPLOYEE', label: 'Employee / Developer' },
  { value: 'MANAGEMENT', label: 'Management' },
  { value: 'FINANCE_HR', label: 'Finance / HR' },
];

export const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  // Add User Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const demoMode = import.meta.env.VITE_DEMO_MODE !== 'false';
  const [password, setPassword] = useState(demoMode ? 'password123' : '');
  const [fullName, setFullName] = useState('');
  const [role, setRole] = useState('EMPLOYEE');
  const [department, setDepartment] = useState('Engineering');
  const [designation, setDesignation] = useState('Full Stack Developer');
  const [hourlyRate, setHourlyRate] = useState(65);
  const [submitting, setSubmitting] = useState(false);

  const loadUsers = async () => {
    try {
      const res = await userApi.getAll();
      setUsers(res.data || []);
    } catch (err) {
      console.error('Failed to load users:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await userApi.create({
        username,
        email,
        password,
        fullName,
        role,
        department,
        designation,
        hourlyRate: Number(hourlyRate),
      });
      setIsModalOpen(false);
      setUsername('');
      setEmail('');
      setFullName('');
      loadUsers();
    } catch (err) {
      console.error('Failed to create user:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredUsers = users.filter((u) => {
    const matchSearch =
      u.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.department?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchRole = roleFilter === 'ALL' || u.role === roleFilter;
    return matchSearch && matchRole;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">User Directory & RBAC</h1>
            <span className="bg-indigo-500/20 text-indigo-400 text-xs font-bold px-2.5 py-0.5 rounded-full border border-indigo-500/30">
              {users.length} Users
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Manage organizational accounts, job titles, departments, and role permissions.</p>
        </div>

        <button onClick={() => setIsModalOpen(true)} className="btn btn-primary btn-sm">
          <Plus className="w-4 h-4" />
          <span>Add User</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="glass-panel p-4 flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-2 bg-slate-900 border border-white/10 rounded-lg px-3 py-1.5 w-full md:w-80 text-xs">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search by name, email, department..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="bg-transparent border-none outline-none text-slate-200 w-full"
          />
        </div>

        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="form-select text-xs py-1.5 w-full md:w-56"
        >
          <option value="ALL">All Roles</option>
          {ROLES.map((r) => (
            <option key={r.value} value={r.value}>{r.label}</option>
          ))}
        </select>
      </div>

      {/* User Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filteredUsers.map((u) => (
          <div key={u.id} className="glass-card p-5 space-y-4 hover:border-indigo-500/40 transition-all">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                <img
                  src={u.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                  alt={u.fullName}
                  className="w-11 h-11 rounded-xl object-cover ring-1 ring-white/10"
                />
                <div>
                  <h3 className="text-sm font-bold text-white leading-tight">{u.fullName}</h3>
                  <p className="text-[11px] text-slate-400 font-mono">@{u.username}</p>
                </div>
              </div>
              <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-indigo-500/15 text-indigo-300 border border-indigo-500/30">
                {u.role?.replace('_', ' ')}
              </span>
            </div>

            <div className="space-y-1.5 text-xs text-slate-300 pt-2 border-t border-white/5">
              <div className="flex items-center gap-2">
                <Briefcase className="w-3.5 h-3.5 text-cyan-400 flex-shrink-0" />
                <span className="truncate">{u.designation} • {u.department}</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                <span className="truncate text-slate-400">{u.email}</span>
              </div>
              <div className="flex items-center gap-2">
                <DollarSign className="w-3.5 h-3.5 text-emerald-400 flex-shrink-0" />
                <span>Rate: <strong className="text-white">${u.hourlyRate}/h</strong></span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Create User Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Register User">
        <form onSubmit={handleCreateUser} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Full Name *</label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                className="form-input text-xs"
                placeholder="e.g. Maya Lin"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Username *</label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="form-input text-xs"
                placeholder="e.g. mayal"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="form-input text-xs"
                placeholder="maya@pmtrack.io"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Password *</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="form-input text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Role *</label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className="form-select text-xs"
              >
                {ROLES.map((r) => (
                  <option key={r.value} value={r.value}>{r.label}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Hourly Rate ($)</label>
              <input
                type="number"
                value={hourlyRate}
                onChange={(e) => setHourlyRate(e.target.value)}
                className="form-input text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Department</label>
              <input
                type="text"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                className="form-input text-xs"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Designation</label>
              <input
                type="text"
                value={designation}
                onChange={(e) => setDesignation(e.target.value)}
                className="form-input text-xs"
              />
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary btn-sm">Cancel</button>
            <button type="submit" disabled={submitting} className="btn btn-primary btn-sm">
              {submitting ? 'Creating...' : 'Create Account'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

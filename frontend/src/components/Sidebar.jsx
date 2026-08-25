import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  FolderKanban,
  CheckSquare,
  Trello,
  Clock,
  CheckCircle2,
  BarChart3,
  BrainCircuit,
  FileSearch,
  Users2,
  Settings,
  ShieldCheck,
  Calendar,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

export const Sidebar = () => {
  const { user, hasRole } = useAuth();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'TEAM_LEAD', 'EMPLOYEE', 'MANAGEMENT', 'FINANCE_HR'] },
    { label: 'Projects', path: '/projects', icon: FolderKanban, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'TEAM_LEAD', 'EMPLOYEE', 'MANAGEMENT', 'FINANCE_HR'] },
    { label: 'My Tasks', path: '/tasks', icon: CheckSquare, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'TEAM_LEAD', 'EMPLOYEE'] },
    { label: 'Task Board (Kanban)', path: '/kanban', icon: Trello, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'TEAM_LEAD', 'EMPLOYEE'] },
    { label: 'Timesheet', path: '/timesheets', icon: Clock, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'TEAM_LEAD', 'EMPLOYEE'] },
    { label: 'Approvals', path: '/approvals', icon: CheckCircle2, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER'], badge: 'Review' },
    { label: 'Reports & Analytics', path: '/reports', icon: BarChart3, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'MANAGEMENT', 'FINANCE_HR'] },
    { label: 'AI Copilot & Ops', path: '/ai-copilot', icon: BrainCircuit, roles: ['SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'MANAGEMENT'], badge: 'AI' },
    { label: 'Audit Trail', path: '/audit-logs', icon: FileSearch, roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGEMENT', 'PROJECT_MANAGER'] },
    { label: 'User Directory', path: '/admin/users', icon: Users2, roles: ['SUPER_ADMIN', 'ADMIN'] },
  ];

  const allowedNav = navItems.filter((item) => !item.roles || hasRole(...item.roles));

  return (
    <aside
      className={`relative z-20 flex-shrink-0 bg-slate-950/70 backdrop-blur-xl border-r border-white/5 flex flex-col justify-between transition-all duration-300 ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      <div className="py-4 px-3 flex-1 flex flex-col gap-1 overflow-y-auto">
        <div className="px-3 mb-2 flex items-center justify-between">
          {!collapsed && (
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Workspace Menu</span>
          )}
          <button
            onClick={() => setCollapsed(!collapsed)}
            className="p-1 rounded-md text-slate-400 hover:text-white hover:bg-slate-800 transition-colors ml-auto"
            title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {allowedNav.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname.startsWith(item.path);

          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group relative ${
                isActive
                  ? 'bg-gradient-to-r from-indigo-600/30 to-indigo-600/10 text-indigo-300 border border-indigo-500/30 shadow-lg shadow-indigo-500/10'
                  : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/50'
              }`}
              title={collapsed ? item.label : undefined}
            >
              <Icon className={`w-4 h-4 flex-shrink-0 transition-colors ${isActive ? 'text-indigo-400' : 'group-hover:text-slate-200'}`} />
              {!collapsed && <span className="flex-1 truncate">{item.label}</span>}
              {!collapsed && item.badge && (
                <span className="text-[9px] font-extrabold uppercase px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
                  {item.badge}
                </span>
              )}
            </NavLink>
          );
        })}
      </div>

      {/* Footer User Info */}
      <div className="p-3 border-t border-white/5">
        <div className={`p-2.5 rounded-xl bg-slate-900/60 border border-white/5 flex items-center ${collapsed ? 'justify-center' : 'gap-3'}`}>
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse flex-shrink-0" title="Connected" />
          {!collapsed && (
            <div className="flex-1 truncate">
              <p className="text-[11px] font-medium text-slate-300 truncate">Platform Active</p>
              <p className="text-[10px] text-slate-500">v1.0.0 Enterprise</p>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
};

import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, FolderKanban, CheckSquare, Trello, Clock,
  CheckCircle2, BarChart3, BrainCircuit, FileSearch, Users2,
  ChevronLeft, ChevronRight, PanelLeftClose, PanelLeft
} from 'lucide-react';

export const Sidebar = () => {
  const { user, hasRole } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  const navGroups = [
    {
      label: 'WORKSPACE',
      items: [
        { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD','EMPLOYEE','MANAGEMENT','FINANCE_HR'] },
        { label: 'Projects', path: '/projects', icon: FolderKanban, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD','EMPLOYEE','MANAGEMENT','FINANCE_HR'] },
        { label: 'My Tasks', path: '/tasks', icon: CheckSquare, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD','EMPLOYEE'] },
        { label: 'Task Board', path: '/kanban', icon: Trello, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD','EMPLOYEE'] },
        { label: 'Timesheets', path: '/timesheets', icon: Clock, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','TEAM_LEAD','EMPLOYEE'] },
      ],
    },
    {
      label: 'MANAGEMENT',
      items: [
        { label: 'Approvals', path: '/approvals', icon: CheckCircle2, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER'] },
        { label: 'Reports & Analytics', path: '/reports', icon: BarChart3, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT','FINANCE_HR'] },
        { label: 'AI Insights', path: '/ai-copilot', icon: BrainCircuit, roles: ['SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT'] },
        { label: 'Audit Trail', path: '/audit-logs', icon: FileSearch, roles: ['SUPER_ADMIN','ADMIN','MANAGEMENT','PROJECT_MANAGER'] },
        { label: 'User Directory', path: '/admin/users', icon: Users2, roles: ['SUPER_ADMIN','ADMIN'] },
      ],
    },
  ];

  return (
    <aside className={`pm-sidebar ${collapsed ? 'pm-sidebar--collapsed' : ''}`}>
      <div className="pm-sidebar__scroll">
        <div className="pm-sidebar__workspace">
          {!collapsed && (
            <div className="pm-sidebar__workspace-text">
              <span>WORKSPACE</span>
              <strong>{user?.department || 'Project Operations'}</strong>
            </div>
          )}
          <button
            type="button"
            className="pm-sidebar__collapse"
            onClick={() => setCollapsed(!collapsed)}
            title={collapsed ? 'Expand navigation' : 'Collapse navigation'}
          >
            {collapsed ? <PanelLeft className="w-4 h-4" /> : <PanelLeftClose className="w-4 h-4" />}
          </button>
        </div>

        {navGroups.map((group) => {
          const allowed = group.items.filter((item) => hasRole(...item.roles));
          if (!allowed.length) return null;
          return (
            <div className="pm-sidebar__group" key={group.label}>
              {!collapsed && <div className="pm-sidebar__label">{group.label}</div>}
              {allowed.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.path}
                    to={item.path}
                    title={collapsed ? item.label : undefined}
                    className={({ isActive }) => `pm-nav-item ${isActive ? 'is-active' : ''}`}
                  >
                    <span className="pm-nav-item__icon"><Icon /></span>
                    {!collapsed && <span className="pm-nav-item__text">{item.label}</span>}
                  </NavLink>
                );
              })}
            </div>
          );
        })}
      </div>

      <div className="pm-sidebar__footer">
        <div className={`pm-sidebar__status ${collapsed ? 'is-collapsed' : ''}`}>
          <span className="pm-status-dot" />
          {!collapsed && (
            <div>
              <strong>System operational</strong>
              <span>PMTrack Enterprise</span>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
};

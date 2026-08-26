import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import {
  Bell, Search, ChevronDown, LogOut, Sparkles, CheckCircle, Layers
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const DEMO_PERSONAS = [
  { username: 'prasanna', name: 'Prasanna Lohar', role: 'PROJECT_MANAGER', label: 'Project Manager', color: '#4f46e5' },
  { username: 'rahul', name: 'Rahul Sharma', role: 'EMPLOYEE', label: 'Employee', color: '#0891b2' },
  { username: 'priya', name: 'Priya Singh', role: 'EMPLOYEE', label: 'Employee', color: '#db2777' },
  { username: 'amit', name: 'Amit Verma', role: 'EMPLOYEE', label: 'Employee', color: '#059669' },
  { username: 'neha', name: 'Neha Patel', role: 'TEAM_LEAD', label: 'Team Lead', color: '#d97706' },
  { username: 'admin', name: 'System Admin', role: 'SUPER_ADMIN', label: 'Super Admin', color: '#7c3aed' },
  { username: 'executive', name: 'Vikram Mehta', role: 'MANAGEMENT', label: 'Management', color: '#2563eb' },
  { username: 'finance', name: 'Ananya Roy', role: 'FINANCE_HR', label: 'Finance / HR', color: '#0d9488' },
];

export const Navbar = () => {
  const { user, logout, switchUser } = useAuth();
  const demoMode = import.meta.env.VITE_DEMO_MODE !== 'false';
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();
  const [showPersonaMenu, setShowPersonaMenu] = useState(false);
  const [showNotifMenu, setShowNotifMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const navigate = useNavigate();

  const closeMenus = () => {
    setShowPersonaMenu(false);
    setShowNotifMenu(false);
    setShowUserMenu(false);
  };

  const handlePersonaSelect = async (username) => {
    setShowPersonaMenu(false);
    await switchUser(username);
    navigate('/dashboard');
  };

  return (
    <header className="pm-header">
      <div className="pm-header__brand" onClick={() => navigate('/dashboard')} role="button">
        <div className="pm-brand-mark"><Layers /></div>
        <div className="pm-brand-copy">
          <strong>PMTrack</strong>
          <span>Project Operations</span>
        </div>
      </div>

      <div className="pm-header__search">
        <Search />
        <input aria-label="Search" placeholder="Search projects, tasks, timesheets..." />
        <kbd>⌘ K</kbd>
      </div>

      <div className="pm-header__actions">
        {demoMode && (
          <div className="pm-menu-wrap">
            <button className="pm-role-button" onClick={() => { setShowPersonaMenu(!showPersonaMenu); setShowNotifMenu(false); setShowUserMenu(false); }}>
              <Sparkles />
              <span>Demo role</span>
              <strong>{user?.fullName?.split(' ')[0] || 'User'}</strong>
              <ChevronDown />
            </button>
            {showPersonaMenu && (
              <div className="pm-dropdown pm-persona-dropdown">
                <div className="pm-dropdown__head">
                  <strong>Switch demo role</strong>
                  <span>Preview the application from another role.</span>
                </div>
                {DEMO_PERSONAS.map((p) => (
                  <button key={p.username} className={`pm-persona ${user?.username === p.username ? 'is-selected' : ''}`} onClick={() => handlePersonaSelect(p.username)}>
                    <span className="pm-persona__avatar" style={{ background: p.color }}>{p.name.charAt(0)}</span>
                    <span><strong>{p.name}</strong><small>{p.label}</small></span>
                    {user?.username === p.username && <CheckCircle />}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        <div className="pm-menu-wrap">
          <button className="pm-icon-button" onClick={() => { setShowNotifMenu(!showNotifMenu); setShowPersonaMenu(false); setShowUserMenu(false); }} aria-label="Notifications">
            <Bell />
            {unreadCount > 0 && <span className="pm-notification-count">{unreadCount}</span>}
          </button>
          {showNotifMenu && (
            <div className="pm-dropdown pm-notification-dropdown">
              <div className="pm-dropdown__head pm-dropdown__head--row">
                <div><strong>Notifications</strong><span>{unreadCount ? `${unreadCount} unread` : 'All caught up'}</span></div>
                {unreadCount > 0 && <button onClick={markAllAsRead}>Mark all read</button>}
              </div>
              <div className="pm-notification-list">
                {!notifications.length ? <div className="pm-empty-mini">No notifications yet.</div> : notifications.map((n) => (
                  <button key={n.id} className={`pm-notification ${!n.isRead ? 'is-unread' : ''}`} onClick={() => { markAsRead(n.id); if (n.linkUrl) navigate(n.linkUrl); closeMenus(); }}>
                    <span className="pm-notification__dot" />
                    <span><strong>{n.title}</strong><small>{n.message}</small></span>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="pm-menu-wrap">
          <button className="pm-user-button" onClick={() => { setShowUserMenu(!showUserMenu); setShowPersonaMenu(false); setShowNotifMenu(false); }}>
            <img src={user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'} alt="" />
            <span className="pm-user-button__copy"><strong>{user?.fullName || 'User'}</strong><small>{user?.role?.replaceAll('_', ' ')}</small></span>
            <ChevronDown />
          </button>
          {showUserMenu && (
            <div className="pm-dropdown pm-user-dropdown">
              <div className="pm-profile-summary">
                <img src={user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'} alt="" />
                <div><strong>{user?.fullName}</strong><span>{user?.email}</span></div>
              </div>
              <div className="pm-profile-role">{user?.role?.replaceAll('_', ' ')}</div>
              <button className="pm-signout" onClick={logout}><LogOut /> Sign out</button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

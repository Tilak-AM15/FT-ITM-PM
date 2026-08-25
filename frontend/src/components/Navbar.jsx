import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import {
  Bell,
  Search,
  ChevronDown,
  User,
  LogOut,
  Sparkles,
  CheckCircle,
  Clock,
  Layers,
  Shield,
  Briefcase,
  Users,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const DEMO_PERSONAS = [
  { username: 'prasanna', name: 'Prasanna Lohar', role: 'PROJECT_MANAGER', label: 'PM / Head of Eng', color: '#6366f1' },
  { username: 'rahul', name: 'Rahul Sharma', role: 'EMPLOYEE', label: 'Senior Developer', color: '#06b6d4' },
  { username: 'priya', name: 'Priya Singh', role: 'EMPLOYEE', label: 'UI/UX & React Dev', color: '#ec4899' },
  { username: 'amit', name: 'Amit Verma', role: 'EMPLOYEE', label: 'Backend Java Dev', color: '#10b981' },
  { username: 'neha', name: 'Neha Patel', role: 'TEAM_LEAD', label: 'Lead QA & Release', color: '#f59e0b' },
  { username: 'admin', name: 'System Admin', role: 'SUPER_ADMIN', label: 'Super Admin', color: '#8b5cf6' },
  { username: 'executive', name: 'Vikram Mehta', role: 'MANAGEMENT', label: 'Executive / VP', color: '#3b82f6' },
  { username: 'finance', name: 'Ananya Roy', role: 'FINANCE_HR', label: 'Finance Controller', color: '#14b8a6' },
];

export const Navbar = ({ onOpenTimeModal, onOpenProjectModal }) => {
  const { user, logout, switchUser } = useAuth();
  const demoMode = import.meta.env.VITE_DEMO_MODE !== 'false';
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();
  const [showPersonaMenu, setShowPersonaMenu] = useState(false);
  const [showNotifMenu, setShowNotifMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const navigate = useNavigate();

  const handlePersonaSelect = async (username) => {
    setShowPersonaMenu(false);
    await switchUser(username);
    navigate('/dashboard');
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-white/5 bg-slate-950/80 backdrop-blur-md px-4 lg:px-8 py-3 flex items-center justify-between">
      {/* Brand & Search */}
      <div className="flex items-center gap-6">
        <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate('/dashboard')}>
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-indigo-500/25">
            <Layers className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-heading font-extrabold text-lg text-white tracking-tight">PMTrack</span>
              <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 uppercase">Enterprise</span>
            </div>
            <p className="text-[11px] text-slate-400 font-medium tracking-wide">Future Transformation</p>
          </div>
        </div>

        {/* Search Bar */}
        <div className="hidden md:flex items-center gap-2 bg-slate-900/90 border border-white/10 rounded-lg px-3 py-1.5 w-64 lg:w-80 text-sm focus-within:border-indigo-500 focus-within:ring-1 focus-within:ring-indigo-500 transition-all">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search projects, tasks, timesheets..."
            className="bg-transparent border-none outline-none text-slate-200 placeholder-slate-500 w-full text-xs"
          />
          <kbd className="hidden lg:inline-block text-[10px] font-mono text-slate-500 bg-slate-800 px-1.5 py-0.5 rounded border border-slate-700">⌘K</kbd>
        </div>
      </div>

      {/* Actions & Profile */}
      <div className="flex items-center gap-3">
        {/* Quick Demo Switcher */}
        {demoMode && <div className="relative">
          <button
            onClick={() => { setShowPersonaMenu(!showPersonaMenu); setShowNotifMenu(false); setShowUserMenu(false); }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-indigo-950/60 border border-indigo-500/30 text-indigo-300 hover:bg-indigo-900/60 hover:text-white transition-all text-xs font-semibold"
          >
            <Sparkles className="w-3.5 h-3.5 text-indigo-400 animate-pulse" />
            <span className="hidden sm:inline">Role Switcher:</span>
            <span className="text-white bg-indigo-600/40 px-1.5 py-0.5 rounded">{user?.fullName?.split(' ')[0] || 'Persona'}</span>
            <ChevronDown className="w-3.5 h-3.5" />
          </button>

          {showPersonaMenu && (
            <div className="absolute right-0 mt-2 w-72 bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-xl shadow-2xl p-2 z-50 animate-in fade-in zoom-in-95 duration-100">
              <div className="px-3 py-2 border-b border-white/5">
                <p className="text-xs font-bold text-slate-300 uppercase tracking-wider">1-Click Persona Testing</p>
                <p className="text-[11px] text-slate-400">Switch instantly to experience role-specific views.</p>
              </div>
              <div className="py-1 max-h-64 overflow-y-auto">
                {DEMO_PERSONAS.map((p) => (
                  <button
                    key={p.username}
                    onClick={() => handlePersonaSelect(p.username)}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded-lg text-left text-xs transition-colors hover:bg-slate-800/80 ${
                      user?.username === p.username ? 'bg-indigo-600/20 text-indigo-300 font-semibold' : 'text-slate-300'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <div className="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold text-white shadow-sm" style={{ backgroundColor: p.color }}>
                        {p.name[0]}
                      </div>
                      <div>
                        <p className="font-medium text-slate-200">{p.name}</p>
                        <p className="text-[10px] text-slate-400">{p.label}</p>
                      </div>
                    </div>
                    {user?.username === p.username && <CheckCircle className="w-4 h-4 text-indigo-400" />}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>}

        {/* Notifications Dropdown */}
        <div className="relative">
          <button
            onClick={() => { setShowNotifMenu(!showNotifMenu); setShowPersonaMenu(false); setShowUserMenu(false); }}
            className="relative p-2 rounded-lg bg-slate-900 border border-white/5 text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 flex h-4 min-w-4 px-1 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white animate-pulse">
                {unreadCount}
              </span>
            )}
          </button>

          {showNotifMenu && (
            <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-xl shadow-2xl z-50">
              <div className="flex items-center justify-between px-4 py-3 border-b border-white/5">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-slate-200 uppercase tracking-wider">Notifications</span>
                  {unreadCount > 0 && (
                    <span className="bg-rose-500/20 text-rose-300 text-[10px] font-semibold px-2 py-0.5 rounded-full border border-rose-500/30">
                      {unreadCount} new
                    </span>
                  )}
                </div>
                {unreadCount > 0 && (
                  <button onClick={markAllAsRead} className="text-[11px] text-indigo-400 hover:text-indigo-300 font-medium">
                    Mark all read
                  </button>
                )}
              </div>
              <div className="max-h-72 overflow-y-auto divide-y divide-white/5">
                {notifications.length === 0 ? (
                  <div className="p-6 text-center text-xs text-slate-400">No notifications yet.</div>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      onClick={() => { markAsRead(n.id); if (n.linkUrl) navigate(n.linkUrl); setShowNotifMenu(false); }}
                      className={`p-3.5 hover:bg-slate-800/60 cursor-pointer transition-colors ${!n.isRead ? 'bg-indigo-950/20' : ''}`}
                    >
                      <div className="flex items-start gap-2.5">
                        <div className="w-2 h-2 rounded-full mt-1.5 flex-shrink-0" style={{ backgroundColor: !n.isRead ? '#6366f1' : '#475569' }} />
                        <div className="flex-1">
                          <p className="text-xs font-semibold text-slate-200">{n.title}</p>
                          <p className="text-[11px] text-slate-400 mt-0.5 leading-relaxed">{n.message}</p>
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* User Avatar Menu */}
        <div className="relative">
          <button
            onClick={() => { setShowUserMenu(!showUserMenu); setShowPersonaMenu(false); setShowNotifMenu(false); }}
            className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-lg bg-slate-900 border border-white/5 hover:border-white/15 transition-colors"
          >
            <img
              src={user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
              alt={user?.fullName}
              className="w-6 h-6 rounded-full object-cover ring-1 ring-indigo-500/50"
            />
            <div className="hidden sm:block text-left">
              <p className="text-xs font-semibold text-slate-200 leading-tight">{user?.fullName}</p>
              <p className="text-[10px] text-indigo-400 font-medium">{user?.role?.replace('_', ' ')}</p>
            </div>
            <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
          </button>

          {showUserMenu && (
            <div className="absolute right-0 mt-2 w-56 bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-xl shadow-2xl p-1 z-50">
              <div className="px-3 py-2.5 border-b border-white/5">
                <p className="text-xs font-bold text-slate-200">{user?.fullName}</p>
                <p className="text-[11px] text-slate-400 truncate">{user?.email}</p>
                <span className="inline-block mt-1 text-[10px] font-semibold uppercase px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  {user?.role}
                </span>
              </div>
              <button
                onClick={logout}
                className="w-full flex items-center gap-2 px-3 py-2 mt-1 rounded-lg text-left text-xs text-rose-400 hover:bg-rose-950/40 hover:text-rose-300 transition-colors"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Sign Out</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { ExecutiveDashboard } from './ExecutiveDashboard';
import { EmployeeDashboard } from './EmployeeDashboard';
import { LayoutDashboard, UserCheck, DollarSign, ShieldAlert } from 'lucide-react';

export const DashboardRouter = ({ onOpenProjectModal, onOpenTimeModal }) => {
  const { user } = useAuth();
  
  // Default view based on role
  const defaultTab = (user?.role === 'EMPLOYEE') ? 'employee' : 'executive';
  const [activeTab, setActiveTab] = useState(defaultTab);

  return (
    <div className="space-y-6">
      {/* Top View Selector Pill */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 p-1 rounded-xl bg-slate-900 border border-white/5">
          <button
            onClick={() => setActiveTab('executive')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'executive'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <LayoutDashboard className="w-3.5 h-3.5" />
            <span>Management & PM View</span>
          </button>

          <button
            onClick={() => setActiveTab('employee')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              activeTab === 'employee'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <UserCheck className="w-3.5 h-3.5" />
            <span>Employee Workspace</span>
          </button>
        </div>
      </div>

      {activeTab === 'executive' ? (
        <ExecutiveDashboard
          onOpenProjectModal={onOpenProjectModal}
          onOpenTimeModal={onOpenTimeModal}
          scope={user?.role === 'PROJECT_MANAGER' || user?.role === 'TEAM_LEAD' ? 'pm' : 'management'}
        />
      ) : (
        <EmployeeDashboard />
      )}
    </div>
  );
};

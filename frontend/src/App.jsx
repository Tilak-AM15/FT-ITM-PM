import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';

// Pages
import { Login } from './pages/Login';
import { DashboardRouter } from './pages/Dashboards/DashboardRouter';
import { ProjectList } from './pages/Projects/ProjectList';
import { ProjectDetail } from './pages/Projects/ProjectDetail';
import { TaskList } from './pages/Tasks/TaskList';
import { TaskKanban } from './pages/Tasks/TaskKanban';
import { TimesheetHub } from './pages/Timesheets/TimesheetHub';
import { ApprovalDesk } from './pages/Approvals/ApprovalDesk';
import { ReportsHub } from './pages/Reports/ReportsHub';
import { AiInsightsHub } from './pages/AiCopilot/AiInsightsHub';
import { AuditLogView } from './pages/AuditLogs/AuditLogView';
import { UserManagement } from './pages/Admin/UserManagement';

const ProtectedLayout = () => {
  const { isAuthenticated } = useAuth();
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);
  const [isTimeModalOpen, setIsTimeModalOpen] = useState(false);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col relative overflow-hidden">
      {/* Background Ambient Glow */}
      <div className="ambient-glow" />

      {/* Top Navbar */}
      <Navbar
        onOpenProjectModal={() => setIsProjectModalOpen(true)}
        onOpenTimeModal={() => setIsTimeModalOpen(true)}
      />

      <div className="flex-1 flex overflow-hidden">
        {/* Responsive Sidebar */}
        <Sidebar />

        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-8 relative z-10 max-w-7xl mx-auto w-full">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route
              path="/dashboard"
              element={
                <DashboardRouter
                  onOpenProjectModal={() => setIsProjectModalOpen(true)}
                  onOpenTimeModal={() => setIsTimeModalOpen(true)}
                />
              }
            />
            <Route
              path="/projects"
              element={
                <ProjectList
                  isModalOpen={isProjectModalOpen}
                  setIsModalOpen={setIsProjectModalOpen}
                />
              }
            />
            <Route path="/projects/:id" element={<ProjectDetail />} />
            <Route path="/tasks" element={<TaskList />} />
            <Route path="/kanban" element={<TaskKanban />} />
            <Route path="/timesheets" element={<TimesheetHub />} />
            <Route path="/approvals" element={<ApprovalDesk />} />
            <Route path="/reports" element={<ReportsHub />} />
            <Route path="/ai-copilot" element={<AiInsightsHub />} />
            <Route path="/audit-logs" element={<AuditLogView />} />
            <Route path="/admin/users" element={<UserManagement />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <NotificationProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/*" element={<ProtectedLayout />} />
          </Routes>
        </Router>
      </NotificationProvider>
    </AuthProvider>
  );
}

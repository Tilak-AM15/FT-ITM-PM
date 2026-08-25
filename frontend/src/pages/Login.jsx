import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Layers, Lock, User, ArrowRight, Sparkles, Shield, CheckCircle2 } from 'lucide-react';

export const Login = () => {
  const { login, switchUser, loading } = useAuth();
  const demoMode = import.meta.env.VITE_DEMO_MODE !== 'false';
  const [username, setUsername] = useState(demoMode ? 'prasanna' : '');
  const [password, setPassword] = useState(demoMode ? 'password123' : '');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid username or password');
    }
  };

  const handleQuickLogin = async (user) => {
    setError('');
    try {
      await switchUser(user);
      navigate('/dashboard');
    } catch (err) {
      setError('Quick login failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 p-4 relative overflow-hidden">
      {/* Background ambient lighting */}
      <div className="ambient-glow" />
      <div className="absolute -top-40 -left-40 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-cyan-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-12 gap-8 relative z-10 items-center">
        {/* Left Side: Branding & Value Proposition */}
        <div className="lg:col-span-6 space-y-6 text-center lg:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Future Transformation Enterprise Platform</span>
          </div>

          <h1 className="text-4xl lg:text-5xl font-extrabold text-white tracking-tight leading-tight font-heading">
            Integrated Project & <span className="bg-gradient-to-r from-indigo-400 via-cyan-400 to-indigo-200 bg-clip-text text-transparent">Timesheet Platform</span>
          </h1>

          <p className="text-slate-400 text-sm lg:text-base leading-relaxed">
            One unified digital operating system to <strong className="text-slate-200">Plan, Execute, Track, Approve, and Measure</strong> every project and resource effort with complete auditability.
          </p>

          {/* Key Value Points */}
          <div className="grid grid-cols-2 gap-3 pt-2">
            <div className="p-3 rounded-xl bg-slate-900/60 border border-white/5 flex items-start gap-2.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 mt-0.5 flex-shrink-0" />
              <div>
                <p className="text-xs font-bold text-slate-200">Single Workflow</p>
                <p className="text-[11px] text-slate-400">Project $\to$ Task $\to$ Timesheet $\to$ Approval</p>
              </div>
            </div>
            <div className="p-3 rounded-xl bg-slate-900/60 border border-white/5 flex items-start gap-2.5">
              <CheckCircle2 className="w-4 h-4 text-cyan-400 mt-0.5 flex-shrink-0" />
              <div>
                <p className="text-xs font-bold text-slate-200">Executive KPIs</p>
                <p className="text-[11px] text-slate-400">Real-time effort, variance & utilization</p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Login & Persona Quick-Select */}
        <div className="lg:col-span-6">
          <div className="glass-card p-6 sm:p-8 relative">
            <div className="flex items-center justify-between pb-6 mb-6 border-b border-white/5">
              <div>
                <h2 className="text-xl font-bold text-white font-heading">Sign In to PMTrack</h2>
                <p className="text-xs text-slate-400 mt-0.5">Enter credentials or choose a pre-configured persona below.</p>
              </div>
              <div className="w-10 h-10 rounded-xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
                <Shield className="w-5 h-5" />
              </div>
            </div>

            {error && (
              <div className="p-3 mb-4 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="form-group">
                <label className="form-label">Username</label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
                  <input
                    type="text"
                    required
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="form-input pl-9"
                    placeholder="e.g. prasanna"
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Password</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="form-input pl-9"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary w-full py-2.5 text-sm font-semibold"
              >
                <span>{loading ? 'Authenticating...' : 'Sign In'}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </form>

            {demoMode && <>
            {/* Quick Demo Personas */}
            <div className="mt-6 pt-5 border-t border-white/5">
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2.5">
                ⚡ 1-Click Demo Personas (No Password Required)
              </p>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => handleQuickLogin('prasanna')}
                  className="p-2 rounded-lg bg-indigo-950/40 hover:bg-indigo-900/60 border border-indigo-500/20 text-left transition-all group"
                >
                  <p className="text-xs font-bold text-indigo-300 group-hover:text-white">Prasanna Lohar</p>
                  <p className="text-[10px] text-slate-400">Project Manager / Lead</p>
                </button>
                <button
                  onClick={() => handleQuickLogin('rahul')}
                  className="p-2 rounded-lg bg-cyan-950/40 hover:bg-cyan-900/60 border border-cyan-500/20 text-left transition-all group"
                >
                  <p className="text-xs font-bold text-cyan-300 group-hover:text-white">Rahul Sharma</p>
                  <p className="text-[10px] text-slate-400">Employee / Developer</p>
                </button>
                <button
                  onClick={() => handleQuickLogin('executive')}
                  className="p-2 rounded-lg bg-slate-900 hover:bg-slate-800 border border-white/10 text-left transition-all group"
                >
                  <p className="text-xs font-bold text-slate-200 group-hover:text-white">Executive Management</p>
                  <p className="text-[10px] text-slate-400">VP Operations / KPIs</p>
                </button>
                <button
                  onClick={() => handleQuickLogin('admin')}
                  className="p-2 rounded-lg bg-purple-950/40 hover:bg-purple-900/60 border border-purple-500/20 text-left transition-all group"
                >
                  <p className="text-xs font-bold text-purple-300 group-hover:text-white">Super Admin</p>
                  <p className="text-[10px] text-slate-400">RBAC & Master Data</p>
                </button>
              </div>
            </div>
            </>}
          </div>
        </div>
      </div>
    </div>
  );
};

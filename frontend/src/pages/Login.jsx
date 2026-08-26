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
    try { await login(username, password); navigate('/dashboard'); }
    catch (err) { setError(err.response?.data?.message || 'Invalid username or password'); }
  };

  const handleQuickLogin = async (user) => {
    setError('');
    try { await switchUser(user); navigate('/dashboard'); }
    catch { setError('Quick login failed'); }
  };

  const personas = [
    ['prasanna', 'Prasanna Lohar', 'Project Manager'],
    ['rahul', 'Rahul Sharma', 'Employee'],
    ['priya', 'Priya Singh', 'Employee'],
    ['amit', 'Amit Verma', 'Employee'],
    ['neha', 'Neha Patel', 'Team Lead'],
  ];

  return (
    <div className="pm-login">
      <div className="pm-login__glow pm-login__glow--one" />
      <div className="pm-login__glow pm-login__glow--two" />

      <div className="pm-login__layout">
        <section className="pm-login__intro">
          <div className="pm-login__brand">
            <div className="pm-brand-mark"><Layers /></div>
            <div><strong>PMTrack</strong><span>Project Operations Platform</span></div>
          </div>

          <div className="pm-login__eyebrow"><Sparkles /> FUTURE TRANSFORMATION</div>
          <h1>Plan work.<br /><span>Track progress.</span><br />Deliver better.</h1>
          <p>One workspace for projects, tasks, people, timesheets and approvals — designed to keep delivery teams aligned.</p>

          <div className="pm-login__flow">
            {['Projects', 'Tasks', 'Timesheets', 'Approvals'].map((item, i) => (
              <React.Fragment key={item}>
                <div><span>{i + 1}</span><strong>{item}</strong></div>
                {i < 3 && <i>→</i>}
              </React.Fragment>
            ))}
          </div>
        </section>

        <section className="pm-login__card">
          <div className="pm-login__card-head">
            <div>
              <span className="pm-login__card-kicker">WELCOME BACK</span>
              <h2>Sign in to PMTrack</h2>
              <p>Use your workspace credentials to continue.</p>
            </div>
            <div className="pm-login__secure"><Shield /></div>
          </div>

          {error && <div className="pm-alert">{error}</div>}

          <form onSubmit={handleSubmit} className="pm-login-form">
            <label>Username
              <div className="pm-field">
                <User />
                <input type="text" required value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Enter username" />
              </div>
            </label>

            <label>Password
              <div className="pm-field">
                <Lock />
                <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Enter password" />
              </div>
            </label>

            <button className="pm-login-submit" type="submit" disabled={loading}>
              <span>{loading ? 'Signing in…' : 'Sign in'}</span>
              {!loading && <ArrowRight />}
            </button>
          </form>

          {demoMode && (
            <div className="pm-demo">
              <div className="pm-demo__head">
                <div><strong>Demo workspace</strong><span>Choose a role to explore the UI.</span></div>
                <span className="pm-demo__badge">DEMO</span>
              </div>
              <div className="pm-demo__grid">
                {personas.map(([id, name, role]) => (
                  <button key={id} onClick={() => handleQuickLogin(id)}>
                    <span className="pm-demo__avatar">{name.charAt(0)}</span>
                    <span><strong>{name}</strong><small>{role}</small></span>
                    <ArrowRight />
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="pm-login__footer">
            <CheckCircle2 /> Secure role-based workspace
          </div>
        </section>
      </div>
    </div>
  );
};

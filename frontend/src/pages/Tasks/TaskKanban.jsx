import React, { useState, useEffect } from 'react';
import { taskApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import {
  Trello,
  Plus,
  Clock,
  Calendar,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  ArrowLeft,
  MessageSquare,
  ListTodo,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const COLUMNS = [
  { id: 'TO_DO', title: 'To Do', color: '#94a3b8', border: 'border-slate-700' },
  { id: 'IN_PROGRESS', title: 'In Progress', color: '#06b6d4', border: 'border-cyan-500/30' },
  { id: 'BLOCKED', title: 'Blocked', color: '#ef4444', border: 'border-rose-500/30' },
  { id: 'COMPLETED', title: 'Completed', color: '#10b981', border: 'border-emerald-500/30' },
];

export const TaskKanban = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const loadTasks = async () => {
    try {
      const res = await taskApi.getMyTasks();
      setTasks(res.data || []);
    } catch (err) {
      console.error('Failed to load tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleMoveStatus = async (taskId, newStatus) => {
    try {
      await taskApi.updateStatus(taskId, {
        status: newStatus,
        progressPercentage: newStatus === 'COMPLETED' ? 100 : newStatus === 'IN_PROGRESS' ? 50 : 0,
      });
      loadTasks();
    } catch (err) {
      console.error('Failed to update task status:', err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">Interactive Kanban Board</h1>
          <p className="text-xs text-slate-400 mt-0.5">Visualize workflow stages and fast-track sprint execution.</p>
        </div>

        <button onClick={() => navigate('/tasks')} className="btn btn-secondary btn-sm">
          <span>Table View</span>
        </button>
      </div>

      {/* 4 Column Kanban Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-start">
        {COLUMNS.map((col) => {
          const colTasks = tasks.filter((t) => t.status === col.id);

          return (
            <div key={col.id} className={`glass-panel p-4 rounded-xl border ${col.border} space-y-3`}>
              <div className="flex items-center justify-between pb-2 border-b border-white/5">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: col.color }} />
                  <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider">{col.title}</h3>
                </div>
                <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">
                  {colTasks.length}
                </span>
              </div>

              <div className="space-y-3 min-h-[400px]">
                {colTasks.length === 0 ? (
                  <div className="h-32 flex items-center justify-center text-[11px] text-slate-500 border border-dashed border-white/10 rounded-lg">
                    No tasks in {col.title}
                  </div>
                ) : (
                  colTasks.map((t) => (
                    <div
                      key={t.id}
                      className="p-4 rounded-xl bg-slate-900/90 border border-white/10 hover:border-indigo-500/40 shadow-md space-y-3 transition-all group"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <span className="font-mono text-[10px] font-bold text-indigo-400 bg-indigo-500/10 px-1.5 py-0.5 rounded">
                          {t.taskCode}
                        </span>
                        <StatusBadge status={t.priority} />
                      </div>

                      <h4 className="text-xs font-bold text-white leading-tight group-hover:text-indigo-300 transition-colors">
                        {t.title}
                      </h4>

                      <p className="text-[11px] text-slate-400 truncate">
                        {t.projectName}
                      </p>

                      {/* Subtasks Progress */}
                      {t.subTasks && t.subTasks.length > 0 && (
                        <div className="text-[10px] text-slate-400 flex items-center gap-1.5">
                          <ListTodo className="w-3 h-3 text-cyan-400" />
                          <span>
                            {t.subTasks.filter((st) => st.completed).length} / {t.subTasks.length} subtasks
                          </span>
                        </div>
                      )}

                      {/* Footer Row */}
                      <div className="pt-2 border-t border-white/5 flex items-center justify-between text-[10px] text-slate-400">
                        <div className="flex items-center gap-1">
                          <Clock className="w-3 h-3 text-slate-500" />
                          <span>{t.actualHours}h / {t.estimatedHours}h</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-slate-500" />
                          <span>{t.dueDate}</span>
                        </div>
                      </div>

                      {/* Quick Move Buttons */}
                      <div className="pt-2 flex items-center justify-between border-t border-white/5 gap-1">
                        {col.id !== 'TO_DO' && (
                          <button
                            onClick={() => handleMoveStatus(t.id, col.id === 'COMPLETED' ? 'IN_PROGRESS' : col.id === 'BLOCKED' ? 'IN_PROGRESS' : 'TO_DO')}
                            className="p-1 rounded text-slate-400 hover:text-white hover:bg-slate-800 transition-colors text-[10px] flex items-center gap-0.5"
                            title="Move backwards"
                          >
                            <ArrowLeft className="w-3 h-3" />
                            <span>Prev</span>
                          </button>
                        )}
                        {col.id !== 'BLOCKED' && col.id !== 'COMPLETED' && (
                          <button
                            onClick={() => handleMoveStatus(t.id, 'BLOCKED')}
                            className="p-1 rounded text-rose-400 hover:bg-rose-950/40 transition-colors text-[10px]"
                            title="Mark Blocked"
                          >
                            Block
                          </button>
                        )}
                        {col.id !== 'COMPLETED' && (
                          <button
                            onClick={() => handleMoveStatus(t.id, col.id === 'TO_DO' ? 'IN_PROGRESS' : 'COMPLETED')}
                            className="p-1 rounded text-indigo-400 hover:text-indigo-300 hover:bg-indigo-950/40 transition-colors text-[10px] flex items-center gap-0.5 ml-auto"
                            title="Advance stage"
                          >
                            <span>{col.id === 'TO_DO' ? 'Start' : 'Complete'}</span>
                            <ArrowRight className="w-3 h-3" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

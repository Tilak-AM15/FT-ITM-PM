import React, { useState, useEffect } from 'react';
import { auditApi } from '../../api/endpoints';
import {
  FileSearch,
  Search,
  Filter,
  Shield,
  Clock,
  User,
} from 'lucide-react';

export const AuditLogView = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [entityFilter, setEntityFilter] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const loadLogs = async () => {
    try {
      const res = await auditApi.getAll();
      setLogs(res.data || []);
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLogs();
  }, []);

  const filteredLogs = logs.filter((l) => {
    const matchEntity = entityFilter === 'ALL' || l.entityName === entityFilter;
    const matchSearch =
      l.action?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      l.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      l.details?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchEntity && matchSearch;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">System Audit Trail</h1>
            <span className="bg-emerald-500/20 text-emerald-400 text-xs font-bold px-2.5 py-0.5 rounded-full border border-emerald-500/30">
              Immutable Log
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Captures 100% of critical lifecycle transactions: User + Action + Timestamp + Previous & New Values.</p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="glass-panel p-4 flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-2 bg-slate-900 border border-white/10 rounded-lg px-3 py-1.5 w-full md:w-80 text-xs">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search action, user, or details..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="bg-transparent border-none outline-none text-slate-200 w-full"
          />
        </div>

        <select
          value={entityFilter}
          onChange={(e) => setEntityFilter(e.target.value)}
          className="form-select text-xs py-1.5 w-full md:w-48"
        >
          <option value="ALL">All Entities</option>
          <option value="Project">Project</option>
          <option value="Task">Task</option>
          <option value="Timesheet">Timesheet</option>
          <option value="User">User</option>
        </select>
      </div>

      {/* Audit Log Table */}
      <div className="glass-card p-6 space-y-4">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
          </div>
        ) : filteredLogs.length === 0 ? (
          <div className="text-center py-12 text-slate-400 text-xs">No audit logs found.</div>
        ) : (
          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>User</th>
                  <th>Action</th>
                  <th>Entity</th>
                  <th>Previous Value</th>
                  <th>New Value</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {filteredLogs.map((l) => (
                  <tr key={l.id}>
                    <td className="font-mono text-xs text-slate-400 whitespace-nowrap">{l.timestamp?.replace('T', ' ').slice(0, 19)}</td>
                    <td className="font-bold text-xs text-indigo-300">{l.username}</td>
                    <td>
                      <span className="font-mono text-[11px] font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-200 border border-white/10">
                        {l.action}
                      </span>
                    </td>
                    <td className="text-xs text-slate-300 font-semibold">{l.entityName} #{l.entityId || '—'}</td>
                    <td className="text-xs text-slate-400 max-w-xs truncate">{l.previousValue || '—'}</td>
                    <td className="text-xs text-emerald-400 max-w-xs truncate font-semibold">{l.newValue || '—'}</td>
                    <td className="text-xs text-slate-300 max-w-sm truncate">{l.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

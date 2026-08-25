import React, { useState, useEffect } from 'react';
import { approvalApi } from '../../api/endpoints';
import { StatusBadge } from '../../components/StatusBadge';
import { Modal } from '../../components/Modal';
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  User,
  Check,
  Search,
  CheckCheck,
  RotateCcw,
} from 'lucide-react';

export const ApprovalDesk = () => {
  const [pendingList, setPendingList] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toastMsg, setToastMsg] = useState('');

  // Reject / Correction Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [actionType, setActionType] = useState('REJECT'); // 'REJECT' or 'REQUEST_CORRECTION'
  const [activeTsId, setActiveTsId] = useState(null);
  const [comment, setComment] = useState('');

  const loadPending = async () => {
    try {
      const res = await approvalApi.getPending();
      setPendingList(res.data || []);
      setSelectedIds([]);
    } catch (err) {
      console.error('Failed to load pending approvals:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPending();
  }, []);

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedIds(pendingList.map((p) => p.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleToggleSelect = (id) => {
    if (selectedIds.includes(id)) {
      setSelectedIds(selectedIds.filter((x) => x !== id));
    } else {
      setSelectedIds([...selectedIds, id]);
    }
  };

  const handleApproveSelected = async () => {
    if (selectedIds.length === 0) return;
    try {
      await approvalApi.process({
        timesheetIds: selectedIds,
        action: 'APPROVE',
        comment: 'Batch approved by Project Manager',
      });
      setToastMsg(`Successfully approved ${selectedIds.length} timesheet(s)!`);
      setTimeout(() => setToastMsg(''), 4000);
      loadPending();
    } catch (err) {
      console.error('Approval failed:', err);
    }
  };

  const handleOpenActionModal = (id, type) => {
    setActiveTsId(id);
    setActionType(type);
    setComment('');
    setIsModalOpen(true);
  };

  const handleConfirmActionModal = async (e) => {
    e.preventDefault();
    if (!comment) return;
    try {
      const ids = activeTsId ? [activeTsId] : selectedIds;
      await approvalApi.process({
        timesheetIds: ids,
        action: actionType,
        comment,
      });
      setIsModalOpen(false);
      setToastMsg(actionType === 'REJECT' ? 'Timesheet rejected with note' : 'Correction requested from employee');
      setTimeout(() => setToastMsg(''), 4000);
      loadPending();
    } catch (err) {
      console.error('Action failed:', err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header matching OCR Page 3 & 6 */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-white/5">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl lg:text-3xl font-extrabold text-white font-heading">Project Manager Approval Desk</h1>
            <span className="bg-indigo-500/20 text-indigo-400 text-xs font-bold px-2.5 py-0.5 rounded-full border border-indigo-500/30">
              {pendingList.length} Pending
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Review, verify, approve, or request revisions for employee timesheet submissions.</p>
        </div>

        {/* Batch Action Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleApproveSelected}
            disabled={selectedIds.length === 0}
            className="btn btn-success btn-sm"
          >
            <CheckCheck className="w-4 h-4" />
            <span>Approve Selected ({selectedIds.length})</span>
          </button>

          <button
            onClick={() => { setActiveTsId(null); setActionType('REJECT'); setIsModalOpen(true); }}
            disabled={selectedIds.length === 0}
            className="btn btn-danger btn-sm"
          >
            <XCircle className="w-4 h-4" />
            <span>Reject Selected</span>
          </button>
        </div>
      </div>

      {toastMsg && (
        <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs font-semibold flex items-center gap-2 animate-in fade-in">
          <Check className="w-4 h-4" />
          <span>{toastMsg}</span>
        </div>
      )}

      {/* Approval Table matching OCR Page 3 */}
      <div className="glass-card p-6 space-y-4">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="w-8 h-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
          </div>
        ) : pendingList.length === 0 ? (
          <div className="text-center py-16 space-y-3">
            <div className="w-12 h-12 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-white">All Caught Up!</h3>
            <p className="text-xs text-slate-400">There are no pending timesheet submissions requiring your review right now.</p>
          </div>
        ) : (
          <div className="table-container">
            <table className="modern-table">
              <thead>
                <tr>
                  <th className="w-10">
                    <input
                      type="checkbox"
                      onChange={handleSelectAll}
                      checked={selectedIds.length === pendingList.length && pendingList.length > 0}
                      className="accent-indigo-500"
                    />
                  </th>
                  <th>Employee</th>
                  <th>Project</th>
                  <th>Task</th>
                  <th>Date</th>
                  <th>Hours</th>
                  <th>Billable</th>
                  <th>Work Description</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {pendingList.map((item) => (
                  <tr key={item.id} className={selectedIds.includes(item.id) ? 'bg-indigo-950/20' : ''}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedIds.includes(item.id)}
                        onChange={() => handleToggleSelect(item.id)}
                        className="accent-indigo-500"
                      />
                    </td>
                    <td>
                      <div className="flex items-center gap-2">
                        <img
                          src={item.user?.avatarUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150'}
                          alt={item.user?.fullName}
                          className="w-7 h-7 rounded-full object-cover ring-1 ring-white/10"
                        />
                        <div>
                          <p className="font-bold text-slate-200 text-xs">{item.user?.fullName}</p>
                          <p className="text-[10px] text-slate-400">{item.user?.designation}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="font-semibold text-xs text-slate-200 block">{item.projectName}</span>
                      <span className="text-[10px] text-slate-400 font-mono">{item.projectCode}</span>
                    </td>
                    <td>
                      <span className="text-xs text-slate-300 block">{item.taskTitle}</span>
                      <span className="text-[10px] text-indigo-400 font-mono">{item.taskCode}</span>
                    </td>
                    <td className="text-xs text-slate-300 font-mono">{item.workDate}</td>
                    <td className="text-xs font-black text-white">{item.hoursWorked}h</td>
                    <td>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${item.billable ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-700 text-slate-300'}`}>
                        {item.billable ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td className="text-xs text-slate-400 max-w-xs truncate" title={item.description}>
                      {item.description}
                    </td>
                    <td><StatusBadge status={item.status} /></td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button
                          onClick={async () => {
                            await approvalApi.process({ timesheetIds: [item.id], action: 'APPROVE', comment: 'Approved' });
                            loadPending();
                          }}
                          className="p-1 rounded hover:bg-emerald-950/50 text-emerald-400 hover:text-emerald-300 transition-colors"
                          title="Approve Entry"
                        >
                          <CheckCircle2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleOpenActionModal(item.id, 'REQUEST_CORRECTION')}
                          className="p-1 rounded hover:bg-amber-950/50 text-amber-400 hover:text-amber-300 transition-colors"
                          title="Request Correction"
                        >
                          <RotateCcw className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleOpenActionModal(item.id, 'REJECT')}
                          className="p-1 rounded hover:bg-rose-950/50 text-rose-400 hover:text-rose-300 transition-colors"
                          title="Reject Entry"
                        >
                          <XCircle className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Reject / Request Correction Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={actionType === 'REJECT' ? 'Reject Timesheet Submission' : 'Request Revision from Employee'}
      >
        <form onSubmit={handleConfirmActionModal} className="space-y-4">
          <p className="text-xs text-slate-400">
            {actionType === 'REJECT'
              ? 'Please specify a clear reason for rejecting this effort submission so the employee can correct and resubmit.'
              : 'Specify what modifications or extra details are needed for this timesheet entry.'}
          </p>

          <div className="form-group">
            <label className="form-label">Reviewer Comments / Required Action *</label>
            <textarea
              required
              rows="3"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              className="form-textarea text-xs"
              placeholder="e.g. Please clarify task breakdown for 8h logged on staging deployment."
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-secondary btn-sm">Cancel</button>
            <button
              type="submit"
              className={`btn btn-sm ${actionType === 'REJECT' ? 'btn-danger' : 'btn-primary'}`}
            >
              {actionType === 'REJECT' ? 'Confirm Rejection' : 'Send Revision Request'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

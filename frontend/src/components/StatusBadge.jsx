import React from 'react';

export const StatusBadge = ({ status }) => {
  if (!status) return null;
  const s = String(status).toUpperCase();

  let className = 'badge-draft';
  let label = status;

  if (s === 'ACTIVE') {
    className = 'badge-active';
    label = 'Active';
  } else if (s === 'COMPLETED') {
    className = 'badge-completed';
    label = 'Completed';
  } else if (s === 'ON_HOLD' || s === 'HOLD') {
    className = 'badge-hold';
    label = 'On Hold';
  } else if (s === 'CANCELLED') {
    className = 'badge-cancelled';
    label = 'Cancelled';
  } else if (s === 'DRAFT') {
    className = 'badge-draft';
    label = 'Draft';
  } else if (s === 'SUBMITTED') {
    className = 'badge-submitted';
    label = 'Submitted';
  } else if (s === 'APPROVED') {
    className = 'badge-approved';
    label = 'Approved';
  } else if (s === 'REJECTED') {
    className = 'badge-rejected';
    label = 'Rejected';
  } else if (s === 'UNDER_REVIEW' || s === 'RESUBMITTED') {
    className = 'badge-review';
    label = s === 'UNDER_REVIEW' ? 'Under Review' : 'Resubmitted';
  } else if (s === 'IN_PROGRESS') {
    className = 'badge-submitted';
    label = 'In Progress';
  } else if (s === 'TO_DO') {
    className = 'badge-draft';
    label = 'To Do';
  } else if (s === 'BLOCKED') {
    className = 'badge-rejected';
    label = 'Blocked';
  } else if (s === 'HIGH') {
    className = 'badge-high';
    label = 'High Priority';
  } else if (s === 'CRITICAL') {
    className = 'badge-critical';
    label = 'Critical Priority';
  } else if (s === 'MEDIUM') {
    className = 'badge-medium';
    label = 'Medium Priority';
  } else if (s === 'LOW') {
    className = 'badge-low';
    label = 'Low Priority';
  }

  return <span className={`badge ${className}`}>{label}</span>;
};

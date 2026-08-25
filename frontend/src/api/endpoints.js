import api, { API_BASE_URL } from './client';

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getMe: () => api.get('/auth/me'),
};

export const userApi = {
  getAll: () => api.get('/users'),
  getByRole: (role) => api.get(`/users/by-role/${role}`),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
};

export const projectApi = {
  getAll: () => api.get('/projects'),
  getById: (id) => api.get(`/projects/${id}`),
  create: (data) => api.post('/projects', data),
  update: (id, data) => api.put(`/projects/${id}`, data),
  addMember: (id, data) => api.post(`/projects/${id}/members`, data),
  removeMember: (id, userId) => api.delete(`/projects/${id}/members/${userId}`),
  getMilestones: (id) => api.get(`/projects/${id}/milestones`),
  addMilestone: (id, data) => api.post(`/projects/${id}/milestones`, data),
  getRisks: (id) => api.get(`/projects/${id}/risks`),
  addRisk: (id, data) => api.post(`/projects/${id}/risks`, data),
};

export const taskApi = {
  getMyTasks: () => api.get('/tasks'),
  getByProject: (projectId) => api.get(`/tasks/project/${projectId}`),
  getById: (id) => api.get(`/tasks/${id}`),
  create: (data) => api.post('/tasks', data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  updateStatus: (id, data) => api.patch(`/tasks/${id}/status`, data),
  addSubTask: (id, data) => api.post(`/tasks/${id}/subtasks`, data),
  toggleSubTask: (subTaskId) => api.post(`/tasks/subtasks/${subTaskId}/toggle`),
  addComment: (id, data) => api.post(`/tasks/${id}/comments`, data),
};

export const timesheetApi = {
  getMy: (params) => api.get('/timesheets/my', { params }),
  getById: (id) => api.get(`/timesheets/${id}`),
  save: (data) => api.post('/timesheets', data),
  saveWeekly: (data) => api.post('/timesheets/weekly', data),
  delete: (id) => api.delete(`/timesheets/${id}`),
};

export const approvalApi = {
  getPending: () => api.get('/approvals/pending'),
  process: (data) => api.post('/approvals/process', data),
};

export const dashboardApi = {
  getManagement: () => api.get('/dashboards/management'),
  getProjectManager: () => api.get('/dashboards/project-manager'),
  getEmployee: () => api.get('/dashboards/employee'),
};

export const reportApi = {
  getProjects: (params) => api.get('/reports/projects', { params }),
  getResources: (params) => api.get('/reports/resources', { params }),
  getTimesheets: (params) => api.get('/reports/timesheets', { params }),
  getCsvUrl: (params) => {
    const entries = Object.entries(params || {}).filter(
      ([, value]) => value !== undefined && value !== null && value !== ''
    );
    const query = new URLSearchParams(entries).toString();
    return `${API_BASE_URL}/reports/timesheets/csv${query ? '?' + query : ''}`;
  },
  downloadCsv: (params) =>
    api.get('/reports/timesheets/csv', {
      params,
      responseType: 'blob',
      headers: { Accept: 'text/csv' },
    }),
};

export const notificationApi = {
  getAll: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllAsRead: () => api.post('/notifications/mark-all-read'),
};

export const auditApi = {
  getAll: () => api.get('/audit-logs'),
  getByEntity: (entity) => api.get(`/audit-logs/entity/${entity}`),
};

export const aiApi = {
  getExecutiveSummary: () => api.get('/ai-copilot/executive-summary'),
};

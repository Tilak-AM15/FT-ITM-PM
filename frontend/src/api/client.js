import axios from 'axios';

export const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || '/api'
).replace(/\/$/, '');

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('pmtrack_token');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('pmtrack_token');
      localStorage.removeItem('pmtrack_user');
      window.location.assign('/login');
    }
    return Promise.reject(error);
  }
);

export default api;

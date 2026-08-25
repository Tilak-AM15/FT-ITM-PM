import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/endpoints';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('pmtrack_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('pmtrack_token') || null);
  const [loading, setLoading] = useState(false);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const res = await authApi.login({ username, password });
      const { token: jwtToken, user: userData } = res.data;
      setToken(jwtToken);
      setUser(userData);
      localStorage.setItem('pmtrack_token', jwtToken);
      localStorage.setItem('pmtrack_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const register = async (formData) => {
    setLoading(true);
    try {
      const res = await authApi.register(formData);
      const { token: jwtToken, user: userData } = res.data;
      setToken(jwtToken);
      setUser(userData);
      localStorage.setItem('pmtrack_token', jwtToken);
      localStorage.setItem('pmtrack_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('pmtrack_token');
    localStorage.removeItem('pmtrack_user');
  };

  // 1-Click Persona Demo Switcher
  const switchUser = async (username, password = 'password123') => {
    if (username === 'admin') password = 'admin123';
    return await login(username, password);
  };

  const hasRole = (...roles) => {
    if (!user) return false;
    return roles.includes(user.role);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        switchUser,
        hasRole,
        isAuthenticated: !!token && !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

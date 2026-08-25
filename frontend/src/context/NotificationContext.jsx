import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { notificationApi } from '../api/endpoints';
import { useAuth } from './AuthContext';

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const fetchNotifications = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      const [listRes, countRes] = await Promise.all([
        notificationApi.getAll(),
        notificationApi.getUnreadCount(),
      ]);
      setNotifications(listRes.data || []);
      setUnreadCount(countRes.data?.unreadCount || 0);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchNotifications();
      const interval = setInterval(fetchNotifications, 15000); // 15s poll
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user?.id, fetchNotifications]);

  const markAsRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark read:', err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all read:', err);
    }
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        fetchNotifications,
        markAsRead,
        markAllAsRead,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => useContext(NotificationContext);

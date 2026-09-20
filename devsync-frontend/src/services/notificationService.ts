import { api } from '../lib/api';
import { ApiResponse, Notification, ActivityLog, AuditLog, Setting, PageResponse } from '../types';

export const notificationService = {
  // Notifications
  listNotifications: async (page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<Notification>>>('/api/v1/notifications', {
      params: { page, size },
    });
    return response.data;
  },

  markRead: async (id: string) => {
    const response = await api.put<ApiResponse<Notification>>(`/api/v1/notifications/${id}/read`);
    return response.data;
  },

  markAllRead: async () => {
    const response = await api.put<ApiResponse<void>>('/api/v1/notifications/read');
    return response.data;
  },

  // Activity Logs
  listActivities: async (entityId: string, entityType: string) => {
    const response = await api.get<ApiResponse<ActivityLog[]>>('/api/v1/activity', {
      params: { entityId, entityType },
    });
    return response.data;
  },

  // Audit Logs (Admins only)
  listAuditLogs: async (page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<AuditLog>>>('/api/v1/audit-logs', {
      params: { page, size },
    });
    return response.data;
  },

  // Settings
  getUserSettings: async () => {
    const response = await api.get<ApiResponse<Setting>>('/api/v1/settings/user');
    return response.data;
  },

  updateUserSettings: async (settings: Partial<Setting>) => {
    const response = await api.put<ApiResponse<Setting>>('/api/v1/settings/user', settings);
    return response.data;
  },

  getOrgSettings: async (orgId: string) => {
    const response = await api.get<ApiResponse<Setting>>(`/api/v1/settings/org/${orgId}`);
    return response.data;
  },

  updateOrgSettings: async (orgId: string, settings: Partial<Setting>) => {
    const response = await api.put<ApiResponse<Setting>>(`/api/v1/settings/org/${orgId}`, settings);
    return response.data;
  },
};

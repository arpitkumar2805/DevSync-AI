import { api } from '../lib/api';
import { ApiResponse, Sprint, Task, Comment, Attachment, PageResponse } from '../types';

export interface CreateTaskParams {
  projectId: string;
  sprintId?: string;
  parentId?: string;
  title: string;
  description?: string;
  type: 'STORY' | 'BUG' | 'TASK' | 'EPIC';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  storyPoints?: number;
  dueDate?: string;
  labels?: string;
}

export interface UpdateTaskParams {
  title?: string;
  description?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  storyPoints?: number;
  dueDate?: string;
  labels?: string;
}

export const taskService = {
  // Sprints
  createSprint: async (params: { projectId: string; name: string; goal?: string; startDate?: string; endDate?: string }) => {
    const response = await api.post<ApiResponse<Sprint>>('/api/v1/sprints', params);
    return response.data;
  },

  getSprint: async (id: string) => {
    const response = await api.get<ApiResponse<Sprint>>(`/api/v1/sprints/${id}`);
    return response.data;
  },

  updateSprint: async (id: string, params: { name: string; goal?: string; startDate?: string; endDate?: string }) => {
    const response = await api.put<ApiResponse<Sprint>>(`/api/v1/sprints/${id}`, params);
    return response.data;
  },

  deleteSprint: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/sprints/${id}`);
    return response.data;
  },

  listSprintsByProject: async (projectId: string, page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<Sprint>>>('/api/v1/sprints', {
      params: { projectId, page, size },
    });
    return response.data;
  },

  startSprint: async (id: string) => {
    const response = await api.post<ApiResponse<Sprint>>(`/api/v1/sprints/${id}/start`);
    return response.data;
  },

  closeSprint: async (id: string) => {
    const response = await api.post<ApiResponse<Sprint>>(`/api/v1/sprints/${id}/close`);
    return response.data;
  },

  // Tasks
  createTask: async (params: CreateTaskParams) => {
    const response = await api.post<ApiResponse<Task>>('/api/v1/tasks', params);
    return response.data;
  },

  getTask: async (id: string) => {
    const response = await api.get<ApiResponse<Task>>(`/api/v1/tasks/${id}`);
    return response.data;
  },

  updateTask: async (id: string, params: UpdateTaskParams) => {
    const response = await api.put<ApiResponse<Task>>(`/api/v1/tasks/${id}`, params);
    return response.data;
  },

  deleteTask: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/tasks/${id}`);
    return response.data;
  },

  updateTaskStatus: async (id: string, status: string) => {
    const response = await api.put<ApiResponse<Task>>(`/api/v1/tasks/${id}/status`, { status });
    return response.data;
  },

  assignTask: async (id: string, assigneeId: string | null) => {
    const response = await api.put<ApiResponse<Task>>(`/api/v1/tasks/${id}/assign`, { assigneeId });
    return response.data;
  },

  listTasks: async (filters: { projectId?: string; sprintId?: string; assigneeId?: string; page?: number; size?: number }) => {
    const response = await api.get<ApiResponse<PageResponse<Task>>>('/api/v1/tasks', {
      params: filters,
    });
    return response.data;
  },

  getSubtasks: async (id: string) => {
    const response = await api.get<ApiResponse<Task[]>>(`/api/v1/tasks/${id}/subtasks`);
    return response.data;
  },

  addDependency: async (id: string, dependsOnTaskId: string, dependencyType = 'BLOCKS') => {
    const response = await api.post<ApiResponse<any>>(`/api/v1/tasks/${id}/dependencies`, { dependsOnTaskId, dependencyType });
    return response.data;
  },

  removeDependency: async (id: string, dependsOnTaskId: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/tasks/${id}/dependencies/${dependsOnTaskId}`);
    return response.data;
  },

  getDependencies: async (id: string) => {
    const response = await api.get<ApiResponse<any[]>>(`/api/v1/tasks/${id}/dependencies`);
    return response.data;
  },

  // Comments
  createComment: async (taskId: string, content: string, parentId?: string) => {
    const response = await api.post<ApiResponse<Comment>>(`/api/v1/tasks/${taskId}/comments`, { content, parentId });
    return response.data;
  },

  listComments: async (taskId: string, page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<Comment>>>(`/api/v1/tasks/${taskId}/comments`, {
      params: { page, size },
    });
    return response.data;
  },

  updateComment: async (taskId: string, commentId: string, content: string) => {
    const response = await api.put<ApiResponse<Comment>>(`/api/v1/tasks/${taskId}/comments/${commentId}`, { content });
    return response.data;
  },

  deleteComment: async (taskId: string, commentId: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/tasks/${taskId}/comments/${commentId}`);
    return response.data;
  },

  // Attachments
  uploadAttachment: async (taskId: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<Attachment>>(`/api/v1/tasks/${taskId}/attachments`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  listAttachments: async (taskId: string) => {
    const response = await api.get<ApiResponse<Attachment[]>>(`/api/v1/tasks/${taskId}/attachments`);
    return response.data;
  },

  deleteAttachment: async (taskId: string, attachmentId: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/tasks/${taskId}/attachments/${attachmentId}`);
    return response.data;
  },

  getAttachmentUrl: (taskId: string, attachmentId: string) => {
    const token = localStorage.getItem('accessToken');
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    return `${baseUrl}/api/v1/tasks/${taskId}/attachments/${attachmentId}?access_token=${token}`;
  },
};

import { api } from '../lib/api';
import { ApiResponse } from '../types';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export const aiService = {
  getSprintSummary: async (sprintId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/sprint-summary/${sprintId}`);
    return response.data;
  },

  getTaskPrioritization: async (projectId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/task-priority/${projectId}`);
    return response.data;
  },

  estimateStoryPoints: async (taskId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/estimate-points/${taskId}`);
    return response.data;
  },

  explainBug: async (taskId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/explain-bug/${taskId}`);
    return response.data;
  },

  getReleaseNotes: async (sprintId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/release-notes/${sprintId}`);
    return response.data;
  },

  generateDocumentation: async (projectId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/generate-docs/${projectId}`);
    return response.data;
  },

  getStandupSummary: async (teamId: string) => {
    const response = await api.post<ApiResponse<string>>(`/api/v1/ai/standup-summary/${teamId}`);
    return response.data;
  },

  getProjectHealth: async (projectId: string) => {
    const response = await api.get<ApiResponse<string>>(`/api/v1/ai/project-health/${projectId}`);
    return response.data;
  },

  getDeadlineRisk: async (projectId: string) => {
    const response = await api.get<ApiResponse<string>>(`/api/v1/ai/deadline-risk/${projectId}`);
    return response.data;
  },

  sendChatMessage: async (message: string, projectId?: string) => {
    const response = await api.post<ApiResponse<string>>('/api/v1/ai/chat', { message, projectId });
    return response.data;
  },

  generateProject: async (prompt: string, organizationId: string) => {
    const response = await api.post<ApiResponse<string>>('/api/v1/ai/generate-project', { prompt, organizationId });
    return response.data;
  },
};

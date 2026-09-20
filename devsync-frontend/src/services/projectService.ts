import { api } from '../lib/api';
import { ApiResponse, Project, ProjectMember, PageResponse } from '../types';

export interface DashboardMetrics {
  projectId: string;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  byStatus: Record<string, number>;
  byPriority: Record<string, number>;
  byAssignee: Record<string, number>;
}

export interface BurndownDataPoint {
  date: string;
  remainingPoints: number;
  idealPoints: number;
}

export interface VelocityDataPoint {
  sprintName: string;
  committedPoints: number;
  completedPoints: number;
}

export const projectService = {
  createProject: async (name: string, description?: string, teamId?: string) => {
    const response = await api.post<ApiResponse<Project>>('/api/v1/projects', { name, description, teamId });
    return response.data;
  },

  getProject: async (id: string) => {
    const response = await api.get<ApiResponse<Project>>(`/api/v1/projects/${id}`);
    return response.data;
  },

  updateProject: async (id: string, name: string, description?: string) => {
    const response = await api.put<ApiResponse<Project>>(`/api/v1/projects/${id}`, { name, description });
    return response.data;
  },

  deleteProject: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/projects/${id}`);
    return response.data;
  },

  updateProjectStatus: async (id: string, status: string) => {
    const response = await api.put<ApiResponse<Project>>(`/api/v1/projects/${id}/status`, { status });
    return response.data;
  },

  listProjects: async (page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<Project>>>('/api/v1/projects', {
      params: { page, size },
    });
    return response.data;
  },

  addProjectMember: async (projectId: string, userId: string, role: string) => {
    const response = await api.post<ApiResponse<ProjectMember>>(`/api/v1/projects/${projectId}/members`, { userId, role });
    return response.data;
  },

  removeProjectMember: async (projectId: string, userId: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/projects/${projectId}/members/${userId}`);
    return response.data;
  },

  getProjectMembers: async (projectId: string, page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<ProjectMember>>>(`/api/v1/projects/${projectId}/members`, {
      params: { page, size },
    });
    return response.data;
  },

  // Dashboard / Analytics
  getDashboardMetrics: async (projectId: string) => {
    const response = await api.get<ApiResponse<DashboardMetrics>>(`/api/v1/dashboard/project/${projectId}`);
    return response.data;
  },

  getBurndownData: async (projectId: string) => {
    const response = await api.get<ApiResponse<BurndownDataPoint[]>>(`/api/v1/dashboard/project/${projectId}/burndown`);
    return response.data;
  },

  getVelocityData: async (projectId: string) => {
    const response = await api.get<ApiResponse<VelocityDataPoint[]>>(`/api/v1/dashboard/project/${projectId}/velocity`);
    return response.data;
  },
};

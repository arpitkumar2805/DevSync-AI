import { api } from '../lib/api';
import { ApiResponse, Organization, Team, TeamMember, User, PageResponse } from '../types';

export const orgService = {
  // Organizations
  createOrg: async (name: string, description?: string) => {
    const response = await api.post<ApiResponse<Organization>>('/api/v1/orgs', { name, description });
    return response.data;
  },

  getOrg: async (id: string) => {
    const response = await api.get<ApiResponse<Organization>>(`/api/v1/orgs/${id}`);
    return response.data;
  },

  updateOrg: async (id: string, name: string, description?: string) => {
    const response = await api.put<ApiResponse<Organization>>(`/api/v1/orgs/${id}`, { name, description });
    return response.data;
  },

  deleteOrg: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/orgs/${id}`);
    return response.data;
  },

  listOrgs: async (page = 0, size = 20) => {
    const response = await api.get<ApiResponse<PageResponse<Organization>>>('/api/v1/orgs', {
      params: { page, size },
    });
    return response.data;
  },

  inviteMember: async (orgId: string, email: string, roleName: string) => {
    const response = await api.post<ApiResponse<void>>(`/api/v1/orgs/${orgId}/invite`, { email, roleName });
    return response.data;
  },

  // Teams
  createTeam: async (name: string, description?: string, organizationId?: string) => {
    const response = await api.post<ApiResponse<Team>>('/api/v1/teams', { name, description, organizationId });
    return response.data;
  },

  getTeam: async (id: string) => {
    const response = await api.get<ApiResponse<Team>>(`/api/v1/teams/${id}`);
    return response.data;
  },

  updateTeam: async (id: string, name: string, description?: string) => {
    const response = await api.put<ApiResponse<Team>>(`/api/v1/teams/${id}`, { name, description });
    return response.data;
  },

  deleteTeam: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/teams/${id}`);
    return response.data;
  },

  listTeams: async (page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<Team>>>('/api/v1/teams', {
      params: { page, size },
    });
    return response.data;
  },

  addTeamMember: async (teamId: string, userId: string, role: 'MEMBER' | 'LEAD') => {
    const response = await api.post<ApiResponse<TeamMember>>(`/api/v1/teams/${teamId}/members`, { userId, role });
    return response.data;
  },

  removeTeamMember: async (teamId: string, userId: string) => {
    const response = await api.delete<ApiResponse<void>>(`/api/v1/teams/${teamId}/members/${userId}`);
    return response.data;
  },

  getTeamMembers: async (teamId: string, page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<TeamMember>>>(`/api/v1/teams/${teamId}/members`, {
      params: { page, size },
    });
    return response.data;
  },

  // Users / Members
  listUsers: async (page = 0, size = 50) => {
    const response = await api.get<ApiResponse<PageResponse<User>>>('/api/v1/users', {
      params: { page, size },
    });
    return response.data;
  },

  getUser: async (id: string) => {
    const response = await api.get<ApiResponse<User>>(`/api/v1/users/${id}`);
    return response.data;
  },

  updateUser: async (id: string, firstName: string, lastName: string, avatarUrl?: string) => {
    const response = await api.put<ApiResponse<User>>(`/api/v1/users/${id}`, { firstName, lastName, avatarUrl });
    return response.data;
  },

  assignUserRole: async (id: string, roleName: string) => {
    const response = await api.put<ApiResponse<User>>(`/api/v1/users/${id}/role`, { roleName });
    return response.data;
  },

  deactivateUser: async (id: string) => {
    const response = await api.put<ApiResponse<void>>(`/api/v1/users/${id}/deactivate`);
    return response.data;
  },

  reactivateUser: async (id: string) => {
    const response = await api.put<ApiResponse<void>>(`/api/v1/users/${id}/reactivate`);
    return response.data;
  },
};

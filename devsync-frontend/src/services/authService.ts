import { api } from '../lib/api';
import { ApiResponse, AuthResponse } from '../types';

export const authService = {
  login: async (email: string, password: string) => {
    const response = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', { email, password });
    return response.data;
  },

  register: async (email: string, password: string, firstName: string, lastName: string, organizationName?: string) => {
    const response = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', {
      email,
      password,
      firstName,
      lastName,
      organizationName,
    });
    return response.data;
  },

  logout: async () => {
    const response = await api.post<ApiResponse<void>>('/api/v1/auth/logout');
    return response.data;
  },
};

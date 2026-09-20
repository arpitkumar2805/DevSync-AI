import axios from 'axios';
import { ApiResponse, AuthResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to append JWT
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to automatically refresh token
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Check if the error is 401 and the request hasn't been retried yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Avoid infinite loop if refreshing fails
      if (originalRequest.url?.includes('/api/v1/auth/refresh') || originalRequest.url?.includes('/api/v1/auth/login')) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      const email = localStorage.getItem('userEmail');

      if (!refreshToken || !email) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const response = await axios.post<ApiResponse<AuthResponse>>(`${API_BASE_URL}/api/v1/auth/refresh`, {
          email,
          refreshToken,
        });

        if (response.data?.success && response.data.data) {
          const authData = response.data.data;
          localStorage.setItem('accessToken', authData.accessToken);
          localStorage.setItem('refreshToken', authData.refreshToken);

          api.defaults.headers.common.Authorization = `Bearer ${authData.accessToken}`;
          originalRequest.headers.Authorization = `Bearer ${authData.accessToken}`;

          processQueue(null, authData.accessToken);
          return api(originalRequest);
        } else {
          throw new Error('Refresh failed');
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

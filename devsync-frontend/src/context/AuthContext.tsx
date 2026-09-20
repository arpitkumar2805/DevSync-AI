import React, { createContext, useEffect, useState } from 'react';
import { User, AuthResponse } from '../types';
import { api } from '../lib/api';
import { orgService } from '../services/orgService';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (authData: AuthResponse) => Promise<void>;
  logout: () => void;
  updateUser: (user: User) => void;
  activeOrgId: string | null;
  setActiveOrgId: (orgId: string | null) => void;
  activeOrgName: string | null;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeOrgName, setActiveOrgName] = useState<string | null>(null);
  const [activeOrgId, setActiveOrgState] = useState<string | null>(() => {
    return localStorage.getItem('activeOrgId');
  });

  const setActiveOrgId = (orgId: string | null) => {
    setActiveOrgState(orgId);
    if (orgId) {
      localStorage.setItem('activeOrgId', orgId);
    } else {
      localStorage.removeItem('activeOrgId');
    }
  };

  const fetchOrgName = async (orgId: string) => {
    try {
      const res = await orgService.getOrg(orgId);
      if (res.success && res.data) {
        setActiveOrgName(res.data.name);
      } else {
        setActiveOrgName(null);
      }
    } catch {
      setActiveOrgName(null);
    }
  };

  useEffect(() => {
    if (activeOrgId) {
      fetchOrgName(activeOrgId);
    } else {
      setActiveOrgName(null);
    }
  }, [activeOrgId]);

  const fetchCurrentUser = async () => {
    try {
      const response = await api.get('/api/v1/users/me');
      if (response.data?.success && response.data.data) {
        const profile = response.data.data;
        setUser(profile);
        if (profile.organizationId) {
          setActiveOrgId(profile.organizationId);
        }
      }
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
      logout();
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      fetchCurrentUser();
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (authData: AuthResponse) => {
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    localStorage.setItem('userEmail', authData.email);
    if (authData.organizationId) {
      setActiveOrgId(authData.organizationId);
    }
    await fetchCurrentUser();
  };

  const logout = () => {
    // Attempt blacklist call in background if token exists
    const token = localStorage.getItem('accessToken');
    if (token) {
      api.post('/api/v1/auth/logout').catch(() => {});
    }
    localStorage.clear();
    setUser(null);
    setActiveOrgState(null);
    setLoading(false);
  };

  const updateUser = (updatedUser: User) => {
    setUser(updatedUser);
    if (updatedUser.organizationId) {
      setActiveOrgId(updatedUser.organizationId);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        updateUser,
        activeOrgId,
        setActiveOrgId,
        activeOrgName,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

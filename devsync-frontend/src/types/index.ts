export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
  roleName: string;
  organizationId?: string;
  active: boolean;
  createdAt: string;
}

export interface Organization {
  id: string;
  name: string;
  slug: string;
  description?: string;
  subscriptionTier: string;
  createdAt: string;
}

export interface Team {
  id: string;
  organizationId: string;
  name: string;
  description?: string;
  memberCount?: number;
  createdAt: string;
}

export interface TeamMember {
  id: string;
  teamId: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'MEMBER' | 'LEAD';
}

export interface Project {
  id: string;
  organizationId: string;
  name: string;
  description?: string;
  status: 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';
  teamId?: string;
  memberCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectMember {
  id: string;
  projectId: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface Sprint {
  id: string;
  projectId: string;
  name: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
  status: 'PLANNED' | 'ACTIVE' | 'CLOSED';
  taskCount?: number;
  completedTaskCount?: number;
  createdAt: string;
}

export interface Task {
  id: string;
  projectId: string;
  sprintId?: string;
  parentId?: string;
  assigneeId?: string;
  reporterId: string;
  title: string;
  description?: string;
  type: 'STORY' | 'BUG' | 'TASK' | 'EPIC';
  status: 'BACKLOG' | 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  storyPoints?: number;
  dueDate?: string;
  labels?: string;
  subtaskCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: string;
  taskId: string;
  userId: string;
  userEmail: string;
  userFirstName: string;
  userLastName: string;
  content: string;
  parentId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Attachment {
  id: string;
  taskId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  filePath: string;
  uploadedBy: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  entityType?: string;
  entityId?: string;
  read: boolean;
  createdAt: string;
}

export interface ActivityLog {
  id: string;
  entityId: string;
  entityType: string;
  action: string;
  description: string;
  actorId: string;
  actorName: string;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  actorId: string;
  actorEmail: string;
  action: string;
  details: string;
  ipAddress: string;
  timestamp: string;
}

export interface Setting {
  id: string;
  userId?: string;
  organizationId?: string;
  theme: 'LIGHT' | 'DARK' | 'SYSTEM';
  timezone: string;
  notificationChannels: string; // comma-separated e.g. "IN_APP,EMAIL"
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  roleName: string;
  organizationId?: string;
  accessTokenExpiration: number;
}

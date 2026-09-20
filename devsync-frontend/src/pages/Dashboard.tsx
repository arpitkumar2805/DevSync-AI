import React, { useEffect, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { Sprint } from '../types';
import { projectService } from '../services/projectService';
import { taskService } from '../services/taskService';
import { orgService } from '../services/orgService';
import { aiService } from '../services/aiService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import {
  FolderKanban,
  CheckCircle,
  Clock,
  TrendingUp,
  Brain,
  Plus,
  Calendar as CalendarIcon,
  Sparkles,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
} from 'recharts';

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [projectCount, setProjectCount] = useState(0);
  const [taskMetrics, setTaskMetrics] = useState({ total: 0, active: 0, completed: 0 });
  const [teamMembers, setTeamMembers] = useState<any[]>([]);
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [aiInsight, setAiInsight] = useState('Loading intelligence reports...');
  const [_, setLoading] = useState(true);

  const [activityData, setActivityData] = useState<any[]>([
    { name: 'Mon', completed: 0, created: 0 },
    { name: 'Tue', completed: 0, created: 0 },
    { name: 'Wed', completed: 0, created: 0 },
    { name: 'Thu', completed: 0, created: 0 },
    { name: 'Fri', completed: 0, created: 0 },
    { name: 'Sat', completed: 0, created: 0 },
    { name: 'Sun', completed: 0, created: 0 },
  ]);

  const loadDashboardData = async () => {
    try {
      const projectsRes = await projectService.listProjects();
      if (projectsRes.success && projectsRes.data) {
        setProjectCount(projectsRes.data.totalElements);
        const projects = projectsRes.data.content;

        // Fetch active task info
        const tasksRes = await taskService.listTasks({ page: 0, size: 100 });
        if (tasksRes.success && tasksRes.data) {
          const tasks = tasksRes.data.content;
          const completed = tasks.filter(t => t.status === 'DONE').length;
          const active = tasks.filter(t => t.status !== 'DONE' && t.status !== 'BACKLOG').length;
          setTaskMetrics({
            total: tasksRes.data.totalElements,
            active,
            completed
          });

          // Build the last 7 calendar days (oldest first, today last), keyed by actual
          // date rather than weekday name, so a task from a previous week never gets
          // counted as part of "this week" just because it falls on the same weekday.
          const dayLabels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
          const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate());
          const today = startOfDay(new Date());

          const last7Days = Array.from({ length: 7 }, (_, i) => {
            const date = new Date(today);
            date.setDate(date.getDate() - (6 - i));
            return date;
          });

          const bucketsByDateKey = new Map(
            last7Days.map((date) => [date.toISOString().slice(0, 10), { completed: 0, created: 0 }])
          );

          tasks.forEach((task) => {
            if (task.createdAt) {
              const key = startOfDay(new Date(task.createdAt)).toISOString().slice(0, 10);
              const bucket = bucketsByDateKey.get(key);
              if (bucket) bucket.created += 1;
            }
            if (task.status === 'DONE' && task.updatedAt) {
              const key = startOfDay(new Date(task.updatedAt)).toISOString().slice(0, 10);
              const bucket = bucketsByDateKey.get(key);
              if (bucket) bucket.completed += 1;
            }
          });

          const formattedActivityData = last7Days.map((date) => ({
            name: dayLabels[date.getDay()],
            ...bucketsByDateKey.get(date.toISOString().slice(0, 10))!,
          }));
          setActivityData(formattedActivityData);
        }

        // Fetch sprints for each project to show actual milestones
        const allSprints: Sprint[] = [];
        for (const proj of projects) {
          const sprintsRes = await taskService.listSprintsByProject(proj.id);
          if (sprintsRes.success && sprintsRes.data) {
            allSprints.push(...sprintsRes.data.content);
          }
        }
        setSprints(allSprints);
      }

      const usersRes = await orgService.listUsers();
      if (usersRes.success && usersRes.data) {
        setTeamMembers(usersRes.data.content);
      }

      // Fetch AI standup / project insight
      const projects = projectsRes.data?.content || [];
      if (projects.length > 0) {
        const aiRes = await aiService.getProjectHealth(projects[0].id);
        if (aiRes.success && aiRes.data) {
          setAiInsight(aiRes.data);
        } else {
          setAiInsight('Focus on completing high priority items in the active backlog list.');
        }
      } else {
        setAiInsight('Create a project and add tasks to generate AI productivity insights.');
      }
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex justify-between items-center bg-card/40 border border-border p-6 rounded-2xl">
        <div>
          <h1 className="text-xl font-bold tracking-tight">
            Welcome back, {user?.firstName || 'Developer'}
          </h1>
          <p className="text-xs text-muted-foreground mt-1">
            Here is what is happening in your organization today.
          </p>
        </div>
        <Button size="sm" onClick={() => navigate('/projects')}>
          <Plus className="h-4 w-4 mr-2" /> New Project
        </Button>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="hover:border-primary/20 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <span className="text-xs font-semibold text-muted-foreground">Total Projects</span>
            <FolderKanban className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{projectCount}</div>
            <p className="text-[10px] text-muted-foreground mt-1">Managed workspaces</p>
          </CardContent>
        </Card>

        <Card className="hover:border-primary/20 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <span className="text-xs font-semibold text-muted-foreground">Active Tasks</span>
            <Clock className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{taskMetrics.active}</div>
            <p className="text-[10px] text-muted-foreground mt-1">In progress or review state</p>
          </CardContent>
        </Card>

        <Card className="hover:border-primary/20 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <span className="text-xs font-semibold text-muted-foreground">Completed Tasks</span>
            <CheckCircle className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{taskMetrics.completed}</div>
            <p className="text-[10px] text-muted-foreground mt-1">Closed successfully</p>
          </CardContent>
        </Card>

        <Card className="hover:border-primary/20 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <span className="text-xs font-semibold text-muted-foreground">Velocity</span>
            <TrendingUp className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {taskMetrics.total > 0 ? Math.round((taskMetrics.completed / taskMetrics.total) * 100) : 0}%
            </div>
            <p className="text-[10px] text-muted-foreground mt-1">Overall completion rate</p>
          </CardContent>
        </Card>
      </div>

      {/* Grid Layout for Analytics & AI */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Productivity Chart */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Team Output Timeline</CardTitle>
            <CardDescription>Created vs Completed tasks over this week</CardDescription>
          </CardHeader>
          <CardContent className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={activityData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="completedGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.2}/>
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="createdGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.2}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#2e303a" />
                <XAxis dataKey="name" stroke="#6b7280" fontSize={11} />
                <YAxis stroke="#6b7280" fontSize={11} />
                <RechartsTooltip contentStyle={{ backgroundColor: '#18181b', borderColor: '#27272a' }} />
                <Area type="monotone" dataKey="completed" stroke="#10b981" fillOpacity={1} fill="url(#completedGrad)" strokeWidth={2} />
                <Area type="monotone" dataKey="created" stroke="#3b82f6" fillOpacity={1} fill="url(#createdGrad)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* AI Insight Sidebar */}
        <Card className="flex flex-col">
          <CardHeader className="flex flex-row items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-indigo-500/10 flex items-center justify-center border border-indigo-500/20 text-indigo-400">
              <Brain className="h-4 w-4" />
            </div>
            <div>
              <CardTitle className="text-sm">AI Health Assessment</CardTitle>
              <CardDescription className="text-[10px]">Real-time sprint intelligence</CardDescription>
            </div>
          </CardHeader>
          <CardContent className="flex-1 flex flex-col justify-between">
            <div className="text-xs text-muted-foreground bg-secondary/30 border border-border/40 p-4 rounded-xl leading-relaxed flex-1 overflow-y-auto mb-4">
              <div className="flex items-center gap-1.5 font-semibold text-foreground mb-2">
                <Sparkles className="h-3.5 w-3.5 text-indigo-400 animate-pulse" />
                <span>Sprint Health Insight</span>
              </div>
              {aiInsight}
            </div>
            <Button size="sm" variant="outline" className="w-full text-xs" onClick={() => navigate('/ai-assistant')}>
              <Brain className="h-3.5 w-3.5 mr-2" /> Chat with Assistant
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Team & Calendar quick details */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Team Members List */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Team Directory</CardTitle>
            <CardDescription>Current active team players in this workspace</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {teamMembers.length > 0 ? (
                teamMembers.map((member) => (
                  <div key={member.id} className="flex justify-between items-center p-3 rounded-lg bg-secondary/10 border border-border/40 hover:border-border transition-colors">
                    <div className="flex items-center gap-3">
                      <div className="h-9 w-9 rounded-full bg-primary/10 flex items-center justify-center font-bold text-xs">
                        {member.firstName?.[0]}{member.lastName?.[0]}
                      </div>
                      <div>
                        <p className="text-sm font-semibold">{member.firstName} {member.lastName}</p>
                        <p className="text-[10px] text-muted-foreground mt-0.5">{member.email}</p>
                      </div>
                    </div>
                    <span className="text-[10px] font-medium bg-primary/10 border border-primary/20 px-2 py-0.5 rounded-full uppercase">
                      {member.roleName || 'Developer'}
                    </span>
                  </div>
                ))
              ) : (
                <p className="text-xs text-muted-foreground">No members in organization context yet.</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Sprint Goals & Targets */}
        <Card>
          <CardHeader>
            <CardTitle>Sprint Goals & Targets</CardTitle>
            <CardDescription>Crucial milestones for the active cycle</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {sprints.length > 0 ? (
                sprints.map((sprint) => (
                  <div key={sprint.id} className="flex items-start gap-3 text-xs leading-none">
                    <CalendarIcon className="h-4 w-4 mt-0.5 text-indigo-400" />
                    <div>
                      <p className="font-semibold">{sprint.name}</p>
                      <p className="text-[10px] text-muted-foreground mt-1">
                        End Date: {sprint.endDate ? new Date(sprint.endDate).toLocaleDateString() : 'No end date set'}
                      </p>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-xs text-muted-foreground">No active sprint goals or targets.</p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

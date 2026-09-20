import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { projectService } from '../services/projectService';
import { taskService } from '../services/taskService';
import { orgService } from '../services/orgService';
import { Project, Sprint, Task, ProjectMember, User } from '../types';
import { useToast } from '../context/ToastContext';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../components/ui/Dialog';
import {
  Calendar,
  Plus,
  ArrowRight,
  Play,
  X,
} from 'lucide-react';

const sprintSchema = zod.object({
  name: zod.string().min(1, 'Sprint name is required'),
  goal: zod.string().optional(),
  startDate: zod.string().optional(),
  endDate: zod.string().optional(),
});

type SprintFields = zod.infer<typeof sprintSchema>;

export const ProjectDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();

  const [project, setProject] = useState<Project | null>(null);
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [backlogTasks, setBacklogTasks] = useState<Task[]>([]);
  const [members, setMembers] = useState<ProjectMember[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  // Dialogs control
  const [sprintDialogOpen, setSprintDialogOpen] = useState(false);
  const [memberDialogOpen, setMemberDialogOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedMemberRole, setSelectedMemberRole] = useState('MEMBER');

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SprintFields>({
    resolver: zodResolver(sprintSchema),
  });

  const loadData = async () => {
    if (!id) return;
    try {
      const projRes = await projectService.getProject(id);
      if (projRes.success && projRes.data) {
        setProject(projRes.data);
      }

      const sprintsRes = await taskService.listSprintsByProject(id);
      if (sprintsRes.success && sprintsRes.data) {
        setSprints(sprintsRes.data.content);
      }

      const tasksRes = await taskService.listTasks({ projectId: id, page: 0, size: 200 });
      if (tasksRes.success && tasksRes.data) {
        const backlog = tasksRes.data.content.filter((t) => !t.sprintId);
        setBacklogTasks(backlog);
      }

      const membersRes = await projectService.getProjectMembers(id);
      if (membersRes.success && membersRes.data) {
        setMembers(membersRes.data.content);
      }

      const usersRes = await orgService.listUsers();
      if (usersRes.success && usersRes.data) {
        setUsers(usersRes.data.content);
      }
    } catch (err) {
      console.error(err);
      toast('Failed to load workspace', 'Check connections.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const onAddSprint = async (data: SprintFields) => {
    if (!id) return;
    try {
      const res = await taskService.createSprint({
        projectId: id,
        name: data.name,
        goal: data.goal || undefined,
        startDate: data.startDate || undefined,
        endDate: data.endDate || undefined,
      });

      if (res.success) {
        toast('Sprint Created', `Sprint "${data.name}" added successfully.`, 'success');
        reset();
        setSprintDialogOpen(false);
        loadData();
      }
    } catch {
      toast('Create sprint failed', 'Check dates parameters.', 'error');
    }
  };

  const onAddMember = async () => {
    if (!id || !selectedUserId) return;
    try {
      const res = await projectService.addProjectMember(id, selectedUserId, selectedMemberRole);
      if (res.success) {
        toast('Member Added', 'Added member to project workspace.', 'success');
        setMemberDialogOpen(false);
        setSelectedUserId('');
        loadData();
      }
    } catch {
      toast('Failed to add member', 'User might already be added.', 'error');
    }
  };

  const onRemoveMember = async (userId: string) => {
    if (!id) return;
    try {
      const res = await projectService.removeProjectMember(id, userId);
      if (res.success) {
        toast('Member Removed', 'Removed member from project.', 'success');
        loadData();
      }
    } catch {
      toast('Failed to remove member', undefined, 'error');
    }
  };

  if (loading) {
    return <div className="h-96 bg-card border border-border rounded-2xl animate-pulse" />;
  }

  if (!project) {
    return <div className="text-center py-12">Workspace not found</div>;
  }

  const activeSprint = sprints.find((s) => s.status === 'ACTIVE');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">{project.name}</h1>
          <p className="text-xs text-muted-foreground mt-1">
            {project.description || 'No project description added yet.'}
          </p>
        </div>
        <div className="flex gap-2">
          {activeSprint ? (
            <Button size="sm" variant="outline" onClick={() => navigate(`/projects/${id}/board`)}>
              <Play className="h-4 w-4 mr-1.5 text-emerald-500 fill-emerald-500" /> Active Board
            </Button>
          ) : (
            <Button size="sm" variant="outline" disabled>
              No Active Sprint
            </Button>
          )}
          <Button size="sm" onClick={() => setSprintDialogOpen(true)}>
            <Plus className="h-4 w-4 mr-1.5" /> Plan Sprint
          </Button>
        </div>
      </div>

      {/* Workspace content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sprint / Backlog Panel */}
        <div className="lg:col-span-2 space-y-6">
          {/* Active Sprint card */}
          <Card>
            <CardHeader className="pb-3 flex flex-row justify-between items-start">
              <div>
                <CardTitle className="text-sm">Active Cycle</CardTitle>
                <CardDescription className="text-[10px]">Current active iteration board</CardDescription>
              </div>
              <span className="text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full uppercase">
                Active
              </span>
            </CardHeader>
            <CardContent>
              {activeSprint ? (
                <div className="flex justify-between items-center bg-secondary/20 p-4 rounded-xl border border-border/40">
                  <div>
                    <h3 className="text-sm font-semibold">{activeSprint.name}</h3>
                    <p className="text-xs text-muted-foreground mt-1">{activeSprint.goal || 'No goal set'}</p>
                    <div className="flex gap-4 text-[10px] text-muted-foreground mt-3">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-3 w-3" /> {activeSprint.startDate || 'No date'} - {activeSprint.endDate || 'No date'}
                      </span>
                    </div>
                  </div>
                  <Button size="sm" onClick={() => navigate(`/projects/${id}/board`)}>
                    Go to Board <ArrowRight className="h-3.5 w-3.5 ml-1.5" />
                  </Button>
                </div>
              ) : (
                <div className="text-center py-6 text-xs text-muted-foreground border border-dashed rounded-xl bg-secondary/10">
                  No active sprint. Open sprint planner to launch a sprint iteration.
                </div>
              )}
            </CardContent>
          </Card>

          {/* Sprints list */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Sprints Planner</CardTitle>
              <CardDescription className="text-[10px]">Planned and closed sprint iterations</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {sprints.filter((s) => s.status !== 'ACTIVE').length > 0 ? (
                sprints
                  .filter((s) => s.status !== 'ACTIVE')
                  .map((sprint) => (
                    <div
                      key={sprint.id}
                      className="flex justify-between items-center p-3 rounded-lg border border-border/40 hover:border-border transition-colors text-xs bg-secondary/10"
                    >
                      <div>
                        <p className="font-semibold">{sprint.name}</p>
                        <p className="text-muted-foreground mt-1">{sprint.goal || 'No goal set.'}</p>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="text-[10px] font-semibold bg-primary/10 border border-primary/20 px-2 py-0.5 rounded-full uppercase">
                          {sprint.status}
                        </span>
                        {sprint.status === 'PLANNED' && (
                          <Button size="sm" onClick={() => navigate(`/projects/${id}/board`)} className="h-7 text-[10px] px-2.5">
                            Open Board
                          </Button>
                        )}
                      </div>
                    </div>
                  ))
              ) : (
                <p className="text-xs text-muted-foreground text-center py-4">No planned sprints. Click Plan Sprint to start.</p>
              )}
            </CardContent>
          </Card>

          {/* Backlog List */}
          <Card>
            <CardHeader className="flex flex-row justify-between items-center">
              <div>
                <CardTitle className="text-sm">Backlog Tasks</CardTitle>
                <CardDescription className="text-[10px]">Items not yet committed to a sprint cycle</CardDescription>
              </div>
              <Button size="sm" variant="outline" className="h-7 text-[10px] px-2.5" onClick={() => navigate(`/projects/${id}/board`)}>
                Add Task
              </Button>
            </CardHeader>
            <CardContent className="space-y-2">
              {backlogTasks.length > 0 ? (
                backlogTasks.map((task) => (
                  <div
                    key={task.id}
                    className="flex justify-between items-center p-2.5 rounded-lg border border-border/30 hover:border-border bg-secondary/5 text-xs transition-colors"
                  >
                    <div>
                      <span className="font-semibold text-primary mr-2 uppercase text-[10px] border border-border px-1.5 py-0.5 rounded">
                        {task.type}
                      </span>
                      <span className="text-foreground font-medium">{task.title}</span>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full uppercase ${
                        task.priority === 'CRITICAL' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' : 'bg-secondary/40 text-muted-foreground border border-border'
                      }`}>
                        {task.priority}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-xs text-muted-foreground text-center py-4">Backlog is empty.</p>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar Members Panel */}
        <div className="space-y-6">
          <Card>
            <CardHeader className="flex flex-row justify-between items-center pb-3">
              <div>
                <CardTitle className="text-sm">Project Members</CardTitle>
                <CardDescription className="text-[10px]">Workspace collaborators</CardDescription>
              </div>
              <Button size="sm" variant="ghost" className="h-7 w-7 p-0 rounded-full" onClick={() => setMemberDialogOpen(true)}>
                <Plus className="h-4 w-4" />
              </Button>
            </CardHeader>
            <CardContent className="space-y-3">
              {members.length > 0 ? (
                members.map((m) => (
                  <div key={m.id} className="flex justify-between items-center text-xs p-2 rounded-lg hover:bg-secondary/20 border border-border/40 transition-colors">
                    <div>
                      <p className="font-semibold">{m.firstName} {m.lastName}</p>
                      <p className="text-[10px] text-muted-foreground mt-0.5">{m.email}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-[9px] uppercase px-1.5 py-0.5 rounded bg-primary/10 border border-primary/20">
                        {m.role}
                      </span>
                      <Button variant="ghost" size="icon" className="h-6 w-6 text-rose-400 hover:text-rose-600" onClick={() => onRemoveMember(m.userId)}>
                        <X className="h-3 w-3" />
                      </Button>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-xs text-muted-foreground">No collaborators added.</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Plan Sprint Dialog */}
      <Dialog open={sprintDialogOpen} onOpenChange={setSprintDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Plan New Sprint</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onAddSprint)} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold mb-1.5">Sprint Name</label>
              <Input placeholder="Sprint-01" error={errors.name?.message} {...register('name')} />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1.5">Sprint Goal</label>
              <textarea
                placeholder="Deliver authentication flow and project structures..."
                className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 min-h-[60px]"
                {...register('goal')}
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold mb-1.5">Start Date</label>
                <Input type="date" {...register('startDate')} />
              </div>
              <div>
                <label className="block text-xs font-semibold mb-1.5">End Date</label>
                <Input type="date" {...register('endDate')} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" size="sm" onClick={() => setSprintDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" loading={isSubmitting}>
                Plan Sprint
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Add Member Dialog */}
      <Dialog open={memberDialogOpen} onOpenChange={setMemberDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Project Member</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div>
              <label className="block text-xs font-semibold mb-1.5">Select User</label>
              <select
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
                className="flex h-10 w-full rounded-md border border-input bg-card px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="">Select Collaborator</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.firstName} {u.lastName} ({u.email})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1.5">Project Role</label>
              <select
                value={selectedMemberRole}
                onChange={(e) => setSelectedMemberRole(e.target.value)}
                className="flex h-10 w-full rounded-md border border-input bg-card px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="MEMBER">Member</option>
                <option value="OWNER">Owner</option>
                <option value="VIEWER">Viewer</option>
              </select>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" size="sm" onClick={() => setMemberDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="button" size="sm" onClick={onAddMember}>
                Add Collaborator
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { taskService } from '../services/taskService';
import { projectService } from '../services/projectService';
import { aiService } from '../services/aiService';
import { Task, Sprint, ProjectMember, Comment } from '../types';
import { useToast } from '../context/ToastContext';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../components/ui/Dialog';
import {
  Plus,
  Play,
  CheckCircle,
  Sparkles,
  Trash2,
  X,
} from 'lucide-react';

const taskSchema = zod.object({
  title: zod.string().min(1, 'Task title is required').max(500, 'Title is too long'),
  description: zod.string().optional(),
  type: zod.enum(['STORY', 'BUG', 'TASK', 'EPIC']),
  priority: zod.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']),
  storyPoints: zod.number().min(0).max(100).optional(),
  dueDate: zod.string().optional(),
  labels: zod.string().optional(),
});

type TaskFields = zod.infer<typeof taskSchema>;

export const SprintBoard: React.FC = () => {
  const { id: projectId } = useParams<{ id: string }>();
  const { toast } = useToast();

  // Sprints & Tasks State
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [activeSprint, setActiveSprint] = useState<Sprint | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [members, setMembers] = useState<ProjectMember[]>([]);

  // Selected Task Detail Drawer / Modal state
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [aiAnalysis, setAiAnalysis] = useState('');
  const [aiLoading, setAiLoading] = useState(false);

  // Dialog Controls
  const [taskDialogOpen, setTaskDialogOpen] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<TaskFields>({
    resolver: zodResolver(taskSchema),
    defaultValues: {
      type: 'TASK',
      priority: 'MEDIUM',
    },
  });

  const loadBoardData = async () => {
    if (!projectId) return;
    try {
      // Load sprints
      const sprintsRes = await taskService.listSprintsByProject(projectId);
      if (sprintsRes.success && sprintsRes.data) {
        const sprintList = sprintsRes.data.content;
        setSprints(sprintList);
        const active = sprintList.find((s) => s.status === 'ACTIVE') || sprintList[0] || null;
        setActiveSprint(active);
      }

      // Load project members
      const membersRes = await projectService.getProjectMembers(projectId);
      if (membersRes.success && membersRes.data) {
        setMembers(membersRes.data.content);
      }
    } catch (err) {
      console.error(err);
      toast('Failed to load board configurations', undefined, 'error');
    }
  };

  const loadSprintTasks = async () => {
    if (!projectId || !activeSprint) return;
    try {
      const tasksRes = await taskService.listTasks({
        projectId,
        sprintId: activeSprint.id,
        page: 0,
        size: 100,
      });
      if (tasksRes.success && tasksRes.data) {
        setTasks(tasksRes.data.content);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadBoardData();
  }, [projectId]);

  useEffect(() => {
    if (activeSprint) {
      loadSprintTasks();
    }
  }, [activeSprint]);

  // Sprint Controls
  const handleStartSprint = async () => {
    if (!activeSprint) return;
    try {
      const res = await taskService.startSprint(activeSprint.id);
      if (res.success) {
        toast('Sprint Started', `Sprint "${activeSprint.name}" is now active!`, 'success');
        loadBoardData();
      }
    } catch {
      toast('Sprint start failed', 'Confirm no other active sprints exist.', 'error');
    }
  };

  const handleCloseSprint = async () => {
    if (!activeSprint) return;
    try {
      const res = await taskService.closeSprint(activeSprint.id);
      if (res.success) {
        toast('Sprint Closed', `Sprint "${activeSprint.name}" is now closed.`, 'success');
        loadBoardData();
      }
    } catch {
      toast('Failed to close sprint', undefined, 'error');
    }
  };

  // Task CRUD operations
  const onCreateTaskSubmit = async (data: TaskFields) => {
    if (!projectId) return;
    try {
      const res = await taskService.createTask({
        projectId,
        sprintId: activeSprint?.id,
        title: data.title,
        description: data.description || undefined,
        type: data.type,
        priority: data.priority,
        storyPoints: data.storyPoints,
        dueDate: data.dueDate || undefined,
        labels: data.labels || undefined,
      });

      if (res.success) {
        toast('Task Created', 'Added new task to board list.', 'success');
        reset();
        setTaskDialogOpen(false);
        loadSprintTasks();
      }
    } catch {
      toast('Failed to create task', undefined, 'error');
    }
  };

  const updateTaskStatus = async (taskId: string, targetStatus: string) => {
    try {
      const res = await taskService.updateTaskStatus(taskId, targetStatus);
      if (res.success) {
        toast('Status updated', undefined, 'success');
        loadSprintTasks();
        if (selectedTask?.id === taskId) {
          setSelectedTask(res.data || null);
        }
      }
    } catch {
      toast('Invalid state transition', undefined, 'error');
    }
  };

  const handleTaskAssign = async (taskId: string, assigneeId: string) => {
    try {
      const res = await taskService.assignTask(taskId, assigneeId || null);
      if (res.success) {
        toast('Assignee updated', undefined, 'success');
        loadSprintTasks();
        if (selectedTask?.id === taskId) {
          setSelectedTask(res.data || null);
        }
      }
    } catch {
      toast('Failed to assign task', undefined, 'error');
    }
  };

  // Task Details Drawer helpers
  const handleOpenTask = async (task: Task) => {
    setSelectedTask(task);
    setAiAnalysis('');
    try {
      const commentsRes = await taskService.listComments(task.id);
      if (commentsRes.success && commentsRes.data) {
        setComments(commentsRes.data.content);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const submitComment = async () => {
    if (!selectedTask || !newComment.trim()) return;
    try {
      const res = await taskService.createComment(selectedTask.id, newComment);
      if (res.success && res.data) {
        setComments((prev) => [...prev, res.data!]);
        setNewComment('');
        toast('Comment added', undefined, 'success');
      }
    } catch {
      toast('Failed to add comment', undefined, 'error');
    }
  };

  // Spring AI integrations
  const runAIEstimate = async () => {
    if (!selectedTask) return;
    setAiLoading(true);
    setAiAnalysis('Requesting AI Story Points calculation...');
    try {
      const res = await aiService.estimateStoryPoints(selectedTask.id);
      if (res.success && res.data) {
        setAiAnalysis(res.data);
      }
    } catch {
      setAiAnalysis('AI estimation service failed.');
    } finally {
      setAiLoading(false);
    }
  };

  const runAIExtraInsight = async () => {
    if (!selectedTask) return;
    setAiLoading(true);
    setAiAnalysis('Running diagnostic audit on task details...');
    try {
      const res =
        selectedTask.type === 'BUG'
          ? await aiService.explainBug(selectedTask.id)
          : await aiService.sendChatMessage(`Generate summary action plan for this task: ${selectedTask.title}`);
      if (res.success && res.data) {
        setAiAnalysis(res.data);
      }
    } catch {
      setAiAnalysis('AI diagnostic failed.');
    } finally {
      setAiLoading(false);
    }
  };

  const columns = [
    { id: 'BACKLOG', name: 'Backlog', color: 'border-slate-500/20 bg-slate-900/5' },
    { id: 'TODO', name: 'To Do', color: 'border-blue-500/20 bg-blue-900/5' },
    { id: 'IN_PROGRESS', name: 'In Progress', color: 'border-amber-500/20 bg-amber-900/5' },
    { id: 'REVIEW', name: 'In Review', color: 'border-purple-500/20 bg-purple-900/5' },
    { id: 'DONE', name: 'Done', color: 'border-emerald-500/20 bg-emerald-900/5' },
  ];

  return (
    <div className="space-y-6">
      {/* Board Top Actions */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        {/* Sprint selector */}
        <div className="flex items-center gap-3">
          <select
            value={activeSprint?.id || ''}
            onChange={(e) => {
              const selected = sprints.find((s) => s.id === e.target.value);
              if (selected) setActiveSprint(selected);
            }}
            className="flex h-9 w-48 rounded-lg border border-border bg-card px-3 text-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          >
            {sprints.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name} ({s.status})
              </option>
            ))}
          </select>

          {activeSprint?.status === 'PLANNED' && (
            <Button size="sm" onClick={handleStartSprint}>
              <Play className="h-3.5 w-3.5 mr-1" /> Start Sprint
            </Button>
          )}

          {activeSprint?.status === 'ACTIVE' && (
            <Button size="sm" variant="danger" onClick={handleCloseSprint}>
              <CheckCircle className="h-3.5 w-3.5 mr-1" /> Close Sprint
            </Button>
          )}
        </div>

        <Button size="sm" onClick={() => setTaskDialogOpen(true)}>
          <Plus className="h-4.5 w-4.5 mr-1" /> Add Task
        </Button>
      </div>

      {/* Kanban Board Area */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4 overflow-x-auto min-h-[500px] pb-4">
        {columns.map((col) => {
          const colTasks = tasks.filter((t) => t.status === col.id);
          return (
            <div key={col.id} className={`flex flex-col border border-border/80 rounded-xl p-3 min-w-[240px] ${col.color}`}>
              {/* Header */}
              <div className="flex justify-between items-center mb-4">
                <span className="font-semibold text-xs text-foreground/80 tracking-wide uppercase">
                  {col.name}
                </span>
                <span className="text-[10px] bg-secondary/80 px-2 py-0.5 rounded-full font-bold">
                  {colTasks.length}
                </span>
              </div>

              {/* Tasks mapping */}
              <div className="flex-1 space-y-3 overflow-y-auto max-h-[500px]">
                {colTasks.map((task) => (
                  <Card
                    key={task.id}
                    className="hover:border-primary/20 transition-all cursor-pointer p-3.5 bg-card/60 flex flex-col gap-3 group relative"
                    onClick={() => handleOpenTask(task)}
                  >
                    <div>
                      <div className="flex justify-between items-start gap-1">
                        <span className="text-[9px] font-bold text-muted-foreground uppercase tracking-wide">
                          {task.type}
                        </span>
                        <span className={`text-[8px] font-semibold uppercase px-1.5 py-0.5 rounded ${
                          task.priority === 'CRITICAL' ? 'bg-rose-500/10 text-rose-400' : 'bg-secondary text-muted-foreground'
                        }`}>
                          {task.priority}
                        </span>
                      </div>
                      <h4 className="text-xs font-semibold leading-normal group-hover:text-primary transition-colors mt-1.5">
                        {task.title}
                      </h4>
                    </div>

                    <div className="flex justify-between items-center text-[10px] text-muted-foreground pt-1.5 border-t border-border/40">
                      <span className="bg-primary/5 px-2 py-0.5 rounded border border-border">
                        SP: {task.storyPoints || '-'}
                      </span>
                      {task.assigneeId && (
                        <div className="h-5 w-5 rounded-full bg-primary/10 flex items-center justify-center font-bold text-[9px] border border-border">
                          {members.find((m) => m.userId === task.assigneeId)?.firstName?.[0]}
                        </div>
                      )}
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      {/* Task Creation Modal */}
      <Dialog open={taskDialogOpen} onOpenChange={setTaskDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Task</DialogTitle>
          </DialogHeader>

          <form onSubmit={handleSubmit(onCreateTaskSubmit)} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold mb-1.5">Title</label>
              <Input placeholder="Fix broken auth token refresh" error={errors.title?.message} {...register('title')} />
            </div>

            <div>
              <label className="block text-xs font-semibold mb-1.5">Description (Optional)</label>
              <textarea
                placeholder="Include bug logs, expected outputs, or code targets..."
                className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring min-h-[60px]"
                {...register('description')}
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold mb-1.5">Type</label>
                <select className="flex h-10 w-full rounded-md border border-input bg-card px-3 text-sm" {...register('type')}>
                  <option value="TASK">Task</option>
                  <option value="BUG">Bug</option>
                  <option value="STORY">Story</option>
                  <option value="EPIC">Epic</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold mb-1.5">Priority</label>
                <select className="flex h-10 w-full rounded-md border border-input bg-card px-3 text-sm" {...register('priority')}>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold mb-1.5">Story Points</label>
                <Input type="number" placeholder="5" {...register('storyPoints', { valueAsNumber: true })} />
              </div>

              <div>
                <label className="block text-xs font-semibold mb-1.5">Due Date</label>
                <Input type="date" {...register('dueDate')} />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" size="sm" onClick={() => setTaskDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" loading={isSubmitting}>
                Add Task
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Task Drawer details overlay */}
      {selectedTask && (
        <div className="fixed inset-y-0 right-0 z-50 w-full max-w-lg bg-card border-l border-border shadow-2xl p-6 overflow-y-auto animate-in slide-in-from-right duration-250 flex flex-col justify-between">
          <div className="space-y-6">
            {/* Header / Drawer Controls */}
            <div className="flex justify-between items-start border-b border-border pb-4">
              <div>
                <span className="text-[9px] font-bold text-muted-foreground uppercase">{selectedTask.type} details</span>
                <h3 className="text-sm font-bold text-foreground mt-1">{selectedTask.title}</h3>
              </div>
              <button onClick={() => setSelectedTask(null)} className="h-6 w-6 rounded-md hover:bg-secondary flex items-center justify-center">
                <X className="h-4 w-4" />
              </button>
            </div>

            {/* AI Assistant panel */}
            <div className="bg-indigo-950/20 border border-indigo-500/20 p-4 rounded-xl space-y-3">
              <div className="flex justify-between items-center">
                <span className="flex items-center gap-1.5 text-xs font-bold text-indigo-400">
                  <Sparkles className="h-4 w-4 animate-pulse" /> Spring AI Assistant
                </span>
                <div className="flex gap-2">
                  <Button size="sm" variant="ghost" className="h-7 text-[10px] border border-indigo-500/30 text-indigo-300" onClick={runAIEstimate} disabled={aiLoading}>
                    Estimate Points
                  </Button>
                  <Button size="sm" variant="ghost" className="h-7 text-[10px] border border-indigo-500/30 text-indigo-300" onClick={runAIExtraInsight} disabled={aiLoading}>
                    {selectedTask.type === 'BUG' ? 'Explain Bug' : 'Action Plan'}
                  </Button>
                </div>
              </div>
              {aiAnalysis && (
                <div className="text-[11px] bg-indigo-950/40 p-3 rounded-lg border border-indigo-500/10 text-indigo-200 leading-relaxed max-h-40 overflow-y-auto whitespace-pre-wrap">
                  {aiAnalysis}
                </div>
              )}
            </div>

            {/* Task parameters settings */}
            <div className="grid grid-cols-2 gap-4 text-xs">
              <div>
                <label className="text-[10px] font-bold text-muted-foreground uppercase block mb-1">Status</label>
                <select
                  value={selectedTask.status}
                  onChange={(e) => updateTaskStatus(selectedTask.id, e.target.value)}
                  className="w-full h-8 border border-border bg-card rounded px-2"
                >
                  <option value="BACKLOG">Backlog</option>
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="REVIEW">Review</option>
                  <option value="DONE">Done</option>
                </select>
              </div>

              <div>
                <label className="text-[10px] font-bold text-muted-foreground uppercase block mb-1">Assignee</label>
                <select
                  value={selectedTask.assigneeId || ''}
                  onChange={(e) => handleTaskAssign(selectedTask.id, e.target.value)}
                  className="w-full h-8 border border-border bg-card rounded px-2"
                >
                  <option value="">Unassigned</option>
                  {members.map((m) => (
                    <option key={m.id} value={m.userId}>
                      {m.firstName} {m.lastName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Threaded comments section */}
            <div className="space-y-3">
              <span className="text-[10px] font-bold text-muted-foreground uppercase block">Discussion thread</span>
              <div className="flex gap-2">
                <Input placeholder="Type comment..." value={newComment} onChange={(e) => setNewComment(e.target.value)} />
                <Button size="sm" onClick={submitComment}>
                  Comment
                </Button>
              </div>
              <div className="space-y-2.5 max-h-48 overflow-y-auto">
                {comments.map((c) => (
                  <div key={c.id} className="p-2.5 rounded-lg border border-border/40 text-[11px] bg-secondary/10">
                    <div className="flex justify-between items-center mb-1">
                      <span className="font-semibold">{c.userFirstName} {c.userLastName}</span>
                      <span className="text-[9px] text-muted-foreground">{new Date(c.createdAt).toLocaleDateString()}</span>
                    </div>
                    <p className="text-muted-foreground">{c.content}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="border-t border-border pt-4 mt-6">
            <Button variant="ghost" className="w-full text-xs text-rose-400 hover:text-rose-600 hover:bg-rose-500/5 justify-start h-8" onClick={async () => {
              await taskService.deleteTask(selectedTask.id);
              toast('Task deleted', undefined, 'success');
              setSelectedTask(null);
              loadSprintTasks();
            }}>
              <Trash2 className="h-4 w-4 mr-2" /> Delete Task
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

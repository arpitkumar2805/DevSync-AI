import React, { useState, useEffect } from 'react';
import { taskService } from '../services/taskService';
import { Task } from '../types';
import { useToast } from '../context/ToastContext';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Calendar as CalendarIcon, Clock } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

import { projectService } from '../services/projectService';

export const Calendar: React.FC = () => {
  const { toast } = useToast();
  const { user } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user?.id) return;
    setLoading(true);
    projectService
      .listProjects(0, 100)
      .then(async (projectsRes) => {
        if (projectsRes.success && projectsRes.data) {
          const projectList = projectsRes.data.content;
          const allTasks: Task[] = [];
          
          for (const proj of projectList) {
            try {
              const tasksRes = await taskService.listTasks({ projectId: proj.id, page: 0, size: 100 });
              if (tasksRes.success && tasksRes.data) {
                allTasks.push(...tasksRes.data.content);
              }
            } catch (err) {
              console.error(`Failed to load tasks for project ${proj.id}`, err);
            }
          }
          
          // Filter tasks that actually have a dueDate
          setTasks(allTasks.filter((t) => t.dueDate));
        }
      })
      .catch(() => {
        toast('Failed to load calendar tasks', undefined, 'error');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [user?.id]);

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Calendar Milestones</h1>
        <p className="text-xs text-muted-foreground mt-1">Deadlines, delivery checkpoints, and sprint iteration scopes.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Upcoming Task Target Dates</CardTitle>
          <CardDescription className="text-[10px]">Track item deadlines grouped chronologically</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {loading ? (
            <div className="space-y-2 animate-pulse">
              <div className="h-12 bg-secondary/40 rounded-lg" />
              <div className="h-12 bg-secondary/40 rounded-lg" />
            </div>
          ) : tasks.length > 0 ? (
            tasks
              .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())
              .map((t) => (
                <div
                  key={t.id}
                  className="flex justify-between items-center p-3 rounded-lg border border-border/40 hover:border-border transition-all bg-secondary/10 text-xs"
                >
                  <div className="flex items-center gap-3">
                    <CalendarIcon className="h-4 w-4 text-primary shrink-0" />
                    <div>
                      <p className="font-semibold text-foreground">{t.title}</p>
                      <p className="text-[10px] text-muted-foreground mt-0.5">Type: {t.type} | Priority: {t.priority}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0 bg-primary/10 border border-primary/20 px-2.5 py-1 rounded text-[10px]">
                    <Clock className="h-3 w-3 text-primary" />
                    <span>Due: {t.dueDate}</span>
                  </div>
                </div>
              ))
          ) : (
            <div className="text-center py-12 text-xs text-muted-foreground border border-dashed rounded-xl">
              No tasks with active deadlines found. Create tasks with due dates to list milestones.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

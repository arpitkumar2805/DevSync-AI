import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { projectService } from '../services/projectService';
import { orgService } from '../services/orgService';
import { aiService } from '../services/aiService';
import { useAuth } from '../hooks/useAuth';
import { Project, Team } from '../types';
import { useToast } from '../context/ToastContext';
import { Card, CardHeader, CardTitle, CardDescription, CardFooter } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../components/ui/Dialog';
import { Plus, ArrowRight, FolderKanban } from 'lucide-react';

const projectSchema = zod.object({
  name: zod.string().min(1, 'Project name is required').max(200, 'Name is too long'),
  description: zod.string().optional(),
  teamId: zod.string().optional(),
});

type ProjectFields = zod.infer<typeof projectSchema>;

export const Projects: React.FC = () => {
  const navigate = useNavigate();
  const { activeOrgId } = useAuth();
  const { toast } = useToast();
  const [projects, setProjects] = useState<Project[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [isAiMode, setIsAiMode] = useState(false);
  const [aiPrompt, setAiPrompt] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProjectFields>({
    resolver: zodResolver(projectSchema),
  });

  const loadProjectsAndTeams = async () => {
    try {
      const projectsRes = await projectService.listProjects();
      if (projectsRes.success && projectsRes.data) {
        setProjects(projectsRes.data.content);
      }

      const teamsRes = await orgService.listTeams();
      if (teamsRes.success && teamsRes.data) {
        setTeams(teamsRes.data.content);
      }
    } catch (error) {
      console.error(error);
      toast('Failed to load projects', 'Please check your connection.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProjectsAndTeams();
  }, []);

  const onSubmit = async (data: ProjectFields) => {
    if (isAiMode) {
      if (!aiPrompt.trim()) {
        toast('Prompt is required', 'Please enter what you want the AI to build.', 'error');
        return;
      }
      if (!activeOrgId) {
        toast('Organization context is missing', 'Please select or create an organization first.', 'error');
        return;
      }
      setIsGenerating(true);
      try {
        const response = await aiService.generateProject(aiPrompt, activeOrgId);
        if (response.success) {
          toast('AI Generation Complete', response.data || 'Project backlog generated successfully!', 'success');
          setAiPrompt('');
          setIsAiMode(false);
          setCreateDialogOpen(false);
          loadProjectsAndTeams();
        } else {
          toast('Generation failed', response.message || 'Check prompt.', 'error');
        }
      } catch (error: any) {
        console.error(error);
        toast('Generation failed', error.response?.data?.message || 'Server error during generation', 'error');
      } finally {
        setIsGenerating(false);
      }
      return;
    }

    try {
      const response = await projectService.createProject(
        data.name,
        data.description || undefined,
        data.teamId || undefined
      );

      if (response.success && response.data) {
        toast('Project Created', `Project "${data.name}" is now ready!`, 'success');
        reset();
        setCreateDialogOpen(false);
        loadProjectsAndTeams();
      } else {
        toast('Create failed', response.message || 'Check inputs.', 'error');
      }
    } catch (error: any) {
      console.error(error);
      toast('Create failed', error.response?.data?.message || 'Server error', 'error');
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Projects</h1>
          <p className="text-xs text-muted-foreground mt-1">
            Build and track repositories, tasks, and project sprints
          </p>
        </div>
        <Button size="sm" onClick={() => setCreateDialogOpen(true)}>
          <Plus className="h-4.5 w-4.5 mr-1" /> Create Project
        </Button>
      </div>

      {/* Projects List */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-40 bg-card border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : projects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {projects.map((project) => (
            <Card
              key={project.id}
              className="hover:border-primary/30 transition-all group flex flex-col justify-between"
            >
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div className="h-9 w-9 rounded-lg bg-primary/5 flex items-center justify-center border border-border/60">
                    <FolderKanban className="h-4.5 w-4.5 text-muted-foreground group-hover:text-primary transition-colors" />
                  </div>
                  <span className="text-[10px] font-semibold bg-emerald-950/20 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full uppercase">
                    {project.status}
                  </span>
                </div>
                <CardTitle className="mt-4 text-base group-hover:text-primary transition-colors truncate">
                  {project.name}
                </CardTitle>
                <CardDescription className="line-clamp-2 text-xs leading-normal mt-1.5 min-h-[32px]">
                  {project.description || 'No description provided.'}
                </CardDescription>
              </CardHeader>
              <CardFooter className="pt-2">
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-xs hover:bg-secondary/40 justify-between px-2 h-8"
                  onClick={() => navigate(`/projects/${project.id}`)}
                >
                  View Details
                  <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5" />
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="flex flex-col items-center justify-center p-12 text-center border-dashed">
          <div className="h-12 w-12 rounded-xl bg-secondary flex items-center justify-center mb-4">
            <FolderKanban className="h-6 w-6 text-muted-foreground" />
          </div>
          <CardTitle className="text-base">No projects found</CardTitle>
          <CardDescription className="max-w-xs mx-auto text-xs mt-2 leading-relaxed">
            Get started by creating your very first project space in this organization.
          </CardDescription>
          <Button size="sm" className="mt-6" onClick={() => setCreateDialogOpen(true)}>
            Create Project
          </Button>
        </Card>
      )}

      {/* Create Project Modal */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Project</DialogTitle>
          </DialogHeader>

          <form
            onSubmit={(e) => {
              if (isAiMode) {
                e.preventDefault();
                onSubmit({} as any);
              } else {
                handleSubmit(onSubmit)(e);
              }
            }}
            className="space-y-4"
          >
            <div className="flex items-center gap-2 pb-2 border-b border-border/40">
              <input
                type="checkbox"
                id="aiMode"
                checked={isAiMode}
                onChange={(e) => setIsAiMode(e.target.checked)}
                className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
              />
              <label htmlFor="aiMode" className="text-xs font-bold text-indigo-400 flex items-center gap-1.5 cursor-pointer">
                Generate project timeline & backlog with AI
              </label>
            </div>

            {isAiMode ? (
              <div>
                <label className="block text-xs font-semibold mb-1.5 text-indigo-400">AI Prompt</label>
                <textarea
                  placeholder="e.g. Build an ecommerce platform with payment gateway, product catalogs, shopping carts, and order processing..."
                  className="flex w-full rounded-md border border-indigo-500/30 bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 min-h-[120px]"
                  value={aiPrompt}
                  onChange={(e) => setAiPrompt(e.target.value)}
                />
              </div>
            ) : (
              <>
                <div>
                  <label className="block text-xs font-semibold mb-1.5">Project Name</label>
                  <Input
                    placeholder="DevSync Platform Upgrade"
                    error={errors.name?.message}
                    {...register('name')}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold mb-1.5">Description (Optional)</label>
                  <textarea
                    placeholder="Describe key scopes, tech stacks, or project parameters..."
                    className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 min-h-[80px]"
                    {...register('description')}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold mb-1.5">Assign Team (Optional)</label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-card px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    {...register('teamId')}
                  >
                    <option value="">Select a Team</option>
                    {teams.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.name}
                      </option>
                    ))}
                  </select>
                </div>
              </>
            )}

            <DialogFooter className="pt-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setCreateDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit" size="sm" loading={isAiMode ? isGenerating : isSubmitting}>
                {isAiMode ? 'Generate with AI' : 'Create Project'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

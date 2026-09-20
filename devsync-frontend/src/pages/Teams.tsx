import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { orgService } from '../services/orgService';
import { Team, TeamMember, User } from '../types';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../hooks/useAuth';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../components/ui/Dialog';
import { Plus, X } from 'lucide-react';

const teamSchema = zod.object({
  name: zod.string().min(1, 'Team name is required'),
  description: zod.string().optional(),
});

type TeamFields = zod.infer<typeof teamSchema>;

export const Teams: React.FC = () => {
  const { toast } = useToast();
  const { activeOrgId } = useAuth();
  const [teams, setTeams] = useState<Team[]>([]);
  const [activeTeam, setActiveTeam] = useState<Team | null>(null);
  const [members, setMembers] = useState<TeamMember[]>([]);
  const [users, setUsers] = useState<User[]>([]);

  // Dialog controllers
  const [teamDialogOpen, setTeamDialogOpen] = useState(false);
  const [memberDialogOpen, setMemberDialogOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedRole, setSelectedRole] = useState<'MEMBER' | 'LEAD'>('MEMBER');

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<TeamFields>({
    resolver: zodResolver(teamSchema),
  });

  const loadTeams = async () => {
    try {
      const res = await orgService.listTeams();
      if (res.success && res.data) {
        const teamList = res.data.content;
        setTeams(teamList);
        if (teamList.length > 0 && !activeTeam) {
          setActiveTeam(teamList[0]);
        }
      }

      const usersRes = await orgService.listUsers();
      if (usersRes.success && usersRes.data) {
        setUsers(usersRes.data.content);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const loadTeamMembers = async () => {
    if (!activeTeam) return;
    try {
      const res = await orgService.getTeamMembers(activeTeam.id);
      if (res.success && res.data) {
        setMembers(res.data.content);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadTeams();
  }, []);

  useEffect(() => {
    if (activeTeam) {
      loadTeamMembers();
    }
  }, [activeTeam]);

  const onAddTeamSubmit = async (data: TeamFields) => {
    try {
      const res = await orgService.createTeam(data.name, data.description || undefined, activeOrgId || undefined);
      if (res.success && res.data) {
        toast('Team Created', `Team "${data.name}" added successfully.`, 'success');
        reset();
        setTeamDialogOpen(false);
        loadTeams();
      }
    } catch {
      toast('Failed to create team', undefined, 'error');
    }
  };

  const handleAddMember = async () => {
    if (!activeTeam || !selectedUserId) return;
    try {
      const res = await orgService.addTeamMember(activeTeam.id, selectedUserId, selectedRole);
      if (res.success) {
        toast('Member added', undefined, 'success');
        setMemberDialogOpen(false);
        setSelectedUserId('');
        loadTeamMembers();
      }
    } catch {
      toast('Failed to add member', 'User might already be a member.', 'error');
    }
  };

  const handleRemoveMember = async (userId: string) => {
    if (!activeTeam) return;
    try {
      const res = await orgService.removeTeamMember(activeTeam.id, userId);
      if (res.success) {
        toast('Member removed', undefined, 'success');
        loadTeamMembers();
      }
    } catch {
      toast('Failed to remove member', undefined, 'error');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Teams</h1>
          <p className="text-xs text-muted-foreground mt-1">
            Orchestrate developers, QA engineers, and agile workspaces
          </p>
        </div>
        <Button size="sm" onClick={() => setTeamDialogOpen(true)} disabled={!activeOrgId}>
          <Plus className="h-4.5 w-4.5 mr-1" /> Create Team
        </Button>
      </div>

      {!activeOrgId && (
        <div className="p-4 bg-amber-500/10 border border-amber-500/20 text-amber-400 rounded-lg text-xs leading-relaxed">
          Please select or create an organization in the <strong>Organizations</strong> page first before managing teams.
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Teams Sidebar */}
        <div className="space-y-4">
          <Card>
            <CardHeader className="py-3 border-b border-border/40">
              <CardTitle className="text-xs uppercase tracking-wide text-muted-foreground">Team Spaces</CardTitle>
            </CardHeader>
            <CardContent className="p-3 space-y-2 max-h-[400px] overflow-y-auto">
              {teams.length > 0 ? (
                teams.map((t) => (
                  <button
                    key={t.id}
                    onClick={() => setActiveTeam(t)}
                    className={`w-full text-left p-3 rounded-lg text-xs font-semibold border transition-all ${
                      activeTeam?.id === t.id
                        ? 'bg-primary text-primary-foreground border-primary'
                        : 'bg-card text-foreground hover:bg-secondary/40 border-border/40'
                    }`}
                  >
                    {t.name}
                  </button>
                ))
              ) : (
                <p className="text-xs text-muted-foreground py-4 text-center">No teams found.</p>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Team Detail Contents */}
        <div className="lg:col-span-2">
          {activeTeam ? (
            <Card>
              <CardHeader className="flex flex-row justify-between items-start border-b border-border/40 pb-4">
                <div>
                  <CardTitle className="text-base">{activeTeam.name}</CardTitle>
                  <CardDescription className="text-xs mt-1">{activeTeam.description || 'No description.'}</CardDescription>
                </div>
                <Button size="sm" onClick={() => setMemberDialogOpen(true)}>
                  <Plus className="h-4 w-4 mr-1.5" /> Add Member
                </Button>
              </CardHeader>
              <CardContent className="pt-6">
                <h4 className="text-xs font-bold text-muted-foreground uppercase tracking-wide mb-3">Team Collaborators</h4>
                <div className="space-y-3.5">
                  {members.length > 0 ? (
                    members.map((m) => (
                      <div
                        key={m.id}
                        className="flex justify-between items-center text-xs p-3 rounded-lg border border-border/40 hover:border-border transition-colors bg-secondary/10"
                      >
                        <div className="flex items-center gap-3">
                          <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center font-bold text-xs">
                            {m.firstName?.[0]}{m.lastName?.[0]}
                          </div>
                          <div>
                            <p className="font-semibold">{m.firstName} {m.lastName}</p>
                            <p className="text-[10px] text-muted-foreground mt-0.5">{m.email}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className={`text-[9px] uppercase px-1.5 py-0.5 rounded ${
                            m.role === 'LEAD' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' : 'bg-primary/10 border border-primary/20'
                          }`}>
                            {m.role}
                          </span>
                          <Button variant="ghost" size="icon" className="h-7 w-7 text-rose-400 hover:text-rose-600" onClick={() => handleRemoveMember(m.userId)}>
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-muted-foreground text-center py-4">No team members added yet.</p>
                  )}
                </div>
              </CardContent>
            </Card>
          ) : (
            <div className="text-center py-20 text-xs text-muted-foreground border border-dashed rounded-xl">
              No team selected. Click Create Team to get started.
            </div>
          )}
        </div>
      </div>

      {/* Create Team Dialog */}
      <Dialog open={teamDialogOpen} onOpenChange={setTeamDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Team</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onAddTeamSubmit)} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold mb-1.5">Team Name</label>
              <Input placeholder="Back-End Engineering" error={errors.name?.message} {...register('name')} />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1.5">Description (Optional)</label>
              <textarea
                placeholder="Database models, Docker configurations, Java roadmap tasks..."
                className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring min-h-[80px]"
                {...register('description')}
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" size="sm" onClick={() => setTeamDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" loading={isSubmitting}>
                Create Team
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Add Member Dialog */}
      <Dialog open={memberDialogOpen} onOpenChange={setMemberDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Team Member</DialogTitle>
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
              <label className="block text-xs font-semibold mb-1.5">Team Role</label>
              <select
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value as 'MEMBER' | 'LEAD')}
                className="flex h-10 w-full rounded-md border border-input bg-card px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="MEMBER">Member</option>
                <option value="LEAD">Team Lead</option>
              </select>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" size="sm" onClick={() => setMemberDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="button" size="sm" onClick={handleAddMember}>
                Add Member
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

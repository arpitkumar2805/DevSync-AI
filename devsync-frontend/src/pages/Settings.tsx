import React, { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useTheme } from '../context/ThemeContext';
import { orgService } from '../services/orgService';
import { notificationService } from '../services/notificationService';
import { useToast } from '../context/ToastContext';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Building, Shield, User, Sun, Moon } from 'lucide-react';

export const Settings: React.FC = () => {
  const { user, activeOrgId } = useAuth();
  const { theme, setTheme } = useTheme();
  const { toast } = useToast();

  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState('DEVELOPER');
  const [inviteLoading, setInviteLoading] = useState(false);

  const [auditLogs, setAuditLogs] = useState<any[]>([]);

  useEffect(() => {
    if (user?.roleName === 'SUPER_ADMIN' || user?.roleName === 'ORG_ADMIN') {
      notificationService.listAuditLogs().then((res) => {
        if (res.success && res.data) {
          setAuditLogs(res.data.content);
        }
      });
    }
  }, [user]);

  const handleSendInvite = async () => {
    if (!activeOrgId || !inviteEmail.trim()) {
      toast('Please enter a valid email address', undefined, 'error');
      return;
    }
    setInviteLoading(true);
    try {
      const res = await orgService.inviteMember(activeOrgId, inviteEmail, inviteRole);
      if (res.success) {
        toast('Invitation Sent', `Email invite successfully sent to ${inviteEmail}`, 'success');
        setInviteEmail('');
      }
    } catch {
      toast('Invite failed', 'Something went wrong.', 'error');
    } finally {
      setInviteLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Welcome Settings Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Settings</h1>
        <p className="text-xs text-muted-foreground mt-1">Configure profile details, workspace themes, and team access.</p>
      </div>

      <div className="space-y-6">
        {/* Profile Card */}
        <Card>
          <CardHeader className="pb-3 flex flex-row items-center gap-2">
            <User className="h-5 w-5 text-primary" />
            <div>
              <CardTitle className="text-sm">User Profile</CardTitle>
              <CardDescription className="text-[10px]">Your personal account parameters</CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-3.5 text-xs">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-[10px] font-bold text-muted-foreground uppercase block mb-1">First Name</label>
                <Input value={user?.firstName || ''} readOnly className="bg-secondary/20" />
              </div>
              <div>
                <label className="text-[10px] font-bold text-muted-foreground uppercase block mb-1">Last Name</label>
                <Input value={user?.lastName || ''} readOnly className="bg-secondary/20" />
              </div>
            </div>
            <div>
              <label className="text-[10px] font-bold text-muted-foreground uppercase block mb-1">Email Address</label>
              <Input value={user?.email || ''} readOnly className="bg-secondary/20" />
            </div>
          </CardContent>
        </Card>

        {/* Invite Member Card (Admins only) */}
        {activeOrgId && (user?.roleName === 'SUPER_ADMIN' || user?.roleName === 'ORG_ADMIN') && (
          <Card>
            <CardHeader className="pb-3 flex flex-row items-center gap-2">
              <Building className="h-5 w-5 text-indigo-400" />
              <div>
                <CardTitle className="text-sm">Invite Members</CardTitle>
                <CardDescription className="text-[10px]">Add team members to your organization workspace</CardDescription>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex flex-col sm:flex-row gap-3">
                <div className="flex-1">
                  <Input
                    type="email"
                    placeholder="developer@acme.com"
                    value={inviteEmail}
                    onChange={(e) => setInviteEmail(e.target.value)}
                  />
                </div>
                <div className="w-full sm:w-48">
                  <select
                    value={inviteRole}
                    onChange={(e) => setInviteRole(e.target.value)}
                    className="flex h-10 w-full rounded-md border border-input bg-card px-3 py-2 text-sm focus-visible:outline-none"
                  >
                    <option value="DEVELOPER">Developer</option>
                    <option value="PROJECT_MANAGER">Project Manager</option>
                    <option value="QA_ENGINEER">QA Engineer</option>
                    <option value="TEAM_LEAD">Team Lead</option>
                    <option value="ORG_ADMIN">Org Admin</option>
                  </select>
                </div>
                <Button size="sm" onClick={handleSendInvite} loading={inviteLoading} className="h-10 shrink-0">
                  Send Invite
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Theme Settings Card */}
        <Card>
          <CardHeader className="pb-3 flex flex-row items-center gap-2">
            <Sun className="h-5 w-5 text-amber-400" />
            <div>
              <CardTitle className="text-sm">Theme Settings</CardTitle>
              <CardDescription className="text-[10px]">Adjust background layout display themes</CardDescription>
            </div>
          </CardHeader>
          <CardContent className="flex gap-3">
            <Button
              variant={theme === 'light' ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setTheme('light')}
              className="text-xs flex items-center gap-1.5"
            >
              <Sun className="h-4 w-4" /> Light
            </Button>
            <Button
              variant={theme === 'dark' ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setTheme('dark')}
              className="text-xs flex items-center gap-1.5"
            >
              <Moon className="h-4 w-4" /> Dark
            </Button>
          </CardContent>
        </Card>

        {/* Security Logs (Admins only) */}
        {auditLogs.length > 0 && (
          <Card>
            <CardHeader className="flex flex-row items-center gap-2 pb-3">
              <Shield className="h-5 w-5 text-emerald-400" />
              <div>
                <CardTitle className="text-sm">Audit Security Logs</CardTitle>
                <CardDescription className="text-[10px]">Immutable workspace modification timestamps</CardDescription>
              </div>
            </CardHeader>
            <CardContent className="space-y-2.5 max-h-52 overflow-y-auto">
              {auditLogs.map((log) => (
                <div key={log.id} className="p-3 bg-secondary/10 border border-border/40 rounded-lg text-[10px] leading-relaxed flex justify-between">
                  <div>
                    <p className="font-semibold text-foreground">{log.action}</p>
                    <p className="text-muted-foreground mt-0.5">{log.details}</p>
                    <p className="text-muted-foreground mt-0.5">IP: {log.ipAddress}</p>
                  </div>
                  <span className="text-muted-foreground shrink-0">{new Date(log.timestamp).toLocaleString()}</span>
                </div>
              ))}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
};

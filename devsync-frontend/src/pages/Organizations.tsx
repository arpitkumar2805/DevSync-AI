import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { orgService } from '../services/orgService';
import { Organization } from '../types';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../hooks/useAuth';
import { Card, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../components/ui/Dialog';
import { Plus, Building, Check, ArrowRight } from 'lucide-react';

const orgSchema = zod.object({
  name: zod.string().min(1, 'Organization name is required').max(100, 'Name must be at most 100 characters'),
  description: zod.string().optional(),
});

type OrgFields = zod.infer<typeof orgSchema>;

export const Organizations: React.FC = () => {
  const { toast } = useToast();
  const { activeOrgId, setActiveOrgId } = useAuth();
  const [orgs, setOrgs] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<OrgFields>({
    resolver: zodResolver(orgSchema),
  });

  const loadOrgs = async () => {
    try {
      const res = await orgService.listOrgs(0, 100);
      if (res.success && res.data) {
        setOrgs(res.data.content);
      }
    } catch {
      toast('Failed to load organizations', undefined, 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrgs();
  }, []);

  const onSubmit = async (data: OrgFields) => {
    try {
      const res = await orgService.createOrg(data.name, data.description || undefined);
      if (res.success && res.data) {
        toast('Organization Created', `Organization "${data.name}" added successfully.`, 'success');
        reset();
        setDialogOpen(false);
        setActiveOrgId(res.data.id);
        loadOrgs();
      }
    } catch {
      toast('Failed to create organization', undefined, 'error');
    }
  };

  const handleSelectOrg = (orgId: string) => {
    setActiveOrgId(orgId);
    toast('Organization Selected', 'Your active workspace context has been updated.', 'success');
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Organizations</h1>
          <p className="text-xs text-muted-foreground mt-1">
            Switch between corporate workspaces, development houses, or personal projects.
          </p>
        </div>
        <Button size="sm" onClick={() => setDialogOpen(true)}>
          <Plus className="h-4.5 w-4.5 mr-1" /> Create Org
        </Button>
      </div>

      {/* Grid List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {loading ? (
          <div className="space-y-2 animate-pulse col-span-2">
            <div className="h-20 bg-secondary/40 rounded-xl" />
            <div className="h-20 bg-secondary/40 rounded-xl" />
          </div>
        ) : orgs.length > 0 ? (
          orgs.map((org) => {
            const isActive = activeOrgId === org.id;
            return (
              <Card
                key={org.id}
                onClick={() => handleSelectOrg(org.id)}
                className={`cursor-pointer transition-all duration-200 border hover:border-primary/40 relative ${
                  isActive
                    ? 'border-primary/50 bg-primary/5 shadow-[0_0_15px_rgba(var(--primary-rgb),0.05)]'
                    : 'border-border/40 hover:bg-secondary/10'
                }`}
              >
                <CardContent className="p-5 flex justify-between items-center">
                  <div className="flex items-center gap-4">
                    <div className={`h-10 w-10 rounded-lg flex items-center justify-center transition-colors ${
                      isActive ? 'bg-primary/20 text-primary' : 'bg-secondary/60 text-muted-foreground'
                    }`}>
                      <Building className="h-5 w-5" />
                    </div>
                    <div>
                      <h3 className="text-sm font-semibold">{org.name}</h3>
                      <p className="text-[10px] text-muted-foreground mt-0.5 max-w-[240px] truncate">
                        {org.description || 'No description provided.'}
                      </p>
                    </div>
                  </div>
                  <div>
                    {isActive ? (
                      <span className="h-6 w-6 rounded-full bg-primary/20 text-primary flex items-center justify-center">
                        <Check className="h-4.5 w-4.5" />
                      </span>
                    ) : (
                      <ArrowRight className="h-4.5 w-4.5 text-muted-foreground/40" />
                    )}
                  </div>
                </CardContent>
              </Card>
            );
          })
        ) : (
          <div className="text-center py-16 text-xs text-muted-foreground border border-dashed rounded-xl col-span-2">
            No organizations found. Click Create Org to configure your first workspace.
          </div>
        )}
      </div>

      {/* Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Organization</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold mb-1.5">Organization Name</label>
              <Input placeholder="Acme Corporation" error={errors.name?.message} {...register('name')} />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1.5">Description (Optional)</label>
              <textarea
                placeholder="Core product lines, DevOps squads, client delivery frameworks..."
                className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring min-h-[80px]"
                {...register('description')}
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" size="sm" onClick={() => setDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" size="sm" loading={isSubmitting}>
                Create Organization
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

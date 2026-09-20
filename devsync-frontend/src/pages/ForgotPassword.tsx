import React from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { useToast } from '../context/ToastContext';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { FileCode, ArrowLeft } from 'lucide-react';

const forgotPasswordSchema = zod.object({
  email: zod.string().email('Invalid email address'),
});

type ForgotPasswordFields = zod.infer<typeof forgotPasswordSchema>;

export const ForgotPassword: React.FC = () => {
  const { toast } = useToast();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotPasswordFields>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data: ForgotPasswordFields) => {
    // Note: Forgot password route is modeled as future scope or simple API wrapper.
    // We simulate client response and inform the user.
    try {
      // Mock / placeholder response for recovery
      toast('Reset Link Sent', `We sent a recovery link to ${data.email}`, 'success');
    } catch {
      toast('Request failed', 'Something went wrong. Try again.', 'error');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="w-full max-w-md space-y-6 bg-card border border-border p-8 rounded-2xl shadow-xl">
        {/* Header */}
        <div className="flex flex-col items-center text-center">
          <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center border border-primary/20 mb-4">
            <FileCode className="h-6 w-6 text-primary" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Forgot password?</h2>
          <p className="text-xs text-muted-foreground mt-1">
            No worries, enter your email and we will send you a reset link
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold mb-1.5">Email address</label>
            <Input
              type="email"
              placeholder="name@organization.com"
              error={errors.email?.message}
              {...register('email')}
            />
          </div>

          <Button type="submit" loading={isSubmitting} className="w-full mt-2">
            Send Reset Link
          </Button>
        </form>

        {/* Footer */}
        <div className="text-center text-xs mt-6">
          <Link to="/login" className="inline-flex items-center gap-1.5 text-muted-foreground hover:text-foreground">
            <ArrowLeft className="h-3.5 w-3.5" />
            Back to login
          </Link>
        </div>
      </div>
    </div>
  );
};

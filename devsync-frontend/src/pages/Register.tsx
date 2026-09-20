import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as zod from 'zod';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../context/ToastContext';
import { authService } from '../services/authService';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { FileCode } from 'lucide-react';

const registerSchema = zod.object({
  firstName: zod.string().min(1, 'First name is required'),
  lastName: zod.string().min(1, 'Last name is required'),
  email: zod.string().email('Invalid email address'),
  password: zod.string().min(6, 'Password must be at least 6 characters'),
  organizationName: zod.string().optional(),
});

type RegisterFields = zod.infer<typeof registerSchema>;

export const Register: React.FC = () => {
  const { login } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFields>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFields) => {
    try {
      const response = await authService.register(
        data.email,
        data.password,
        data.firstName,
        data.lastName,
        data.organizationName || undefined
      );

      if (response.success && response.data) {
        await login(response.data);
        toast('Account created', 'Welcome to DevSync AI!', 'success');
        navigate('/');
      } else {
        toast('Registration failed', response.message || 'Check your details.', 'error');
      }
    } catch (error: any) {
      console.error(error);
      toast('Registration failed', error.response?.data?.message || 'Server error', 'error');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 py-12">
      <div className="w-full max-w-md space-y-6 bg-card border border-border p-8 rounded-2xl shadow-xl">
        {/* Header */}
        <div className="flex flex-col items-center text-center">
          <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center border border-primary/20 mb-4">
            <FileCode className="h-6 w-6 text-primary" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Create your account</h2>
          <p className="text-xs text-muted-foreground mt-1">
            Get started with DevSync AI to build, plan, and track sprints
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold mb-1.5">First name</label>
              <Input
                placeholder="John"
                error={errors.firstName?.message}
                {...register('firstName')}
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1.5">Last name</label>
              <Input
                placeholder="Doe"
                error={errors.lastName?.message}
                {...register('lastName')}
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1.5">Email address</label>
            <Input
              type="email"
              placeholder="john@example.com"
              error={errors.email?.message}
              {...register('email')}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1.5">Password</label>
            <Input
              type="password"
              placeholder="••••••••"
              error={errors.password?.message}
              {...register('password')}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1.5">Organization name (Optional)</label>
            <Input
              placeholder="Acme Corp"
              error={errors.organizationName?.message}
              {...register('organizationName')}
            />
          </div>

          <Button type="submit" loading={isSubmitting} className="w-full mt-2">
            Create Account
          </Button>
        </form>

        {/* Footer */}
        <div className="text-center text-xs mt-6">
          <span className="text-muted-foreground">Already have an account? </span>
          <Link to="/login" className="font-semibold text-primary hover:underline">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};

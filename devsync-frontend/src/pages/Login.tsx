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

const loginSchema = zod.object({
  email: zod.string().email('Invalid email address'),
  password: zod.string().min(6, 'Password must be at least 6 characters'),
});

type LoginFields = zod.infer<typeof loginSchema>;

export const Login: React.FC = () => {
  const { login } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFields>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFields) => {
    try {
      const response = await authService.login(data.email, data.password);
      if (response.success && response.data) {
        await login(response.data);
        toast('Logged in successfully', `Welcome back, ${response.data.email}!`, 'success');
        navigate('/');
      } else {
        toast('Login failed', response.message || 'Check your credentials.', 'error');
      }
    } catch (error: any) {
      console.error(error);
      toast('Login failed', error.response?.data?.message || 'Server error', 'error');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="w-full max-w-md space-y-8 bg-card border border-border p-8 rounded-2xl shadow-xl">
        {/* Header */}
        <div className="flex flex-col items-center text-center">
          <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center border border-primary/20 mb-4">
            <FileCode className="h-6 w-6 text-primary" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Sign in to DevSync AI</h2>
          <p className="text-xs text-muted-foreground mt-2">
            Enter your credentials to access your organization dashboard
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

          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="text-xs font-semibold">Password</label>
              <Link to="/forgot-password" className="text-xs text-primary/80 hover:underline">
                Forgot password?
              </Link>
            </div>
            <Input
              type="password"
              placeholder="••••••••"
              error={errors.password?.message}
              {...register('password')}
            />
          </div>

          <Button type="submit" loading={isSubmitting} className="w-full mt-2">
            Sign In
          </Button>
        </form>

        {/* Footer */}
        <div className="text-center text-xs mt-6">
          <span className="text-muted-foreground">New to DevSync AI? </span>
          <Link to="/register" className="font-semibold text-primary hover:underline">
            Create an account
          </Link>
        </div>
      </div>
    </div>
  );
};

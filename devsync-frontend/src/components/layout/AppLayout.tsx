import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useTheme } from '../../context/ThemeContext';
import { useToast } from '../../context/ToastContext';
import {
  LayoutDashboard,
  FolderKanban,
  Users2,
  Calendar,
  Settings,
  LogOut,
  Moon,
  Sun,
  Bot,
  Bell,
  Menu,
  X,
  FileCode,
  Building,
} from 'lucide-react';
import { Button } from '../ui/Button';

interface AppLayoutProps {
  children: React.ReactNode;
}

export const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const { user, logout, activeOrgName } = useAuth();
  const { theme, setTheme } = useTheme();
  const { toast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  const menuItems = [
    { name: 'Dashboard', icon: LayoutDashboard, path: '/' },
    { name: 'Organizations', icon: Building, path: '/organizations' },
    { name: 'Projects', icon: FolderKanban, path: '/projects' },
    { name: 'Teams', icon: Users2, path: '/teams' },
    { name: 'Calendar', icon: Calendar, path: '/calendar' },
    { name: 'AI Assistant', icon: Bot, path: '/ai-assistant' },
    { name: 'Settings', icon: Settings, path: '/settings' },
  ];

  const handleLogout = () => {
    logout();
    toast('Logged out successfully', 'Come back soon!', 'success');
    navigate('/login');
  };

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  return (
    <div className="flex h-screen bg-background text-foreground overflow-hidden">
      {/* Sidebar for Desktop */}
      <aside className="hidden md:flex flex-col w-64 border-r border-border bg-card shrink-0">
        {/* Logo */}
        <div className="h-16 flex items-center gap-2 px-6 border-b border-border">
          <FileCode className="h-6 w-6 text-primary" />
          <span className="font-bold text-lg tracking-tight">DevSync AI</span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
          {menuItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-md transition-all ${
                  isActive
                    ? 'bg-primary text-primary-foreground'
                    : 'text-muted-foreground hover:bg-secondary hover:text-foreground'
                }`}
              >
                <item.icon className="h-4 w-4" />
                {item.name}
              </Link>
            );
          })}
        </nav>

        {/* Footer info & Logout */}
        <div className="p-4 border-t border-border space-y-3 bg-secondary/20">
          <div className="flex items-center gap-3 px-2">
            <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center font-semibold text-sm border border-primary/20 text-foreground">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-xs font-semibold truncate leading-none">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-[10px] text-muted-foreground truncate mt-1">
                {user?.email}
              </p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={handleLogout}
            className="w-full justify-start text-muted-foreground hover:text-destructive text-xs py-1.5 h-auto"
          >
            <LogOut className="h-4 w-4 mr-2" />
            Sign Out
          </Button>
        </div>
      </aside>

      {/* Mobile Drawer Sidebar */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <div className="fixed inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setSidebarOpen(false)} />
          <aside className="relative flex flex-col w-64 border-r border-border bg-card animate-in slide-in-from-left duration-200">
            <div className="h-16 flex items-center justify-between px-6 border-b border-border">
              <div className="flex items-center gap-2">
                <FileCode className="h-6 w-6 text-primary" />
                <span className="font-bold text-lg">DevSync AI</span>
              </div>
              <button onClick={() => setSidebarOpen(false)}>
                <X className="h-5 w-5" />
              </button>
            </div>
            <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
              {menuItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.name}
                    to={item.path}
                    onClick={() => setSidebarOpen(false)}
                    className={`flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-md transition-all ${
                      isActive
                        ? 'bg-primary text-primary-foreground'
                        : 'text-muted-foreground hover:bg-secondary hover:text-foreground'
                    }`}
                  >
                    <item.icon className="h-4 w-4" />
                    {item.name}
                  </Link>
                );
              })}
            </nav>
            <div className="p-4 border-t border-border space-y-3 bg-secondary/20">
              <div className="flex items-center gap-3 px-2">
                <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center font-semibold text-sm">
                  {user?.firstName?.[0]}{user?.lastName?.[0]}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-semibold truncate leading-none">
                    {user?.firstName} {user?.lastName}
                  </p>
                  <p className="text-[10px] text-muted-foreground truncate mt-1">
                    {user?.email}
                  </p>
                </div>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="w-full justify-start text-muted-foreground hover:text-destructive text-xs py-1.5 h-auto"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Sign Out
              </Button>
            </div>
          </aside>
        </div>
      )}

      {/* Main Content Area */}
      <div className="flex flex-col flex-1 overflow-hidden">
        {/* Header / Navbar */}
        <header className="h-16 border-b border-border bg-card/50 backdrop-blur-md flex items-center justify-between px-6 z-10 shrink-0">
          <div className="flex items-center gap-4">
            <button className="md:hidden" onClick={() => setSidebarOpen(true)}>
              <Menu className="h-5 w-5" />
            </button>
            <div className="hidden sm:flex items-center gap-2 text-sm text-muted-foreground border border-border px-3 py-1.5 rounded-lg bg-secondary/30">
              <Building className="h-4 w-4" />
              <span>Org context:</span>
              <span className="font-semibold text-foreground truncate max-w-[120px]">
                {activeOrgName || 'None'}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {/* Theme Toggle */}
            <Button variant="ghost" size="icon" onClick={toggleTheme} className="rounded-full">
              {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
            </Button>

            {/* AI Assistant Quick Trigger */}
            <Button
              variant="ghost"
              size="icon"
              className="rounded-full relative hover:text-primary"
              onClick={() => navigate('/ai-assistant')}
            >
              <Bot className="h-4 w-4" />
            </Button>

            {/* Notifications Trigger */}
            <div className="relative">
              <Button
                variant="ghost"
                size="icon"
                className="rounded-full relative"
                onClick={() => setNotificationsOpen(!notificationsOpen)}
              >
                <Bell className="h-4 w-4" />
                <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-rose-500" />
              </Button>

              {notificationsOpen && (
                <>
                  <div className="fixed inset-0 z-20" onClick={() => setNotificationsOpen(false)} />
                  <div className="absolute right-0 mt-2 w-80 rounded-xl border border-border bg-card p-4 shadow-xl z-30 animate-in fade-in slide-in-from-top-2 duration-150">
                    <div className="flex justify-between items-center mb-3">
                      <h3 className="font-bold text-sm">Notifications</h3>
                      <button
                        className="text-[10px] text-muted-foreground hover:underline"
                        onClick={() => {
                          toast('Marked all as read', undefined, 'success');
                          setNotificationsOpen(false);
                        }}
                      >
                        Mark all read
                      </button>
                    </div>
                    <div className="space-y-2 max-h-60 overflow-y-auto">
                      <div className="p-2.5 rounded-lg hover:bg-secondary/40 transition-colors text-xs border border-border/40">
                        <p className="font-semibold">Task Assigned</p>
                        <p className="text-muted-foreground mt-0.5">You have been assigned to sprint planning task.</p>
                      </div>
                      <div className="p-2.5 rounded-lg hover:bg-secondary/40 transition-colors text-xs border border-border/40">
                        <p className="font-semibold">Sprint Started</p>
                        <p className="text-muted-foreground mt-0.5">Sprint-01 has been set active.</p>
                      </div>
                    </div>
                  </div>
                </>
              )}
            </div>

            {/* User Profile avatar */}
            <div
              className="h-8 w-8 rounded-full bg-primary/10 border border-border/60 flex items-center justify-center font-bold text-xs cursor-pointer"
              onClick={() => navigate('/settings')}
            >
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
          </div>
        </header>

        {/* Content Portal */}
        <main className="flex-1 overflow-y-auto bg-background p-6">
          <div className="max-w-7xl mx-auto space-y-6 animate-in fade-in duration-200">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

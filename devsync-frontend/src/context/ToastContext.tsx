import React, { createContext, useContext, useState, useCallback } from 'react';
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';
import { twMerge } from 'tailwind-merge';
import { clsx } from 'clsx';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastMessage {
  id: string;
  type: ToastType;
  title: string;
  description?: string;
}

interface ToastContextType {
  toast: (title: string, description?: string, type?: ToastType) => void;
  toasts: ToastMessage[];
  dismiss: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const toast = useCallback((title: string, description?: string, type: ToastType = 'info') => {
    const id = Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, title, description, type }]);

    // Auto-dismiss after 4 seconds
    setTimeout(() => dismiss(id), 4000);
  }, [dismiss]);

  return (
    <ToastContext.Provider value={{ toast, toasts, dismiss }}>
      {children}
      {/* Toast Container Portal */}
      <div className="fixed bottom-4 right-4 z-[9999] flex flex-col gap-2 w-full max-w-sm">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={twMerge(
              'flex gap-3 p-4 rounded-lg border shadow-lg backdrop-blur-md animate-in slide-in-from-bottom-5 duration-200 bg-card text-card-foreground',
              clsx({
                'border-emerald-500/30 bg-emerald-950/20 text-emerald-300': t.type === 'success',
                'border-rose-500/30 bg-rose-950/20 text-rose-300': t.type === 'error',
                'border-blue-500/30 bg-blue-950/20 text-blue-300': t.type === 'info',
              })
            )}
          >
            <div className="mt-0.5 shrink-0">
              {t.type === 'success' && <CheckCircle className="h-5 w-5 text-emerald-400" />}
              {t.type === 'error' && <AlertCircle className="h-5 w-5 text-rose-400" />}
              {t.type === 'info' && <Info className="h-5 w-5 text-blue-400" />}
            </div>
            <div className="flex-1">
              <h4 className="text-sm font-semibold leading-tight">{t.title}</h4>
              {t.description && <p className="text-xs mt-1 leading-normal opacity-90">{t.description}</p>}
            </div>
            <button
              onClick={() => dismiss(t.id)}
              className="shrink-0 rounded-sm opacity-70 hover:opacity-100 focus:outline-none cursor-pointer"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

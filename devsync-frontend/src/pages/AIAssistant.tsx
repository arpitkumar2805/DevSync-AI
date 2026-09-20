import React, { useState, useEffect } from 'react';
import { aiService, ChatMessage } from '../services/aiService';
import { projectService } from '../services/projectService';
import { Project } from '../types';
import { useToast } from '../context/ToastContext';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Brain, Bot, Send, Sparkles, AlertTriangle, ShieldCheck } from 'lucide-react';
import { twMerge } from 'tailwind-merge';

export const AIAssistant: React.FC = () => {
  const { toast } = useToast();
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  
  // Chat console states
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  // Command insights
  const [aiReport, setAiReport] = useState('');
  const [reportLoading, setReportLoading] = useState(false);

  useEffect(() => {
    projectService.listProjects().then((res) => {
      if (res.success && res.data) {
        setProjects(res.data.content);
        if (res.data.content.length > 0) {
          setSelectedProjectId(res.data.content[0].id);
        }
      }
    });
  }, []);

  const handleSendMessage = async () => {
    if (!inputMessage.trim()) return;
    const userMsg = inputMessage;
    setInputMessage('');
    setMessages((prev) => [...prev, { role: 'user', content: userMsg }]);
    setChatLoading(true);

    try {
      const response = await aiService.sendChatMessage(userMsg, selectedProjectId || undefined);
      if (response.success && response.data) {
        setMessages((prev) => [...prev, { role: 'assistant', content: response.data! }]);
      } else {
        setMessages((prev) => [...prev, { role: 'assistant', content: response.message || 'Apologies, AI chat request failed.' }]);
      }
    } catch (err: any) {
      const errMsg = err.response?.data?.message || err.message || 'Apologies, AI chat request failed.';
      setMessages((prev) => [...prev, { role: 'assistant', content: `Error: ${errMsg}` }]);
    } finally {
      setChatLoading(false);
    }
  };

  const runCommand = async (command: 'health' | 'risk' | 'docs' | 'prioritize') => {
    if (!selectedProjectId) {
      toast('Please select a project context', undefined, 'error');
      return;
    }
    setReportLoading(true);
    setAiReport('Querying AI Engine, please wait...');

    try {
      let res;
      if (command === 'health') {
        res = await aiService.getProjectHealth(selectedProjectId);
      } else if (command === 'risk') {
        res = await aiService.getDeadlineRisk(selectedProjectId);
      } else if (command === 'docs') {
        res = await aiService.generateDocumentation(selectedProjectId);
      } else {
        res = await aiService.getTaskPrioritization(selectedProjectId);
      }

      if (res.success && res.data) {
        setAiReport(res.data);
      }
    } catch {
      setAiReport('Intelligence query failed.');
    } finally {
      setReportLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[calc(100vh-140px)]">
      {/* AI Controls and Prompt Templates */}
      <div className="space-y-6 flex flex-col justify-between">
        <div className="space-y-6">
          <Card>
            <CardHeader className="pb-3 flex flex-row items-center gap-2">
              <div className="h-8 w-8 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400">
                <Brain className="h-4.5 w-4.5" />
              </div>
              <div>
                <CardTitle className="text-sm">Context Settings</CardTitle>
                <CardDescription className="text-[10px]">Select active workspace for analytics</CardDescription>
              </div>
            </CardHeader>
            <CardContent>
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="flex h-10 w-full rounded-md border border-input bg-card px-3 text-sm focus-visible:outline-none"
              >
                <option value="">Select Project</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </CardContent>
          </Card>

          {/* Quick Prompt Command triggers */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">AI Agent Toolkits</CardTitle>
              <CardDescription className="text-[10px]">Automated task audit tools</CardDescription>
            </CardHeader>
            <CardContent className="grid grid-cols-2 gap-2 text-xs">
              <Button variant="outline" className="text-xs h-9 justify-start" onClick={() => runCommand('health')} disabled={reportLoading}>
                <ShieldCheck className="h-4 w-4 mr-1.5 text-emerald-400" /> Health Score
              </Button>
              <Button variant="outline" className="text-xs h-9 justify-start" onClick={() => runCommand('risk')} disabled={reportLoading}>
                <AlertTriangle className="h-4 w-4 mr-1.5 text-rose-400" /> Deadline Risks
              </Button>
              <Button variant="outline" className="text-xs h-9 justify-start" onClick={() => runCommand('docs')} disabled={reportLoading}>
                <Sparkles className="h-4 w-4 mr-1.5 text-indigo-400" /> Release Notes
              </Button>
              <Button variant="outline" className="text-xs h-9 justify-start" onClick={() => runCommand('prioritize')} disabled={reportLoading}>
                <Bot className="h-4 w-4 mr-1.5 text-amber-400" /> Prioritize Tasks
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* Report summary console view */}
        <Card className="flex-1 mt-6 overflow-hidden flex flex-col">
          <CardHeader className="py-3 border-b border-border/40 shrink-0">
            <CardTitle className="text-xs">Audit Analysis Report</CardTitle>
          </CardHeader>
          <CardContent className="p-4 flex-1 overflow-y-auto text-[11px] leading-relaxed text-muted-foreground whitespace-pre-wrap">
            {aiReport || 'Trigger an AI agent tool on the left to populate analyses report.'}
          </CardContent>
        </Card>
      </div>

      {/* Main AI Chat Console Panel */}
      <Card className="lg:col-span-2 flex flex-col justify-between overflow-hidden">
        {/* Title */}
        <CardHeader className="border-b border-border/40 shrink-0 flex flex-row items-center gap-3">
          <div className="h-9 w-9 rounded-full bg-primary/10 flex items-center justify-center border border-border">
            <Bot className="h-4.5 w-4.5" />
          </div>
          <div>
            <CardTitle className="text-sm">AI Chat</CardTitle>
            <CardDescription className="text-[10px]">Contextual intelligence query over project documentation</CardDescription>
          </div>
        </CardHeader>

        {/* Chat Feed */}
        <div className="flex-1 p-6 space-y-4 overflow-y-auto max-h-[500px]">
          {messages.length > 0 ? (
            messages.map((m, idx) => (
              <div
                key={idx}
                className={twMerge(
                  'flex gap-3 text-xs leading-relaxed max-w-[80%] rounded-xl p-3',
                  m.role === 'user'
                    ? 'ml-auto bg-primary text-primary-foreground'
                    : 'mr-auto bg-secondary/60 border border-border/40 text-foreground'
                )}
              >
                <div>{m.content}</div>
              </div>
            ))
          ) : (
            <div className="flex flex-col items-center justify-center h-full text-center py-20 text-muted-foreground">
              <Sparkles className="h-8 w-8 text-indigo-400 mb-2 animate-pulse" />
              <p className="text-xs">Ask the assistant regarding active timelines, assignees, or story estimations.</p>
            </div>
          )}
        </div>

        {/* Chat input footer */}
        <div className="p-4 border-t border-border/40 shrink-0 bg-secondary/10 flex gap-2">
          <Input
            placeholder="Query workspace repository..."
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
          />
          <Button size="icon" onClick={handleSendMessage} loading={chatLoading} className="rounded-lg h-10 w-10 shrink-0">
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </Card>
    </div>
  );
};

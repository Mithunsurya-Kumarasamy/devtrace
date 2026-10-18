import React from 'react';
import { 
  LayoutDashboard, 
  BarChart2, 
  GitPullRequest, 
  Users, 
  Flame, 
  Network,
  MessageSquareText, 
  Cpu, 
  ShieldAlert,
  FolderGit2, 
  RefreshCw 
} from 'lucide-react';

export type NavTab = 
  | 'overview' 
  | 'analytics' 
  | 'process-mining' 
  | 'collaboration' 
  | 'codebase'
  | 'dependencies'
  | 'issues' 
  | 'ml-insights' 
  | 'pr-risk'
  | 'repositories' 
  | 'sync-jobs';

interface SidebarProps {
  activeTab: NavTab;
  onTabChange: (tab: NavTab) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ activeTab, onTabChange }) => {
  const navItems: { id: NavTab; label: string; icon: React.ReactNode; badge?: string }[] = [
    { id: 'overview', label: 'Executive Overview', icon: <LayoutDashboard className="w-4 h-4" /> },
    { id: 'analytics', label: 'Development Metrics', icon: <BarChart2 className="w-4 h-4" /> },
    { id: 'process-mining', label: 'Process Mining', icon: <GitPullRequest className="w-4 h-4" /> },
    { id: 'collaboration', label: 'Collaboration Network', icon: <Users className="w-4 h-4" /> },
    { id: 'codebase', label: 'Codebase Hotspots', icon: <Flame className="w-4 h-4" /> },
    { id: 'dependencies', label: 'Architecture & Coupling', icon: <Network className="w-4 h-4" /> },
    { id: 'issues', label: 'Issue NLP & Themes', icon: <MessageSquareText className="w-4 h-4" /> },
    { id: 'ml-insights', label: 'Anomaly Detection', icon: <Cpu className="w-4 h-4" />, badge: 'ML' },
    { id: 'pr-risk', label: 'PR Risk Guardrails', icon: <ShieldAlert className="w-4 h-4" />, badge: 'AI' },
    { id: 'repositories', label: 'Repositories', icon: <FolderGit2 className="w-4 h-4" /> },
    { id: 'sync-jobs', label: 'Sync Pipeline', icon: <RefreshCw className="w-4 h-4" /> },
  ];

  return (
    <aside className="w-64 bg-surface border-r border-border min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between shrink-0">
      <div className="space-y-1">
        <div className="px-3 py-2 text-xs font-semibold text-slate-500 uppercase tracking-wider">
          Intelligence Modules
        </div>
        {navItems.map((item) => {
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onTabChange(item.id)}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                isActive
                  ? 'bg-blue-600/10 text-blue-400 border border-blue-500/20 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
              }`}
            >
              <div className="flex items-center space-x-3 truncate">
                <span className={isActive ? 'text-blue-400' : 'text-slate-500'}>
                  {item.icon}
                </span>
                <span className="truncate">{item.label}</span>
              </div>
              {item.badge && (
                <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-purple-500/20 text-purple-300 border border-purple-500/30">
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </div>

      <div className="pt-4 border-t border-border/50 text-xs text-slate-500 px-3">
        <div className="flex items-center space-x-2">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
          <p className="font-medium text-slate-300">DevTrace Core Engine</p>
        </div>
        <p className="mt-1 text-slate-500 font-mono text-[11px]">Spring Boot 3 • Postgres 16 • KRaft</p>
      </div>
    </aside>
  );
};

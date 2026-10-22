import React, { useEffect, useState } from 'react';
import { Activity, Github, Radio, CheckCircle, AlertCircle, Calendar, ChevronDown, FolderGit2 } from 'lucide-react';
import { api } from '../../services/api';
import { HealthResponse } from '../../types';
import { useRepository } from '../../context/RepositoryContext';

interface HeaderProps {
  activeTab?: string;
}

export const Header: React.FC<HeaderProps> = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const { repositories, selectedRepoId, setSelectedRepoId, timeWindowDays, setTimeWindowDays } = useRepository();

  useEffect(() => {
    api.getHealth()
      .then((data) => {
        setHealth(data);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  return (
    <header className="h-16 bg-surface border-b border-border flex items-center justify-between px-6 sticky top-0 z-50">
      <div className="flex items-center space-x-6">
        {/* Brand */}
        <div className="flex items-center space-x-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-blue-600/10 border border-blue-500/20 text-blue-400 shadow-inner">
            <Activity className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-bold text-lg tracking-tight text-white font-sans">DevTrace</span>
              <span className="text-[10px] px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono font-medium">v1.0.0</span>
            </div>
            <p className="text-xs text-slate-400">Software Engineering Intelligence</p>
          </div>
        </div>

        {/* Repository Dropdown Selector */}
        <div className="hidden lg:flex items-center space-x-2 bg-slate-900 border border-border px-3 py-1.5 rounded-lg">
          <FolderGit2 className="w-4 h-4 text-blue-400 shrink-0" />
          <span className="text-xs text-slate-400">Target Repo:</span>
          {repositories.length > 0 ? (
            <div className="relative inline-block">
              <select
                value={selectedRepoId || ''}
                onChange={(e) => setSelectedRepoId(e.target.value)}
                className="bg-transparent text-xs font-medium text-slate-200 outline-none cursor-pointer pr-4 appearance-none"
              >
                {repositories.map((repo) => (
                  <option key={repo.id} value={repo.id} className="bg-slate-900 text-slate-200">
                    {repo.fullName}
                  </option>
                ))}
              </select>
              <ChevronDown className="w-3 h-3 text-slate-400 absolute right-0 top-1 pointer-events-none" />
            </div>
          ) : (
            <span className="text-xs text-slate-500 italic">No repositories registered</span>
          )}
        </div>

        {/* Time Window Selector */}
        <div className="hidden md:flex items-center space-x-1 bg-slate-900/80 border border-border p-1 rounded-lg text-xs">
          <Calendar className="w-3.5 h-3.5 text-slate-400 ml-1.5 mr-0.5" />
          {[30, 90, 180].map((days) => (
            <button
              key={days}
              onClick={() => setTimeWindowDays(days)}
              className={`px-2.5 py-1 rounded text-xs font-medium transition ${
                timeWindowDays === days
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              {days}d
            </button>
          ))}
        </div>
      </div>

      <div className="flex items-center space-x-4">
        {/* Live System Health Badge */}
        <div className="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-slate-900 border border-border text-xs">
          <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
          <span className="text-slate-400 hidden sm:inline">Engine:</span>
          {loading ? (
            <span className="text-slate-500">Connecting...</span>
          ) : health?.status === 'UP' ? (
            <span className="text-emerald-400 font-medium flex items-center space-x-1">
              <CheckCircle className="w-3 h-3 mr-1" />
              Operational
            </span>
          ) : (
            <span className="text-amber-400 font-medium flex items-center space-x-1">
              <AlertCircle className="w-3 h-3 mr-1" />
              Standby
            </span>
          )}
        </div>

        <a
          href="https://github.com/Mithunsurya-Kumarasamy/devtrace"
          target="_blank"
          rel="noopener noreferrer"
          className="p-2 rounded-lg bg-slate-900 border border-border text-slate-400 hover:text-white hover:border-slate-700 transition"
          title="GitHub Repository"
        >
          <Github className="w-4 h-4" />
        </a>
      </div>
    </header>
  );
};

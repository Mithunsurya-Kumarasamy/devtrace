import React, { useEffect, useState } from 'react';
import { Activity, Github, Radio, CheckCircle, AlertCircle } from 'lucide-react';
import { api } from '../../services/api';
import { HealthResponse } from '../../types';

interface HeaderProps {
  activeTab?: string;
}

export const Header: React.FC<HeaderProps> = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState(true);

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
      <div className="flex items-center space-x-3">
        <div className="flex items-center justify-center w-10 h-10 rounded-lg bg-blue-600/10 border border-blue-500/20 text-blue-400">
          <Activity className="w-5 h-5" />
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <span className="font-bold text-lg tracking-tight text-white font-sans">DevTrace</span>
            <span className="text-xs px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono">v1.0.0</span>
          </div>
          <p className="text-xs text-slate-400">Software Engineering Intelligence Platform</p>
        </div>
      </div>

      <div className="flex items-center space-x-4">
        {/* Live System Health Badge */}
        <div className="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-slate-900 border border-border text-xs">
          <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
          <span className="text-slate-400">Core Services:</span>
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
              Standalone Mode
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

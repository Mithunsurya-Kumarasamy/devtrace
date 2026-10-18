import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  CheckCircle2, 
  ArrowUpRight, 
  RefreshCw,
  Info,
  AlertTriangle
} from 'lucide-react';
import { api } from '../services/api';
import { HealthResponse } from '../types';

export const OverviewPage: React.FC = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadHealth();
  }, []);

  const loadHealth = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getHealth();
      setHealth(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to connect to DevTrace backend');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Platform Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-bold text-white tracking-tight">Software Engineering Intelligence</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              Active Node
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            DevTrace continuously transforms GitHub repository activity into actionable process mining, workflow bottleneck identification, codebase churn hotspots, and explainable PR risk analytics.
          </p>
        </div>
        <div className="mt-4 md:mt-0 flex items-center space-x-3">
          <button 
            onClick={loadHealth}
            className="flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh Diagnostics</span>
          </button>
        </div>
      </div>

      {/* Philosophy / Ethical Guardrail Notice */}
      <div className="flex items-start space-x-3 p-4 rounded-lg bg-blue-950/30 border border-blue-900/50 text-xs text-blue-200/80">
        <Info className="w-4 h-4 text-blue-400 flex-shrink-0 mt-0.5" />
        <p>
          <strong className="text-blue-300">DevTrace Engineering Principle:</strong> Activity metrics and commit counts do not objectively measure individual developer productivity. DevTrace focuses exclusively on identifying process bottlenecks, review wait times, module hotspots, and systemic engineering friction.
        </p>
      </div>

      {/* Error state if backend disconnected */}
      {error && (
        <div className="flex items-center space-x-3 p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          <AlertTriangle className="w-4 h-4 text-rose-400 flex-shrink-0" />
          <p>
            <strong>Backend Connection Notice:</strong> {error}. Ensure backend is running on port 8080.
          </p>
        </div>
      )}

      {/* Backend Diagnostics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>PLATFORM STATUS</span>
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="mt-2 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-white">
              {health?.status || (loading ? '...' : 'OFFLINE')}
            </span>
            <span className="text-xs text-emerald-400 font-mono">v{health?.version || '1.0.0'}</span>
          </div>
          <p className="mt-1 text-xs text-slate-400">
            {health?.application || 'DevTrace Engine'}
          </p>
        </div>

        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>PERSISTENT STORAGE</span>
            <CheckCircle2 className="w-4 h-4 text-blue-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-white">
            {health?.services?.database || 'PostgreSQL'}
          </div>
          <p className="mt-1 text-xs text-slate-400">Flyway schema versioning ready</p>
        </div>

        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>INGESTION PIPELINE</span>
            <RefreshCw className="w-4 h-4 text-purple-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-white">
            {health?.services?.ingestionPipeline || 'Kafka KRaft'}
          </div>
          <p className="mt-1 text-xs text-slate-400">Asynchronous idempotent workers</p>
        </div>

        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>ANALYTICS ENGINE</span>
            <ArrowUpRight className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-white">
            {health?.services?.analyticsEngine || 'Process Mining'}
          </div>
          <p className="mt-1 text-xs text-slate-400">Graph & ML anomaly detection</p>
        </div>
      </div>

      {/* Getting Started / Next Steps Card */}
      <div className="dev-card p-6">
        <h2 className="text-base font-semibold text-white">DevTrace Architecture Foundation</h2>
        <p className="text-sm text-slate-400 mt-1">
          Phase 01 foundation is running. Connect a GitHub repository in Phase 03 to stream commits, pull requests, issues, and releases through the Kafka event pipeline.
        </p>

        <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-lg bg-slate-900 border border-border">
            <span className="text-xs font-mono text-blue-400">PIPELINE 01</span>
            <h3 className="text-sm font-semibold text-slate-200 mt-1">GitHub Event Ingestion</h3>
            <p className="text-xs text-slate-400 mt-1">
              Supports checkpointed incremental synchronization with rate-limit and backoff handling.
            </p>
          </div>

          <div className="p-4 rounded-lg bg-slate-900 border border-border">
            <span className="text-xs font-mono text-purple-400">PIPELINE 02</span>
            <h3 className="text-sm font-semibold text-slate-200 mt-1">Process Mining & Churn</h3>
            <p className="text-xs text-slate-400 mt-1">
              Reconstructs PR lifecycle wait cycles and detects file modification churn hotspots.
            </p>
          </div>

          <div className="p-4 rounded-lg bg-slate-900 border border-border">
            <span className="text-xs font-mono text-emerald-400">PIPELINE 03</span>
            <h3 className="text-sm font-semibold text-slate-200 mt-1">Explainable PR Risk & Graphs</h3>
            <p className="text-xs text-slate-400 mt-1">
              Provides collaboration centrality, issue categorization, and explainable risk scores.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

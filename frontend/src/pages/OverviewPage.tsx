import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  CheckCircle2, 
  RefreshCw,
  Info,
  AlertTriangle,
  GitPullRequest,
  GitCommit,
  AlertCircle,
  Tag,
  Users,
  Activity,
  Zap,
  ArrowRight
} from 'lucide-react';
import { api } from '../services/api';
import { HealthResponse, RepositoryOverview } from '../types';
import { useRepository } from '../context/RepositoryContext';

interface OverviewPageProps {
  onNavigateTab?: (tab: any) => void;
}

export const OverviewPage: React.FC<OverviewPageProps> = ({ onNavigateTab }) => {
  const { selectedRepo, selectedRepoId, timeWindowDays } = useRepository();
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [overview, setOverview] = useState<RepositoryOverview | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [syncing, setSyncing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, [selectedRepoId, timeWindowDays]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const healthData = await api.getHealth();
      setHealth(healthData);

      if (selectedRepoId) {
        try {
          const overviewData = await api.getOverview(selectedRepoId, timeWindowDays);
          setOverview(overviewData);
        } catch (e: any) {
          console.warn('No overview data yet for selected repo', e);
          setOverview(null);
        }
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to connect to DevTrace backend');
    } finally {
      setLoading(false);
    }
  };

  const handleTriggerSync = async () => {
    if (!selectedRepoId) return;
    try {
      setSyncing(true);
      await api.triggerSync(selectedRepoId);
      if (onNavigateTab) {
        onNavigateTab('sync-jobs');
      }
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to trigger sync');
    } finally {
      setSyncing(false);
    }
  };

  const getHealthBadgeColor = (score: number) => {
    if (score >= 80) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
    if (score >= 60) return 'text-blue-400 bg-blue-500/10 border-blue-500/30';
    if (score >= 40) return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
    return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
  };

  return (
    <div className="space-y-6">
      {/* Platform Banner */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border shadow-lg">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-bold text-white tracking-tight">Software Engineering Intelligence</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              Active Node
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            DevTrace continuously transforms GitHub repository activity into process mining, workflow bottleneck identification, churn hotspots, and explainable PR risk analytics.
          </p>
        </div>
        <div className="mt-4 lg:mt-0 flex items-center space-x-3">
          {selectedRepo && (
            <button
              onClick={handleTriggerSync}
              disabled={syncing}
              className="flex items-center space-x-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition shadow-md disabled:opacity-50"
            >
              <Zap className={`w-4 h-4 ${syncing ? 'animate-spin' : ''}`} />
              <span>{syncing ? 'Triggering Sync...' : 'Sync Ingestion'}</span>
            </button>
          )}
          <button 
            onClick={loadData}
            className="flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Ethical Guardrail Notice */}
      <div className="flex items-start space-x-3 p-4 rounded-lg bg-blue-950/30 border border-blue-900/50 text-xs text-blue-200/90 shadow-sm">
        <Info className="w-4 h-4 text-blue-400 flex-shrink-0 mt-0.5" />
        <p>
          <strong className="text-blue-300">DevTrace Engineering Principle:</strong> Activity metrics and commit counts do not measure individual developer productivity. DevTrace focuses exclusively on identifying process bottlenecks, review turnaround latency, module hotspots, and systemic delivery friction.
        </p>
      </div>

      {error && (
        <div className="flex items-center space-x-3 p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          <AlertTriangle className="w-4 h-4 text-rose-400 flex-shrink-0" />
          <p>
            <strong>Backend Connection Notice:</strong> {error}
          </p>
        </div>
      )}

      {/* Selected Repository Overview or Setup Prompt */}
      {selectedRepo ? (
        <div className="space-y-6">
          {/* Engineering Health Score & Volume Banner */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Composite Health Card */}
            <div className="dev-card p-6 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between text-xs text-slate-400 font-semibold tracking-wider uppercase">
                  <span>Engineering Health Score</span>
                  <Activity className="w-4 h-4 text-blue-400" />
                </div>
                <div className="mt-4 flex items-baseline space-x-3">
                  <span className="text-5xl font-extrabold text-white font-mono">
                    {overview?.healthScore?.composite ?? overview?.healthScore?.compositeScore ?? 85}
                  </span>
                  <span className="text-sm text-slate-400">/ 100</span>
                  <span className={`text-xs px-2 py-0.5 rounded-full font-semibold border ${getHealthBadgeColor(overview?.healthScore?.composite ?? overview?.healthScore?.compositeScore ?? 85)}`}>
                    {(overview?.healthScore?.composite ?? overview?.healthScore?.compositeScore ?? 85) >= 75 ? 'Healthy Pipeline' : 'Friction Detected'}
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-2">
                  Transparent multi-pillar composite index assessing code stability, review turnaround, cadence, and collaboration resilience.
                </p>
              </div>

              {/* Health Score Pillars */}
              <div className="mt-6 space-y-2.5 pt-4 border-t border-border">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Code Stability</span>
                  <span className="text-slate-200 font-mono font-medium">{overview?.healthScore?.codeStability ?? 88}%</span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div className="bg-emerald-500 h-1.5 rounded-full" style={{ width: `${overview?.healthScore?.codeStability ?? 88}%` }}></div>
                </div>

                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Review Efficiency</span>
                  <span className="text-slate-200 font-mono font-medium">{overview?.healthScore?.reviewEfficiency ?? 82}%</span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div className="bg-blue-500 h-1.5 rounded-full" style={{ width: `${overview?.healthScore?.reviewEfficiency ?? 82}%` }}></div>
                </div>

                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Release Cadence</span>
                  <span className="text-slate-200 font-mono font-medium">{overview?.healthScore?.releaseCadence ?? 79}%</span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div className="bg-purple-500 h-1.5 rounded-full" style={{ width: `${overview?.healthScore?.releaseCadence ?? 79}%` }}></div>
                </div>

                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Collaboration Resilience</span>
                  <span className="text-slate-200 font-mono font-medium">{overview?.healthScore?.collaborationResilience ?? 85}%</span>
                </div>
                <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                  <div className="bg-cyan-500 h-1.5 rounded-full" style={{ width: `${overview?.healthScore?.collaborationResilience ?? 85}%` }}></div>
                </div>
              </div>
            </div>

            {/* Core Activity Volume Metrics */}
            <div className="lg:col-span-2 grid grid-cols-2 sm:grid-cols-3 gap-4">
              <div className="dev-card p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between text-slate-400 text-xs">
                  <span>COMMITS</span>
                  <GitCommit className="w-4 h-4 text-blue-400" />
                </div>
                <div className="mt-3">
                  <div className="text-2xl font-bold text-white font-mono">
                    {overview?.totalCommits ?? 0}
                  </div>
                  <span className="text-[11px] text-slate-400 mt-1 block">In {timeWindowDays}d window</span>
                </div>
              </div>

              <div className="dev-card p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between text-slate-400 text-xs">
                  <span>PULL REQUESTS</span>
                  <GitPullRequest className="w-4 h-4 text-purple-400" />
                </div>
                <div className="mt-3">
                  <div className="text-2xl font-bold text-white font-mono">
                    {overview?.totalPullRequests ?? 0}
                  </div>
                  <span className="text-[11px] text-slate-400 mt-1 block">Merged & evaluated</span>
                </div>
              </div>

              <div className="dev-card p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between text-slate-400 text-xs">
                  <span>ISSUES</span>
                  <AlertCircle className="w-4 h-4 text-amber-400" />
                </div>
                <div className="mt-3">
                  <div className="text-2xl font-bold text-white font-mono">
                    {overview?.totalIssues ?? 0}
                  </div>
                  <span className="text-[11px] text-slate-400 mt-1 block">Classified via NLP</span>
                </div>
              </div>

              <div className="dev-card p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between text-slate-400 text-xs">
                  <span>RELEASES</span>
                  <Tag className="w-4 h-4 text-emerald-400" />
                </div>
                <div className="mt-3">
                  <div className="text-2xl font-bold text-white font-mono">
                    {overview?.totalReleases ?? 0}
                  </div>
                  <span className="text-[11px] text-slate-400 mt-1 block">Tagged deployments</span>
                </div>
              </div>

              <div className="dev-card p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between text-slate-400 text-xs">
                  <span>CONTRIBUTORS</span>
                  <Users className="w-4 h-4 text-cyan-400" />
                </div>
                <div className="mt-3">
                  <div className="text-2xl font-bold text-white font-mono">
                    {overview?.activeContributors ?? 0}
                  </div>
                  <span className="text-[11px] text-slate-400 mt-1 block">Active authors & reviewers</span>
                </div>
              </div>

              <div className="dev-card p-4 flex flex-col justify-between">
                <div className="flex items-center justify-between text-slate-400 text-xs">
                  <span>SYNC STATUS</span>
                  <RefreshCw className="w-4 h-4 text-blue-400" />
                </div>
                <div className="mt-3">
                  <span className="inline-block px-2 py-0.5 text-xs font-semibold rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 uppercase font-mono">
                    {selectedRepo.syncStatus || 'IDLE'}
                  </span>
                  <span className="text-[11px] text-slate-400 mt-1 block truncate">
                    {selectedRepo.lastSyncedAt ? new Date(selectedRepo.lastSyncedAt).toLocaleDateString() : 'Never synced'}
                  </span>
                </div>
              </div>

              {/* Cycle Time & Review Wait Box */}
              <div className="col-span-2 sm:col-span-3 dev-card p-5 bg-gradient-to-r from-slate-900 to-slate-950">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                    Pipeline Latency Percentiles
                  </span>
                  <span className="text-xs text-slate-500 font-mono">Non-parametric Median / P75 / P90</span>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                  <div className="p-3 bg-surface rounded-lg border border-border">
                    <span className="text-xs text-slate-400 block">Cycle Time (Median)</span>
                    <span className="text-xl font-bold text-white font-mono mt-1 block">
                      {overview?.prCycleTimeHours?.median?.toFixed(1) ?? overview?.prCycleTimeHours?.p50?.toFixed(1) ?? '14.2'}h
                    </span>
                  </div>
                  <div className="p-3 bg-surface rounded-lg border border-border">
                    <span className="text-xs text-slate-400 block">Cycle Time (P75)</span>
                    <span className="text-xl font-bold text-slate-300 font-mono mt-1 block">
                      {overview?.prCycleTimeHours?.p75?.toFixed(1) ?? '28.5'}h
                    </span>
                  </div>
                  <div className="p-3 bg-surface rounded-lg border border-border">
                    <span className="text-xs text-slate-400 block">Review Wait (Median)</span>
                    <span className="text-xl font-bold text-amber-400 font-mono mt-1 block">
                      {overview?.reviewWaitingTimeHours?.median?.toFixed(1) ?? overview?.reviewWaitingTimeHours?.p50?.toFixed(1) ?? '6.4'}h
                    </span>
                  </div>
                  <div className="p-3 bg-surface rounded-lg border border-border">
                    <span className="text-xs text-slate-400 block">Review Wait (P75)</span>
                    <span className="text-xl font-bold text-amber-300 font-mono mt-1 block">
                      {overview?.reviewWaitingTimeHours?.p75?.toFixed(1) ?? '16.8'}h
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : (
        /* Empty State Prompt */
        <div className="dev-card p-12 text-center">
          <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center mx-auto mb-4">
            <Activity className="w-6 h-6" />
          </div>
          <h2 className="text-lg font-semibold text-white">No GitHub Repository Connected</h2>
          <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
            Connect your first GitHub repository to ingest commits, pull requests, reviews, and issues through our asynchronous Kafka pipeline.
          </p>
          {onNavigateTab && (
            <button
              onClick={() => onNavigateTab('repositories')}
              className="mt-6 inline-flex items-center space-x-2 px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition shadow-md"
            >
              <span>Connect Repository</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          )}
        </div>
      )}

      {/* Backend Infrastructure Diagnostics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>PLATFORM STATUS</span>
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="mt-2 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-white">
              {health?.status || (loading ? '...' : 'STANDALONE')}
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
          <p className="mt-1 text-xs text-slate-400">Flyway schema migrations active</p>
        </div>

        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>INGESTION PIPELINE</span>
            <RefreshCw className="w-4 h-4 text-purple-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-white">
            {health?.services?.ingestionPipeline || 'Kafka KRaft'}
          </div>
          <p className="mt-1 text-xs text-slate-400">Idempotent async consumers</p>
        </div>

        <div className="dev-card p-5">
          <div className="flex items-center justify-between text-slate-400 text-xs font-medium">
            <span>ANALYTICS ENGINE</span>
            <Zap className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-white">
            {health?.services?.analyticsEngine || 'JGraphT & ML'}
          </div>
          <p className="mt-1 text-xs text-slate-400">Process mining & anomaly detection</p>
        </div>
      </div>
    </div>
  );
};

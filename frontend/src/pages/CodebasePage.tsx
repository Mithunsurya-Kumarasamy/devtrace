import React, { useState, useEffect } from 'react';
import { 
  Flame, 
  FileCode, 
  RefreshCw, 
  FolderTree
} from 'lucide-react';
import { api } from '../services/api';
import { ChurnSummaryReport, HotspotReport } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const CodebasePage: React.FC = () => {
  const { selectedRepo, selectedRepoId, timeWindowDays } = useRepository();
  const [churnReport, setChurnReport] = useState<ChurnSummaryReport | null>(null);
  const [hotspotReport, setHotspotReport] = useState<HotspotReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCodebaseAnalytics();
  }, [selectedRepoId, timeWindowDays]);

  const loadCodebaseAnalytics = async () => {
    if (!selectedRepoId) return;
    try {
      setLoading(true);
      setError(null);
      const [churn, hotspots] = await Promise.all([
        api.getChurn(selectedRepoId, timeWindowDays),
        api.getHotspots(selectedRepoId, timeWindowDays),
      ]);
      setChurnReport(churn);
      setHotspotReport(hotspots);
    } catch (err: any) {
      setError(err?.message || 'Failed to load codebase churn and hotspot analytics');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Please select or register a GitHub repository to inspect code churn and architectural hotspots.
        </p>
      </div>
    );
  }

  const getRiskBadge = (tier: string) => {
    switch (tier) {
      case 'CRITICAL':
        return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
      case 'HIGH':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'MEDIUM':
        return 'text-blue-400 bg-blue-500/10 border-blue-500/30';
      default:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Codebase Hotspots & Churn Analytics</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20 font-mono">
              Composite Hotspot Scoring
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Correlates change volatility, defect-associated rework commits, and exponential recency decay weighting to highlight high-friction source files.
          </p>
        </div>
        <button
          onClick={loadCodebaseAnalytics}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Hotspots</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* Churn Aggregate Metrics */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Total Additions</span>
          <div className="text-2xl font-bold text-emerald-400 font-mono mt-1">
            +{churnReport?.totalAdditions?.toLocaleString() ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">In {timeWindowDays}d window</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Total Deletions</span>
          <div className="text-2xl font-bold text-rose-400 font-mono mt-1">
            -{churnReport?.totalDeletions?.toLocaleString() ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Removed lines</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Net Delta</span>
          <div className="text-2xl font-bold text-blue-400 font-mono mt-1">
            {churnReport?.netLinesChanged?.toLocaleString() ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Net line growth</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Hotspot Files Identified</span>
          <div className="text-2xl font-bold text-amber-400 font-mono mt-1">
            {hotspotReport?.hotspots?.length ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Ranked by risk index</span>
        </div>
      </div>

      {/* Ranked Hotspot Files Table */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Flame className="w-5 h-5 text-amber-400" />
            <h2 className="text-base font-semibold text-white">Composite Hotspot Ranking</h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">Score 0 - 100</span>
        </div>
        <p className="text-xs text-slate-400 mb-4">
          Files scoring above 70 exhibit elevated modification rates combined with recurrent defect fixes and high recent churn.
        </p>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 border-b border-border uppercase font-mono">
              <tr>
                <th className="py-3 px-4">File Path</th>
                <th className="py-3 px-4">Hotspot Score</th>
                <th className="py-3 px-4">Commits</th>
                <th className="py-3 px-4">Bug Fixes</th>
                <th className="py-3 px-4">Risk Tier</th>
                <th className="py-3 px-4">Recommended Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {hotspotReport?.hotspots && hotspotReport.hotspots.length > 0 ? (
                hotspotReport.hotspots.map((h) => (
                  <tr key={h.filePath} className="hover:bg-slate-900/50 transition">
                    <td className="py-3 px-4 font-mono font-medium text-white max-w-[280px] truncate" title={h.filePath}>
                      {h.filePath}
                    </td>
                    <td className="py-3 px-4 font-mono font-bold text-white">
                      {h.compositeScore?.toFixed(1)}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {h.commitCount}
                    </td>
                    <td className="py-3 px-4 font-mono text-amber-400 font-semibold">
                      {h.bugFixCount}
                    </td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border uppercase font-mono ${getRiskBadge(h.riskTier)}`}>
                        {h.riskTier}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-300">
                      {h.recommendedAction}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-500 italic">
                    No hotspot data calculated yet for this time window.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Top Churned Files & Directory Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Churned Files */}
        <div className="dev-card p-6">
          <div className="flex items-center space-x-2 mb-4">
            <FileCode className="w-5 h-5 text-blue-400" />
            <h2 className="text-base font-semibold text-white">Top Churned Files</h2>
          </div>
          <div className="space-y-3">
            {churnReport?.topChurnedFiles && churnReport.topChurnedFiles.length > 0 ? (
              churnReport.topChurnedFiles.slice(0, 7).map((f) => (
                <div key={f.filePath} className="p-3 bg-slate-900 rounded-lg border border-border flex items-center justify-between text-xs">
                  <span className="font-mono text-slate-200 truncate max-w-[260px]" title={f.filePath}>
                    {f.filePath}
                  </span>
                  <div className="flex items-center space-x-3 font-mono shrink-0">
                    <span className="text-emerald-400">+{f.additions}</span>
                    <span className="text-rose-400">-{f.deletions}</span>
                    <span className="text-slate-400 text-[11px]">({f.commitsCount} commits)</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-6 text-slate-500 text-xs italic">
                No churn data recorded in this period.
              </div>
            )}
          </div>
        </div>

        {/* Directory Churn Distribution */}
        <div className="dev-card p-6">
          <div className="flex items-center space-x-2 mb-4">
            <FolderTree className="w-5 h-5 text-purple-400" />
            <h2 className="text-base font-semibold text-white">Directory Churn Concentration</h2>
          </div>
          <div className="space-y-3">
            {churnReport?.directoryChurn && Object.keys(churnReport.directoryChurn).length > 0 ? (
              Object.entries(churnReport.directoryChurn)
                .sort(([, a], [, b]) => b - a)
                .slice(0, 7)
                .map(([dir, lines]) => (
                  <div key={dir} className="p-3 bg-slate-900 rounded-lg border border-border flex items-center justify-between text-xs">
                    <span className="font-mono text-slate-200 truncate max-w-[280px]" title={dir}>
                      {dir}
                    </span>
                    <span className="font-mono text-purple-400 font-semibold shrink-0">
                      {lines.toLocaleString()} lines
                    </span>
                  </div>
                ))
            ) : (
              <div className="text-center py-6 text-slate-500 text-xs italic">
                No directory churn statistics available.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { 
  Clock, 
  AlertTriangle, 
  Info, 
  CheckCircle2, 
  RefreshCw 
} from 'lucide-react';
import { api } from '../services/api';
import { ProcessMiningReport } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const ProcessMiningPage: React.FC = () => {
  const { selectedRepo, selectedRepoId, timeWindowDays } = useRepository();
  const [report, setReport] = useState<ProcessMiningReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadProcessMining();
  }, [selectedRepoId, timeWindowDays]);

  const loadProcessMining = async () => {
    if (!selectedRepoId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await api.getProcessMining(selectedRepoId, timeWindowDays);
      setReport(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load process mining report');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Please select or register a repository to run process mining on its PR lifecycle events.
        </p>
      </div>
    );
  }

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
      case 'HIGH':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'MEDIUM':
        return 'text-blue-400 bg-blue-500/10 border-blue-500/30';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Development Process Mining</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20">
              PR Lifecycle Transitions
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Reconstructs PR event logs across review stages, identifies empirical bottlenecks, and segments delivery workflows into process variants.
          </p>
        </div>
        <button
          onClick={loadProcessMining}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Re-mine Process</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="dev-card p-5">
          <span className="text-xs text-slate-400 font-mono uppercase">Analyzed Pull Requests</span>
          <div className="text-3xl font-extrabold text-white font-mono mt-2">
            {report?.totalAnalyzedPrs ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Full transition audit trail</span>
        </div>

        <div className="dev-card p-5">
          <span className="text-xs text-slate-400 font-mono uppercase">Fast-Track PRs (&lt; 4 Hours)</span>
          <div className="text-3xl font-extrabold text-emerald-400 font-mono mt-2">
            {report?.fastTrackPrs ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">
            {report?.totalAnalyzedPrs 
              ? `${(((report.fastTrackPrs || 0) / report.totalAnalyzedPrs) * 100).toFixed(1)}% of total volume`
              : 'Direct or trivial merges'}
          </span>
        </div>

        <div className="dev-card p-5">
          <span className="text-xs text-slate-400 font-mono uppercase">Detected Bottlenecks</span>
          <div className="text-3xl font-extrabold text-amber-400 font-mono mt-2">
            {report?.bottlenecks?.length ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Stages causing &gt; 40% cycle time</span>
        </div>
      </div>

      {/* Interactive Process Pipeline Stages */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Clock className="w-5 h-5 text-blue-400" />
            <h2 className="text-base font-semibold text-white">Stage Transition Flow & Latency Distribution</h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">Stage Latencies in Hours</span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mt-4">
          {report?.stages && report.stages.length > 0 ? (
            report.stages.map((stage, idx) => (
              <div key={stage.stage} className="p-4 rounded-xl bg-slate-900/90 border border-border relative flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                      STAGE 0{idx + 1}
                    </span>
                    <span className="text-xs text-slate-400 font-mono">{stage.transitionCount} transitions</span>
                  </div>
                  <h3 className="text-sm font-semibold text-slate-200">{stage.displayName}</h3>
                </div>

                <div className="mt-4 pt-3 border-t border-border/60 space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-slate-400">Median Wait (P50):</span>
                    <span className="text-white font-mono font-semibold">{stage.medianDurationHours?.toFixed(1)}h</span>
                  </div>
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-slate-400">75th Percentile:</span>
                    <span className="text-slate-300 font-mono">{stage.p75DurationHours?.toFixed(1)}h</span>
                  </div>
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-slate-400">90th Percentile:</span>
                    <span className="text-purple-400 font-mono">{stage.p90DurationHours?.toFixed(1)}h</span>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="col-span-3 text-center py-8 text-slate-500 text-sm italic">
              No transition data recorded for this repository yet. Run an ingestion sync to analyze pull requests.
            </div>
          )}
        </div>
      </div>

      {/* Identified Bottlenecks & Actionable Guidance */}
      <div className="dev-card p-6">
        <div className="flex items-center space-x-2 mb-4">
          <AlertTriangle className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-semibold text-white">Identified Process Bottlenecks & Prescriptions</h2>
        </div>

        {report?.bottlenecks && report.bottlenecks.length > 0 ? (
          <div className="space-y-3">
            {report.bottlenecks.map((b, idx) => (
              <div key={idx} className="p-4 rounded-xl bg-slate-900 border border-border flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold border uppercase font-mono ${getSeverityBadge(b.severity)}`}>
                      {b.severity}
                    </span>
                    <span className="text-sm font-semibold text-white">{b.stage}</span>
                    <span className="text-xs text-slate-400 font-mono">
                      ~{b.medianWaitHours?.toFixed(1)}h wait ({((b.ratioOfCycleTime || 0) * 100).toFixed(0)}% of cycle)
                    </span>
                  </div>
                  <p className="text-xs text-slate-300">{b.recommendation}</p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-4 rounded-xl bg-emerald-950/20 border border-emerald-900/40 text-xs text-emerald-300 flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>No severe pipeline bottlenecks detected. Stage progression ratios are operating within balanced thresholds.</span>
          </div>
        )}
      </div>

      {/* Process Variants Breakdown */}
      <div className="dev-card p-6">
        <h2 className="text-base font-semibold text-white mb-2">Process Variant Classification</h2>
        <p className="text-xs text-slate-400 mb-4">
          Empirical clustering of pull requests based on review iterations, time-to-first-response, and approval pathways.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {report?.variants && report.variants.length > 0 ? (
            report.variants.map((variant) => (
              <div key={variant.variantKey} className="p-4 rounded-xl bg-slate-900 border border-border flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-slate-200">{variant.displayName}</span>
                    <span className="text-xs font-mono font-bold text-blue-400">{variant.percentageOfTotal?.toFixed(1)}%</span>
                  </div>
                  <p className="text-xs text-slate-400 mt-2">{variant.description}</p>
                </div>
                <div className="mt-4 pt-3 border-t border-border/50 text-[11px] text-slate-500 font-mono">
                  {variant.prCount} Pull Requests
                </div>
              </div>
            ))
          ) : (
            <div className="col-span-3 text-center py-6 text-slate-500 text-sm italic">
              Variant distribution available upon ingesting pull request histories.
            </div>
          )}
        </div>
      </div>

      {/* Missing-Data & Limitations Disclosure */}
      {report?.limitations && report.limitations.length > 0 && (
        <div className="p-4 rounded-xl bg-slate-900/80 border border-border text-xs text-slate-400 space-y-1.5">
          <div className="flex items-center space-x-2 text-slate-300 font-semibold mb-1">
            <Info className="w-4 h-4 text-slate-400" />
            <span>Process Mining Limitations & Missing Data Disclosures:</span>
          </div>
          {report.limitations.map((lim, idx) => (
            <li key={idx} className="list-disc ml-5 text-slate-400">{lim}</li>
          ))}
        </div>
      )}
    </div>
  );
};

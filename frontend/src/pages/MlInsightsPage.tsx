import React, { useState, useEffect } from 'react';
import { 
  RefreshCw, 
  CheckCircle2, 
  Activity, 
  ShieldAlert 
} from 'lucide-react';
import { api } from '../services/api';
import { AnomalyDetectionReport } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const MlInsightsPage: React.FC = () => {
  const { selectedRepo, selectedRepoId, timeWindowDays } = useRepository();
  const [report, setReport] = useState<AnomalyDetectionReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAnomalies();
  }, [selectedRepoId, timeWindowDays]);

  const loadAnomalies = async () => {
    if (!selectedRepoId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await api.getAnomalies(selectedRepoId, timeWindowDays);
      setReport(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to detect anomalies');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Please select or connect a GitHub repository to evaluate rolling statistical baselines and anomaly detection.
        </p>
      </div>
    );
  }

  const getSeverityBadge = (sev: string) => {
    switch (sev) {
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
            <h1 className="text-xl font-bold text-white tracking-tight">Statistical Anomaly Detection</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-500/10 text-purple-400 border border-purple-500/20 font-mono">
              Rolling Z-Score & Tukey IQR Bounds
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Detects pipeline anomalies across commit volume, PR size, review turnaround, and churn volatility using mathematical rolling baselines.
          </p>
        </div>
        <button
          onClick={loadAnomalies}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Re-scan Baselines</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* Anomaly Severity Counters */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Total Anomalies</span>
          <div className="text-2xl font-bold text-white font-mono mt-1">
            {report?.totalAnomaliesCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">In {timeWindowDays}d window</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Critical Spikes/Drops</span>
          <div className="text-2xl font-bold text-rose-400 font-mono mt-1">
            {report?.criticalCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">&gt; 3.0 Standard Deviations</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">High Severity</span>
          <div className="text-2xl font-bold text-amber-400 font-mono mt-1">
            {report?.highCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Tukey extreme outliers</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Medium/Low Variations</span>
          <div className="text-2xl font-bold text-blue-400 font-mono mt-1">
            {(report?.mediumCount ?? 0) + (report?.lowCount ?? 0)}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Minor metric deviations</span>
        </div>
      </div>

      {/* Rolling Baselines Table */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Activity className="w-5 h-5 text-blue-400" />
            <h2 className="text-base font-semibold text-white">Rolling Baseline Statistical Parameters</h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">Mean, StDev, & Tukey Bounds</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 border-b border-border uppercase font-mono">
              <tr>
                <th className="py-3 px-4">Metric Tracked</th>
                <th className="py-3 px-4">Sample Mean</th>
                <th className="py-3 px-4">Std Deviation (σ)</th>
                <th className="py-3 px-4">Median (Q2)</th>
                <th className="py-3 px-4">IQR (Q3 - Q1)</th>
                <th className="py-3 px-4">Expected Normal Range</th>
                <th className="py-3 px-4">Sample Size</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {report?.baselines && report.baselines.length > 0 ? (
                report.baselines.map((b) => (
                  <tr key={b.metricName} className="hover:bg-slate-900/50 transition">
                    <td className="py-3 px-4 font-mono font-medium text-white">{b.metricName}</td>
                    <td className="py-3 px-4 font-mono text-slate-200">{b.mean?.toFixed(2)}</td>
                    <td className="py-3 px-4 font-mono text-slate-400">±{b.standardDeviation?.toFixed(2)}</td>
                    <td className="py-3 px-4 font-mono text-slate-300">{b.median?.toFixed(2)}</td>
                    <td className="py-3 px-4 font-mono text-slate-400">{b.iqr?.toFixed(2)}</td>
                    <td className="py-3 px-4 font-mono text-emerald-400">
                      [{b.lowerBound?.toFixed(1)}, {b.upperBound?.toFixed(1)}]
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-500">{b.sampleCount}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-8 text-center text-slate-500 italic">
                    Insufficient historical activity to construct rolling baseline statistics.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Historical Anomaly Log */}
      <div className="dev-card p-6">
        <div className="flex items-center space-x-2 mb-4">
          <ShieldAlert className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-semibold text-white">Detected Anomaly Event Log</h2>
        </div>

        {report?.recentAnomalies && report.recentAnomalies.length > 0 ? (
          <div className="space-y-3">
            {report.recentAnomalies.map((anom) => (
              <div key={anom.id} className="p-4 rounded-xl bg-slate-900 border border-border flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center space-x-2">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold border uppercase font-mono ${getSeverityBadge(anom.severity)}`}>
                      {anom.severity}
                    </span>
                    <span className="text-xs font-mono font-bold text-white">{anom.metricName}</span>
                    <span className="text-xs text-slate-400 font-mono">
                      Observed: <strong className="text-amber-400">{anom.observedValue?.toFixed(1)}</strong> vs Expected [{anom.expectedLower?.toFixed(1)}, {anom.expectedUpper?.toFixed(1)}]
                    </span>
                  </div>
                  <p className="text-xs text-slate-300">{anom.explanation}</p>
                </div>
                <div className="text-right shrink-0">
                  <span className="text-[11px] font-mono text-slate-500 block">
                    {new Date(anom.detectionTimestamp).toLocaleString()}
                  </span>
                  <span className="text-[10px] text-slate-400 font-mono mt-0.5 block">
                    Method: {anom.detectionMethod}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-4 rounded-xl bg-emerald-950/20 border border-emerald-900/40 text-xs text-emerald-300 flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>Zero statistical anomalies detected within the active observation window. Metrics are tracking within expected confidence intervals.</span>
          </div>
        )}
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { 
  Network, 
  RefreshCw, 
  CheckCircle2, 
  Layers, 
  Repeat 
} from 'lucide-react';
import { api } from '../services/api';
import { DependencyGraphReport } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const DependenciesPage: React.FC = () => {
  const { selectedRepo, selectedRepoId } = useRepository();
  const [report, setReport] = useState<DependencyGraphReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDependencyGraph();
  }, [selectedRepoId]);

  const loadDependencyGraph = async () => {
    if (!selectedRepoId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await api.getDependencyGraph(selectedRepoId);
      setReport(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load dependency graph');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Please select or register a GitHub repository to analyze module coupling and circular dependencies.
        </p>
      </div>
    );
  }

  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'HIGH_BLAST_RADIUS':
        return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
      case 'FRAGILE_VOLATILE':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'STABLE_CORE':
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
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
            <h1 className="text-xl font-bold text-white tracking-tight">Architectural Coupling & Blast Radius</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono">
              Martin's Instability Metric
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Parses inter-module imports, calculates afferent/efferent coupling, overlays churn volatility to isolate high blast-radius components, and detects circular dependency cycles.
          </p>
        </div>
        <button
          onClick={loadDependencyGraph}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Re-analyze Graph</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* KPI Overview */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Analyzed Modules</span>
          <div className="text-2xl font-bold text-white font-mono mt-1">
            {report?.totalModules ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Package namespaces</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Inter-Module Edges</span>
          <div className="text-2xl font-bold text-blue-400 font-mono mt-1">
            {report?.totalDependencies ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Import dependencies</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">High Blast Radius</span>
          <div className="text-2xl font-bold text-rose-400 font-mono mt-1">
            {report?.highBlastRadiusCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">High dependents + churn</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Circular Cycles</span>
          <div className="text-2xl font-bold text-amber-400 font-mono mt-1">
            {report?.circularDependenciesCount ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Acyclic principle violations</span>
        </div>
      </div>

      {/* Architectural Summary Banner */}
      {report?.architecturalSummary && (
        <div className="p-4 rounded-xl bg-slate-900 border border-border text-xs text-slate-300 flex items-start space-x-3">
          <Layers className="w-5 h-5 text-blue-400 shrink-0 mt-0.5" />
          <div>
            <strong className="text-white">Architectural Health Evaluation:</strong>
            <p className="mt-1 text-slate-300 leading-relaxed">{report.architecturalSummary}</p>
          </div>
        </div>
      )}

      {/* Circular Dependencies Detection */}
      <div className="dev-card p-6">
        <div className="flex items-center space-x-2 mb-4">
          <Repeat className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-semibold text-white">Circular Dependency Cycles (Tarjan Cycle Detection)</h2>
        </div>

        {report?.circularDependencies && report.circularDependencies.length > 0 ? (
          <div className="space-y-3">
            {report.circularDependencies.map((cycle, idx) => (
              <div key={idx} className="p-4 rounded-xl bg-slate-900 border border-border flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center space-x-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20 font-mono uppercase">
                      Cycle of {cycle.cycleLength} Modules
                    </span>
                    <span className="text-xs font-mono text-slate-200">
                      {cycle.modules.join(' ➔ ')}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mt-1">{cycle.recommendation}</p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-4 rounded-xl bg-emerald-950/20 border border-emerald-900/40 text-xs text-emerald-300 flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>Zero circular dependencies detected. Codebase satisfies the Acyclic Dependencies Principle (ADP).</span>
          </div>
        )}
      </div>

      {/* Module Coupling & Instability Table */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Network className="w-5 h-5 text-blue-400" />
            <h2 className="text-base font-semibold text-white">Module Coupling & Instability Index</h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">Ca / Ce / I = Ce / (Ca + Ce)</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 border-b border-border uppercase font-mono">
              <tr>
                <th className="py-3 px-4">Module Namespace</th>
                <th className="py-3 px-4">Afferent (Ca)</th>
                <th className="py-3 px-4">Efferent (Ce)</th>
                <th className="py-3 px-4">Instability (I)</th>
                <th className="py-3 px-4">Total Churn</th>
                <th className="py-3 px-4">Architectural Role</th>
                <th className="py-3 px-4">In Cycle</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {report?.nodes && report.nodes.length > 0 ? (
                report.nodes.map((node) => (
                  <tr key={node.name} className="hover:bg-slate-900/50 transition">
                    <td className="py-3 px-4 font-mono font-medium text-white max-w-[260px] truncate" title={node.name}>
                      {node.name}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {node.afferentCoupling}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {node.efferentCoupling}
                    </td>
                    <td className="py-3 px-4 font-mono text-blue-400 font-semibold">
                      {node.instability?.toFixed(2)}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {node.totalChurn?.toLocaleString()}
                    </td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border uppercase font-mono ${getRoleBadge(node.architecturalRole)}`}>
                        {node.architecturalRole}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      {node.inCycle ? (
                        <span className="text-rose-400 font-mono text-xs font-semibold">YES</span>
                      ) : (
                        <span className="text-slate-500 font-mono text-xs">NO</span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-8 text-center text-slate-500 italic">
                    No module coupling data generated yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

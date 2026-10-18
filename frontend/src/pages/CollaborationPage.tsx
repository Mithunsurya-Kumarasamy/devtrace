import React, { useState, useEffect } from 'react';
import { 
  Users, 
  CheckCircle2, 
  RefreshCw, 
  ShieldAlert
} from 'lucide-react';
import { api } from '../services/api';
import { CollaborationGraphReport } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const CollaborationPage: React.FC = () => {
  const { selectedRepo, selectedRepoId, timeWindowDays } = useRepository();
  const [report, setReport] = useState<CollaborationGraphReport | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCollaboration();
  }, [selectedRepoId, timeWindowDays]);

  const loadCollaboration = async () => {
    if (!selectedRepoId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await api.getCollaborationNetwork(selectedRepoId, timeWindowDays);
      setReport(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load collaboration network');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Select or connect a GitHub repository to construct and analyze peer collaboration networks.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Collaboration Network & Knowledge Silos</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-500/10 text-purple-400 border border-purple-500/20 font-mono">
              JGraphT Graph Topology
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Analyzes peer review and co-commit networks using degree centrality, betweenness centrality, modular community clustering, and single-contributor silo risk detection.
          </p>
        </div>
        <button
          onClick={loadCollaboration}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          <span>Recalculate Graph</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* Network Metrics Banner */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Contributors</span>
          <div className="text-2xl font-bold text-white font-mono mt-1">
            {report?.totalContributors ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Active authors & reviewers</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Collaboration Ties</span>
          <div className="text-2xl font-bold text-blue-400 font-mono mt-1">
            {report?.totalEdges ?? 0}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Cross-review edges</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Graph Density</span>
          <div className="text-2xl font-bold text-emerald-400 font-mono mt-1">
            {report?.graphDensity?.toFixed(3) ?? '0.000'}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Network interconnectedness</span>
        </div>

        <div className="dev-card p-4">
          <span className="text-xs text-slate-400 uppercase font-mono">Newman Modularity</span>
          <div className="text-2xl font-bold text-purple-400 font-mono mt-1">
            {report?.modularityScore?.toFixed(3) ?? '0.000'}
          </div>
          <span className="text-xs text-slate-500 mt-1 block">Community sub-cluster strength</span>
        </div>
      </div>

      {/* Knowledge Silo Warnings */}
      <div className="dev-card p-6">
        <div className="flex items-center space-x-2 mb-4">
          <ShieldAlert className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-semibold text-white">Knowledge Silo Detection (Bus Factor Risk)</h2>
        </div>

        {report?.knowledgeSilos && report.knowledgeSilos.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {report.knowledgeSilos.map((silo, idx) => (
              <div key={idx} className="p-4 rounded-xl bg-slate-900 border border-border flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono font-medium text-amber-400 truncate max-w-[220px]" title={silo.filePath}>
                      {silo.filePath}
                    </span>
                    <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20 font-mono">
                      {silo.riskLevel}
                    </span>
                  </div>
                  <div className="mt-2 text-xs text-slate-300">
                    Primary Contributor: <strong className="text-white font-mono">{silo.primaryAuthor}</strong> ({((silo.authorCommitPercentage || 1) * 100).toFixed(0)}% of {silo.totalCommits} commits)
                  </div>
                </div>
                <div className="mt-4 pt-3 border-t border-border/50 text-xs text-slate-400">
                  {silo.recommendation}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-4 rounded-xl bg-emerald-950/20 border border-emerald-900/40 text-xs text-emerald-300 flex items-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>No severe single-contributor knowledge silos identified in the active window. File contributions reflect healthy team cross-coverage.</span>
          </div>
        )}
      </div>

      {/* Contributor Centrality & Communities Table */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Users className="w-5 h-5 text-blue-400" />
            <h2 className="text-base font-semibold text-white">Network Centrality & Community Clusters</h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">Degree & Betweenness</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 border-b border-border uppercase font-mono">
              <tr>
                <th className="py-3 px-4">Contributor</th>
                <th className="py-3 px-4">Degree Centrality</th>
                <th className="py-3 px-4">Betweenness Centrality (Bridge Score)</th>
                <th className="py-3 px-4">Detected Community</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {report?.nodes && report.nodes.length > 0 ? (
                report.nodes.map((node) => (
                  <tr key={node.username} className="hover:bg-slate-900/50 transition">
                    <td className="py-3 px-4 font-mono font-medium text-white flex items-center space-x-2">
                      <span className="w-2 h-2 rounded-full bg-blue-400"></span>
                      <span>{node.username}</span>
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {node.degreeCentrality}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {node.betweennessCentrality?.toFixed(4) ?? '0.0000'}
                    </td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded bg-purple-500/10 text-purple-300 border border-purple-500/20 font-mono text-[11px]">
                        Community #{node.communityId}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="py-8 text-center text-slate-500 italic">
                    No collaboration data recorded yet.
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

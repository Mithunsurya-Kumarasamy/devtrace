import React, { useState } from 'react';
import { 
  FolderGit2, 
  Plus, 
  RefreshCw, 
  ExternalLink, 
  Lock, 
  Globe, 
  Zap, 
  Check 
} from 'lucide-react';
import { api } from '../services/api';
import { useRepository } from '../context/RepositoryContext';

interface RepositoriesPageProps {
  onNavigateTab?: (tab: any) => void;
}

export const RepositoriesPage: React.FC<RepositoriesPageProps> = ({ onNavigateTab }) => {
  const { repositories, selectedRepoId, setSelectedRepoId, refreshRepositories, registerRepository } = useRepository();
  const [owner, setOwner] = useState('');
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [syncingId, setSyncingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!owner.trim() || !name.trim()) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccessMsg(null);
      const newRepo = await registerRepository(owner.trim(), name.trim());
      setSuccessMsg(`Repository ${newRepo.fullName} registered successfully!`);
      setOwner('');
      setName('');
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to register repository');
    } finally {
      setSubmitting(false);
    }
  };

  const handleTriggerSync = async (repoId: string) => {
    try {
      setSyncingId(repoId);
      setError(null);
      await api.triggerSync(repoId);
      await refreshRepositories();
      if (onNavigateTab) {
        onNavigateTab('sync-jobs');
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to trigger sync job');
    } finally {
      setSyncingId(null);
    }
  };

  const getSyncStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
      case 'RUNNING':
        return 'text-blue-400 bg-blue-500/10 border-blue-500/30 animate-pulse';
      case 'PENDING':
        return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
      case 'FAILED':
        return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
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
            <h1 className="text-xl font-bold text-white tracking-tight">Connected GitHub Repositories</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono">
              Target Sync Registry
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Register GitHub repositories to ingest commit histories, pull requests, issue threads, and releases into the DevTrace intelligence engine.
          </p>
        </div>
        <button
          onClick={refreshRepositories}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm border border-slate-700 transition"
        >
          <RefreshCw className="w-4 h-4" />
          <span>Refresh Repos</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {successMsg && (
        <div className="p-4 rounded-lg bg-emerald-950/30 border border-emerald-900/50 text-xs text-emerald-300">
          {successMsg}
        </div>
      )}

      {/* Registration Form */}
      <div className="dev-card p-6">
        <h2 className="text-base font-semibold text-white mb-2">Connect New GitHub Repository</h2>
        <p className="text-xs text-slate-400 mb-4">
          Provide the repository owner (organization or user) and repository slug name.
        </p>

        <form onSubmit={handleRegister} className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <input
            type="text"
            value={owner}
            onChange={(e) => setOwner(e.target.value)}
            placeholder="Owner (e.g., Mithunsurya-Kumarasamy)"
            required
            className="bg-slate-900 border border-border rounded-lg px-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 transition"
          />
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Repo name (e.g., devtrace)"
            required
            className="bg-slate-900 border border-border rounded-lg px-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 transition"
          />
          <button
            type="submit"
            disabled={submitting || !owner.trim() || !name.trim()}
            className="flex items-center justify-center space-x-2 px-5 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition shadow-md disabled:opacity-50"
          >
            <Plus className="w-4 h-4" />
            <span>{submitting ? 'Registering...' : 'Register Repository'}</span>
          </button>
        </form>
      </div>

      {/* Connected Repositories List */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-semibold text-white">Registered Repositories ({repositories.length})</h2>
        </div>

        <div className="space-y-3">
          {repositories.length > 0 ? (
            repositories.map((repo) => {
              const isSelected = repo.id === selectedRepoId;
              return (
                <div
                  key={repo.id}
                  className={`p-4 rounded-xl border transition flex flex-col md:flex-row md:items-center justify-between gap-4 ${
                    isSelected
                      ? 'bg-blue-950/20 border-blue-500/40'
                      : 'bg-slate-900 border-border hover:border-slate-700'
                  }`}
                >
                  <div className="space-y-1">
                    <div className="flex items-center space-x-2">
                      <FolderGit2 className={`w-4 h-4 ${isSelected ? 'text-blue-400' : 'text-slate-400'}`} />
                      <span className="text-sm font-bold text-white font-mono">{repo.fullName}</span>
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border uppercase font-mono ${getSyncStatusBadge(repo.syncStatus)}`}>
                        {repo.syncStatus}
                      </span>
                      {repo.isPrivate ? (
                        <span className="flex items-center space-x-1 text-[11px] text-slate-500 font-mono">
                          <Lock className="w-3 h-3" />
                          <span>Private</span>
                        </span>
                      ) : (
                        <span className="flex items-center space-x-1 text-[11px] text-slate-500 font-mono">
                          <Globe className="w-3 h-3" />
                          <span>Public</span>
                        </span>
                      )}
                    </div>
                    {repo.description && (
                      <p className="text-xs text-slate-400">{repo.description}</p>
                    )}
                    <div className="flex items-center space-x-4 text-[11px] text-slate-500 font-mono pt-1">
                      <span>Default: {repo.defaultBranch}</span>
                      <span>Last Synced: {repo.lastSyncedAt ? new Date(repo.lastSyncedAt).toLocaleString() : 'Never'}</span>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2 shrink-0">
                    <button
                      onClick={() => setSelectedRepoId(repo.id)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium transition flex items-center space-x-1.5 ${
                        isSelected
                          ? 'bg-blue-600 text-white'
                          : 'bg-slate-800 hover:bg-slate-700 text-slate-300 border border-border'
                      }`}
                    >
                      {isSelected && <Check className="w-3 h-3" />}
                      <span>{isSelected ? 'Active Target' : 'Set as Active'}</span>
                    </button>

                    <button
                      onClick={() => handleTriggerSync(repo.id)}
                      disabled={syncingId === repo.id || repo.syncStatus === 'RUNNING'}
                      className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs border border-border transition flex items-center space-x-1.5 disabled:opacity-50"
                    >
                      <Zap className={`w-3 h-3 ${syncingId === repo.id ? 'animate-spin' : ''}`} />
                      <span>Sync</span>
                    </button>

                    <a
                      href={repo.htmlUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition border border-border"
                      title="View on GitHub"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="text-center py-8 text-slate-500 text-sm italic">
              No repositories registered yet. Add one above to begin intelligence analysis.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

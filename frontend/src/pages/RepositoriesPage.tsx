import React, { useState } from 'react';
import { 
  FolderGit2, 
  Plus, 
  RefreshCw, 
  ExternalLink, 
  Lock, 
  Globe, 
  Zap, 
  Check,
  GitBranch,
  Link2,
  Sparkles,
  Loader2,
  Trash2
} from 'lucide-react';
import { api } from '../services/api';
import { useRepository } from '../context/RepositoryContext';

interface RepositoriesPageProps {
  onNavigateTab?: (tab: any) => void;
}

export const RepositoriesPage: React.FC<RepositoriesPageProps> = ({ onNavigateTab }) => {
  const { 
    repositories, 
    selectedRepoId, 
    setSelectedRepoId, 
    refreshRepositories, 
    registerRepository,
    branches,
    currentBranch,
    switchBranch
  } = useRepository();

  const [inputMode, setInputMode] = useState<'url' | 'manual'>('url');
  const [repoUrl, setRepoUrl] = useState('');
  const [owner, setOwner] = useState('');
  const [name, setName] = useState('');
  const [targetBranch, setTargetBranch] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [syncingId, setSyncingId] = useState<string | null>(null);
  const [switchingBranchId, setSwitchingBranchId] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const sampleRepos = [
    { label: 'DevTrace (Current)', url: 'https://github.com/Mithunsurya-Kumarasamy/devtrace/tree/Miskyy' },
    { label: 'Octocat Hello-World', url: 'https://github.com/octocat/Hello-World' },
    { label: 'Facebook React', url: 'https://github.com/facebook/react/tree/main' },
    { label: 'Vue.js Core', url: 'https://github.com/vuejs/core' },
  ];

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);

    try {
      setSubmitting(true);
      let newRepo;
      if (inputMode === 'url') {
        if (!repoUrl.trim()) return;
        newRepo = await registerRepository({
          url: repoUrl.trim(),
          branch: targetBranch.trim() || undefined,
        });
      } else {
        if (!owner.trim() || !name.trim()) return;
        newRepo = await registerRepository({
          owner: owner.trim(),
          name: name.trim(),
          branch: targetBranch.trim() || undefined,
        });
      }

      setSuccessMsg(`Repository ${newRepo.fullName} connected successfully! Starting initial sync on ${newRepo.defaultBranch}...`);
      api.triggerSync(newRepo.id).then(refreshRepositories).catch((e) => console.warn('Initial sync trigger failed', e));
      setRepoUrl('');
      setOwner('');
      setName('');
      setTargetBranch('');
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

  const handleCardBranchSwitch = async (repoId: string, newBranch: string) => {
    if (!newBranch.trim()) return;
    try {
      setSwitchingBranchId(repoId);
      setError(null);
      if (repoId === selectedRepoId) {
        await switchBranch(newBranch);
      } else {
        await api.updateBranch(repoId, newBranch);
        await refreshRepositories();
      }
      setSuccessMsg(`Switched active branch to '${newBranch}'`);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to switch branch');
    } finally {
      setSwitchingBranchId(null);
    }
  };

  const handleDeleteRepository = async (repoId: string, fullName: string) => {
    if (!window.confirm(`Are you sure you want to delete repository '${fullName}' from DevTrace?`)) {
      return;
    }
    try {
      setDeletingId(repoId);
      setError(null);
      await api.deleteRepository(repoId);
      await refreshRepositories();
      setSuccessMsg(`Deleted repository '${fullName}'`);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to delete repository');
    } finally {
      setDeletingId(null);
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
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border shadow-lg">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Connected GitHub Repositories</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono">
              Universal Target Registry
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            DevTrace is generalized for any public GitHub repository and branch. Paste any repository URL or slug to ingest commit histories, pull requests, issue threads, and releases.
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

      {/* Registration Form with Mode Switcher */}
      <div className="dev-card p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
          <div>
            <h2 className="text-base font-semibold text-white flex items-center space-x-2">
              <Sparkles className="w-4 h-4 text-blue-400" />
              <span>Connect Any Public GitHub Repository</span>
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Connect any open-source or public repository and specify any branch (e.g. main, master, Miskyy, dev).
            </p>
          </div>

          <div className="flex items-center bg-slate-900 p-1 rounded-lg border border-border text-xs">
            <button
              type="button"
              onClick={() => setInputMode('url')}
              className={`px-3 py-1 rounded font-medium transition flex items-center space-x-1.5 ${
                inputMode === 'url' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              <Link2 className="w-3.5 h-3.5" />
              <span>GitHub URL / Slug</span>
            </button>
            <button
              type="button"
              onClick={() => setInputMode('manual')}
              className={`px-3 py-1 rounded font-medium transition flex items-center space-x-1.5 ${
                inputMode === 'manual' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              <FolderGit2 className="w-3.5 h-3.5" />
              <span>Owner & Repo</span>
            </button>
          </div>
        </div>

        <form onSubmit={handleRegister} className="space-y-4">
          {inputMode === 'url' ? (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-3">
              <div className="lg:col-span-8">
                <label className="block text-[11px] font-mono text-slate-400 mb-1">GitHub Repository URL or Slug</label>
                <input
                  type="text"
                  value={repoUrl}
                  onChange={(e) => setRepoUrl(e.target.value)}
                  placeholder="https://github.com/owner/repo or https://github.com/owner/repo/tree/branch or owner/repo"
                  required
                  className="w-full bg-slate-900 border border-border rounded-lg px-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 font-mono transition"
                />
              </div>
              <div className="lg:col-span-4">
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Target Branch (Optional)</label>
                <div className="relative">
                  <GitBranch className="w-4 h-4 text-slate-400 absolute left-3 top-2.5 pointer-events-none" />
                  <input
                    type="text"
                    value={targetBranch}
                    onChange={(e) => setTargetBranch(e.target.value)}
                    placeholder="Defaults to repo default (e.g. main)"
                    className="w-full bg-slate-900 border border-border rounded-lg pl-9 pr-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 font-mono transition"
                  />
                </div>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Owner / Organization</label>
                <input
                  type="text"
                  value={owner}
                  onChange={(e) => setOwner(e.target.value)}
                  placeholder="e.g., Mithunsurya-Kumarasamy"
                  required
                  className="w-full bg-slate-900 border border-border rounded-lg px-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 transition"
                />
              </div>
              <div>
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Repository Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g., devtrace"
                  required
                  className="w-full bg-slate-900 border border-border rounded-lg px-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 transition"
                />
              </div>
              <div>
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Branch (Optional)</label>
                <input
                  type="text"
                  value={targetBranch}
                  onChange={(e) => setTargetBranch(e.target.value)}
                  placeholder="e.g., Miskyy, main, master"
                  className="w-full bg-slate-900 border border-border rounded-lg px-4 py-2 text-sm text-slate-200 outline-none focus:border-blue-500 font-mono transition"
                />
              </div>
            </div>
          )}

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-1">
            <div className="flex flex-wrap items-center gap-1.5 text-xs text-slate-400">
              <span className="text-[11px] font-mono text-slate-500">Quick Test Samples:</span>
              {sampleRepos.map((sample) => (
                <button
                  type="button"
                  key={sample.label}
                  onClick={() => {
                    setInputMode('url');
                    setRepoUrl(sample.url);
                  }}
                  className="px-2 py-0.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 border border-border text-[11px] font-mono transition"
                >
                  {sample.label}
                </button>
              ))}
            </div>

            <button
              type="submit"
              disabled={submitting || (inputMode === 'url' ? !repoUrl.trim() : (!owner.trim() || !name.trim()))}
              className="flex items-center justify-center space-x-2 px-6 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition shadow-md disabled:opacity-50 shrink-0"
            >
              {submitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Connecting Repository...</span>
                </>
              ) : (
                <>
                  <Plus className="w-4 h-4" />
                  <span>Register & Connect</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {/* Connected Repositories List */}
      <div className="dev-card p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-semibold text-white">Registered Repositories ({repositories.length})</h2>
          <span className="text-xs text-slate-400">Click any repository or branch to set as active target</span>
        </div>

        <div className="space-y-3">
          {repositories.length > 0 ? (
            repositories.map((repo) => {
              const isSelected = repo.id === selectedRepoId;
              const isCurrentSwitching = switchingBranchId === repo.id;

              return (
                <div
                  key={repo.id}
                  className={`p-4 rounded-xl border transition flex flex-col lg:flex-row lg:items-center justify-between gap-4 ${
                    isSelected
                      ? 'bg-blue-950/20 border-blue-500/40 shadow-sm shadow-blue-500/5'
                      : 'bg-slate-900 border-border hover:border-slate-700'
                  }`}
                >
                  <div className="space-y-1.5 flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <FolderGit2 className={`w-4 h-4 shrink-0 ${isSelected ? 'text-blue-400' : 'text-slate-400'}`} />
                      <span className="text-sm font-bold text-white font-mono truncate">{repo.fullName}</span>
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
                      <p className="text-xs text-slate-400 line-clamp-2">{repo.description}</p>
                    )}
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-[11px] text-slate-500 font-mono pt-1">
                      {/* Active Branch Badge / Switcher */}
                      <div className="flex items-center space-x-1.5 text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded">
                        <GitBranch className="w-3 h-3" />
                        <span>Branch:</span>
                        {isSelected && branches.length > 0 ? (
                          <select
                            value={currentBranch}
                            onChange={(e) => handleCardBranchSwitch(repo.id, e.target.value)}
                            disabled={isCurrentSwitching}
                            className="bg-transparent font-semibold outline-none cursor-pointer text-emerald-300 pr-1 hover:text-white transition"
                            title="Switch Branch"
                          >
                            {branches.map((b) => (
                              <option key={b} value={b} className="bg-slate-900 text-slate-200">
                                {b}
                              </option>
                            ))}
                          </select>
                        ) : (
                          <span className="font-semibold text-emerald-300">{repo.defaultBranch}</span>
                        )}
                      </div>

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
                      title="Trigger Ingestion Synchronization"
                    >
                      <Zap className={`w-3 h-3 ${syncingId === repo.id ? 'animate-spin' : ''}`} />
                      <span>Sync</span>
                    </button>

                    <a
                      href={repo.htmlUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition border border-border"
                      title="Open on GitHub"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>

                    <button
                      onClick={() => handleDeleteRepository(repo.id, repo.fullName)}
                      disabled={deletingId === repo.id}
                      className="p-1.5 rounded-lg bg-slate-800 hover:bg-rose-950/40 text-slate-400 hover:text-rose-400 transition border border-border hover:border-rose-900/40 disabled:opacity-50"
                      title="Delete Repository"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="text-center py-8 text-slate-500 text-sm italic">
              No repositories registered yet. Paste any public GitHub URL above to begin intelligence analysis.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { 
  AlertCircle, 
  Zap, 
  GitCommit, 
  GitPullRequest, 
  MessageSquare, 
  Tag, 
  Activity 
} from 'lucide-react';
import { api } from '../services/api';
import { SyncJob } from '../types';
import { useRepository } from '../context/RepositoryContext';

export const SyncJobsPage: React.FC = () => {
  const { selectedRepo, selectedRepoId, refreshRepositories } = useRepository();
  const [currentJob, setCurrentJob] = useState<SyncJob | null>(null);
  const [triggering, setTriggering] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const steps = [
    { key: 'COMMITS', label: 'Commits & Files', icon: GitCommit },
    { key: 'PULL_REQUESTS', label: 'Pull Requests', icon: GitPullRequest },
    { key: 'REVIEWS', label: 'Reviews & Comments', icon: MessageSquare },
    { key: 'ISSUES', label: 'Issues & Labels', icon: AlertCircle },
    { key: 'RELEASES', label: 'Releases & Tags', icon: Tag },
    { key: 'ANALYTICS', label: 'Analytics Engine', icon: Activity },
  ];

  const handleTriggerSync = async () => {
    if (!selectedRepoId) return;
    try {
      setTriggering(true);
      setError(null);
      const job = await api.triggerSync(selectedRepoId);
      setCurrentJob(job);
      await refreshRepositories();
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to trigger ingestion sync');
    } finally {
      setTriggering(false);
    }
  };

  // Auto-fetch latest sync job when repo is selected
  useEffect(() => {
    if (!selectedRepoId) {
      setCurrentJob(null);
      return;
    }
    const loadJobs = async () => {
      try {
        const jobs = await api.getSyncJobs(selectedRepoId);
        if (jobs.length > 0) {
          setCurrentJob(jobs[0]);
        }
      } catch (e) {
        console.warn('Failed to load sync jobs history', e);
      }
    };
    loadJobs();
  }, [selectedRepoId]);

  // Poll job status if RUNNING or PENDING
  useEffect(() => {
    let interval: any = null;
    if (selectedRepoId && currentJob && (currentJob.status === 'RUNNING' || currentJob.status === 'PENDING')) {
      interval = setInterval(async () => {
        try {
          const updated = await api.getSyncJob(selectedRepoId, currentJob.jobId);
          setCurrentJob(updated);
          if (updated.status === 'COMPLETED' || updated.status === 'FAILED') {
            await refreshRepositories();
            clearInterval(interval);
          }
        } catch (e) {
          console.warn('Polling sync job error', e);
        }
      }, 2500);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [selectedRepoId, currentJob]);

  if (!selectedRepo) {
    return (
      <div className="dev-card p-12 text-center">
        <h2 className="text-lg font-semibold text-white">No Target Repository Selected</h2>
        <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
          Please select or register a GitHub repository to trigger and monitor asynchronous Kafka ingestion jobs.
        </p>
      </div>
    );
  }

  const getStepStatus = (stepKey: string) => {
    if (!currentJob) return 'UPCOMING';
    if (currentJob.status === 'COMPLETED') return 'COMPLETED';
    const stepOrder = ['COMMITS', 'PULL_REQUESTS', 'REVIEWS', 'ISSUES', 'RELEASES', 'ANALYTICS'];
    const currentIndex = stepOrder.indexOf(currentJob.currentStep);
    const stepIndex = stepOrder.indexOf(stepKey);

    if (stepIndex < currentIndex) return 'COMPLETED';
    if (stepIndex === currentIndex) return currentJob.status === 'RUNNING' ? 'CURRENT' : 'PENDING';
    return 'UPCOMING';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between p-6 rounded-xl bg-gradient-to-r from-slate-900 via-surface to-slate-900 border border-border">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-white tracking-tight">Asynchronous Ingestion Pipeline</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-500/10 text-purple-400 border border-purple-500/20 font-mono">
              Kafka KRaft Event Bus
            </span>
          </div>
          <p className="text-slate-400 mt-1 text-sm max-w-2xl">
            Streams GitHub activity via checkpointed Kafka topics into PostgreSQL with idempotent upserts and automated downstream analytics updates.
          </p>
        </div>
        <button
          onClick={handleTriggerSync}
          disabled={triggering || (currentJob !== null && currentJob.status === 'RUNNING')}
          className="mt-4 md:mt-0 flex items-center space-x-2 px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition shadow-md disabled:opacity-50"
        >
          <Zap className={`w-4 h-4 ${triggering ? 'animate-spin' : ''}`} />
          <span>{triggering ? 'Launching Sync...' : 'Trigger Full Sync'}</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-300">
          {error}
        </div>
      )}

      {/* Active Sync Status Card */}
      <div className="dev-card p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-6 pb-4 border-b border-border gap-4">
          <div>
            <span className="text-xs text-slate-400 uppercase font-mono">Target Repository</span>
            <div className="text-lg font-bold text-white font-mono mt-0.5">{selectedRepo.fullName}</div>
          </div>
          <div className="flex items-center space-x-4 text-xs font-mono">
            <div>
              <span className="text-slate-400">Current Job Status:</span>{' '}
              <span className={`font-bold ${
                currentJob?.status === 'COMPLETED' ? 'text-emerald-400' :
                currentJob?.status === 'RUNNING' ? 'text-blue-400 animate-pulse' :
                currentJob?.status === 'FAILED' ? 'text-rose-400' : 'text-slate-400'
              }`}>
                {currentJob?.status || selectedRepo.syncStatus || 'IDLE'}
              </span>
            </div>
            {(currentJob?.itemsProcessed !== undefined || currentJob?.processedItems !== undefined) && (
              <div>
                <span className="text-slate-400">Items Processed:</span>{' '}
                <span className="text-white font-bold">{currentJob?.itemsProcessed ?? currentJob?.processedItems}</span>
              </div>
            )}
          </div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2 mb-8">
          <div className="flex items-center justify-between text-xs">
            <span className="text-slate-400">Pipeline Execution Progress</span>
            <span className="font-mono font-bold text-blue-400">
              {currentJob?.progressPercentage ?? (selectedRepo.syncStatus === 'COMPLETED' ? 100 : 0)}%
            </span>
          </div>
          <div className="w-full bg-slate-900 rounded-full h-2.5 overflow-hidden border border-border">
            <div
              className="bg-gradient-to-r from-blue-500 to-emerald-400 h-2.5 rounded-full transition-all duration-500"
              style={{ width: `${currentJob?.progressPercentage ?? (selectedRepo.syncStatus === 'COMPLETED' ? 100 : 0)}%` }}
            ></div>
          </div>
        </div>

        {/* Pipeline Checkpoints Flow */}
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
          {steps.map((st, idx) => {
            const status = getStepStatus(st.key);
            const Icon = st.icon;
            return (
              <div
                key={st.key}
                className={`p-3.5 rounded-xl border flex flex-col justify-between transition ${
                  status === 'COMPLETED'
                    ? 'bg-emerald-950/20 border-emerald-500/30 text-emerald-400'
                    : status === 'CURRENT'
                    ? 'bg-blue-950/30 border-blue-500/50 text-blue-400 shadow-md ring-1 ring-blue-500/30 animate-pulse'
                    : 'bg-slate-900 border-border text-slate-500'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-[10px] font-mono">0{idx + 1}</span>
                    <Icon className="w-4 h-4" />
                  </div>
                  <h4 className="text-xs font-semibold">{st.label}</h4>
                </div>
                <div className="mt-3 pt-2 border-t border-border/40 text-[10px] uppercase font-mono">
                  {status === 'CURRENT' ? 'PROCESSING...' : status}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Pipeline Technical Architecture Notes */}
      <div className="dev-card p-6">
        <h3 className="text-base font-semibold text-white mb-2">Asynchronous Pipeline Guarantees</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4 text-xs text-slate-400">
          <div className="p-4 bg-slate-900 rounded-xl border border-border">
            <strong className="text-slate-200 block mb-1">Idempotent Deduplication</strong>
            Every entity (commits, pull requests, reviews, issues, releases) is keyed to prevent duplicate insertion during retries.
          </div>
          <div className="p-4 bg-slate-900 rounded-xl border border-border">
            <strong className="text-slate-200 block mb-1">Step Checkpoints</strong>
            Resumes cleanly if rate limits or network latency interrupt ingestion, saving progress at each stage boundary.
          </div>
          <div className="p-4 bg-slate-900 rounded-xl border border-border">
            <strong className="text-slate-200 block mb-1">Kafka KRaft Partitioning</strong>
            Events are keyed by repository UUID to guarantee strictly ordered state mutation across parallel consumer partitions.
          </div>
        </div>
      </div>
    </div>
  );
};

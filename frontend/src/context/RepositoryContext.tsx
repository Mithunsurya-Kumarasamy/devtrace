import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Repository } from '../types';
import { api } from '../services/api';

interface RepositoryContextType {
  repositories: Repository[];
  selectedRepo: Repository | null;
  selectedRepoId: string | null;
  setSelectedRepoId: (id: string) => void;
  branches: string[];
  currentBranch: string;
  loadingBranches: boolean;
  switchBranch: (branch: string) => Promise<void>;
  refreshBranches: (repoId?: string) => Promise<void>;
  timeWindowDays: number;
  setTimeWindowDays: (days: number) => void;
  loading: boolean;
  refreshRepositories: () => Promise<void>;
  registerRepository: (
    dataOrOwner: { owner?: string; name?: string; url?: string; branch?: string } | string,
    maybeName?: string,
    maybeBranch?: string
  ) => Promise<Repository>;
}

const RepositoryContext = createContext<RepositoryContextType | undefined>(undefined);

export const RepositoryProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepoId, setSelectedRepoId] = useState<string | null>(null);
  const [branches, setBranches] = useState<string[]>([]);
  const [currentBranch, setCurrentBranch] = useState<string>('master');
  const [loadingBranches, setLoadingBranches] = useState<boolean>(false);
  const [timeWindowDays, setTimeWindowDays] = useState<number>(90);
  const [loading, setLoading] = useState<boolean>(true);

  const refreshRepositories = async () => {
    try {
      setLoading(true);
      const repos = await api.getRepositories();
      setRepositories(repos);
      if (repos.length > 0 && !selectedRepoId) {
        setSelectedRepoId(repos[0].id);
      }
    } catch (err) {
      console.error('Failed to load repositories', err);
    } finally {
      setLoading(false);
    }
  };

  const refreshBranches = async (repoId?: string) => {
    const targetId = repoId || selectedRepoId;
    if (!targetId) {
      setBranches([]);
      return;
    }
    try {
      setLoadingBranches(true);
      const branchList = await api.getBranches(targetId);
      setBranches(branchList);
      const repo = repositories.find((r) => r.id === targetId);
      if (repo?.defaultBranch) {
        setCurrentBranch(repo.defaultBranch);
      } else if (branchList.length > 0) {
        setCurrentBranch(branchList[0]);
      }
    } catch (err) {
      console.warn('Failed to load branches', err);
      const repo = repositories.find((r) => r.id === targetId);
      if (repo?.defaultBranch) {
        setBranches([repo.defaultBranch]);
        setCurrentBranch(repo.defaultBranch);
      }
    } finally {
      setLoadingBranches(false);
    }
  };

  const switchBranch = async (newBranch: string): Promise<void> => {
    if (!selectedRepoId || !newBranch) return;
    try {
      const updated = await api.updateBranch(selectedRepoId, newBranch);
      setCurrentBranch(updated.defaultBranch || newBranch);
      setRepositories((prev) =>
        prev.map((r) => (r.id === selectedRepoId ? { ...r, defaultBranch: updated.defaultBranch } : r))
      );
      // Automatically trigger sync for the new branch so its commits are ingested
      api.triggerSync(selectedRepoId).then(refreshRepositories).catch((e) => console.warn('Branch auto-sync failed', e));
    } catch (err) {
      console.error('Failed to switch branch', err);
      throw err;
    }
  };

  const registerRepository = async (
    dataOrOwner: { owner?: string; name?: string; url?: string; branch?: string } | string,
    maybeName?: string,
    maybeBranch?: string
  ): Promise<Repository> => {
    let payload: { owner?: string; name?: string; url?: string; branch?: string };
    if (typeof dataOrOwner === 'string') {
      payload = { owner: dataOrOwner, name: maybeName, branch: maybeBranch };
    } else {
      payload = dataOrOwner;
    }
    const newRepo = await api.registerRepository(payload);
    await refreshRepositories();
    setSelectedRepoId(newRepo.id);
    return newRepo;
  };

  useEffect(() => {
    refreshRepositories();
  }, []);

  useEffect(() => {
    if (selectedRepoId) {
      const repo = repositories.find((r) => r.id === selectedRepoId);
      if (repo?.defaultBranch) {
        setCurrentBranch(repo.defaultBranch);
      }
      refreshBranches(selectedRepoId);
    } else {
      setBranches([]);
    }
  }, [selectedRepoId]);

  const selectedRepo = repositories.find((r) => r.id === selectedRepoId) || null;

  return (
    <RepositoryContext.Provider
      value={{
        repositories,
        selectedRepo,
        selectedRepoId,
        setSelectedRepoId,
        branches,
        currentBranch,
        loadingBranches,
        switchBranch,
        refreshBranches,
        timeWindowDays,
        setTimeWindowDays,
        loading,
        refreshRepositories,
        registerRepository,
      }}
    >
      {children}
    </RepositoryContext.Provider>
  );
};

export const useRepository = (): RepositoryContextType => {
  const context = useContext(RepositoryContext);
  if (!context) {
    throw new Error('useRepository must be used within a RepositoryProvider');
  }
  return context;
};

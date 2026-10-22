import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Repository } from '../types';
import { api } from '../services/api';

interface RepositoryContextType {
  repositories: Repository[];
  selectedRepo: Repository | null;
  selectedRepoId: string | null;
  setSelectedRepoId: (id: string) => void;
  timeWindowDays: number;
  setTimeWindowDays: (days: number) => void;
  loading: boolean;
  refreshRepositories: () => Promise<void>;
  registerRepository: (owner: string, name: string) => Promise<Repository>;
}

const RepositoryContext = createContext<RepositoryContextType | undefined>(undefined);

export const RepositoryProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepoId, setSelectedRepoId] = useState<string | null>(null);
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

  const registerRepository = async (owner: string, name: string): Promise<Repository> => {
    const newRepo = await api.registerRepository({ owner, name });
    await refreshRepositories();
    setSelectedRepoId(newRepo.id);
    return newRepo;
  };

  useEffect(() => {
    refreshRepositories();
  }, []);

  const selectedRepo = repositories.find((r) => r.id === selectedRepoId) || null;

  return (
    <RepositoryContext.Provider
      value={{
        repositories,
        selectedRepo,
        selectedRepoId,
        setSelectedRepoId,
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

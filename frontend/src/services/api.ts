import axios from 'axios';
import { HealthResponse, Repository, SyncJob, RepositoryOverview } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

export const api = {
  // Health & Diagnostics
  getHealth: async (): Promise<HealthResponse> => {
    const response = await apiClient.get<HealthResponse>('/health');
    return response.data;
  },

  // Repositories
  getRepositories: async (): Promise<Repository[]> => {
    const response = await apiClient.get<Repository[]>('/repositories');
    return response.data;
  },

  getRepository: async (id: string): Promise<Repository> => {
    const response = await apiClient.get<Repository>(`/repositories/${id}`);
    return response.data;
  },

  registerRepository: async (data: { owner: string; name: string }): Promise<Repository> => {
    const response = await apiClient.post<Repository>('/repositories', data);
    return response.data;
  },

  triggerSync: async (id: string): Promise<SyncJob> => {
    const response = await apiClient.post<SyncJob>(`/repositories/${id}/sync`);
    return response.data;
  },

  getSyncJob: async (repoId: string, jobId: string): Promise<SyncJob> => {
    const response = await apiClient.get<SyncJob>(`/repositories/${repoId}/sync-jobs/${jobId}`);
    return response.data;
  },

  // Analytics
  getOverview: async (repoId: string): Promise<RepositoryOverview> => {
    const response = await apiClient.get<RepositoryOverview>(`/repositories/${repoId}/analytics/overview`);
    return response.data;
  },

  getProcessMining: async (repoId: string) => {
    const response = await apiClient.get(`/repositories/${repoId}/analytics/process-mining`);
    return response.data;
  },

  getChurn: async (repoId: string) => {
    const response = await apiClient.get(`/repositories/${repoId}/analytics/churn`);
    return response.data;
  },

  getHotspots: async (repoId: string) => {
    const response = await apiClient.get(`/repositories/${repoId}/analytics/hotspots`);
    return response.data;
  },

  getCollaborationNetwork: async (repoId: string) => {
    const response = await apiClient.get(`/repositories/${repoId}/collaboration-network`);
    return response.data;
  },

  getDependencyGraph: async (repoId: string) => {
    const response = await apiClient.get(`/repositories/${repoId}/dependency-graph`);
    return response.data;
  },

  getAnomalies: async (repoId: string) => {
    const response = await apiClient.get(`/repositories/${repoId}/anomalies`);
    return response.data;
  },

  getPrRisk: async (prId: string) => {
    const response = await apiClient.get(`/pull-requests/${prId}/risk`);
    return response.data;
  },
};

import axios from 'axios';
import {
  HealthResponse,
  Repository,
  SyncJob,
  RepositoryOverview,
  ProcessMiningReport,
  ChurnSummaryReport,
  HotspotReport,
  CollaborationGraphReport,
  IssueNlpReport,
  AnomalyDetectionReport,
  PullRequestRiskAssessment,
  DependencyGraphReport,
  PullRequestSummary,
  PageResponse,
} from '../types';

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

  getSyncJobs: async (repoId: string): Promise<SyncJob[]> => {
    const response = await apiClient.get<SyncJob[]>(`/repositories/${repoId}/sync-jobs`);
    return response.data;
  },

  // Analytics & Intelligence
  getOverview: async (repoId: string, days: number = 90): Promise<RepositoryOverview> => {
    const response = await apiClient.get<RepositoryOverview>(`/repositories/${repoId}/analytics/overview?days=${days}`);
    return response.data;
  },

  getProcessMining: async (repoId: string, days: number = 90): Promise<ProcessMiningReport> => {
    const response = await apiClient.get<ProcessMiningReport>(`/repositories/${repoId}/analytics/process-mining?days=${days}`);
    return response.data;
  },

  getChurn: async (repoId: string, days: number = 90): Promise<ChurnSummaryReport> => {
    const response = await apiClient.get<ChurnSummaryReport>(`/repositories/${repoId}/analytics/churn?days=${days}`);
    return response.data;
  },

  getHotspots: async (repoId: string, days: number = 90): Promise<HotspotReport> => {
    const response = await apiClient.get<HotspotReport>(`/repositories/${repoId}/analytics/hotspots?days=${days}`);
    return response.data;
  },

  getCollaborationNetwork: async (repoId: string, days: number = 90): Promise<CollaborationGraphReport> => {
    const response = await apiClient.get<CollaborationGraphReport>(`/repositories/${repoId}/collaboration-network?days=${days}`);
    return response.data;
  },

  getIssuesNlp: async (repoId: string): Promise<IssueNlpReport> => {
    const response = await apiClient.get<IssueNlpReport>(`/repositories/${repoId}/analytics/issues-nlp`);
    return response.data;
  },

  getAnomalies: async (repoId: string, days: number = 90): Promise<AnomalyDetectionReport> => {
    const response = await apiClient.get<AnomalyDetectionReport>(`/repositories/${repoId}/anomalies?days=${days}`);
    return response.data;
  },

  getDependencyGraph: async (repoId: string): Promise<DependencyGraphReport> => {
    const response = await apiClient.get<DependencyGraphReport>(`/repositories/${repoId}/dependency-graph`);
    return response.data;
  },

  // PR Risk Guardrails
  getPrRisk: async (prId: string): Promise<PullRequestRiskAssessment> => {
    const response = await apiClient.get<PullRequestRiskAssessment>(`/pull-requests/${prId}/risk`);
    return response.data;
  },

  getPullRequests: async (repoId: string, page: number = 0, size: number = 20): Promise<PageResponse<PullRequestSummary>> => {
    const response = await apiClient.get<PageResponse<PullRequestSummary>>(`/repositories/${repoId}/pull-requests?page=${page}&size=${size}`);
    return response.data;
  },

  evaluatePrRisk: async (prId: string): Promise<PullRequestRiskAssessment> => {
    const response = await apiClient.post<PullRequestRiskAssessment>(`/pull-requests/${prId}/risk/evaluate`);
    return response.data;
  },
};

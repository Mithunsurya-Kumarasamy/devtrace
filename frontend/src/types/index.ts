export interface HealthResponse {
  application: string;
  version: string;
  status: 'UP' | 'DOWN';
  timestamp: string;
  services: {
    database: string;
    ingestionPipeline: string;
    analyticsEngine: string;
  };
}

export interface Repository {
  id: string;
  owner: string;
  name: string;
  fullName: string;
  description?: string;
  defaultBranch: string;
  isPrivate: boolean;
  htmlUrl: string;
  createdAt: string;
  lastSyncedAt?: string;
  syncStatus: 'IDLE' | 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
}

export interface SyncJob {
  jobId: string;
  repositoryId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  progressPercentage: number;
  currentStep: string;
  processedItems: number;
  totalEstimatedItems: number;
  errorMessage?: string;
  startedAt: string;
  completedAt?: string;
}

export interface RepositoryOverview {
  repositoryId: string;
  repositoryName: string;
  totalCommits: number;
  totalPullRequests: number;
  totalIssues: number;
  totalReleases: number;
  activeContributors: number;
  prCycleTimeHours: {
    median: number;
    p75: number;
    p90: number;
  };
  reviewWaitingTimeHours: {
    median: number;
    p75: number;
  };
  healthScore: {
    composite: number;
    codeStability: number;
    reviewEfficiency: number;
    releaseCadence: number;
    collaborationResilience: number;
  };
}

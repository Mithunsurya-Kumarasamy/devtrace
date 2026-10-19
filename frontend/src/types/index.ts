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

// Process Mining
export interface ProcessStage {
  stage: string;
  displayName: string;
  avgDurationHours: number;
  medianDurationHours: number;
  p75DurationHours: number;
  p90DurationHours: number;
  transitionCount: number;
}

export interface ProcessBottleneck {
  stage: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  medianWaitHours: number;
  ratioOfCycleTime: number;
  recommendation: string;
}

export interface ProcessVariant {
  variantKey: string;
  displayName: string;
  prCount: number;
  percentageOfTotal: number;
  description: string;
}

export interface ProcessMiningReport {
  repositoryId: string;
  repositoryName: string;
  totalAnalyzedPrs: number;
  fastTrackPrs: number;
  stages: ProcessStage[];
  bottlenecks: ProcessBottleneck[];
  variants: ProcessVariant[];
  limitations: string[];
}

// Churn & Hotspots
export interface FileChurn {
  filePath: string;
  additions: number;
  deletions: number;
  totalChurn: number;
  commitsCount: number;
}

export interface DailyChurn {
  date: string;
  additions: number;
  deletions: number;
  totalChurn: number;
  commitCount: number;
}

export interface ChurnSummaryReport {
  repositoryId: string;
  timeWindowDays: number;
  totalCommits: number;
  totalAdditions: number;
  totalDeletions: number;
  netLinesChanged: number;
  topChurnedFiles: FileChurn[];
  directoryChurn: Record<string, number>;
  dailyChurn: DailyChurn[];
}

export interface HotspotItem {
  filePath: string;
  compositeScore: number;
  commitCount: number;
  bugFixCount: number;
  recencyWeight: number;
  riskTier: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  recommendedAction: string;
}

export interface HotspotReport {
  repositoryId: string;
  timeWindowDays: number;
  hotspots: HotspotItem[];
  summary: string;
}

// Collaboration Graph
export interface CollaborationNode {
  username: string;
  degreeCentrality: number;
  betweennessCentrality: number;
  communityId: number;
}

export interface CollaborationEdge {
  source: string;
  target: string;
  weight: number;
  pullRequestId?: string;
}

export interface KnowledgeSilo {
  filePath: string;
  primaryAuthor: string;
  authorCommitPercentage: number;
  totalCommits: number;
  riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  recommendation: string;
}

export interface CollaborationGraphReport {
  repositoryId: string;
  totalContributors: number;
  totalEdges: number;
  graphDensity: number;
  modularityScore: number;
  nodes: CollaborationNode[];
  edges: CollaborationEdge[];
  knowledgeSilos: KnowledgeSilo[];
}

// Issue NLP
export interface CategoryEvaluationMetric {
  category: string;
  precision: number;
  recall: number;
  f1Score: number;
  support: number;
}

export interface ClassificationEvaluation {
  totalEvaluatedIssues: number;
  totalLabeledIssues: number;
  agreementRate: number;
  categoryMetrics: CategoryEvaluationMetric[];
  confusionMatrix: Record<string, Record<string, number>>;
  categories: string[];
}

export interface IssueCategorySummary {
  categoryKey: string;
  categoryName: string;
  totalIssues: number;
  openIssues: number;
  closedIssues: number;
  percentage: number;
  medianResolutionHours: number;
  p75ResolutionHours: number;
  averageResolutionHours: number;
  topKeywords: string[];
}

export interface IssueClassificationItem {
  issueNumber: number;
  title: string;
  predictedCategory: string;
  confidenceScore: number;
  groundTruthCategory?: string;
  isAgreed: boolean;
}

export interface IssueNlpReport {
  repositoryId: string;
  totalAnalyzedIssues: number;
  labeledIssuesCount: number;
  unlabeledIssuesCount: number;
  categories: IssueCategorySummary[];
  evaluation: ClassificationEvaluation;
  classifiedIssuesSample: IssueClassificationItem[];
}

// Anomaly Detection
export interface MetricBaseline {
  metricName: string;
  mean: number;
  standardDeviation: number;
  median: number;
  q1: number;
  q3: number;
  iqr: number;
  lowerBound: number;
  upperBound: number;
  sampleCount: number;
}

export interface AnomalyRecord {
  id: string;
  metricName: string;
  observedValue: number;
  expectedLower: number;
  expectedUpper: number;
  baselineMean?: number;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  detectionMethod: string;
  explanation: string;
  detectionTimestamp: string;
}

export interface AnomalyDetectionReport {
  repositoryId: string;
  totalAnomaliesCount: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  baselines: MetricBaseline[];
  recentAnomalies: AnomalyRecord[];
}

// PR Risk Estimation
export interface RiskFactor {
  factorName: string;
  riskScore: number;
  weight: number;
  description: string;
  recommendation: string;
}

export interface PullRequestSummary {
  id: string;
  prNumber: number;
  title: string;
  state: string;
  authorLogin: string;
  additions: number;
  deletions: number;
  isMerged: boolean;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface PullRequestRiskAssessment {
  pullRequestId: string;
  prNumber: number;
  title: string;
  authorLogin: string;
  riskScore: number;
  riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  confidence: number;
  isDataSufficient: boolean;
  dataSufficiencyWarning?: string;
  modelVersion: string;
  isHeuristic: boolean;
  evaluatedAt: string;
  riskFactors: RiskFactor[];
  guardrailRecommendations: string[];
}

// Dependency Graph
export interface DependencyNode {
  id: string;
  name: string;
  type: string;
  afferentCoupling: number;
  efferentCoupling: number;
  instability: number;
  totalChurn: number;
  hotspotLevel: string;
  architecturalRole: 'HIGH_BLAST_RADIUS' | 'FRAGILE_VOLATILE' | 'STABLE_CORE' | 'NORMAL';
  inCycle: boolean;
}

export interface DependencyEdge {
  source: string;
  target: string;
  weight: number;
}

export interface CircularDependency {
  cycleLength: number;
  modules: string[];
  recommendation: string;
}

export interface DependencyGraphReport {
  repositoryId: string;
  totalModules: number;
  totalDependencies: number;
  hasCircularDependencies: boolean;
  circularDependenciesCount: number;
  highBlastRadiusCount: number;
  fragileComponentsCount: number;
  nodes: DependencyNode[];
  edges: DependencyEdge[];
  circularDependencies: CircularDependency[];
  architecturalSummary: string;
}

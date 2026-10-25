package com.devtrace.service;

import com.devtrace.dto.*;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.entity.SyncCheckpointEntity;
import com.devtrace.entity.SyncJobEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.kafka.event.SyncTriggerEvent;
import com.devtrace.kafka.producer.DevTraceEventProducer;
import com.devtrace.repository.RepositoryEntityRepository;
import com.devtrace.repository.SyncJobEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class SyncJobService {

    private static final Logger log = LoggerFactory.getLogger(SyncJobService.class);

    private final RepositoryEntityRepository repositoryRepo;
    private final SyncJobEntityRepository syncJobRepo;
    private final GitHubApiClient gitHubApiClient;
    private final IngestionOrchestrator ingestionOrchestrator;
    private final DevTraceEventProducer eventProducer;

    @org.springframework.beans.factory.annotation.Value("${spring.kafka.listener.auto-startup:true}")
    private boolean kafkaListenerAutoStartup = true;

    public SyncJobService(
            RepositoryEntityRepository repositoryRepo,
            SyncJobEntityRepository syncJobRepo,
            GitHubApiClient gitHubApiClient,
            IngestionOrchestrator ingestionOrchestrator,
            DevTraceEventProducer eventProducer) {
        this.repositoryRepo = repositoryRepo;
        this.syncJobRepo = syncJobRepo;
        this.gitHubApiClient = gitHubApiClient;
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public SyncJobResponse triggerSync(String repositoryId, boolean forceFullSync) {
        return triggerSync(repositoryId, forceFullSync, null);
    }

    @Transactional
    public SyncJobResponse triggerSync(String repositoryId, boolean forceFullSync, String branch) {
        RepositoryEntity repository = repositoryRepo.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with ID: " + repositoryId));

        SyncJobEntity job = new SyncJobEntity(repository);
        job.setStatus("PENDING");
        job.setCurrentStep("QUEUED");
        job.setProgressPercentage(0);
        job = syncJobRepo.save(job);

        repository.setSyncStatus("PENDING");
        repositoryRepo.save(repository);

        // Emit Kafka trigger event if Kafka is enabled and listener is active
        boolean kafkaDispatched = false;
        try {
            if (kafkaListenerAutoStartup && eventProducer != null && eventProducer.isKafkaAvailable()) {
                kafkaDispatched = eventProducer.sendSyncTrigger(new SyncTriggerEvent(
                        job.getId(),
                        repository.getId(),
                        repository.getOwner(),
                        repository.getName(),
                        forceFullSync,
                        branch
                ));
            }
        } catch (Exception e) {
            log.warn("Kafka producer unavailable or standalone execution. Proceeding with in-process worker: {}", e.getMessage());
        }

        // Kick off async execution only if not dispatched to Kafka consumer
        if (!kafkaDispatched) {
            final String finalJobId = job.getId();
            final String finalRepoId = repository.getId();
            CompletableFuture.runAsync(() -> {
                try {
                    executeSync(finalJobId, finalRepoId, forceFullSync, branch);
                } catch (Exception ex) {
                    log.error("Async execution error for job {}: {}", finalJobId, ex.getMessage(), ex);
                }
            });
        }

        return SyncJobResponse.fromEntity(job);
    }

    @Async
    public CompletableFuture<Void> executeSyncAsync(String jobId, String repositoryId, boolean forceFullSync) {
        return executeSyncAsync(jobId, repositoryId, forceFullSync, null);
    }

    @Async
    public CompletableFuture<Void> executeSyncAsync(String jobId, String repositoryId, boolean forceFullSync, String branch) {
        executeSync(jobId, repositoryId, forceFullSync, branch);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void executeSync(String jobId, String repositoryId, boolean forceFullSync) {
        executeSync(jobId, repositoryId, forceFullSync, null);
    }

    @Transactional
    public void executeSync(String jobId, String repositoryId, boolean forceFullSync, String branch) {
        Optional<SyncJobEntity> jobOpt = syncJobRepo.findById(jobId);
        Optional<RepositoryEntity> repoOpt = repositoryRepo.findById(repositoryId);

        if (jobOpt.isEmpty() || repoOpt.isEmpty()) {
            log.error("Sync job or repository not found for execution: jobId={}, repoId={}", jobId, repositoryId);
            return;
        }

        SyncJobEntity job = jobOpt.get();
        RepositoryEntity repository = repoOpt.get();

        log.info("Starting ingestion sync: repo={}, jobId={}, branch={}", repository.getFullName(), jobId, branch);
        job.setStatus("RUNNING");
        job.setCurrentStep("INGESTING_COMMITS");
        job.setProgressPercentage(10);
        syncJobRepo.save(job);

        repository.setSyncStatus("RUNNING");
        repositoryRepo.save(repository);

        try {
            int totalProcessed = 0;

            // 1. Commits Stage
            String branchName = (branch != null && !branch.isBlank()) ? branch.trim() : (repository.getDefaultBranch() != null ? repository.getDefaultBranch() : "master");
            String branchCheckpoint = "COMMITS_" + branchName;
            String commitSince = null;
            if (!forceFullSync) {
                Optional<SyncCheckpointEntity> cp = ingestionOrchestrator.getCheckpoint(repository, branchCheckpoint);
                if (cp.isPresent() && cp.get().getLastSyncTimestamp() != null) {
                    commitSince = cp.get().getLastSyncTimestamp().toString();
                }
            }

            int commitPage = 1;
            boolean hasMoreCommits = true;
            Instant latestCommitTime = null;

            while (hasMoreCommits && commitPage <= 10) { // Safety ceiling per sync run
                List<GitHubCommitDto> commits = gitHubApiClient.fetchCommits(
                        repository.getOwner(), repository.getName(), branchName, commitSince, commitPage, 100);

                if (commits.isEmpty()) {
                    hasMoreCommits = false;
                } else {
                    for (GitHubCommitDto c : commits) {
                        ingestionOrchestrator.processCommit(repository, c, branchName);
                        totalProcessed++;
                        if (c.getCommit() != null && c.getCommit().getCommitter() != null && c.getCommit().getCommitter().getDate() != null) {
                            Instant t = Instant.parse(c.getCommit().getCommitter().getDate());
                            if (latestCommitTime == null || t.isAfter(latestCommitTime)) {
                                latestCommitTime = t;
                            }
                        }
                    }
                    if (commits.size() < 100) hasMoreCommits = false;
                    else commitPage++;
                }
            }

            if (latestCommitTime != null) {
                ingestionOrchestrator.updateCheckpoint(repository, branchCheckpoint, latestCommitTime, null);
                if (branchName.equals(repository.getDefaultBranch())) {
                    ingestionOrchestrator.updateCheckpoint(repository, "COMMITS", latestCommitTime, null);
                }
            }

            job.setCurrentStep("INGESTING_PULL_REQUESTS");
            job.setProgressPercentage(45);
            job.setItemsProcessed(totalProcessed);
            syncJobRepo.save(job);

            // 2. Pull Requests Stage
            int prPage = 1;
            boolean hasMorePrs = true;
            Instant latestPrTime = null;

            while (hasMorePrs && prPage <= 10) {
                List<GitHubPullRequestDto> prs = gitHubApiClient.fetchPullRequests(
                        repository.getOwner(), repository.getName(), "all", prPage, 50);

                if (prs.isEmpty()) {
                    hasMorePrs = false;
                } else {
                    for (GitHubPullRequestDto pr : prs) {
                        List<GitHubReviewDto> reviews = gitHubApiClient.fetchPullRequestReviews(
                                repository.getOwner(), repository.getName(), pr.getNumber());
                        ingestionOrchestrator.processPullRequest(repository, pr, reviews);
                        totalProcessed++;
                        if (pr.getCreatedAt() != null) {
                            Instant t = Instant.parse(pr.getCreatedAt());
                            if (latestPrTime == null || t.isAfter(latestPrTime)) {
                                latestPrTime = t;
                            }
                        }
                    }
                    if (prs.size() < 50) hasMorePrs = false;
                    else prPage++;
                }
            }

            if (latestPrTime != null) {
                ingestionOrchestrator.updateCheckpoint(repository, "PULL_REQUESTS", latestPrTime, null);
            }

            job.setCurrentStep("INGESTING_ISSUES");
            job.setProgressPercentage(75);
            job.setItemsProcessed(totalProcessed);
            syncJobRepo.save(job);

            // 3. Issues Stage
            String issueSince = null;
            if (!forceFullSync) {
                Optional<SyncCheckpointEntity> cp = ingestionOrchestrator.getCheckpoint(repository, "ISSUES");
                if (cp.isPresent() && cp.get().getLastSyncTimestamp() != null) {
                    issueSince = cp.get().getLastSyncTimestamp().toString();
                }
            }

            int issuePage = 1;
            boolean hasMoreIssues = true;
            Instant latestIssueTime = null;

            while (hasMoreIssues && issuePage <= 10) {
                List<GitHubIssueDto> issues = gitHubApiClient.fetchIssues(
                        repository.getOwner(), repository.getName(), "all", issueSince, issuePage, 50);

                if (issues.isEmpty()) {
                    hasMoreIssues = false;
                } else {
                    for (GitHubIssueDto issue : issues) {
                        if (!issue.isPullRequest()) {
                            ingestionOrchestrator.processIssue(repository, issue);
                            totalProcessed++;
                        }
                        if (issue.getCreatedAt() != null) {
                            Instant t = Instant.parse(issue.getCreatedAt());
                            if (latestIssueTime == null || t.isAfter(latestIssueTime)) {
                                latestIssueTime = t;
                            }
                        }
                    }
                    if (issues.size() < 50) hasMoreIssues = false;
                    else issuePage++;
                }
            }

            if (latestIssueTime != null) {
                ingestionOrchestrator.updateCheckpoint(repository, "ISSUES", latestIssueTime, null);
            }

            job.setCurrentStep("INGESTING_RELEASES");
            job.setProgressPercentage(90);
            job.setItemsProcessed(totalProcessed);
            syncJobRepo.save(job);

            // 4. Releases Stage
            List<GitHubReleaseDto> releases = gitHubApiClient.fetchReleases(repository.getOwner(), repository.getName(), 1, 50);
            for (GitHubReleaseDto r : releases) {
                ingestionOrchestrator.processRelease(repository, r);
                totalProcessed++;
            }

            // Mark completed
            job.setStatus("COMPLETED");
            job.setCurrentStep("COMPLETED");
            job.setProgressPercentage(100);
            job.setItemsProcessed(totalProcessed);
            job.setCompletedAt(Instant.now());
            syncJobRepo.save(job);

            repository.setSyncStatus("COMPLETED");
            repository.setLastSyncedAt(Instant.now());
            repositoryRepo.save(repository);

            log.info("Ingestion completed successfully: repo={}, itemsProcessed={}", repository.getFullName(), totalProcessed);

        } catch (Exception ex) {
            log.error("Sync job failed for repository {}: {}", repository.getFullName(), ex.getMessage(), ex);
            job.setStatus("FAILED");
            job.setCurrentStep("ERROR");
            job.setErrorMessage(ex.getMessage());
            job.setCompletedAt(Instant.now());
            syncJobRepo.save(job);

            repository.setSyncStatus("FAILED");
            repositoryRepo.save(repository);
        }
    }

    @Transactional(readOnly = true)
    public List<SyncJobResponse> getSyncJobsForRepository(String repositoryId) {
        if (!repositoryRepo.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }
        return syncJobRepo.findByRepositoryIdOrderByStartedAtDesc(repositoryId).stream()
                .map(SyncJobResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SyncJobResponse getSyncJob(String repositoryId, String jobId) {
        SyncJobEntity job = syncJobRepo.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Sync job with ID " + jobId + " not found"));

        if (!job.getRepository().getId().equals(repositoryId)) {
            throw new ResourceNotFoundException("Sync job does not belong to specified repository");
        }

        return SyncJobResponse.fromEntity(job);
    }
}

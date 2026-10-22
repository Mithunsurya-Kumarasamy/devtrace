package com.devtrace.service;

import com.devtrace.dto.*;
import com.devtrace.entity.*;
import com.devtrace.exception.GitHubApiException;
import com.devtrace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SyncJobServiceTest {

    @Autowired
    private SyncJobService syncJobService;

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    @Autowired
    private SyncJobEntityRepository syncJobRepo;

    @Autowired
    private CommitEntityRepository commitRepo;

    @Autowired
    private PullRequestEntityRepository pullRequestRepo;

    @Autowired
    private IssueEntityRepository issueRepo;

    @Autowired
    private ReleaseEntityRepository releaseRepo;

    @Autowired
    private SyncCheckpointEntityRepository checkpointRepo;

    @MockBean
    private GitHubApiClient gitHubApiClient;

    private RepositoryEntity testRepo;

    @BeforeEach
    void setUp() {
        testRepo = repositoryRepo.save(new RepositoryEntity("devtrace-org", "intelligence-repo"));
    }

    @Test
    @DisplayName("Should trigger sync, return valid job response, and track status")
    void testTriggerSync() {
        SyncJobResponse response = syncJobService.triggerSync(testRepo.getId(), false);

        assertNotNull(response.getJobId());
        assertEquals(testRepo.getId(), response.getRepositoryId());
        assertTrue(List.of("PENDING", "RUNNING", "COMPLETED").contains(response.getStatus()));

        SyncJobResponse fetched = syncJobService.getSyncJob(testRepo.getId(), response.getJobId());
        assertEquals(response.getJobId(), fetched.getJobId());
        assertTrue(List.of("PENDING", "RUNNING", "COMPLETED").contains(fetched.getStatus()));
    }

    @Test
    @DisplayName("Should execute sync stages, persist commits, PRs, issues, releases, and update checkpoints")
    void testExecuteSyncLifecycle() {
        // Mock Commit
        GitHubCommitDto commitDto = new GitHubCommitDto();
        commitDto.setSha("a1b2c3d4e5f60718293a4b5c6d7e8f9a0b1c2d3e");
        GitHubCommitDto.CommitInfo cInfo = new GitHubCommitDto.CommitInfo();
        cInfo.setMessage("feat: add streaming anomaly detection");
        GitHubCommitDto.GitUser author = new GitHubCommitDto.GitUser();
        author.setName("Senior Dev");
        author.setEmail("dev@devtrace.internal");
        author.setDate("2026-10-18T12:00:00Z");
        cInfo.setAuthor(author);
        cInfo.setCommitter(author);
        commitDto.setCommit(cInfo);

        GitHubCommitDto.CommitStats stats = new GitHubCommitDto.CommitStats();
        stats.setAdditions(85);
        stats.setDeletions(10);
        stats.setTotal(95);
        commitDto.setStats(stats);

        // Mock PR
        GitHubPullRequestDto prDto = new GitHubPullRequestDto();
        prDto.setNumber(10);
        prDto.setTitle("Add process mining engine");
        prDto.setState("closed");
        GitHubPullRequestDto.GitHubUser prUser = new GitHubPullRequestDto.GitHubUser();
        prUser.setLogin("contributor-alpha");
        prDto.setUser(prUser);
        prDto.setCreatedAt("2026-10-18T09:00:00Z");
        prDto.setClosedAt("2026-10-18T11:00:00Z");
        prDto.setMergedAt("2026-10-18T11:00:00Z");
        prDto.setMerged(true);

        // Mock PR Review
        GitHubReviewDto reviewDto = new GitHubReviewDto();
        reviewDto.setId(555L);
        GitHubReviewDto.GitHubUser reviewer = new GitHubReviewDto.GitHubUser();
        reviewer.setLogin("tech-lead");
        reviewDto.setUser(reviewer);
        reviewDto.setState("APPROVED");
        reviewDto.setSubmittedAt("2026-10-18T10:30:00Z");

        // Mock Issue
        GitHubIssueDto issueDto = new GitHubIssueDto();
        issueDto.setNumber(4);
        issueDto.setTitle("Bug: Memory leak in graph centrality calculation");
        issueDto.setState("open");
        GitHubIssueDto.GitHubUser issueUser = new GitHubIssueDto.GitHubUser();
        issueUser.setLogin("qa-tester");
        issueDto.setUser(issueUser);
        GitHubIssueDto.LabelInfo label = new GitHubIssueDto.LabelInfo();
        label.setName("bug");
        issueDto.setLabels(List.of(label));
        issueDto.setCreatedAt("2026-10-18T08:00:00Z");

        // Mock Release
        GitHubReleaseDto releaseDto = new GitHubReleaseDto();
        releaseDto.setTagName("v1.0.0");
        releaseDto.setName("Production Release 1.0.0");
        releaseDto.setPublishedAt("2026-10-18T14:00:00Z");

        when(gitHubApiClient.fetchCommits(anyString(), anyString(), any(), eq(1), anyInt()))
                .thenReturn(List.of(commitDto));
        when(gitHubApiClient.fetchPullRequests(anyString(), anyString(), anyString(), eq(1), anyInt()))
                .thenReturn(List.of(prDto));
        when(gitHubApiClient.fetchPullRequestReviews(anyString(), anyString(), eq(10)))
                .thenReturn(List.of(reviewDto));
        when(gitHubApiClient.fetchIssues(anyString(), anyString(), anyString(), any(), eq(1), anyInt()))
                .thenReturn(List.of(issueDto));
        when(gitHubApiClient.fetchReleases(anyString(), anyString(), eq(1), anyInt()))
                .thenReturn(List.of(releaseDto));

        // Create job
        SyncJobEntity job = syncJobRepo.save(new SyncJobEntity(testRepo));

        // Execute sync
        syncJobService.executeSync(job.getId(), testRepo.getId(), false);

        // Verify entities persisted
        assertEquals(1, commitRepo.countByRepositoryId(testRepo.getId()));
        assertEquals(1, pullRequestRepo.countByRepositoryId(testRepo.getId()));
        assertEquals(1, issueRepo.countByRepositoryId(testRepo.getId()));
        assertEquals(1, releaseRepo.countByRepositoryId(testRepo.getId()));

        // Verify PR review was populated
        PullRequestEntity savedPr = pullRequestRepo.findByRepositoryIdAndPrNumber(testRepo.getId(), 10).orElseThrow();
        assertEquals("APPROVED", savedPr.getReviews().get(0).getState());
        assertEquals(Instant.parse("2026-10-18T10:30:00Z"), savedPr.getFirstReviewAt());

        // Verify Issue category inferred
        IssueEntity savedIssue = issueRepo.findByRepositoryIdAndIssueNumber(testRepo.getId(), 4).orElseThrow();
        assertEquals("BUG", savedIssue.getCategory());

        // Verify Job status completed
        SyncJobEntity updatedJob = syncJobRepo.findById(job.getId()).orElseThrow();
        assertEquals("COMPLETED", updatedJob.getStatus());
        assertEquals(100, updatedJob.getProgressPercentage());
        assertEquals(4, updatedJob.getItemsProcessed());

        // Verify Checkpoints updated
        Optional<SyncCheckpointEntity> commitCp = checkpointRepo.findByRepositoryIdAndResourceType(testRepo.getId(), "COMMITS");
        assertTrue(commitCp.isPresent());
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), commitCp.get().getLastSyncTimestamp());
    }

    @Test
    @DisplayName("Should handle duplicate ingestion events idempotently without throwing constraint violations")
    void testIdempotentIngestion() {
        GitHubCommitDto commitDto = new GitHubCommitDto();
        commitDto.setSha("duplicate-sha-1234567890abcdef12345678");
        GitHubCommitDto.CommitInfo cInfo = new GitHubCommitDto.CommitInfo();
        cInfo.setMessage("initial version");
        commitDto.setCommit(cInfo);

        GitHubCommitDto.CommitStats stats = new GitHubCommitDto.CommitStats();
        stats.setAdditions(20);
        commitDto.setStats(stats);

        when(gitHubApiClient.fetchCommits(anyString(), anyString(), any(), eq(1), anyInt()))
                .thenReturn(List.of(commitDto));
        when(gitHubApiClient.fetchPullRequests(anyString(), anyString(), anyString(), eq(1), anyInt()))
                .thenReturn(Collections.emptyList());
        when(gitHubApiClient.fetchIssues(anyString(), anyString(), anyString(), any(), eq(1), anyInt()))
                .thenReturn(Collections.emptyList());
        when(gitHubApiClient.fetchReleases(anyString(), anyString(), eq(1), anyInt()))
                .thenReturn(Collections.emptyList());

        SyncJobEntity job1 = syncJobRepo.save(new SyncJobEntity(testRepo));
        syncJobService.executeSync(job1.getId(), testRepo.getId(), false);
        assertEquals(1, commitRepo.countByRepositoryId(testRepo.getId()));

        // Update message in second ingestion run
        cInfo.setMessage("updated version with more details");
        stats.setAdditions(50);

        SyncJobEntity job2 = syncJobRepo.save(new SyncJobEntity(testRepo));
        syncJobService.executeSync(job2.getId(), testRepo.getId(), false);

        // Should still have exactly 1 commit, with updated message and stats
        assertEquals(1, commitRepo.countByRepositoryId(testRepo.getId()));
        CommitEntity commit = commitRepo.findByRepositoryIdAndSha(testRepo.getId(), commitDto.getSha()).orElseThrow();
        assertEquals("updated version with more details", commit.getMessage());
        assertEquals(50, commit.getAdditions());
    }

    @Test
    @DisplayName("Should transition job to FAILED state and capture error message on failure")
    void testFailureRecovery() {
        when(gitHubApiClient.fetchCommits(anyString(), anyString(), any(), eq(1), anyInt()))
                .thenThrow(new GitHubApiException("GitHub connection timed out", 504));

        SyncJobEntity job = syncJobRepo.save(new SyncJobEntity(testRepo));
        syncJobService.executeSync(job.getId(), testRepo.getId(), false);

        SyncJobEntity failedJob = syncJobRepo.findById(job.getId()).orElseThrow();
        assertEquals("FAILED", failedJob.getStatus());
        assertEquals("ERROR", failedJob.getCurrentStep());
        assertTrue(failedJob.getErrorMessage().contains("GitHub connection timed out"));

        RepositoryEntity updatedRepo = repositoryRepo.findById(testRepo.getId()).orElseThrow();
        assertEquals("FAILED", updatedRepo.getSyncStatus());
    }

    @Test
    @DisplayName("Should retrieve sync jobs history ordered by startedAt descending (BUG-04)")
    void testListSyncJobsHistory() {
        SyncJobEntity job1 = new SyncJobEntity(testRepo);
        job1.setStatus("COMPLETED");
        job1.setStartedAt(Instant.now().minusSeconds(100));
        syncJobRepo.save(job1);

        SyncJobEntity job2 = new SyncJobEntity(testRepo);
        job2.setStatus("RUNNING");
        job2.setStartedAt(Instant.now());
        syncJobRepo.save(job2);

        List<SyncJobResponse> jobs = syncJobService.getSyncJobsForRepository(testRepo.getId());

        assertEquals(2, jobs.size());
        assertEquals(job2.getId(), jobs.get(0).getJobId());
        assertEquals(job1.getId(), jobs.get(1).getJobId());
    }

    @Test
    @DisplayName("Should provide processedItems and totalEstimatedItems aliases (BUG-06)")
    void testSyncJobResponseAliases() {
        SyncJobEntity job = new SyncJobEntity(testRepo);
        job.setItemsProcessed(42);
        job.setTotalItemsEstimated(100);
        syncJobRepo.save(job);

        SyncJobResponse res = syncJobService.getSyncJob(testRepo.getId(), job.getId());

        assertEquals(42, res.getItemsProcessed());
        assertEquals(42, res.getProcessedItems());
        assertEquals(100, res.getTotalItemsEstimated());
        assertEquals(100, res.getTotalEstimatedItems());
    }
}

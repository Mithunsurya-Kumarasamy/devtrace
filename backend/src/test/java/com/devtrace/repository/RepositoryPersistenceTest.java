package com.devtrace.repository;

import com.devtrace.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryPersistenceTest {

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    @Autowired
    private CommitEntityRepository commitRepo;

    @Autowired
    private PullRequestEntityRepository pullRequestRepo;

    @Autowired
    private IssueEntityRepository issueRepo;

    @Autowired
    private SyncJobEntityRepository syncJobRepo;

    @Test
    @DisplayName("Should persist and retrieve a Repository entity")
    void testRepositoryPersistence() {
        RepositoryEntity repo = new RepositoryEntity("Mithunsurya-Kumarasamy", "devtrace");
        repo.setDescription("Software Engineering Intelligence Platform");
        repo.setHtmlUrl("https://github.com/Mithunsurya-Kumarasamy/devtrace");
        repo.setDefaultBranch("master");

        RepositoryEntity saved = repositoryRepo.save(repo);
        assertNotNull(saved.getId());

        Optional<RepositoryEntity> found = repositoryRepo.findByFullName("Mithunsurya-Kumarasamy/devtrace");
        assertTrue(found.isPresent());
        assertEquals("master", found.get().getDefaultBranch());
        assertEquals("IDLE", found.get().getSyncStatus());
    }

    @Test
    @DisplayName("Should persist Commit with CommitFiles and calculate metrics")
    void testCommitAndFilePersistence() {
        RepositoryEntity repo = repositoryRepo.save(new RepositoryEntity("test-org", "test-repo"));

        CommitEntity commit = new CommitEntity(repo, "abc1234567890abcdef1234567890abcdef12", "feat: initial commit", Instant.now());
        commit.setAuthorName("Dev Lead");
        commit.setAuthorEmail("lead@devtrace.internal");
        commit.setAdditions(120);
        commit.setDeletions(15);
        commit.setTotalChanges(135);

        CommitFileEntity file1 = new CommitFileEntity(commit, "src/main/App.java", "modified", 100, 10, 110);
        CommitFileEntity file2 = new CommitFileEntity(commit, "README.md", "modified", 20, 5, 25);
        commit.getFiles().add(file1);
        commit.getFiles().add(file2);

        CommitEntity savedCommit = commitRepo.save(commit);
        assertNotNull(savedCommit.getId());
        assertEquals(2, savedCommit.getFiles().size());

        Optional<CommitEntity> retrieved = commitRepo.findByRepositoryIdAndSha(repo.getId(), commit.getSha());
        assertTrue(retrieved.isPresent());
        assertEquals("Dev Lead", retrieved.get().getAuthorName());
        assertEquals(1, commitRepo.countDistinctAuthorsByRepositoryId(repo.getId()));
    }

    @Test
    @DisplayName("Should persist PullRequest with Review relationships")
    void testPullRequestWithReviews() {
        RepositoryEntity repo = repositoryRepo.save(new RepositoryEntity("test-org", "pr-repo"));

        PullRequestEntity pr = new PullRequestEntity(repo, 42, "feat: implement process mining", "open", "developer1");
        pr.setBaseBranch("master");
        pr.setHeadBranch("feature/process-mining");
        pr.setAdditions(250);
        pr.setDeletions(30);

        PullRequestReviewEntity review = new PullRequestReviewEntity(pr, "senior-reviewer", "APPROVED", Instant.now());
        pr.getReviews().add(review);

        PullRequestEntity savedPr = pullRequestRepo.save(pr);
        assertNotNull(savedPr.getId());

        Optional<PullRequestEntity> foundPr = pullRequestRepo.findByRepositoryIdAndPrNumber(repo.getId(), 42);
        assertTrue(foundPr.isPresent());
        assertEquals("developer1", foundPr.get().getAuthorLogin());
        assertEquals(1, foundPr.get().getReviews().size());
        assertEquals("APPROVED", foundPr.get().getReviews().get(0).getState());
    }

    @Test
    @DisplayName("Should persist Issues and track categories")
    void testIssuePersistence() {
        RepositoryEntity repo = repositoryRepo.save(new RepositoryEntity("test-org", "issue-repo"));

        IssueEntity issue = new IssueEntity(repo, 101, "Bug: Race condition in ingestion checkpoint", "open", "qa-eng");
        issue.setCategory("BUG");
        issue.setCategoryConfidence(0.95);
        issue.setLabels("[\"bug\", \"concurrency\"]");

        IssueEntity savedIssue = issueRepo.save(issue);
        assertNotNull(savedIssue.getId());

        assertEquals(1, issueRepo.findByRepositoryIdAndCategory(repo.getId(), "BUG").size());
    }

    @Test
    @DisplayName("Should manage Synchronization Jobs status transitions")
    void testSyncJobLifecycle() {
        RepositoryEntity repo = repositoryRepo.save(new RepositoryEntity("test-org", "sync-repo"));

        SyncJobEntity job = new SyncJobEntity(repo);
        job.setStatus("RUNNING");
        job.setCurrentStep("INGESTING_COMMITS");
        job.setProgressPercentage(45);
        job.setItemsProcessed(150);
        job.setTotalItemsEstimated(330);

        SyncJobEntity saved = syncJobRepo.save(job);
        assertNotNull(saved.getId());

        Optional<SyncJobEntity> activeJob = syncJobRepo.findFirstByRepositoryIdAndStatus(repo.getId(), "RUNNING");
        assertTrue(activeJob.isPresent());
        assertEquals(45, activeJob.get().getProgressPercentage());
    }
}

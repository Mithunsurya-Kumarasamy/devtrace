package com.devtrace.service.analytics;

import com.devtrace.dto.analytics.RepositoryOverviewDto;
import com.devtrace.entity.*;
import com.devtrace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoreAnalyticsServiceTest {

    @Autowired
    private CoreAnalyticsService analyticsService;

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    @Autowired
    private CommitEntityRepository commitRepo;

    @Autowired
    private PullRequestEntityRepository pullRequestRepo;

    @Autowired
    private IssueEntityRepository issueRepo;

    @Autowired
    private ReleaseEntityRepository releaseRepo;

    private RepositoryEntity testRepo;

    @BeforeEach
    void setUp() {
        testRepo = repositoryRepo.save(new RepositoryEntity("analytics-org", "metrics-repo"));
    }

    @Test
    @DisplayName("Should return clean zero-distribution overview for empty repository")
    void testEmptyRepositoryOverview() {
        RepositoryOverviewDto overview = analyticsService.getRepositoryOverview(testRepo.getId(), 30);

        assertNotNull(overview);
        assertEquals(0, overview.getTotalCommits());
        assertEquals(0, overview.getTotalPullRequests());
        assertEquals(0.0, overview.getPrCycleTimeHours().getP50());
        assertEquals(0.0, overview.getReviewWaitingTimeHours().getP50());
        assertTrue(overview.getHealthScore().getCompositeScore() > 0.0);
    }

    @Test
    @DisplayName("Should calculate exact PR cycle percentiles and review waiting times using deterministic fixtures")
    void testDeterministicPrCalculations() {
        Instant baseTime = Instant.parse("2026-10-18T00:00:00Z");

        // PR 1: duration = 2.0 hours, wait = 1.0 hour
        PullRequestEntity pr1 = new PullRequestEntity(testRepo, 1, "PR 1", "closed", "dev1");
        pr1.setCreatedAt(baseTime);
        pr1.setFirstReviewAt(baseTime.plus(1, ChronoUnit.HOURS));
        pr1.setMergedAt(baseTime.plus(2, ChronoUnit.HOURS));
        pr1.setMerged(true);

        // PR 2: duration = 4.0 hours, wait = 2.0 hours
        PullRequestEntity pr2 = new PullRequestEntity(testRepo, 2, "PR 2", "closed", "dev2");
        pr2.setCreatedAt(baseTime);
        pr2.setFirstReviewAt(baseTime.plus(2, ChronoUnit.HOURS));
        pr2.setMergedAt(baseTime.plus(4, ChronoUnit.HOURS));
        pr2.setMerged(true);

        // PR 3: duration = 8.0 hours, wait = 4.0 hours
        PullRequestEntity pr3 = new PullRequestEntity(testRepo, 3, "PR 3", "closed", "dev3");
        pr3.setCreatedAt(baseTime);
        pr3.setFirstReviewAt(baseTime.plus(4, ChronoUnit.HOURS));
        pr3.setMergedAt(baseTime.plus(8, ChronoUnit.HOURS));
        pr3.setMerged(true);

        // PR 4: duration = 16.0 hours, wait = 8.0 hours
        PullRequestEntity pr4 = new PullRequestEntity(testRepo, 4, "PR 4", "closed", "dev4");
        pr4.setCreatedAt(baseTime);
        pr4.setFirstReviewAt(baseTime.plus(8, ChronoUnit.HOURS));
        pr4.setMergedAt(baseTime.plus(16, ChronoUnit.HOURS));
        pr4.setMerged(true);

        pullRequestRepo.saveAll(List.of(pr1, pr2, pr3, pr4));

        RepositoryOverviewDto overview = analyticsService.getRepositoryOverview(testRepo.getId(), 30);

        assertEquals(4, overview.getTotalPullRequests());
        assertEquals(4, overview.getMergedPullRequests());

        // Cycle times: [2.0, 4.0, 8.0, 16.0] -> Median (p50) = 6.0, Min = 2.0, Max = 16.0
        assertEquals(2.0, overview.getPrCycleTimeHours().getMin());
        assertEquals(6.0, overview.getPrCycleTimeHours().getP50());
        assertEquals(16.0, overview.getPrCycleTimeHours().getMax());

        // Wait times: [1.0, 2.0, 4.0, 8.0] -> Median (p50) = 3.0, Min = 1.0, Max = 8.0
        assertEquals(1.0, overview.getReviewWaitingTimeHours().getMin());
        assertEquals(3.0, overview.getReviewWaitingTimeHours().getP50());
        assertEquals(8.0, overview.getReviewWaitingTimeHours().getMax());
    }

    @Test
    @DisplayName("Should calculate transparent engineering health scores accurately")
    void testHealthScoreCalculations() {
        // Add 2 releases
        releaseRepo.save(new ReleaseEntity(testRepo, "v1.0", "Release 1.0", Instant.now().minus(10, ChronoUnit.DAYS)));
        releaseRepo.save(new ReleaseEntity(testRepo, "v1.1", "Release 1.1", Instant.now().minus(2, ChronoUnit.DAYS)));

        // Add 1 bug issue and 2 feature issues
        IssueEntity bug = new IssueEntity(testRepo, 1, "Crash on startup", "closed", "user1");
        bug.setCategory("BUG");
        IssueEntity feat1 = new IssueEntity(testRepo, 2, "Dark theme", "open", "user2");
        feat1.setCategory("FEATURE");
        IssueEntity feat2 = new IssueEntity(testRepo, 3, "Export CSV", "open", "user3");
        feat2.setCategory("FEATURE");
        issueRepo.saveAll(List.of(bug, feat1, feat2));

        // Add 3 commits from 3 distinct authors
        CommitEntity c1 = new CommitEntity(testRepo, "sha1", "feat", Instant.now());
        c1.setAuthorEmail("dev1@devtrace.internal");
        CommitEntity c2 = new CommitEntity(testRepo, "sha2", "feat", Instant.now());
        c2.setAuthorEmail("dev2@devtrace.internal");
        CommitEntity c3 = new CommitEntity(testRepo, "sha3", "feat", Instant.now());
        c3.setAuthorEmail("dev3@devtrace.internal");
        commitRepo.saveAll(List.of(c1, c2, c3));

        RepositoryOverviewDto overview = analyticsService.getRepositoryOverview(testRepo.getId(), 30);

        assertNotNull(overview.getHealthScore());
        assertTrue(overview.getHealthScore().getCompositeScore() >= 60.0);
        assertTrue(overview.getHealthScore().getCodeStability() > 70.0);
        assertEquals(80.0, overview.getHealthScore().getReleaseCadence());
        assertEquals(75.0, overview.getHealthScore().getCollaborationResilience());
        assertNotNull(overview.getHealthScore().getCalculationExplanation());
    }

    @Test
    @DisplayName("Should query state instead of category for open issues count (BUG-01)")
    void testOpenIssuesCountMatchesState() {
        IssueEntity openIssue1 = new IssueEntity(testRepo, 10, "Open bug", "open", "user1");
        openIssue1.setCategory("BUG_DEFECT");

        IssueEntity openIssue2 = new IssueEntity(testRepo, 11, "Open task", "open", "user2");
        openIssue2.setCategory("FEATURE");

        IssueEntity closedIssue = new IssueEntity(testRepo, 12, "Closed issue", "closed", "user3");
        closedIssue.setCategory("BUG");

        issueRepo.saveAll(List.of(openIssue1, openIssue2, closedIssue));

        RepositoryOverviewDto overview = analyticsService.getRepositoryOverview(testRepo.getId(), 30);

        assertEquals(3, overview.getTotalIssues());
        assertEquals(2, overview.getOpenIssues());
    }

    @Test
    @DisplayName("Should recognize NLP BUG_DEFECT in Code Stability calculation (BUG-03)")
    void testNlpBugDefectIncludedInCodeStability() {
        // 2 issues: 1 NLP categorized BUG_DEFECT, 1 FEATURE
        IssueEntity nlpBug = new IssueEntity(testRepo, 20, "Memory leak detected", "closed", "user1");
        nlpBug.setCategory("BUG_DEFECT");

        IssueEntity feat = new IssueEntity(testRepo, 21, "Add dark theme", "open", "user2");
        feat.setCategory("FEATURE");

        issueRepo.saveAll(List.of(nlpBug, feat));

        RepositoryOverviewDto overview = analyticsService.getRepositoryOverview(testRepo.getId(), 30);

        // bugCount = 1, issues.size() = 2, bugRatio = 0.5. Code stability = 100 - (0.5 * 60) = 70.0
        assertEquals(70.0, overview.getHealthScore().getCodeStability());
    }

    @Test
    @DisplayName("Should support alias getters on DTOs for frontend compatibility (BUG-08, BUG-09)")
    void testDtoAliases() {
        RepositoryOverviewDto overview = analyticsService.getRepositoryOverview(testRepo.getId(), 30);

        assertEquals(overview.getHealthScore().getCompositeScore(), overview.getHealthScore().getComposite());
        assertEquals(overview.getPrCycleTimeHours().getP50(), overview.getPrCycleTimeHours().getMedian());
        assertEquals(overview.getReviewWaitingTimeHours().getP50(), overview.getReviewWaitingTimeHours().getMedian());
    }
}

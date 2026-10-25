package com.devtrace.service.analytics;

import com.devtrace.dto.GitHubPullRequestDto;
import com.devtrace.dto.analytics.RepositoryOverviewDto;
import com.devtrace.dto.collaboration.CollaborationNetworkDto;
import com.devtrace.dto.processmining.ProcessMiningReportDto;
import com.devtrace.entity.CommitEntity;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.repository.*;
import com.devtrace.service.IngestionOrchestrator;
import com.devtrace.service.collaboration.CollaborationGraphService;
import com.devtrace.service.processmining.ProcessMiningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DashboardMetricsAuditRegressionTest {

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    @Autowired
    private CommitEntityRepository commitRepo;

    @Autowired
    private CommitFileEntityRepository commitFileRepo;

    @Autowired
    private PullRequestEntityRepository pullRequestRepo;

    @Autowired
    private PullRequestReviewEntityRepository reviewRepo;

    @Autowired
    private IngestionOrchestrator ingestionOrchestrator;

    @Autowired
    private CoreAnalyticsService coreAnalyticsService;

    @Autowired
    private ProcessMiningService processMiningService;

    @Autowired
    private CollaborationGraphService collaborationGraphService;

    private RepositoryEntity testRepo;

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity();
        testRepo.setGithubRepoId(999000L + System.currentTimeMillis() % 10000);
        testRepo.setOwner("test-audit-owner");
        testRepo.setName("test-audit-repo-" + UUID.randomUUID().toString().substring(0, 8));
        testRepo.setFullName(testRepo.getOwner() + "/" + testRepo.getName());
        testRepo.setDefaultBranch("main");
        testRepo.setSyncStatus("COMPLETED");
        testRepo = repositoryRepo.save(testRepo);
    }

    @Test
    @DisplayName("Audit 1: Merged PRs with mergedAt must be recorded as isMerged=true even when DTO merged flag is false")
    void testMergedPrDetectionFromMergedAtTimestamp() {
        GitHubPullRequestDto dto = new GitHubPullRequestDto();
        dto.setNumber(101);
        dto.setTitle("Feature PR 101");
        dto.setState("closed");
        dto.setMerged(false); // Typical GitHub list PR payload where boolean merged is missing/false
        dto.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS).toString());
        dto.setClosedAt(Instant.now().minus(1, ChronoUnit.DAYS).toString());
        dto.setMergedAt(Instant.now().minus(1, ChronoUnit.DAYS).toString());

        PullRequestEntity saved = ingestionOrchestrator.processPullRequest(testRepo, dto, Collections.emptyList());

        assertThat(saved.isMerged())
                .as("PR with non-null mergedAt must have isMerged evaluated as true")
                .isTrue();
        assertThat(saved.getMergedAt()).isNotNull();

        // Verify retrieval from repository
        PullRequestEntity retrieved = pullRequestRepo.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.isMerged()).isTrue();
    }

    @Test
    @DisplayName("Audit 2: Cycle time and Process Mining correctly classify merged PR vs abandoned PR")
    void testProcessMiningClassificationAndCycleTime() {
        Instant now = Instant.now();

        // 1. Ingest a merged PR (12h cycle time)
        GitHubPullRequestDto mergedDto = new GitHubPullRequestDto();
        mergedDto.setNumber(201);
        mergedDto.setTitle("Merged PR");
        mergedDto.setState("closed");
        mergedDto.setMerged(false);
        mergedDto.setCreatedAt(now.minus(24, ChronoUnit.HOURS).toString());
        mergedDto.setClosedAt(now.minus(12, ChronoUnit.HOURS).toString());
        mergedDto.setMergedAt(now.minus(12, ChronoUnit.HOURS).toString());
        ingestionOrchestrator.processPullRequest(testRepo, mergedDto, Collections.emptyList());

        // 2. Ingest an unmerged closed PR (abandoned)
        GitHubPullRequestDto abandonedDto = new GitHubPullRequestDto();
        abandonedDto.setNumber(202);
        abandonedDto.setTitle("Abandoned PR");
        abandonedDto.setState("closed");
        abandonedDto.setMerged(false);
        abandonedDto.setCreatedAt(now.minus(48, ChronoUnit.HOURS).toString());
        abandonedDto.setClosedAt(now.minus(36, ChronoUnit.HOURS).toString());
        abandonedDto.setMergedAt(null);
        ingestionOrchestrator.processPullRequest(testRepo, abandonedDto, Collections.emptyList());

        // Overview metrics verification
        RepositoryOverviewDto overview = coreAnalyticsService.getRepositoryOverview(testRepo.getId(), 30);
        assertThat(overview.getMergedPullRequests())
                .as("Only the merged PR should be counted in mergedPullRequests")
                .isEqualTo(1);
        assertThat(overview.getPrCycleTimeHours().getMedian())
                .as("Cycle time median should be approximately 12.0 hours")
                .isEqualTo(12.0);

        // Process mining report verification
        ProcessMiningReportDto miningReport = processMiningService.analyzeProcessMining(testRepo.getId());
        assertThat(miningReport.getTotalAnalyzedPrs()).isEqualTo(2);
        assertThat(miningReport.getMergedPrsCount()).isEqualTo(1);
        assertThat(miningReport.getClosedUnmergedPrsCount()).isEqualTo(1);

        // Verify that the merged PR is NOT classified as ABANDONED
        long abandonedCount = miningReport.getRecentTraces().stream()
                .filter(t -> "ABANDONED".equalsIgnoreCase(t.getVariant()))
                .count();
        long fastOrStandardCount = miningReport.getRecentTraces().stream()
                .filter(t -> "FAST_TRACK".equalsIgnoreCase(t.getVariant()) || "STANDARD".equalsIgnoreCase(t.getVariant()))
                .count();

        assertThat(abandonedCount).as("Only the truly unmerged PR should be ABANDONED").isEqualTo(1);
        assertThat(fastOrStandardCount).as("The merged PR should be FAST_TRACK or STANDARD").isEqualTo(1);
    }

    @Test
    @DisplayName("Audit 3: Collaboration network registers commit authors even if commit files are sparse")
    void testCollaborationNetworkIncludesCommitAuthors() {
        CommitEntity c1 = new CommitEntity();
        c1.setRepository(testRepo);
        c1.setSha("sha111111111111111111111111111111111111");
        c1.setMessage("feat: first commit");
        c1.setAuthorName("alice_dev");
        c1.setAuthorEmail("alice@example.com");
        c1.setCommitDate(Instant.now().minus(5, ChronoUnit.DAYS));
        c1.setBranch("main");
        commitRepo.save(c1);

        CommitEntity c2 = new CommitEntity();
        c2.setRepository(testRepo);
        c2.setSha("sha222222222222222222222222222222222222");
        c2.setMessage("fix: second commit");
        c2.setAuthorName("bob_dev");
        c2.setAuthorEmail("bob@example.com");
        c2.setCommitDate(Instant.now().minus(2, ChronoUnit.DAYS));
        c2.setBranch("main");
        commitRepo.save(c2);

        CollaborationNetworkDto network = collaborationGraphService.getCollaborationNetwork(testRepo.getId());
        assertThat(network.getTotalContributors())
                .as("All commit authors should be tracked in the collaboration network")
                .isGreaterThanOrEqualTo(2);

        boolean hasAlice = network.getNodes().stream().anyMatch(n -> "alice_dev".equalsIgnoreCase(n.getId()) || "alice_dev".equalsIgnoreCase(n.getLabel()));
        boolean hasBob = network.getNodes().stream().anyMatch(n -> "bob_dev".equalsIgnoreCase(n.getId()) || "bob_dev".equalsIgnoreCase(n.getLabel()));
        assertThat(hasAlice).isTrue();
        assertThat(hasBob).isTrue();
    }
}

package com.devtrace.service;

import com.devtrace.dto.processmining.*;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.PullRequestReviewEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.PullRequestEntityRepository;
import com.devtrace.repository.PullRequestReviewEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import com.devtrace.service.processmining.ProcessMiningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessMiningServiceTest {

    @Mock
    private RepositoryEntityRepository repositoryRepo;

    @Mock
    private PullRequestEntityRepository pullRequestRepo;

    @Mock
    private PullRequestReviewEntityRepository reviewRepo;

    @InjectMocks
    private ProcessMiningService processMiningService;

    private RepositoryEntity testRepo;
    private final String repoId = "repo-process-mining-123";

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity("devtrace-org", "test-process-repo");
        testRepo.setId(repoId);
    }

    @Test
    @DisplayName("analyzeProcessMining throws ResourceNotFoundException if repository does not exist")
    void testRepositoryNotFound() {
        when(repositoryRepo.existsById("non-existent")).thenReturn(false);

        assertThatThrownBy(() -> processMiningService.analyzeProcessMining("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @DisplayName("analyzeProcessMining handles empty repository gracefully")
    void testEmptyRepository() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);
        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(Collections.emptyList());
        when(reviewRepo.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repoId)).thenReturn(Collections.emptyList());

        ProcessMiningReportDto report = processMiningService.analyzeProcessMining(repoId);

        assertThat(report).isNotNull();
        assertThat(report.getRepositoryId()).isEqualTo(repoId);
        assertThat(report.getTotalAnalyzedPrs()).isEqualTo(0);
        assertThat(report.getMergedPrsCount()).isEqualTo(0);
        assertThat(report.getStages()).hasSize(3);
        assertThat(report.getBottleneck().getStageKey()).isEqualTo("NONE");
        assertThat(report.getVariants()).isNotEmpty();
        assertThat(report.getMissingDataLimitations()).isNotEmpty();
    }

    @Test
    @DisplayName("analyzeProcessMining correctly categorizes fast-track, review-heavy, stale, and abandoned variants")
    void testProcessVariantsClassification() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        Instant base = Instant.now().minus(30, ChronoUnit.DAYS);

        // PR 1: FAST_TRACK (merged in 2h, 1 approval review)
        PullRequestEntity prFast = new PullRequestEntity(testRepo, 101, "Quick fix", "closed", "alice");
        prFast.setMerged(true);
        prFast.setCreatedAt(base);
        prFast.setFirstReviewAt(base.plus(1, ChronoUnit.HOURS));
        prFast.setApprovedAt(base.plus(1, ChronoUnit.HOURS));
        prFast.setMergedAt(base.plus(2, ChronoUnit.HOURS));

        PullRequestReviewEntity revFast = new PullRequestReviewEntity(prFast, "bob", "APPROVED", base.plus(1, ChronoUnit.HOURS));

        // PR 2: REVIEW_HEAVY (has CHANGES_REQUESTED review)
        PullRequestEntity prHeavy = new PullRequestEntity(testRepo, 102, "Complex feature", "closed", "bob");
        prHeavy.setMerged(true);
        prHeavy.setCreatedAt(base.plus(2, ChronoUnit.DAYS));
        prHeavy.setFirstReviewAt(base.plus(2, ChronoUnit.DAYS).plus(10, ChronoUnit.HOURS));
        prHeavy.setApprovedAt(base.plus(2, ChronoUnit.DAYS).plus(40, ChronoUnit.HOURS));
        prHeavy.setMergedAt(base.plus(2, ChronoUnit.DAYS).plus(45, ChronoUnit.HOURS));

        PullRequestReviewEntity revChanges = new PullRequestReviewEntity(prHeavy, "charlie", "CHANGES_REQUESTED", base.plus(2, ChronoUnit.DAYS).plus(10, ChronoUnit.HOURS));
        PullRequestReviewEntity revApprove = new PullRequestReviewEntity(prHeavy, "charlie", "APPROVED", base.plus(2, ChronoUnit.DAYS).plus(40, ChronoUnit.HOURS));

        // PR 3: ABANDONED (closed without merge)
        PullRequestEntity prAbandoned = new PullRequestEntity(testRepo, 103, "Superseded test", "closed", "dan");
        prAbandoned.setMerged(false);
        prAbandoned.setCreatedAt(base.plus(5, ChronoUnit.DAYS));
        prAbandoned.setClosedAt(base.plus(5, ChronoUnit.DAYS).plus(20, ChronoUnit.HOURS));

        // PR 4: STALE_REVIEW (time to first review > 72 hours, e.g. 100 hours)
        PullRequestEntity prStale = new PullRequestEntity(testRepo, 104, "Slow triage", "closed", "eve");
        prStale.setMerged(true);
        prStale.setCreatedAt(base.plus(10, ChronoUnit.DAYS));
        prStale.setFirstReviewAt(base.plus(10, ChronoUnit.DAYS).plus(100, ChronoUnit.HOURS));
        prStale.setApprovedAt(base.plus(10, ChronoUnit.DAYS).plus(105, ChronoUnit.HOURS));
        prStale.setMergedAt(base.plus(10, ChronoUnit.DAYS).plus(110, ChronoUnit.HOURS));

        PullRequestReviewEntity revStale = new PullRequestReviewEntity(prStale, "frank", "APPROVED", base.plus(10, ChronoUnit.DAYS).plus(100, ChronoUnit.HOURS));

        List<PullRequestEntity> prList = List.of(prFast, prHeavy, prAbandoned, prStale);
        List<PullRequestReviewEntity> revList = List.of(revFast, revChanges, revApprove, revStale);

        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(prList);
        when(reviewRepo.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repoId)).thenReturn(revList);

        ProcessMiningReportDto report = processMiningService.analyzeProcessMining(repoId);

        assertThat(report.getTotalAnalyzedPrs()).isEqualTo(4);
        assertThat(report.getMergedPrsCount()).isEqualTo(3);
        assertThat(report.getClosedUnmergedPrsCount()).isEqualTo(1);

        // Check variant counts
        Map<String, Integer> variantCounts = new HashMap<>();
        report.getVariants().forEach(v -> variantCounts.put(v.getVariantKey(), v.getCount()));

        assertThat(variantCounts.get("FAST_TRACK")).isEqualTo(1);
        assertThat(variantCounts.get("REVIEW_HEAVY")).isEqualTo(1);
        assertThat(variantCounts.get("ABANDONED")).isEqualTo(1);
        assertThat(variantCounts.get("STALE_REVIEW")).isEqualTo(1);

        // Check rework summary
        ChangesRequestedSummaryDto rework = report.getChangesRequestedSummary();
        assertThat(rework.getTotalPrsWithChangesRequested()).isEqualTo(1);
        assertThat(rework.getMaxIterations()).isEqualTo(1);

        // Check stages are populated
        assertThat(report.getStages()).hasSize(3);
        ProcessMiningStageDto bottleneck = report.getStages().stream()
                .filter(ProcessMiningStageDto::isBottleneck)
                .findFirst()
                .orElse(null);
        assertThat(bottleneck).isNotNull();
        assertThat(report.getBottleneck().getStageKey()).isEqualTo(bottleneck.getStageKey());
        assertThat(report.getBottleneck().getRecommendation()).isNotBlank();
    }

    @Test
    @DisplayName("analyzeProcessMining handles direct merges without reviews safely")
    void testDirectMergeWithoutReview() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        Instant now = Instant.now();
        PullRequestEntity directMerge = new PullRequestEntity(testRepo, 201, "Direct Bot Merge", "closed", "dependabot");
        directMerge.setMerged(true);
        directMerge.setCreatedAt(now.minus(4, ChronoUnit.HOURS));
        directMerge.setMergedAt(now);
        // firstReviewAt and approvedAt are null

        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(List.of(directMerge));
        when(reviewRepo.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repoId)).thenReturn(Collections.emptyList());

        ProcessMiningReportDto report = processMiningService.analyzeProcessMining(repoId);

        assertThat(report.getTotalAnalyzedPrs()).isEqualTo(1);
        assertThat(report.getMergedPrsCount()).isEqualTo(1);
        assertThat(report.getStages().get(0).getSampleCount()).isEqualTo(0); // No review
        assertThat(report.getRecentTraces()).hasSize(1);
        assertThat(report.getRecentTraces().get(0).getTimeToFirstReviewHours()).isNull();
        assertThat(report.getRecentTraces().get(0).getTotalCycleTimeHours()).isEqualTo(4.0);
    }
}

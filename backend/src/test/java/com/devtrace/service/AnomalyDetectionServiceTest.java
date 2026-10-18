package com.devtrace.service;

import com.devtrace.dto.anomaly.*;
import com.devtrace.entity.*;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.*;
import com.devtrace.service.anomaly.AnomalyDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private RepositoryEntityRepository repositoryRepo;

    @Mock
    private CommitEntityRepository commitRepo;

    @Mock
    private PullRequestEntityRepository pullRequestRepo;

    @Mock
    private AnomalyRecordEntityRepository anomalyRepo;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    private RepositoryEntity testRepo;
    private final String repoId = "repo-anomaly-test-123";

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity("devtrace-org", "test-anomaly-repo");
        testRepo.setId(repoId);
    }

    @Test
    @DisplayName("detectAndReportAnomalies throws ResourceNotFoundException if repository does not exist")
    void testRepoNotFound() {
        when(repositoryRepo.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anomalyDetectionService.detectAndReportAnomalies("non-existent", 30))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @DisplayName("detectAndReportAnomalies handles empty data gracefully")
    void testEmptyData() {
        when(repositoryRepo.findById(repoId)).thenReturn(Optional.of(testRepo));
        when(commitRepo.findByRepositoryIdAndCommitDateBetweenOrderByCommitDateAsc(eq(repoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());
        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(Collections.emptyList());
        when(anomalyRepo.findByRepositoryIdOrderByDetectionTimestampDesc(repoId)).thenReturn(Collections.emptyList());

        AnomalyDetectionReportDto report = anomalyDetectionService.detectAndReportAnomalies(repoId, 30);

        assertThat(report).isNotNull();
        assertThat(report.getRepositoryId()).isEqualTo(repoId);
        assertThat(report.getBaselines()).hasSize(4);
        assertThat(report.getTotalAnomaliesCount()).isEqualTo(0);
        assertThat(report.getRecentAnomalies()).isEmpty();
    }

    @Test
    @DisplayName("detectAndReportAnomalies detects PR size and review turnaround anomalies and persists them")
    void testDetectPrSizeAndTurnaroundAnomalies() {
        when(repositoryRepo.findById(repoId)).thenReturn(Optional.of(testRepo));
        when(commitRepo.findByRepositoryIdAndCommitDateBetweenOrderByCommitDateAsc(eq(repoId), any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        Instant base = Instant.now().minus(20, ChronoUnit.DAYS);

        // Create 6 normal PRs (size 50-100 lines, turnaround 2-6h)
        List<PullRequestEntity> prs = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            PullRequestEntity pr = new PullRequestEntity(testRepo, i, "Normal PR " + i, "merged", "dev" + i);
            pr.setAdditions(40 + i * 5);
            pr.setDeletions(10 + i * 2);
            pr.setCreatedAt(base.plus(i, ChronoUnit.DAYS));
            pr.setFirstReviewAt(base.plus(i, ChronoUnit.DAYS).plus(2 + i, ChronoUnit.HOURS));
            prs.add(pr);
        }

        // Outlier 1: Massive PR size (3,500 lines changed)
        PullRequestEntity hugePr = new PullRequestEntity(testRepo, 100, "Monolithic refactor", "closed", "alice");
        hugePr.setAdditions(2500);
        hugePr.setDeletions(1000);
        hugePr.setCreatedAt(base.plus(8, ChronoUnit.DAYS));
        hugePr.setFirstReviewAt(base.plus(8, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
        prs.add(hugePr);

        // Outlier 2: Stalled review (180 hours)
        PullRequestEntity stalledPr = new PullRequestEntity(testRepo, 101, "Stalled review feature", "closed", "bob");
        stalledPr.setAdditions(60);
        stalledPr.setDeletions(20);
        stalledPr.setCreatedAt(base.plus(10, ChronoUnit.DAYS));
        stalledPr.setFirstReviewAt(base.plus(10, ChronoUnit.DAYS).plus(180, ChronoUnit.HOURS));
        prs.add(stalledPr);

        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(prs);

        // When saveAll is called, return saved entities
        doAnswer(invocation -> invocation.getArgument(0)).when(anomalyRepo).saveAll(any());

        // Mock findByRepositoryId after detection
        when(anomalyRepo.findByRepositoryIdOrderByDetectionTimestampDesc(repoId))
                .thenAnswer(inv -> List.of(
                        new AnomalyRecordEntity(testRepo, "PR_SIZE", 3500.0, 0.0, 150.0, "CRITICAL", "IQR_Z_SCORE", "Large PR"),
                        new AnomalyRecordEntity(testRepo, "REVIEW_TURNAROUND", 180.0, 0.0, 15.0, "HIGH", "IQR_Z_SCORE", "Stalled review")
                ));

        AnomalyDetectionReportDto report = anomalyDetectionService.detectAndReportAnomalies(repoId, 30);

        // Verify saveAll was called with detected anomalies
        ArgumentCaptor<List<AnomalyRecordEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(anomalyRepo).saveAll(captor.capture());
        List<AnomalyRecordEntity> saved = captor.getValue();
        assertThat(saved).isNotEmpty();
        assertThat(saved.stream().anyMatch(a -> "PR_SIZE".equals(a.getMetricName()))).isTrue();
        assertThat(saved.stream().anyMatch(a -> "REVIEW_TURNAROUND".equals(a.getMetricName()))).isTrue();

        // Verify report
        assertThat(report.getTotalAnomaliesCount()).isEqualTo(2);
        assertThat(report.getCriticalCount()).isGreaterThanOrEqualTo(1);
        assertThat(report.getRecentAnomalies()).hasSize(2);

        // Verify baselines
        MetricBaselineDto prSizeBaseline = report.getBaselines().stream()
                .filter(b -> "PR_SIZE".equals(b.getMetricName()))
                .findFirst()
                .orElse(null);
        assertThat(prSizeBaseline).isNotNull();
        assertThat(prSizeBaseline.getSampleCount()).isEqualTo(8);
        assertThat(prSizeBaseline.getMean()).isGreaterThan(0.0);
        assertThat(prSizeBaseline.getUpperBound()).isGreaterThan(0.0);
    }
}

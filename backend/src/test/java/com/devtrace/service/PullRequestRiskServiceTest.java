package com.devtrace.service;

import com.devtrace.dto.risk.PullRequestRiskAssessmentDto;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.PullRequestReviewEntity;
import com.devtrace.entity.PullRequestRiskPredictionEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.CommitEntityRepository;
import com.devtrace.repository.PullRequestEntityRepository;
import com.devtrace.repository.PullRequestReviewEntityRepository;
import com.devtrace.repository.PullRequestRiskPredictionEntityRepository;
import com.devtrace.service.churn.ChurnAnalyticsService;
import com.devtrace.service.risk.PullRequestRiskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestRiskServiceTest {

    @Mock
    private PullRequestEntityRepository pullRequestRepo;

    @Mock
    private PullRequestReviewEntityRepository reviewRepo;

    @Mock
    private CommitEntityRepository commitRepo;

    @Mock
    private PullRequestRiskPredictionEntityRepository riskRepo;

    @Mock
    private ChurnAnalyticsService churnAnalyticsService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PullRequestRiskService riskService;

    private RepositoryEntity testRepo;
    private final String prId = "pr-risk-123";

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity("devtrace-org", "risk-repo");
        testRepo.setId("repo-123");
    }

    @Test
    @DisplayName("evaluateRisk throws ResourceNotFoundException if pull request does not exist")
    void testPrNotFound() {
        when(pullRequestRepo.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskService.getRiskAssessment("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pull Request not found");
    }

    @Test
    @DisplayName("evaluateRisk classifies small approved PR with experienced author as LOW risk")
    void testLowRiskPr() {
        PullRequestEntity pr = new PullRequestEntity(testRepo, 10, "Small bugfix", "open", "senior-dev");
        pr.setId(prId);
        pr.setAdditions(20);
        pr.setDeletions(5);
        pr.setChangedFiles(2);
        pr.setCreatedAt(Instant.parse("2026-10-14T14:00:00Z")); // Wednesday 14:00 UTC (normal work hours)

        when(pullRequestRepo.findById(prId)).thenReturn(Optional.of(pr));
        when(commitRepo.countByRepositoryId("repo-123")).thenReturn(150L);
        when(pullRequestRepo.countByRepositoryId("repo-123")).thenReturn(40L);
        when(commitRepo.countCommitsByRepositoryIdAndAuthor("repo-123", "senior-dev")).thenReturn(85L);

        PullRequestReviewEntity approval = new PullRequestReviewEntity(pr, "peer-reviewer", "APPROVED", Instant.now());
        when(reviewRepo.findByPullRequestIdOrderBySubmittedAtAsc(prId)).thenReturn(List.of(approval));

        PullRequestRiskAssessmentDto assessment = riskService.getRiskAssessment(prId);

        assertThat(assessment.getRiskScore()).isLessThan(0.30);
        assertThat(assessment.getRiskLevel()).isEqualTo("LOW");
        assertThat(assessment.isDataSufficient()).isTrue();
        assertThat(assessment.getDataSufficiencyWarning()).isNull();
        assertThat(assessment.getRiskFactors()).hasSize(5);
    }

    @Test
    @DisplayName("evaluateRisk classifies large unreviewed PR from new author as HIGH risk with data sufficiency warning")
    void testHighRiskPrWithWarning() {
        PullRequestEntity pr = new PullRequestEntity(testRepo, 20, "Huge refactor", "open", "newbie");
        pr.setId(prId);
        pr.setAdditions(1200);
        pr.setDeletions(800);
        pr.setChangedFiles(35);
        pr.setCreatedAt(Instant.parse("2026-10-18T03:00:00Z")); // Sunday 03:00 UTC (weekend + off-hours)

        when(pullRequestRepo.findById(prId)).thenReturn(Optional.of(pr));
        when(commitRepo.countByRepositoryId("repo-123")).thenReturn(8L); // <20 commits -> insufficient
        when(pullRequestRepo.countByRepositoryId("repo-123")).thenReturn(2L);
        when(commitRepo.countCommitsByRepositoryIdAndAuthor("repo-123", "newbie")).thenReturn(0L); // First-time contributor
        when(reviewRepo.findByPullRequestIdOrderBySubmittedAtAsc(prId)).thenReturn(Collections.emptyList()); // Unreviewed

        when(riskRepo.findByPullRequestId(prId)).thenReturn(Optional.empty());

        PullRequestRiskAssessmentDto assessment = riskService.evaluateAndPersistRisk(prId);

        assertThat(assessment.getRiskScore()).isGreaterThanOrEqualTo(0.70);
        assertThat(assessment.getRiskLevel()).isEqualTo("HIGH");
        assertThat(assessment.isDataSufficient()).isFalse();
        assertThat(assessment.getDataSufficiencyWarning()).contains("Limited historical repository data");
        assertThat(assessment.getGuardrailRecommendations()).isNotEmpty();

        // Verify entity persisted
        verify(riskRepo).save(any(PullRequestRiskPredictionEntity.class));
    }
}

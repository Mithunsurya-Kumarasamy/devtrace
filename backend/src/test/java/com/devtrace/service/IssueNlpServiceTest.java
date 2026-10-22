package com.devtrace.service;

import com.devtrace.dto.issuenlp.*;
import com.devtrace.entity.IssueEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.IssueEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import com.devtrace.service.issuenlp.IssueNlpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueNlpServiceTest {

    @Mock
    private RepositoryEntityRepository repositoryRepo;

    @Mock
    private IssueEntityRepository issueRepo;

    @InjectMocks
    private IssueNlpService issueNlpService;

    private RepositoryEntity testRepo;
    private final String repoId = "repo-nlp-test-123";

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity("devtrace-org", "test-nlp-repo");
        testRepo.setId(repoId);
    }

    @Test
    @DisplayName("analyzeIssuesNlp throws ResourceNotFoundException if repository does not exist")
    void testRepoNotFound() {
        when(repositoryRepo.existsById("non-existent")).thenReturn(false);

        assertThatThrownBy(() -> issueNlpService.analyzeIssuesNlp("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @DisplayName("analyzeIssuesNlp handles empty repository gracefully")
    void testEmptyIssues() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);
        when(issueRepo.findByRepositoryId(repoId)).thenReturn(Collections.emptyList());

        IssueNlpReportDto report = issueNlpService.analyzeIssuesNlp(repoId);

        assertThat(report).isNotNull();
        assertThat(report.getRepositoryId()).isEqualTo(repoId);
        assertThat(report.getTotalAnalyzedIssues()).isEqualTo(0);
        assertThat(report.getCategories()).hasSize(5);
        assertThat(report.getEvaluation().getAgreementRate()).isEqualTo(0.0);
        assertThat(report.getClassifiedIssuesSample()).isEmpty();
    }

    @Test
    @DisplayName("analyzeIssuesNlp classifies issues across 5 categories and calculates evaluation metrics")
    void testCategorizationAndEvaluation() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        Instant base = Instant.now().minus(20, ChronoUnit.DAYS);

        // 1. Bug / Defect (labeled 'bug')
        IssueEntity issueBug = new IssueEntity(testRepo, 1, "Fix nullpointer exception in auth parser", "closed", "alice");
        issueBug.setLabels("bug, backend");
        issueBug.setCreatedAt(base);
        issueBug.setClosedAt(base.plus(24, ChronoUnit.HOURS)); // 24h resolution

        // 2. Feature / Enhancement (labeled 'feature')
        IssueEntity issueFeat = new IssueEntity(testRepo, 2, "Add dark theme support for dashboard", "closed", "bob");
        issueFeat.setLabels("feature, ui");
        issueFeat.setCreatedAt(base.plus(2, ChronoUnit.DAYS));
        issueFeat.setClosedAt(base.plus(2, ChronoUnit.DAYS).plus(48, ChronoUnit.HOURS)); // 48h resolution

        // 3. Documentation (unlabeled, text only)
        IssueEntity issueDoc = new IssueEntity(testRepo, 3, "Update README guide and documentation", "open", "charlie");
        issueDoc.setCreatedAt(base.plus(5, ChronoUnit.DAYS));

        // 4. Tech Debt / Refactor (labeled 'refactor')
        IssueEntity issueRefactor = new IssueEntity(testRepo, 4, "Refactor database query optimization and clean legacy code", "closed", "dan");
        issueRefactor.setLabels("refactor, chore");
        issueRefactor.setCreatedAt(base.plus(8, ChronoUnit.DAYS));
        issueRefactor.setClosedAt(base.plus(8, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS));

        // 5. Question / Support (unlabeled, text only)
        IssueEntity issueQuestion = new IssueEntity(testRepo, 5, "How to configure kafka timeout and clarify consumer settings?", "open", "eve");
        issueQuestion.setCreatedAt(base.plus(12, ChronoUnit.DAYS));

        when(issueRepo.findByRepositoryId(repoId)).thenReturn(List.of(
                issueBug, issueFeat, issueDoc, issueRefactor, issueQuestion
        ));

        IssueNlpReportDto report = issueNlpService.analyzeIssuesNlp(repoId);

        assertThat(report.getTotalAnalyzedIssues()).isEqualTo(5);
        assertThat(report.getLabeledIssuesCount()).isEqualTo(3);
        assertThat(report.getUnlabeledIssuesCount()).isEqualTo(2);

        // Verify categories exist
        assertThat(report.getCategories()).hasSize(5);

        // Check Bug category metrics
        IssueCategorySummaryDto bugSummary = report.getCategories().stream()
                .filter(c -> "BUG_DEFECT".equals(c.getCategoryKey()))
                .findFirst()
                .orElse(null);
        assertThat(bugSummary).isNotNull();
        assertThat(bugSummary.getTotalIssues()).isGreaterThanOrEqualTo(1);
        assertThat(bugSummary.getMedianResolutionHours()).isEqualTo(24.0);

        // Check Evaluation metrics
        ClassificationEvaluationDto eval = report.getEvaluation();
        assertThat(eval.getTotalLabeledIssues()).isEqualTo(3);
        assertThat(eval.getAgreementRate()).isGreaterThan(0.0);
        assertThat(eval.getConfusionMatrix()).isNotEmpty();
        assertThat(eval.getCategoryMetrics()).hasSize(5);

        // Verify sample classification items
        assertThat(report.getClassifiedIssuesSample()).hasSize(5);
        IssueClassificationItemDto bugItem = report.getClassifiedIssuesSample().stream()
                .filter(i -> i.getIssueNumber() == 1)
                .findFirst()
                .orElse(null);
        assertThat(bugItem).isNotNull();
        assertThat(bugItem.getLabelCategory()).isEqualTo("BUG_DEFECT");
        assertThat(bugItem.getPredictedCategory()).isEqualTo("BUG_DEFECT");
        assertThat(bugItem.isAgreement()).isTrue();
    }
}

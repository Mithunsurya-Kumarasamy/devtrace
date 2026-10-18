package com.devtrace.service;

import com.devtrace.dto.churn.*;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.CommitFileEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import com.devtrace.service.churn.ChurnAnalyticsService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChurnAnalyticsServiceTest {

    @Mock
    private RepositoryEntityRepository repositoryRepo;

    @Mock
    private CommitFileEntityRepository commitFileRepo;

    @InjectMocks
    private ChurnAnalyticsService churnAnalyticsService;

    private final String repoId = "repo-churn-test-123";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("calculateChurn throws ResourceNotFoundException if repository does not exist")
    void testCalculateChurnRepoNotFound() {
        when(repositoryRepo.existsById("invalid-id")).thenReturn(false);

        assertThatThrownBy(() -> churnAnalyticsService.calculateChurn("invalid-id", 30))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @DisplayName("calculateChurn handles empty commits gracefully")
    void testCalculateChurnEmpty() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);
        when(commitFileRepo.findFileCommitDetailsSince(eq(repoId), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        RepositoryChurnReportDto report = churnAnalyticsService.calculateChurn(repoId, 30);

        assertThat(report).isNotNull();
        assertThat(report.getRepositoryId()).isEqualTo(repoId);
        assertThat(report.getTotalFilesModified()).isEqualTo(0);
        assertThat(report.getTotalAdditions()).isEqualTo(0);
        assertThat(report.getTotalDeletions()).isEqualTo(0);
        assertThat(report.getTotalChanges()).isEqualTo(0);
        assertThat(report.getNetCodeGrowth()).isEqualTo(0);
        assertThat(report.getTopModifiedFiles()).isEmpty();
        assertThat(report.getDirectoryChurn()).isEmpty();
    }

    @Test
    @DisplayName("calculateChurn calculates file and directory churn accurately")
    void testCalculateChurnWithData() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        Instant now = Instant.now();
        // Object[] row: [filename, sha, message, commitDate, authorName, additions, deletions]
        List<Object[]> rows = List.of(
                new Object[]{"src/main/App.java", "sha1", "feat: initial feature", now.minus(5, ChronoUnit.DAYS), "Alice", 100, 10},
                new Object[]{"src/main/App.java", "sha2", "fix: bug in app", now.minus(3, ChronoUnit.DAYS), "Bob", 30, 5},
                new Object[]{"src/utils/Helper.java", "sha3", "refactor: helper utils", now.minus(2, ChronoUnit.DAYS), "Alice", 40, 20},
                new Object[]{"README.md", "sha4", "docs: update readme", now.minus(1, ChronoUnit.DAYS), "Charlie", 15, 2}
        );

        when(commitFileRepo.findFileCommitDetailsSince(eq(repoId), any(Instant.class)))
                .thenReturn(rows);

        RepositoryChurnReportDto report = churnAnalyticsService.calculateChurn(repoId, 30);

        assertThat(report.getTotalFilesModified()).isEqualTo(3);
        assertThat(report.getTotalAdditions()).isEqualTo(185);
        assertThat(report.getTotalDeletions()).isEqualTo(37);
        assertThat(report.getTotalChanges()).isEqualTo(222);
        assertThat(report.getNetCodeGrowth()).isEqualTo(148);

        // Verify top modified files
        assertThat(report.getTopModifiedFiles()).hasSize(3);
        FileChurnDto topFile = report.getTopModifiedFiles().get(0);
        assertThat(topFile.getFilename()).isEqualTo("src/main/App.java");
        assertThat(topFile.getModificationCount()).isEqualTo(2);
        assertThat(topFile.getAdditions()).isEqualTo(130);
        assertThat(topFile.getDeletions()).isEqualTo(15);
        assertThat(topFile.getTotalChanges()).isEqualTo(145);
        assertThat(topFile.getDistinctAuthorsCount()).isEqualTo(2);
        assertThat(topFile.getDirectory()).isEqualTo("src/main");

        // Verify directory churn
        assertThat(report.getDirectoryChurn()).isNotEmpty();
        DirectoryChurnDto srcMainDir = report.getDirectoryChurn().stream()
                .filter(d -> "src/main".equals(d.getDirectoryPath()))
                .findFirst()
                .orElse(null);
        assertThat(srcMainDir).isNotNull();
        assertThat(srcMainDir.getFilesCount()).isEqualTo(1);
        assertThat(srcMainDir.getTotalModifications()).isEqualTo(2);
        assertThat(srcMainDir.getTotalChanges()).isEqualTo(145);
        assertThat(srcMainDir.getPercentageOfTotalChurn()).isGreaterThan(50.0);
    }

    @Test
    @DisplayName("calculateHotspots ranks files by churn, recency, and defect history")
    void testCalculateHotspots() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        Instant now = Instant.now();
        // File 1: High churn, multiple defect commits -> should be top hotspot
        // File 2: Feature commits only -> lower defect ratio
        List<Object[]> rows = List.of(
                new Object[]{"src/core/Engine.java", "sha1", "fix: null pointer defect in engine", now.minus(1, ChronoUnit.DAYS), "Alice", 120, 40},
                new Object[]{"src/core/Engine.java", "sha2", "bug: memory leak crash fix", now.minus(2, ChronoUnit.DAYS), "Alice", 90, 30},
                new Object[]{"src/core/Engine.java", "sha3", "fix: regression in parser", now.minus(4, ChronoUnit.DAYS), "Bob", 60, 20},
                new Object[]{"src/core/Engine.java", "sha4", "refactor engine loop", now.minus(6, ChronoUnit.DAYS), "Alice", 40, 10},
                new Object[]{"src/ui/Button.java", "sha5", "feat: add primary button component", now.minus(20, ChronoUnit.DAYS), "Charlie", 50, 5}
        );

        when(commitFileRepo.findFileCommitDetailsSince(eq(repoId), any(Instant.class)))
                .thenReturn(rows);

        CodebaseHotspotsReportDto report = churnAnalyticsService.calculateHotspots(repoId, 90, 10);

        assertThat(report.getTotalAnalyzedFiles()).isEqualTo(2);
        assertThat(report.getHotspots()).hasSize(2);

        HotspotFileDto topHotspot = report.getHotspots().get(0);
        assertThat(topHotspot.getFilename()).isEqualTo("src/core/Engine.java");
        assertThat(topHotspot.getModificationCount()).isEqualTo(4);
        assertThat(topHotspot.getDefectCommitCount()).isEqualTo(3);
        assertThat(topHotspot.getDefectRatio()).isEqualTo(0.75);
        assertThat(topHotspot.getPrimaryContributor()).isEqualTo("Alice");
        assertThat(topHotspot.getHotspotScore()).isGreaterThan(70.0);
        assertThat(topHotspot.getRiskLevel()).isIn("CRITICAL", "HIGH");
        assertThat(topHotspot.getExplanation()).contains("bug fixes");

        assertThat(report.getArchitecturalRecommendations()).isNotEmpty();
    }
}

package com.devtrace.service.analytics;

import com.devtrace.dto.analytics.*;
import com.devtrace.entity.*;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoreAnalyticsService {

    private final RepositoryEntityRepository repositoryRepo;
    private final CommitEntityRepository commitRepo;
    private final PullRequestEntityRepository pullRequestRepo;
    private final IssueEntityRepository issueRepo;
    private final ReleaseEntityRepository releaseRepo;

    public CoreAnalyticsService(
            RepositoryEntityRepository repositoryRepo,
            CommitEntityRepository commitRepo,
            PullRequestEntityRepository pullRequestRepo,
            IssueEntityRepository issueRepo,
            ReleaseEntityRepository releaseRepo) {
        this.repositoryRepo = repositoryRepo;
        this.commitRepo = commitRepo;
        this.pullRequestRepo = pullRequestRepo;
        this.issueRepo = issueRepo;
        this.releaseRepo = releaseRepo;
    }

    @Transactional(readOnly = true)
    public RepositoryOverviewDto getRepositoryOverview(String repositoryId, int days) {
        return getRepositoryOverview(repositoryId, days, null);
    }

    @Transactional(readOnly = true)
    public RepositoryOverviewDto getRepositoryOverview(String repositoryId, int days, String branch) {
        RepositoryEntity repo = repositoryRepo.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with ID: " + repositoryId));

        String effectiveBranch = (branch != null && !branch.isBlank()) ? branch.trim() : null;
        int windowDays = (days > 0 && days <= 365) ? days : 30;
        Instant since = Instant.now().minus(Duration.ofDays(windowDays));

        RepositoryOverviewDto overview = new RepositoryOverviewDto();
        overview.setRepositoryId(repo.getId());
        overview.setFullName(repo.getFullName());
        overview.setDefaultBranch(effectiveBranch != null ? effectiveBranch : repo.getDefaultBranch());
        overview.setSyncStatus(repo.getSyncStatus());
        overview.setLastSyncedAt(repo.getLastSyncedAt() != null ? repo.getLastSyncedAt().toString() : null);
        overview.setTimeWindowDays(windowDays);

        // Branch-filtered counts
        overview.setTotalCommits(commitRepo.countByRepositoryIdAndBranch(repositoryId, effectiveBranch));
        overview.setTotalPullRequests(pullRequestRepo.countByRepositoryId(repositoryId));
        overview.setOpenPullRequests((int) pullRequestRepo.countByRepositoryIdAndState(repositoryId, "open"));
        overview.setTotalIssues(issueRepo.countByRepositoryId(repositoryId));
        overview.setOpenIssues((int) issueRepo.countByRepositoryIdAndState(repositoryId, "open"));
        overview.setTotalReleases(releaseRepo.countByRepositoryId(repositoryId));
        overview.setActiveContributors(commitRepo.countDistinctAuthorsByRepositoryIdAndBranch(repositoryId, effectiveBranch));

        // Pull Requests in window
        List<PullRequestEntity> allPrs = pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repositoryId);
        List<PullRequestEntity> mergedPrs = allPrs.stream().filter(PullRequestEntity::isMerged).toList();
        overview.setMergedPullRequests(mergedPrs.size());

        // 1. PR Cycle Time Distribution (hours)
        DescriptiveStatistics cycleStats = new DescriptiveStatistics();
        for (PullRequestEntity pr : mergedPrs) {
            if (pr.getCreatedAt() != null && pr.getMergedAt() != null) {
                double hours = Math.max(0.0, (pr.getMergedAt().getEpochSecond() - pr.getCreatedAt().getEpochSecond()) / 3600.0);
                cycleStats.addValue(hours);
            }
        }
        overview.setPrCycleTimeHours(calculateDistribution(cycleStats));

        // 2. Review Waiting Time Distribution (hours: created_at to first_review_at)
        DescriptiveStatistics waitStats = new DescriptiveStatistics();
        for (PullRequestEntity pr : allPrs) {
            if (pr.getCreatedAt() != null && pr.getFirstReviewAt() != null) {
                double hours = Math.max(0.0, (pr.getFirstReviewAt().getEpochSecond() - pr.getCreatedAt().getEpochSecond()) / 3600.0);
                waitStats.addValue(hours);
            }
        }
        overview.setReviewWaitingTimeHours(calculateDistribution(waitStats));

        // 3. Transparent Engineering Health Score
        overview.setHealthScore(calculateHealthScore(
                allPrs,
                cycleStats.getN() > 0 ? cycleStats.getPercentile(50) : 0.0,
                waitStats.getN() > 0 ? waitStats.getPercentile(50) : 0.0,
                overview.getTotalReleases(),
                overview.getActiveContributors(),
                issueRepo.findByRepositoryId(repositoryId)
        ));

        // 4. Activity Trends for recent window (daily breakdown)
        overview.setRecentActivityTrends(calculateActivityTrends(repositoryId, Math.min(windowDays, 30), effectiveBranch));

        return overview;
    }

    @Transactional(readOnly = true)
    public Page<CommitSummaryDto> getCommits(String repositoryId, int page, int size) {
        return getCommits(repositoryId, page, size, null);
    }

    @Transactional(readOnly = true)
    public Page<CommitSummaryDto> getCommits(String repositoryId, int page, int size, String branch) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        if (branch != null && !branch.isBlank()) {
            return commitRepo.findByRepositoryIdAndBranchOrderByCommitDateDesc(repositoryId, branch.trim(), pageable)
                    .map(CommitSummaryDto::fromEntity);
        }
        return commitRepo.findByRepositoryIdOrderByCommitDateDesc(repositoryId, pageable)
                .map(CommitSummaryDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PullRequestSummaryDto> getPullRequests(String repositoryId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return pullRequestRepo.findByRepositoryIdOrderByCreatedAtDesc(repositoryId, pageable)
                .map(PullRequestSummaryDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<IssueSummaryDto> getIssues(String repositoryId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return issueRepo.findByRepositoryIdOrderByCreatedAtDesc(repositoryId, pageable)
                .map(IssueSummaryDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ReleaseEntity> getReleases(String repositoryId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return releaseRepo.findByRepositoryIdOrderByPublishedAtDesc(repositoryId, pageable);
    }

    private PercentileDistributionDto calculateDistribution(DescriptiveStatistics stats) {
        if (stats.getN() == 0) {
            return new PercentileDistributionDto(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        return new PercentileDistributionDto(
                round(stats.getMin()),
                round(stats.getPercentile(50)),
                round(stats.getPercentile(75)),
                round(stats.getPercentile(90)),
                round(stats.getMax()),
                round(stats.getMean())
        );
    }

    private HealthScoreDto calculateHealthScore(
            List<PullRequestEntity> prs,
            double medianCycleHours,
            double medianWaitHours,
            long releasesCount,
            long contributorsCount,
            List<IssueEntity> issues) {

        // 1. Code Stability: ratio of bug issues to total activity
        long bugCount = issues.stream().filter(i -> i.getCategory() != null && (i.getCategory().toUpperCase().contains("BUG") || i.getCategory().toUpperCase().contains("DEFECT"))).count();
        double bugRatio = issues.isEmpty() ? 0.0 : (double) bugCount / issues.size();
        double codeStability = Math.max(20.0, Math.min(100.0, 100.0 - (bugRatio * 60.0)));

        // 2. Review Efficiency: based on review wait time
        double reviewEfficiency;
        if (medianWaitHours <= 0.0 && prs.isEmpty()) {
            reviewEfficiency = 75.0; // Baseline when no PRs exist
        } else if (medianWaitHours <= 4.0) {
            reviewEfficiency = 95.0;
        } else if (medianWaitHours <= 24.0) {
            reviewEfficiency = 85.0;
        } else if (medianWaitHours <= 72.0) {
            reviewEfficiency = 65.0;
        } else {
            reviewEfficiency = 40.0;
        }

        // 3. Release Cadence: regularity of publication
        double releaseCadence = releasesCount >= 3 ? 95.0 : (releasesCount >= 1 ? 80.0 : 55.0);

        // 4. Collaboration Resilience: bus factor proxy & contributor breadth
        double collaborationResilience;
        if (contributorsCount >= 5) {
            collaborationResilience = 90.0;
        } else if (contributorsCount >= 2) {
            collaborationResilience = 75.0;
        } else {
            collaborationResilience = 50.0;
        }

        // Composite Weighted Score
        double composite = (0.30 * codeStability) + (0.30 * reviewEfficiency) + (0.20 * releaseCadence) + (0.20 * collaborationResilience);

        String explanation = "Composite engineering health weighted: 30% code stability (bug ratio), " +
                "30% review efficiency (median review wait), 20% release cadence, 20% collaboration resilience.";

        return new HealthScoreDto(
                round(composite),
                round(codeStability),
                round(reviewEfficiency),
                round(releaseCadence),
                round(collaborationResilience),
                explanation
        );
    }

    private List<ActivityTrendDto> calculateActivityTrends(String repositoryId, int days, String branch) {
        Map<String, ActivityTrendDto> map = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        // Initialize zero entries for chronological consistency
        for (int i = days - 1; i >= 0; i--) {
            String dateStr = today.minusDays(i).toString();
            map.put(dateStr, new ActivityTrendDto(dateStr));
        }

        Instant since = today.minusDays(days).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant until = Instant.now();

        List<CommitEntity> commits = commitRepo.findByRepositoryIdAndBranchAndCommitDateBetweenOrderByCommitDateAsc(repositoryId, branch, since, until);
        for (CommitEntity c : commits) {
            String d = c.getCommitDate().atZone(ZoneOffset.UTC).toLocalDate().toString();
            ActivityTrendDto dto = map.get(d);
            if (dto != null) {
                dto.setCommitCount(dto.getCommitCount() + 1);
                dto.setLinesAdded(dto.getLinesAdded() + c.getAdditions());
                dto.setLinesDeleted(dto.getLinesDeleted() + c.getDeletions());
            }
        }

        return new ArrayList<>(map.values());
    }

    private double round(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}

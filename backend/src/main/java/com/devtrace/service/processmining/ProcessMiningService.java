package com.devtrace.service.processmining;

import com.devtrace.dto.processmining.*;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.PullRequestReviewEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.PullRequestEntityRepository;
import com.devtrace.repository.PullRequestReviewEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProcessMiningService {

    private final RepositoryEntityRepository repositoryEntityRepository;
    private final PullRequestEntityRepository pullRequestRepository;
    private final PullRequestReviewEntityRepository reviewRepository;

    public ProcessMiningService(RepositoryEntityRepository repositoryEntityRepository,
                                PullRequestEntityRepository pullRequestRepository,
                                PullRequestReviewEntityRepository reviewRepository) {
        this.repositoryEntityRepository = repositoryEntityRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.reviewRepository = reviewRepository;
    }

    public ProcessMiningReportDto analyzeProcessMining(String repositoryId) {
        if (!repositoryEntityRepository.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }

        List<PullRequestEntity> pullRequests = pullRequestRepository.findByRepositoryIdOrderByCreatedAtAsc(repositoryId);
        List<PullRequestReviewEntity> allReviews = reviewRepository.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repositoryId);

        // Group reviews by PR ID
        Map<String, List<PullRequestReviewEntity>> reviewsByPrId = allReviews.stream()
                .filter(r -> r.getPullRequest() != null)
                .collect(Collectors.groupingBy(r -> r.getPullRequest().getId()));

        ProcessMiningReportDto report = new ProcessMiningReportDto();
        report.setRepositoryId(repositoryId);
        report.setTotalAnalyzedPrs(pullRequests.size());

        // Limitations documentation
        report.getMissingDataLimitations().add("Direct Merges: Pull requests merged directly without review have no review timestamps and are excluded from review-wait stage percentiles.");
        report.getMissingDataLimitations().add("Unmerged / Abandoned PRs: Closed pull requests without merge lack merge timestamps; cycle time is measured from creation to close.");
        report.getMissingDataLimitations().add("In-Flight PRs: Open pull requests have no completion timestamps; in-progress wait times are calculated relative to the current analysis time.");
        report.getMissingDataLimitations().add("Revisions & Force-Pushes: Review timestamps capture the latest review submissions; squashed intermediate commits are traced via timeline events.");

        if (pullRequests.isEmpty()) {
            report.getMissingDataLimitations().add("No pull requests have been ingested yet for this repository.");
            report.setStages(createEmptyStages());
            report.setBottleneck(new BottleneckAnalysisDto("NONE", "None", 0.0, 0.0, "LOW",
                    "Insufficient pull request history to identify process bottlenecks.",
                    "Ingest pull request data by triggering a repository sync."));
            report.setVariants(createEmptyVariants());
            report.setChangesRequestedSummary(new ChangesRequestedSummaryDto(0, 0.0, 0.0, 0));
            return report;
        }

        int mergedCount = 0;
        int closedUnmergedCount = 0;
        int openCount = 0;

        List<Double> creationToReviewDurations = new ArrayList<>();
        List<Double> reviewToApprovalDurations = new ArrayList<>();
        List<Double> approvalToMergeDurations = new ArrayList<>();
        List<Double> totalCycleDurations = new ArrayList<>();

        int prsWithChangesRequested = 0;
        int totalChangesRequestedCount = 0;
        int maxChangesRequested = 0;

        List<PullRequestLifecycleTraceDto> traces = new ArrayList<>();
        Map<String, List<Double>> variantCycleTimes = new HashMap<>();
        for (String varKey : List.of("FAST_TRACK", "STANDARD", "REVIEW_HEAVY", "STALE_REVIEW", "ABANDONED")) {
            variantCycleTimes.put(varKey, new ArrayList<>());
        }

        Instant now = Instant.now();

        for (PullRequestEntity pr : pullRequests) {
            List<PullRequestReviewEntity> reviews = reviewsByPrId.getOrDefault(pr.getId(), Collections.emptyList());

            if (pr.isMerged()) {
                mergedCount++;
            } else if (pr.getClosedAt() != null) {
                closedUnmergedCount++;
            } else {
                openCount++;
            }

            // Determine effective timestamps
            Instant firstReviewAt = pr.getFirstReviewAt();
            if (firstReviewAt == null && !reviews.isEmpty()) {
                firstReviewAt = reviews.stream()
                        .map(PullRequestReviewEntity::getSubmittedAt)
                        .filter(Objects::nonNull)
                        .min(Instant::compareTo)
                        .orElse(null);
            }

            Instant approvedAt = pr.getApprovedAt();
            if (approvedAt == null && !reviews.isEmpty()) {
                approvedAt = reviews.stream()
                        .filter(r -> "APPROVED".equalsIgnoreCase(r.getState()) || "APPROVE".equalsIgnoreCase(r.getState()))
                        .map(PullRequestReviewEntity::getSubmittedAt)
                        .filter(Objects::nonNull)
                        .min(Instant::compareTo)
                        .orElse(null);
            }

            // Changes requested count
            int changesRequested = (int) reviews.stream()
                    .filter(r -> "CHANGES_REQUESTED".equalsIgnoreCase(r.getState()))
                    .count();
            if (changesRequested > 0) {
                prsWithChangesRequested++;
                totalChangesRequestedCount += changesRequested;
                if (changesRequested > maxChangesRequested) {
                    maxChangesRequested = changesRequested;
                }
            }

            // Durations
            Double timeToFirstReview = null;
            if (firstReviewAt != null && pr.getCreatedAt() != null && !firstReviewAt.isBefore(pr.getCreatedAt())) {
                timeToFirstReview = round(Duration.between(pr.getCreatedAt(), firstReviewAt).toMillis() / 3600000.0);
                creationToReviewDurations.add(timeToFirstReview);
            }

            Double timeFirstReviewToApproval = null;
            if (firstReviewAt != null && approvedAt != null && !approvedAt.isBefore(firstReviewAt)) {
                timeFirstReviewToApproval = round(Duration.between(firstReviewAt, approvedAt).toMillis() / 3600000.0);
                reviewToApprovalDurations.add(timeFirstReviewToApproval);
            }

            Double timeApprovalToMerge = null;
            if (approvedAt != null && pr.getMergedAt() != null && !pr.getMergedAt().isBefore(approvedAt)) {
                timeApprovalToMerge = round(Duration.between(approvedAt, pr.getMergedAt()).toMillis() / 3600000.0);
                approvalToMergeDurations.add(timeApprovalToMerge);
            }

            // Total cycle time
            Double totalCycle = null;
            if (pr.isMerged() && pr.getMergedAt() != null && pr.getCreatedAt() != null) {
                totalCycle = round(Duration.between(pr.getCreatedAt(), pr.getMergedAt()).toMillis() / 3600000.0);
                totalCycleDurations.add(totalCycle);
            } else if (pr.getClosedAt() != null && pr.getCreatedAt() != null) {
                totalCycle = round(Duration.between(pr.getCreatedAt(), pr.getClosedAt()).toMillis() / 3600000.0);
                totalCycleDurations.add(totalCycle);
            } else if (pr.getCreatedAt() != null) {
                totalCycle = round(Duration.between(pr.getCreatedAt(), now).toMillis() / 3600000.0);
            }

            // Classify process variant
            String variantKey;
            if (!pr.isMerged() && pr.getClosedAt() != null) {
                variantKey = "ABANDONED";
            } else if ((timeToFirstReview != null && timeToFirstReview > 72.0)
                    || ("open".equalsIgnoreCase(pr.getState()) && totalCycle != null && totalCycle > 72.0 && firstReviewAt == null)) {
                variantKey = "STALE_REVIEW";
            } else if (changesRequested > 0 || reviews.size() >= 3) {
                variantKey = "REVIEW_HEAVY";
            } else if (pr.isMerged() && totalCycle != null && totalCycle <= 12.0 && reviews.size() <= 1 && changesRequested == 0) {
                variantKey = "FAST_TRACK";
            } else {
                variantKey = "STANDARD";
            }

            if (totalCycle != null) {
                variantCycleTimes.get(variantKey).add(totalCycle);
            }

            // Trace item
            PullRequestLifecycleTraceDto trace = new PullRequestLifecycleTraceDto();
            trace.setId(pr.getId());
            trace.setPrNumber(pr.getPrNumber());
            trace.setTitle(pr.getTitle());
            trace.setAuthorLogin(pr.getAuthorLogin());
            trace.setState(pr.getState());
            trace.setMerged(pr.isMerged());
            trace.setCreatedAt(pr.getCreatedAt());
            trace.setFirstReviewAt(firstReviewAt);
            trace.setApprovedAt(approvedAt);
            trace.setMergedAt(pr.getMergedAt());
            trace.setClosedAt(pr.getClosedAt());
            trace.setTimeToFirstReviewHours(timeToFirstReview);
            trace.setTimeFirstReviewToApprovalHours(timeFirstReviewToApproval);
            trace.setTimeApprovalToMergeHours(timeApprovalToMerge);
            trace.setTotalCycleTimeHours(totalCycle);
            trace.setReviewCount(reviews.size());
            trace.setChangesRequestedCount(changesRequested);
            trace.setVariant(variantKey);
            traces.add(trace);
        }

        report.setMergedPrsCount(mergedCount);
        report.setClosedUnmergedPrsCount(closedUnmergedCount);
        report.setOpenPrsCount(openCount);
        report.setMedianTotalCycleTimeHours(calculateMedian(totalCycleDurations));

        // Changes requested summary
        double pctWithChanges = pullRequests.isEmpty() ? 0.0 : round((prsWithChangesRequested * 100.0) / pullRequests.size());
        double avgIterations = prsWithChangesRequested == 0 ? 0.0 : round((double) totalChangesRequestedCount / prsWithChangesRequested);
        report.setChangesRequestedSummary(new ChangesRequestedSummaryDto(prsWithChangesRequested, pctWithChanges, avgIterations, maxChangesRequested));

        // Build stages
        double medianCreationToReview = calculateMedian(creationToReviewDurations);
        double medianReviewToApproval = calculateMedian(reviewToApprovalDurations);
        double medianApprovalToMerge = calculateMedian(approvalToMergeDurations);

        double sumMedians = medianCreationToReview + medianReviewToApproval + medianApprovalToMerge;
        double pctStage1 = sumMedians > 0 ? round((medianCreationToReview / sumMedians) * 100.0) : 0.0;
        double pctStage2 = sumMedians > 0 ? round((medianReviewToApproval / sumMedians) * 100.0) : 0.0;
        double pctStage3 = sumMedians > 0 ? round((medianApprovalToMerge / sumMedians) * 100.0) : 0.0;

        ProcessMiningStageDto stage1 = new ProcessMiningStageDto(
                "CREATION_TO_FIRST_REVIEW",
                "PR Creation to First Review",
                "Wait duration between PR submission and the initial review activity.",
                medianCreationToReview,
                calculateP75(creationToReviewDurations),
                calculateP90(creationToReviewDurations),
                calculateAverage(creationToReviewDurations),
                pctStage1,
                creationToReviewDurations.size(),
                false
        );

        ProcessMiningStageDto stage2 = new ProcessMiningStageDto(
                "FIRST_REVIEW_TO_APPROVAL",
                "First Review to Approval",
                "Duration spent addressing review comments, revisions, and obtaining approval.",
                medianReviewToApproval,
                calculateP75(reviewToApprovalDurations),
                calculateP90(reviewToApprovalDurations),
                calculateAverage(reviewToApprovalDurations),
                pctStage2,
                reviewToApprovalDurations.size(),
                false
        );

        ProcessMiningStageDto stage3 = new ProcessMiningStageDto(
                "APPROVAL_TO_MERGE",
                "Approval to Merge",
                "Wait latency from final approval until the PR is merged into base branch.",
                medianApprovalToMerge,
                calculateP75(approvalToMergeDurations),
                calculateP90(approvalToMergeDurations),
                calculateAverage(approvalToMergeDurations),
                pctStage3,
                approvalToMergeDurations.size(),
                false
        );

        // Identify bottleneck
        ProcessMiningStageDto bottleneckStage = stage1;
        if (stage2.getPercentageOfTotalCycle() > bottleneckStage.getPercentageOfTotalCycle()) {
            bottleneckStage = stage2;
        }
        if (stage3.getPercentageOfTotalCycle() > bottleneckStage.getPercentageOfTotalCycle()) {
            bottleneckStage = stage3;
        }

        if (sumMedians > 0) {
            bottleneckStage.setBottleneck(true);
        }

        report.setStages(List.of(stage1, stage2, stage3));
        report.setBottleneck(buildBottleneckAnalysis(bottleneckStage, sumMedians));

        // Build Process Variants
        List<ProcessVariantDto> variants = new ArrayList<>();
        int totalPrCount = pullRequests.size();

        Map<String, String> variantNames = Map.of(
                "FAST_TRACK", "Fast Track",
                "STANDARD", "Standard Workflow",
                "REVIEW_HEAVY", "Review Heavy / Multi-Iteration",
                "STALE_REVIEW", "Stale / Delayed Review",
                "ABANDONED", "Abandoned / Closed Unmerged"
        );

        Map<String, String> variantDescriptions = Map.of(
                "FAST_TRACK", "Quickly merged pull requests with rapid turnaround (<= 12 hours) and minimal review friction.",
                "STANDARD", "Normal pull requests following standard review, approval, and merge lifecycle.",
                "REVIEW_HEAVY", "Pull requests requiring multiple review iterations, revisions, or changes requested.",
                "STALE_REVIEW", "Pull requests experiencing prolonged review queue latency (> 72 hours) or stalled progress.",
                "ABANDONED", "Pull requests closed without being merged into the target branch."
        );

        for (String varKey : List.of("FAST_TRACK", "STANDARD", "REVIEW_HEAVY", "STALE_REVIEW", "ABANDONED")) {
            List<Double> times = variantCycleTimes.get(varKey);
            int count = times.size();
            double pct = totalPrCount == 0 ? 0.0 : round((count * 100.0) / totalPrCount);
            double medCycle = calculateMedian(times);
            variants.add(new ProcessVariantDto(
                    varKey,
                    variantNames.get(varKey),
                    count,
                    pct,
                    medCycle,
                    variantDescriptions.get(varKey)
            ));
        }
        report.setVariants(variants);

        // Sort traces descending by creation time and take top 25
        traces.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        report.setRecentTraces(traces.stream().limit(25).collect(Collectors.toList()));

        return report;
    }

    private BottleneckAnalysisDto buildBottleneckAnalysis(ProcessMiningStageDto stage, double sumMedians) {
        if (sumMedians <= 0.0) {
            return new BottleneckAnalysisDto("NONE", "None", 0.0, 0.0, "LOW",
                    "All stages show zero or instant cycle time in the current dataset.",
                    "Continue ingesting pull requests to capture historical distribution.");
        }

        String severity;
        if (stage.getPercentageOfTotalCycle() >= 60.0) {
            severity = "CRITICAL";
        } else if (stage.getPercentageOfTotalCycle() >= 45.0) {
            severity = "HIGH";
        } else if (stage.getPercentageOfTotalCycle() >= 30.0) {
            severity = "MEDIUM";
        } else {
            severity = "LOW";
        }

        String impactSummary;
        String recommendation;

        switch (stage.getStageKey()) {
            case "CREATION_TO_FIRST_REVIEW":
                impactSummary = String.format("Pull requests spend %.1f%% of their active cycle (median %.1fh) waiting in the queue for an initial review.",
                        stage.getPercentageOfTotalCycle(), stage.getMedianDurationHours());
                recommendation = "Adopt automated PR reviewer rotation, enforce a 24-hour first-response SLA, and encourage smaller pull requests to lower reviewer cognitive barrier.";
                break;
            case "FIRST_REVIEW_TO_APPROVAL":
                impactSummary = String.format("Pull requests spend %.1f%% of their active cycle (median %.1fh) undergoing revision iterations, changes requested, and discussions.",
                        stage.getPercentageOfTotalCycle(), stage.getMedianDurationHours());
                recommendation = "Establish clearer PR contribution guidelines, run pre-commit linters locally to catch trivial feedback early, and facilitate synchronous pairing for complex changes.";
                break;
            case "APPROVAL_TO_MERGE":
                impactSummary = String.format("Pull requests spend %.1f%% of their active cycle (median %.1fh) waiting to be merged after already receiving approval.",
                        stage.getPercentageOfTotalCycle(), stage.getMedianDurationHours());
                recommendation = "Enable GitHub auto-merge for passing builds, optimize CI execution pipelines, and streamline release/branch gating policies.";
                break;
            default:
                impactSummary = "Cycle time is evenly distributed across stages.";
                recommendation = "Maintain current balanced review workflows across the team.";
                break;
        }

        return new BottleneckAnalysisDto(
                stage.getStageKey(),
                stage.getStageName(),
                stage.getPercentageOfTotalCycle(),
                stage.getMedianDurationHours(),
                severity,
                impactSummary,
                recommendation
        );
    }

    private List<ProcessMiningStageDto> createEmptyStages() {
        return List.of(
                new ProcessMiningStageDto("CREATION_TO_FIRST_REVIEW", "PR Creation to First Review", "Wait duration between PR submission and initial review.", 0.0, 0.0, 0.0, 0.0, 0.0, 0, false),
                new ProcessMiningStageDto("FIRST_REVIEW_TO_APPROVAL", "First Review to Approval", "Duration spent addressing review feedback until approval.", 0.0, 0.0, 0.0, 0.0, 0.0, 0, false),
                new ProcessMiningStageDto("APPROVAL_TO_MERGE", "Approval to Merge", "Wait latency from approval to merge.", 0.0, 0.0, 0.0, 0.0, 0.0, 0, false)
        );
    }

    private List<ProcessVariantDto> createEmptyVariants() {
        return List.of(
                new ProcessVariantDto("FAST_TRACK", "Fast Track", 0, 0.0, 0.0, "Quickly merged pull requests."),
                new ProcessVariantDto("STANDARD", "Standard Workflow", 0, 0.0, 0.0, "Standard PR review and merge lifecycle."),
                new ProcessVariantDto("REVIEW_HEAVY", "Review Heavy / Multi-Iteration", 0, 0.0, 0.0, "Multi-iteration pull requests undergoing rework."),
                new ProcessVariantDto("STALE_REVIEW", "Stale / Delayed Review", 0, 0.0, 0.0, "Pull requests experiencing prolonged review queue latency."),
                new ProcessVariantDto("ABANDONED", "Abandoned / Closed Unmerged", 0, 0.0, 0.0, "Pull requests closed without being merged.")
        );
    }

    private double calculateMedian(List<Double> values) {
        return calculatePercentile(values, 50.0);
    }

    private double calculateP75(List<Double> values) {
        return calculatePercentile(values, 75.0);
    }

    private double calculateP90(List<Double> values) {
        return calculatePercentile(values, 90.0);
    }

    private double calculatePercentile(List<Double> values, double percentile) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        if (sorted.size() == 1) {
            return round(sorted.get(0));
        }

        double rank = (percentile / 100.0) * (sorted.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return round(sorted.get(lowerIndex));
        }

        double weight = rank - lowerIndex;
        double value = sorted.get(lowerIndex) + weight * (sorted.get(upperIndex) - sorted.get(lowerIndex));
        return round(value);
    }

    private double calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return round(avg);
    }

    private double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

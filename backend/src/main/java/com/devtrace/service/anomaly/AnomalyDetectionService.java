package com.devtrace.service.anomaly;

import com.devtrace.dto.anomaly.*;
import com.devtrace.entity.*;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnomalyDetectionService {

    private final RepositoryEntityRepository repositoryRepo;
    private final CommitEntityRepository commitRepo;
    private final PullRequestEntityRepository pullRequestRepo;
    private final AnomalyRecordEntityRepository anomalyRepo;

    public AnomalyDetectionService(RepositoryEntityRepository repositoryRepo,
                                  CommitEntityRepository commitRepo,
                                  PullRequestEntityRepository pullRequestRepo,
                                  AnomalyRecordEntityRepository anomalyRepo) {
        this.repositoryRepo = repositoryRepo;
        this.commitRepo = commitRepo;
        this.pullRequestRepo = pullRequestRepo;
        this.anomalyRepo = anomalyRepo;
    }

    @Transactional
    public AnomalyDetectionReportDto detectAndReportAnomalies(String repositoryId, int windowDays) {
        RepositoryEntity repo = repositoryRepo.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with ID: " + repositoryId));

        int days = Math.max(7, windowDays);
        Instant now = Instant.now();
        Instant start = now.minus(days, ChronoUnit.DAYS);

        // Fetch activity data
        List<CommitEntity> commits = commitRepo.findByRepositoryIdAndCommitDateBetweenOrderByCommitDateAsc(repositoryId, start, now);
        List<PullRequestEntity> pullRequests = pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repositoryId);

        List<MetricBaselineDto> baselines = new ArrayList<>();
        List<AnomalyRecordEntity> detectedEntities = new ArrayList<>();

        // 1. Metric: COMMIT_VOLUME (daily commits)
        Map<LocalDate, Long> dailyCommitCounts = new HashMap<>();
        Map<LocalDate, Long> dailyChurnVolumes = new HashMap<>();

        // Initialize every day in range to 0
        LocalDate startDate = start.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate endDate = now.atZone(ZoneOffset.UTC).toLocalDate();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dailyCommitCounts.put(d, 0L);
            dailyChurnVolumes.put(d, 0L);
        }

        for (CommitEntity c : commits) {
            LocalDate d = c.getCommitDate().atZone(ZoneOffset.UTC).toLocalDate();
            dailyCommitCounts.put(d, dailyCommitCounts.getOrDefault(d, 0L) + 1);
            long churn = (long) c.getAdditions() + c.getDeletions();
            dailyChurnVolumes.put(d, dailyChurnVolumes.getOrDefault(d, 0L) + churn);
        }

        List<Double> commitVolumeSeries = dailyCommitCounts.values().stream().map(Long::doubleValue).collect(Collectors.toList());
        MetricBaselineDto commitVolumeBaseline = calculateBaseline("COMMIT_VOLUME", commitVolumeSeries);
        baselines.add(commitVolumeBaseline);
        detectDailyAnomalies(repo, "COMMIT_VOLUME", dailyCommitCounts, commitVolumeBaseline, detectedEntities, "commits/day");

        // 2. Metric: CHURN_VOLATILITY (daily code changes)
        List<Double> churnVolumeSeries = dailyChurnVolumes.values().stream().map(Long::doubleValue).collect(Collectors.toList());
        MetricBaselineDto churnBaseline = calculateBaseline("CHURN_VOLATILITY", churnVolumeSeries);
        baselines.add(churnBaseline);
        detectDailyAnomalies(repo, "CHURN_VOLATILITY", dailyChurnVolumes, churnBaseline, detectedEntities, "lines changed/day");

        // 3. Metric: PR_SIZE (lines changed per PR)
        List<Double> prSizeSeries = new ArrayList<>();
        List<Double> reviewTurnaroundSeries = new ArrayList<>();

        for (PullRequestEntity pr : pullRequests) {
            double size = pr.getAdditions() + pr.getDeletions();
            prSizeSeries.add(size);

            if (pr.getFirstReviewAt() != null && pr.getCreatedAt() != null && !pr.getFirstReviewAt().isBefore(pr.getCreatedAt())) {
                double turnaroundHours = Duration.between(pr.getCreatedAt(), pr.getFirstReviewAt()).toMillis() / 3600000.0;
                reviewTurnaroundSeries.add(turnaroundHours);
            }
        }

        MetricBaselineDto prSizeBaseline = calculateBaseline("PR_SIZE", prSizeSeries);
        baselines.add(prSizeBaseline);
        detectPrSizeAnomalies(repo, pullRequests, prSizeBaseline, detectedEntities);

        // 4. Metric: REVIEW_TURNAROUND (hours to first review)
        MetricBaselineDto reviewTurnaroundBaseline = calculateBaseline("REVIEW_TURNAROUND", reviewTurnaroundSeries);
        baselines.add(reviewTurnaroundBaseline);
        detectReviewTurnaroundAnomalies(repo, pullRequests, reviewTurnaroundBaseline, detectedEntities);

        // Persist newly detected anomalies
        if (!detectedEntities.isEmpty()) {
            anomalyRepo.saveAll(detectedEntities);
        }

        // Retrieve existing & new anomalies for report
        List<AnomalyRecordEntity> allAnomalies = anomalyRepo.findByRepositoryIdOrderByDetectionTimestampDesc(repositoryId);

        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;

        List<AnomalyRecordDto> anomalyDtos = new ArrayList<>();
        for (AnomalyRecordEntity a : allAnomalies) {
            switch (a.getSeverity()) {
                case "CRITICAL": critical++; break;
                case "HIGH": high++; break;
                case "MEDIUM": medium++; break;
                default: low++; break;
            }
            anomalyDtos.add(new AnomalyRecordDto(
                    a.getId(),
                    a.getMetricName(),
                    round(a.getObservedValue()),
                    round(a.getExpectedLower()),
                    round(a.getExpectedUpper()),
                    a.getBaselineMean() != null ? round(a.getBaselineMean()) : null,
                    a.getSeverity(),
                    a.getDetectionMethod(),
                    a.getExplanation(),
                    a.getDetectionTimestamp()
            ));
        }

        AnomalyDetectionReportDto report = new AnomalyDetectionReportDto();
        report.setRepositoryId(repositoryId);
        report.setTotalAnomaliesCount(allAnomalies.size());
        report.setCriticalCount(critical);
        report.setHighCount(high);
        report.setMediumCount(medium);
        report.setLowCount(low);
        report.setBaselines(baselines);
        report.setRecentAnomalies(anomalyDtos.stream().limit(50).collect(Collectors.toList()));

        return report;
    }

    private MetricBaselineDto calculateBaseline(String metricName, List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new MetricBaselineDto(metricName, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0);
        }

        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();

        double sum = 0.0;
        for (double v : sorted) sum += v;
        double mean = sum / n;

        double varSum = 0.0;
        for (double v : sorted) {
            varSum += Math.pow(v - mean, 2);
        }
        double stdDev = Math.sqrt(varSum / n);

        double q1 = percentile(sorted, 25.0);
        double median = percentile(sorted, 50.0);
        double q3 = percentile(sorted, 75.0);
        double iqr = q3 - q1;

        double lowerBound = Math.max(0.0, q1 - 1.5 * iqr);
        double upperBound = q3 + 1.5 * iqr;

        return new MetricBaselineDto(
                metricName,
                round(mean),
                round(stdDev),
                round(median),
                round(q1),
                round(q3),
                round(iqr),
                round(lowerBound),
                round(upperBound),
                n
        );
    }

    private void detectDailyAnomalies(RepositoryEntity repo, String metricName,
                                      Map<LocalDate, Long> dailyData, MetricBaselineDto baseline,
                                      List<AnomalyRecordEntity> detected, String unit) {
        if (baseline.getSampleCount() < 5 || baseline.getStandardDeviation() <= 0.0) return;

        double upperBound = baseline.getUpperBound();
        double q3 = baseline.getQ3();
        double iqr = baseline.getIqr();
        double mean = baseline.getMean();
        double stdDev = baseline.getStandardDeviation();

        for (Map.Entry<LocalDate, Long> entry : dailyData.entrySet()) {
            double val = entry.getValue().doubleValue();
            if (val > upperBound && val > 0) {
                double z = (val - mean) / stdDev;
                String severity;
                if (z >= 3.5 || val > (q3 + 3.0 * iqr)) {
                    severity = "CRITICAL";
                } else if (z >= 2.5 || val > (q3 + 2.0 * iqr)) {
                    severity = "HIGH";
                } else {
                    severity = "MEDIUM";
                }

                String explanation = String.format("On %s, %s of %.0f %s exceeded the upper expected bound of %.1f %s (Z-score: +%.2f). Sudden activity spike indicates batch operation or unusual volatility.",
                        entry.getKey(), metricName, val, unit, upperBound, unit, z);

                detected.add(new AnomalyRecordEntity(
                        repo, metricName, val, baseline.getLowerBound(), upperBound, severity, "IQR_Z_SCORE", explanation
                ));
            }
        }
    }

    private void detectPrSizeAnomalies(RepositoryEntity repo, List<PullRequestEntity> prs,
                                       MetricBaselineDto baseline, List<AnomalyRecordEntity> detected) {
        if (baseline.getSampleCount() < 5 || baseline.getStandardDeviation() <= 0.0) return;

        double upperBound = baseline.getUpperBound();
        double q3 = baseline.getQ3();
        double iqr = baseline.getIqr();
        double mean = baseline.getMean();
        double stdDev = baseline.getStandardDeviation();

        for (PullRequestEntity pr : prs) {
            double size = pr.getAdditions() + pr.getDeletions();
            if (size > upperBound && size > 200) {
                double z = (size - mean) / stdDev;
                String severity;
                if (z >= 3.5 || size > (q3 + 3.0 * iqr)) {
                    severity = "CRITICAL";
                } else if (z >= 2.5 || size > (q3 + 2.0 * iqr)) {
                    severity = "HIGH";
                } else {
                    severity = "MEDIUM";
                }

                String explanation = String.format("PR #%d ('%s') size of %.0f lines changed significantly exceeds the expected upper bound of %.0f lines (mean: %.0f, Z-score: +%.2f). Large PRs impair review depth and increase defect leakage.",
                        pr.getPrNumber(), pr.getTitle(), size, upperBound, mean, z);

                detected.add(new AnomalyRecordEntity(
                        repo, "PR_SIZE", size, baseline.getLowerBound(), upperBound, severity, "IQR_Z_SCORE", explanation
                ));
            }
        }
    }

    private void detectReviewTurnaroundAnomalies(RepositoryEntity repo, List<PullRequestEntity> prs,
                                                 MetricBaselineDto baseline, List<AnomalyRecordEntity> detected) {
        if (baseline.getSampleCount() < 5 || baseline.getStandardDeviation() <= 0.0) return;

        double upperBound = baseline.getUpperBound();
        double q3 = baseline.getQ3();
        double iqr = baseline.getIqr();
        double mean = baseline.getMean();
        double stdDev = baseline.getStandardDeviation();

        for (PullRequestEntity pr : prs) {
            if (pr.getFirstReviewAt() != null && pr.getCreatedAt() != null && !pr.getFirstReviewAt().isBefore(pr.getCreatedAt())) {
                double turnaround = Duration.between(pr.getCreatedAt(), pr.getFirstReviewAt()).toMillis() / 3600000.0;
                if (turnaround > upperBound && turnaround > 24.0) {
                    double z = (turnaround - mean) / stdDev;
                    String severity;
                    if (z >= 3.5 || turnaround > (q3 + 3.0 * iqr)) {
                        severity = "CRITICAL";
                    } else if (z >= 2.5 || turnaround > (q3 + 2.0 * iqr)) {
                        severity = "HIGH";
                    } else {
                        severity = "MEDIUM";
                    }

                    String explanation = String.format("PR #%d ('%s') initial review latency of %.1fh exceeds expected threshold of %.1fh (baseline mean: %.1fh, Z-score: +%.2f). Review stall blocks engineering velocity.",
                            pr.getPrNumber(), pr.getTitle(), turnaround, upperBound, mean, z);

                    detected.add(new AnomalyRecordEntity(
                            repo, "REVIEW_TURNAROUND", turnaround, baseline.getLowerBound(), upperBound, severity, "IQR_Z_SCORE", explanation
                    ));
                }
            }
        }
    }

    private double percentile(List<Double> sorted, double p) {
        if (sorted == null || sorted.isEmpty()) return 0.0;
        if (sorted.size() == 1) return sorted.get(0);
        double rank = (p / 100.0) * (sorted.size() - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        if (lower == upper) return sorted.get(lower);
        double weight = rank - lower;
        return sorted.get(lower) + weight * (sorted.get(upper) - sorted.get(lower));
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

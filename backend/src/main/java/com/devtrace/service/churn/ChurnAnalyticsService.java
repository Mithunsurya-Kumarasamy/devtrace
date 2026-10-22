package com.devtrace.service.churn;

import com.devtrace.dto.churn.*;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.CommitFileEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChurnAnalyticsService {

    private static final Pattern DEFECT_PATTERN = Pattern.compile(
            "\\b(fix|fixed|fixes|bug|bugs|issue|issues|defect|defects|patch|resolve|resolves|resolved|error|crash|regression)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final RepositoryEntityRepository repositoryRepo;
    private final CommitFileEntityRepository commitFileRepo;

    public ChurnAnalyticsService(RepositoryEntityRepository repositoryRepo,
                                 CommitFileEntityRepository commitFileRepo) {
        this.repositoryRepo = repositoryRepo;
        this.commitFileRepo = commitFileRepo;
    }

    public RepositoryChurnReportDto calculateChurn(String repositoryId, int days) {
        if (!repositoryRepo.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }

        int windowDays = Math.max(1, days);
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);

        List<Object[]> rows = commitFileRepo.findFileCommitDetailsSince(repositoryId, since);

        RepositoryChurnReportDto report = new RepositoryChurnReportDto();
        report.setRepositoryId(repositoryId);
        report.setTimeWindowDays(windowDays);

        if (rows.isEmpty()) {
            report.setTotalFilesModified(0);
            report.setTotalAdditions(0);
            report.setTotalDeletions(0);
            report.setTotalChanges(0);
            report.setNetCodeGrowth(0);
            report.setDailyAverageChurn(0.0);
            return report;
        }

        // Process rows by file
        Map<String, FileStatsAccumulator> fileStatsMap = new HashMap<>();

        for (Object[] row : rows) {
            String filename = (String) row[0];
            String sha = (String) row[1];
            String message = (String) row[2];
            Instant commitDate = (Instant) row[3];
            String author = (String) row[4];
            int additions = row[5] != null ? ((Number) row[5]).intValue() : 0;
            int deletions = row[6] != null ? ((Number) row[6]).intValue() : 0;

            fileStatsMap.computeIfAbsent(filename, FileStatsAccumulator::new)
                    .recordCommit(sha, message, commitDate, author, additions, deletions);
        }

        long totalAdditions = 0;
        long totalDeletions = 0;
        long totalChanges = 0;

        List<FileChurnDto> fileChurnList = new ArrayList<>();
        Map<String, DirectoryAccumulator> directoryMap = new HashMap<>();

        for (FileStatsAccumulator acc : fileStatsMap.values()) {
            totalAdditions += acc.additions;
            totalDeletions += acc.deletions;
            totalChanges += acc.totalChanges;

            String directory = extractDirectory(acc.filename);

            FileChurnDto fileDto = new FileChurnDto(
                    acc.filename,
                    directory,
                    acc.commitCount,
                    acc.additions,
                    acc.deletions,
                    acc.totalChanges,
                    acc.authors.size(),
                    acc.lastModifiedAt
            );
            fileChurnList.add(fileDto);

            directoryMap.computeIfAbsent(directory, DirectoryAccumulator::new)
                    .recordFile(acc.filename, acc.commitCount, acc.additions, acc.deletions, acc.totalChanges);
        }

        fileChurnList.sort(Comparator.comparingLong(FileChurnDto::getTotalChanges).reversed());
        report.setTopModifiedFiles(fileChurnList.stream().limit(50).collect(Collectors.toList()));

        // Directory churn
        final long finalTotalChanges = totalChanges;
        List<DirectoryChurnDto> directoryChurnList = directoryMap.values().stream()
                .map(d -> new DirectoryChurnDto(
                        d.directoryPath,
                        d.files.size(),
                        d.totalModifications,
                        d.additions,
                        d.deletions,
                        d.totalChanges,
                        finalTotalChanges > 0 ? round((d.totalChanges * 100.0) / finalTotalChanges) : 0.0
                ))
                .sorted(Comparator.comparingLong(DirectoryChurnDto::getTotalChanges).reversed())
                .limit(20)
                .collect(Collectors.toList());

        report.setDirectoryChurn(directoryChurnList);
        report.setTotalFilesModified(fileStatsMap.size());
        report.setTotalAdditions(totalAdditions);
        report.setTotalDeletions(totalDeletions);
        report.setTotalChanges(totalChanges);
        report.setNetCodeGrowth(totalAdditions - totalDeletions);
        report.setDailyAverageChurn(round((double) totalChanges / windowDays));

        return report;
    }

    public CodebaseHotspotsReportDto calculateHotspots(String repositoryId, int days, int limit) {
        if (!repositoryRepo.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }

        int windowDays = Math.max(1, days);
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);

        List<Object[]> rows = commitFileRepo.findFileCommitDetailsSince(repositoryId, since);

        CodebaseHotspotsReportDto report = new CodebaseHotspotsReportDto();
        report.setRepositoryId(repositoryId);
        report.setTimeWindowDays(windowDays);

        if (rows.isEmpty()) {
            report.setTotalAnalyzedFiles(0);
            report.setCriticalHotspotsCount(0);
            report.setHighHotspotsCount(0);
            report.setMediumHotspotsCount(0);
            report.setLowHotspotsCount(0);
            report.getArchitecturalRecommendations().add("No commit file churn records found within the requested time window.");
            return report;
        }

        // Process rows
        Map<String, FileStatsAccumulator> fileStatsMap = new HashMap<>();
        Instant now = Instant.now();

        for (Object[] row : rows) {
            String filename = (String) row[0];
            String sha = (String) row[1];
            String message = (String) row[2];
            Instant commitDate = (Instant) row[3];
            String author = (String) row[4];
            int additions = row[5] != null ? ((Number) row[5]).intValue() : 0;
            int deletions = row[6] != null ? ((Number) row[6]).intValue() : 0;

            fileStatsMap.computeIfAbsent(filename, FileStatsAccumulator::new)
                    .recordCommit(sha, message, commitDate, author, additions, deletions);
        }

        // Compute max metrics for normalization
        long maxModifications = 1;
        long maxChurn = 1;
        int maxDefects = 1;
        double maxRecency = 1.0;

        for (FileStatsAccumulator acc : fileStatsMap.values()) {
            if (acc.commitCount > maxModifications) maxModifications = acc.commitCount;
            if (acc.totalChanges > maxChurn) maxChurn = acc.totalChanges;
            if (acc.defectCommitCount > maxDefects) maxDefects = acc.defectCommitCount;
            double recency = acc.calculateRecencyScore(now);
            if (recency > maxRecency) maxRecency = recency;
        }

        List<HotspotFileDto> hotspotList = new ArrayList<>();
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (FileStatsAccumulator acc : fileStatsMap.values()) {
            double recency = acc.calculateRecencyScore(now);

            double normMod = (double) acc.commitCount / maxModifications;
            double normChurn = (double) acc.totalChanges / maxChurn;
            double normDefect = (double) acc.defectCommitCount / maxDefects;
            double normRecency = recency / maxRecency;

            // Hotspot score: 30% modification frequency, 25% churn volume, 30% defect history, 15% recency
            double rawScore = (0.30 * normMod + 0.25 * normChurn + 0.30 * normDefect + 0.15 * normRecency) * 100.0;
            double hotspotScore = round(Math.min(100.0, Math.max(0.0, rawScore)));

            String riskLevel;
            if (hotspotScore >= 70.0) {
                riskLevel = "CRITICAL";
                criticalCount++;
            } else if (hotspotScore >= 45.0) {
                riskLevel = "HIGH";
                highCount++;
            } else if (hotspotScore >= 25.0) {
                riskLevel = "MEDIUM";
                mediumCount++;
            } else {
                riskLevel = "LOW";
                lowCount++;
            }

            double defectRatio = acc.commitCount > 0 ? round((double) acc.defectCommitCount / acc.commitCount) : 0.0;

            HotspotFileDto hotspot = new HotspotFileDto();
            hotspot.setFilename(acc.filename);
            hotspot.setDirectory(extractDirectory(acc.filename));
            hotspot.setHotspotScore(hotspotScore);
            hotspot.setRiskLevel(riskLevel);
            hotspot.setModificationCount(acc.commitCount);
            hotspot.setAdditions(acc.additions);
            hotspot.setDeletions(acc.deletions);
            hotspot.setTotalChurn(acc.totalChanges);
            hotspot.setDefectCommitCount(acc.defectCommitCount);
            hotspot.setDefectRatio(defectRatio);
            hotspot.setRecencyWeightedScore(round(recency));
            hotspot.setPrimaryContributor(acc.getPrimaryContributor());
            hotspot.setDistinctAuthorsCount(acc.authors.size());
            hotspot.setLastModifiedAt(acc.lastModifiedAt);
            hotspot.setExplanation(generateExplanation(acc, riskLevel));

            hotspotList.add(hotspot);
        }

        hotspotList.sort(Comparator.comparingDouble(HotspotFileDto::getHotspotScore).reversed());
        int resultLimit = limit > 0 ? limit : 20;

        report.setTotalAnalyzedFiles(fileStatsMap.size());
        report.setCriticalHotspotsCount(criticalCount);
        report.setHighHotspotsCount(highCount);
        report.setMediumHotspotsCount(mediumCount);
        report.setLowHotspotsCount(lowCount);
        report.setHotspots(hotspotList.stream().limit(resultLimit).collect(Collectors.toList()));

        // Architectural recommendations
        List<String> recommendations = new ArrayList<>();
        if (criticalCount > 0) {
            recommendations.add(String.format("Found %d CRITICAL hotspot file(s) with recurrent churn and bug fix cycles. Prioritize comprehensive regression test coverage and module decoupling.", criticalCount));
        }
        if (highCount > 0) {
            recommendations.add(String.format("%d HIGH risk hotspot(s) detected. Recommend peer review checklists and static analysis guardrails for changes affecting these components.", highCount));
        }
        if (criticalCount == 0 && highCount == 0) {
            recommendations.add("No critical hotspots detected in this time window. Code churn appears evenly distributed across healthy maintenance patterns.");
        }
        recommendations.add("Files undergoing >5 bug fix commits warrant architectural decomposition to isolate unstable responsibilities.");

        report.setArchitecturalRecommendations(recommendations);
        return report;
    }

    private String generateExplanation(FileStatsAccumulator acc, String riskLevel) {
        if ("CRITICAL".equals(riskLevel)) {
            return String.format("High change frequency (%d commits) combined with %d bug fixes indicates persistent fragility and architectural stress.",
                    acc.commitCount, acc.defectCommitCount);
        } else if ("HIGH".equals(riskLevel)) {
            return String.format("Substantial churn (%d lines changed across %d commits) with active modification makes this file prone to regression.",
                    acc.totalChanges, acc.commitCount);
        } else if ("MEDIUM".equals(riskLevel)) {
            return String.format("Moderate churn activity with %d contributor(s) and %d defect fix(es).",
                    acc.authors.size(), acc.defectCommitCount);
        } else {
            return "Stable component with low defect history and normal maintenance pace.";
        }
    }

    private String extractDirectory(String filename) {
        if (filename == null || filename.isBlank()) return ".";
        int lastSlash = filename.lastIndexOf('/');
        if (lastSlash == -1) {
            lastSlash = filename.lastIndexOf('\\');
        }
        if (lastSlash > 0) {
            return filename.substring(0, lastSlash);
        }
        return ".";
    }

    private double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Accumulator helper classes
    private static class FileStatsAccumulator {
        final String filename;
        long commitCount = 0;
        long additions = 0;
        long deletions = 0;
        long totalChanges = 0;
        int defectCommitCount = 0;
        Instant lastModifiedAt;
        final Set<String> authors = new HashSet<>();
        final Map<String, Integer> authorCommitCounts = new HashMap<>();
        final List<Instant> commitDates = new ArrayList<>();

        FileStatsAccumulator(String filename) {
            this.filename = filename;
        }

        void recordCommit(String sha, String message, Instant commitDate, String author, int adds, int dels) {
            this.commitCount++;
            this.additions += adds;
            this.deletions += dels;
            this.totalChanges += (adds + dels);

            if (author != null && !author.isBlank()) {
                this.authors.add(author);
                this.authorCommitCounts.put(author, this.authorCommitCounts.getOrDefault(author, 0) + 1);
            }

            if (commitDate != null) {
                this.commitDates.add(commitDate);
                if (this.lastModifiedAt == null || commitDate.isAfter(this.lastModifiedAt)) {
                    this.lastModifiedAt = commitDate;
                }
            }

            if (message != null && DEFECT_PATTERN.matcher(message).find()) {
                this.defectCommitCount++;
            }
        }

        double calculateRecencyScore(Instant now) {
            double score = 0.0;
            for (Instant dt : commitDates) {
                if (dt != null) {
                    double daysAgo = Math.max(0, Duration.between(dt, now).toDays());
                    // Exponential decay half-life ~ 23 days (lambda = 0.03)
                    score += Math.exp(-0.03 * daysAgo);
                }
            }
            return score;
        }

        String getPrimaryContributor() {
            return authorCommitCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
        }
    }

    private static class DirectoryAccumulator {
        final String directoryPath;
        final Set<String> files = new HashSet<>();
        long totalModifications = 0;
        long additions = 0;
        long deletions = 0;
        long totalChanges = 0;

        DirectoryAccumulator(String directoryPath) {
            this.directoryPath = directoryPath;
        }

        void recordFile(String file, long mods, long adds, long dels, long changes) {
            this.files.add(file);
            this.totalModifications += mods;
            this.additions += adds;
            this.deletions += dels;
            this.totalChanges += changes;
        }
    }
}

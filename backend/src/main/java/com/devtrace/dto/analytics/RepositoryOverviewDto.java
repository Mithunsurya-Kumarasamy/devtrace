package com.devtrace.dto.analytics;

import java.util.List;

public class RepositoryOverviewDto {
    private String repositoryId;
    private String fullName;
    private String defaultBranch;
    private String syncStatus;
    private String lastSyncedAt;
    private int timeWindowDays;

    private long totalCommits;
    private long totalPullRequests;
    private long openPullRequests;
    private long mergedPullRequests;
    private long totalIssues;
    private long openIssues;
    private long totalReleases;
    private long activeContributors;

    private PercentileDistributionDto prCycleTimeHours;
    private PercentileDistributionDto reviewWaitingTimeHours;
    private HealthScoreDto healthScore;
    private List<ActivityTrendDto> recentActivityTrends;

    public RepositoryOverviewDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public String getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(String lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public int getTimeWindowDays() { return timeWindowDays; }
    public void setTimeWindowDays(int timeWindowDays) { this.timeWindowDays = timeWindowDays; }
    public long getTotalCommits() { return totalCommits; }
    public void setTotalCommits(long totalCommits) { this.totalCommits = totalCommits; }
    public long getTotalPullRequests() { return totalPullRequests; }
    public void setTotalPullRequests(long totalPullRequests) { this.totalPullRequests = totalPullRequests; }
    public long getOpenPullRequests() { return openPullRequests; }
    public void setOpenPullRequests(long openPullRequests) { this.openPullRequests = openPullRequests; }
    public long getMergedPullRequests() { return mergedPullRequests; }
    public void setMergedPullRequests(long mergedPullRequests) { this.mergedPullRequests = mergedPullRequests; }
    public long getTotalIssues() { return totalIssues; }
    public void setTotalIssues(long totalIssues) { this.totalIssues = totalIssues; }
    public long getOpenIssues() { return openIssues; }
    public void setOpenIssues(long openIssues) { this.openIssues = openIssues; }
    public long getTotalReleases() { return totalReleases; }
    public void setTotalReleases(long totalReleases) { this.totalReleases = totalReleases; }
    public long getActiveContributors() { return activeContributors; }
    public void setActiveContributors(long activeContributors) { this.activeContributors = activeContributors; }
    public PercentileDistributionDto getPrCycleTimeHours() { return prCycleTimeHours; }
    public void setPrCycleTimeHours(PercentileDistributionDto prCycleTimeHours) { this.prCycleTimeHours = prCycleTimeHours; }
    public PercentileDistributionDto getReviewWaitingTimeHours() { return reviewWaitingTimeHours; }
    public void setReviewWaitingTimeHours(PercentileDistributionDto reviewWaitingTimeHours) { this.reviewWaitingTimeHours = reviewWaitingTimeHours; }
    public HealthScoreDto getHealthScore() { return healthScore; }
    public void setHealthScore(HealthScoreDto healthScore) { this.healthScore = healthScore; }
    public List<ActivityTrendDto> getRecentActivityTrends() { return recentActivityTrends; }
    public void setRecentActivityTrends(List<ActivityTrendDto> recentActivityTrends) { this.recentActivityTrends = recentActivityTrends; }
}

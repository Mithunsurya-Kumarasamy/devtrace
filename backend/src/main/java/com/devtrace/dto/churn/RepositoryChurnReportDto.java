package com.devtrace.dto.churn;

import java.util.ArrayList;
import java.util.List;

public class RepositoryChurnReportDto {
    private String repositoryId;
    private int timeWindowDays;
    private int totalFilesModified;
    private long totalAdditions;
    private long totalDeletions;
    private long totalChanges;
    private long netCodeGrowth;
    private double dailyAverageChurn;
    private List<FileChurnDto> topModifiedFiles = new ArrayList<>();
    private List<DirectoryChurnDto> directoryChurn = new ArrayList<>();

    public RepositoryChurnReportDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTimeWindowDays() { return timeWindowDays; }
    public void setTimeWindowDays(int timeWindowDays) { this.timeWindowDays = timeWindowDays; }
    public int getTotalFilesModified() { return totalFilesModified; }
    public void setTotalFilesModified(int totalFilesModified) { this.totalFilesModified = totalFilesModified; }
    public long getTotalAdditions() { return totalAdditions; }
    public void setTotalAdditions(long totalAdditions) { this.totalAdditions = totalAdditions; }
    public long getTotalDeletions() { return totalDeletions; }
    public void setTotalDeletions(long totalDeletions) { this.totalDeletions = totalDeletions; }
    public long getTotalChanges() { return totalChanges; }
    public void setTotalChanges(long totalChanges) { this.totalChanges = totalChanges; }
    public long getNetCodeGrowth() { return netCodeGrowth; }
    public void setNetCodeGrowth(long netCodeGrowth) { this.netCodeGrowth = netCodeGrowth; }
    public double getDailyAverageChurn() { return dailyAverageChurn; }
    public void setDailyAverageChurn(double dailyAverageChurn) { this.dailyAverageChurn = dailyAverageChurn; }
    public List<FileChurnDto> getTopModifiedFiles() { return topModifiedFiles; }
    public void setTopModifiedFiles(List<FileChurnDto> topModifiedFiles) { this.topModifiedFiles = topModifiedFiles; }
    public List<DirectoryChurnDto> getDirectoryChurn() { return directoryChurn; }
    public void setDirectoryChurn(List<DirectoryChurnDto> directoryChurn) { this.directoryChurn = directoryChurn; }
}

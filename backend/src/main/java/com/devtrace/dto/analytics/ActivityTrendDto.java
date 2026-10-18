package com.devtrace.dto.analytics;

public class ActivityTrendDto {
    private String date; // YYYY-MM-DD
    private int commitCount;
    private int prCreatedCount;
    private int prMergedCount;
    private int issuesOpenedCount;
    private int issuesClosedCount;
    private int linesAdded;
    private int linesDeleted;

    public ActivityTrendDto() {}

    public ActivityTrendDto(String date) {
        this.date = date;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getCommitCount() { return commitCount; }
    public void setCommitCount(int commitCount) { this.commitCount = commitCount; }
    public int getPrCreatedCount() { return prCreatedCount; }
    public void setPrCreatedCount(int prCreatedCount) { this.prCreatedCount = prCreatedCount; }
    public int getPrMergedCount() { return prMergedCount; }
    public void setPrMergedCount(int prMergedCount) { this.prMergedCount = prMergedCount; }
    public int getIssuesOpenedCount() { return issuesOpenedCount; }
    public void setIssuesOpenedCount(int issuesOpenedCount) { this.issuesOpenedCount = issuesOpenedCount; }
    public int getIssuesClosedCount() { return issuesClosedCount; }
    public void setIssuesClosedCount(int issuesClosedCount) { this.issuesClosedCount = issuesClosedCount; }
    public int getLinesAdded() { return linesAdded; }
    public void setLinesAdded(int linesAdded) { this.linesAdded = linesAdded; }
    public int getLinesDeleted() { return linesDeleted; }
    public void setLinesDeleted(int linesDeleted) { this.linesDeleted = linesDeleted; }
}

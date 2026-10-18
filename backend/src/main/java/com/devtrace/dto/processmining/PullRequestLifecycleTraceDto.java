package com.devtrace.dto.processmining;

import java.time.Instant;

public class PullRequestLifecycleTraceDto {
    private String id;
    private int prNumber;
    private String title;
    private String authorLogin;
    private String state;
    private boolean isMerged;
    private Instant createdAt;
    private Instant firstReviewAt;
    private Instant approvedAt;
    private Instant mergedAt;
    private Instant closedAt;
    private Double timeToFirstReviewHours;
    private Double timeFirstReviewToApprovalHours;
    private Double timeApprovalToMergeHours;
    private Double totalCycleTimeHours;
    private int reviewCount;
    private int changesRequestedCount;
    private String variant; // FAST_TRACK, STANDARD, REVIEW_HEAVY, STALE_REVIEW, ABANDONED

    public PullRequestLifecycleTraceDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getPrNumber() { return prNumber; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthorLogin() { return authorLogin; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public boolean isMerged() { return isMerged; }
    public void setMerged(boolean merged) { isMerged = merged; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getFirstReviewAt() { return firstReviewAt; }
    public void setFirstReviewAt(Instant firstReviewAt) { this.firstReviewAt = firstReviewAt; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getMergedAt() { return mergedAt; }
    public void setMergedAt(Instant mergedAt) { this.mergedAt = mergedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public Double getTimeToFirstReviewHours() { return timeToFirstReviewHours; }
    public void setTimeToFirstReviewHours(Double timeToFirstReviewHours) { this.timeToFirstReviewHours = timeToFirstReviewHours; }
    public Double getTimeFirstReviewToApprovalHours() { return timeFirstReviewToApprovalHours; }
    public void setTimeFirstReviewToApprovalHours(Double timeFirstReviewToApprovalHours) { this.timeFirstReviewToApprovalHours = timeFirstReviewToApprovalHours; }
    public Double getTimeApprovalToMergeHours() { return timeApprovalToMergeHours; }
    public void setTimeApprovalToMergeHours(Double timeApprovalToMergeHours) { this.timeApprovalToMergeHours = timeApprovalToMergeHours; }
    public Double getTotalCycleTimeHours() { return totalCycleTimeHours; }
    public void setTotalCycleTimeHours(Double totalCycleTimeHours) { this.totalCycleTimeHours = totalCycleTimeHours; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public int getChangesRequestedCount() { return changesRequestedCount; }
    public void setChangesRequestedCount(int changesRequestedCount) { this.changesRequestedCount = changesRequestedCount; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
}

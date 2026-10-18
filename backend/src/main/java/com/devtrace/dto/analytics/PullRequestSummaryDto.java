package com.devtrace.dto.analytics;

import com.devtrace.entity.PullRequestEntity;
import java.time.Instant;

public class PullRequestSummaryDto {
    private String id;
    private int prNumber;
    private String title;
    private String state;
    private String authorLogin;
    private Instant createdAt;
    private Instant closedAt;
    private Instant mergedAt;
    private Instant firstReviewAt;
    private boolean isMerged;
    private int additions;
    private int deletions;
    private int changedFiles;
    private Double cycleTimeHours;
    private Double reviewWaitingTimeHours;

    public PullRequestSummaryDto() {}

    public static PullRequestSummaryDto fromEntity(PullRequestEntity entity) {
        PullRequestSummaryDto dto = new PullRequestSummaryDto();
        dto.setId(entity.getId());
        dto.setPrNumber(entity.getPrNumber());
        dto.setTitle(entity.getTitle());
        dto.setState(entity.getState());
        dto.setAuthorLogin(entity.getAuthorLogin());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setClosedAt(entity.getClosedAt());
        dto.setMergedAt(entity.getMergedAt());
        dto.setFirstReviewAt(entity.getFirstReviewAt());
        dto.setMerged(entity.isMerged());
        dto.setAdditions(entity.getAdditions());
        dto.setDeletions(entity.getDeletions());
        dto.setChangedFiles(entity.getChangedFiles());

        if (entity.getCreatedAt() != null && entity.getMergedAt() != null) {
            long seconds = entity.getMergedAt().getEpochSecond() - entity.getCreatedAt().getEpochSecond();
            dto.setCycleTimeHours(Math.max(0.0, seconds / 3600.0));
        }

        if (entity.getCreatedAt() != null && entity.getFirstReviewAt() != null) {
            long seconds = entity.getFirstReviewAt().getEpochSecond() - entity.getCreatedAt().getEpochSecond();
            dto.setReviewWaitingTimeHours(Math.max(0.0, seconds / 3600.0));
        }

        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getPrNumber() { return prNumber; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getAuthorLogin() { return authorLogin; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public Instant getMergedAt() { return mergedAt; }
    public void setMergedAt(Instant mergedAt) { this.mergedAt = mergedAt; }
    public Instant getFirstReviewAt() { return firstReviewAt; }
    public void setFirstReviewAt(Instant firstReviewAt) { this.firstReviewAt = firstReviewAt; }
    public boolean isMerged() { return isMerged; }
    public void setMerged(boolean merged) { isMerged = merged; }
    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }
    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
    public int getChangedFiles() { return changedFiles; }
    public void setChangedFiles(int changedFiles) { this.changedFiles = changedFiles; }
    public Double getCycleTimeHours() { return cycleTimeHours; }
    public void setCycleTimeHours(Double cycleTimeHours) { this.cycleTimeHours = cycleTimeHours; }
    public Double getReviewWaitingTimeHours() { return reviewWaitingTimeHours; }
    public void setReviewWaitingTimeHours(Double reviewWaitingTimeHours) { this.reviewWaitingTimeHours = reviewWaitingTimeHours; }
}

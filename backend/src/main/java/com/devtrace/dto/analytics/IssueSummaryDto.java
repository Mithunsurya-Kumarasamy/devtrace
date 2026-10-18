package com.devtrace.dto.analytics;

import com.devtrace.entity.IssueEntity;
import java.time.Instant;

public class IssueSummaryDto {
    private String id;
    private int issueNumber;
    private String title;
    private String state;
    private String authorLogin;
    private String labels;
    private int commentsCount;
    private String category;
    private double categoryConfidence;
    private Instant createdAt;
    private Instant closedAt;
    private Double resolutionTimeHours;

    public IssueSummaryDto() {}

    public static IssueSummaryDto fromEntity(IssueEntity entity) {
        IssueSummaryDto dto = new IssueSummaryDto();
        dto.setId(entity.getId());
        dto.setIssueNumber(entity.getIssueNumber());
        dto.setTitle(entity.getTitle());
        dto.setState(entity.getState());
        dto.setAuthorLogin(entity.getAuthorLogin());
        dto.setLabels(entity.getLabels());
        dto.setCommentsCount(entity.getCommentsCount());
        dto.setCategory(entity.getCategory());
        dto.setCategoryConfidence(entity.getCategoryConfidence());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setClosedAt(entity.getClosedAt());

        if (entity.getCreatedAt() != null && entity.getClosedAt() != null) {
            long seconds = entity.getClosedAt().getEpochSecond() - entity.getCreatedAt().getEpochSecond();
            dto.setResolutionTimeHours(Math.max(0.0, seconds / 3600.0));
        }

        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getIssueNumber() { return issueNumber; }
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getAuthorLogin() { return authorLogin; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }
    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getCategoryConfidence() { return categoryConfidence; }
    public void setCategoryConfidence(double categoryConfidence) { this.categoryConfidence = categoryConfidence; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public Double getResolutionTimeHours() { return resolutionTimeHours; }
    public void setResolutionTimeHours(Double resolutionTimeHours) { this.resolutionTimeHours = resolutionTimeHours; }
}

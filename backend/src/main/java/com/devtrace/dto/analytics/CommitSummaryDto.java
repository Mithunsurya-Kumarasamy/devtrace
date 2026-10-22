package com.devtrace.dto.analytics;

import com.devtrace.entity.CommitEntity;
import java.time.Instant;

public class CommitSummaryDto {
    private String id;
    private String sha;
    private String authorName;
    private String authorEmail;
    private Instant commitDate;
    private String message;
    private int additions;
    private int deletions;
    private int totalChanges;

    public CommitSummaryDto() {}

    public static CommitSummaryDto fromEntity(CommitEntity entity) {
        CommitSummaryDto dto = new CommitSummaryDto();
        dto.setId(entity.getId());
        dto.setSha(entity.getSha());
        dto.setAuthorName(entity.getAuthorName());
        dto.setAuthorEmail(entity.getAuthorEmail());
        dto.setCommitDate(entity.getCommitDate());
        dto.setMessage(entity.getMessage());
        dto.setAdditions(entity.getAdditions());
        dto.setDeletions(entity.getDeletions());
        dto.setTotalChanges(entity.getTotalChanges());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSha() { return sha; }
    public void setSha(String sha) { this.sha = sha; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public Instant getCommitDate() { return commitDate; }
    public void setCommitDate(Instant commitDate) { this.commitDate = commitDate; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }
    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
    public int getTotalChanges() { return totalChanges; }
    public void setTotalChanges(int totalChanges) { this.totalChanges = totalChanges; }
}

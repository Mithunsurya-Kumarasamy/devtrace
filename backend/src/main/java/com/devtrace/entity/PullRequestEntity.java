package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pull_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_repo_pr_number", columnNames = {"repository_id", "pr_number"})
})
public class PullRequestEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(name = "pr_number", nullable = false)
    private int prNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(name = "author_login", nullable = false, length = 100)
    private String authorLogin;

    @Column(name = "author_avatar_url", length = 500)
    private String authorAvatarUrl;

    @Column(name = "base_branch", length = 100)
    private String baseBranch;

    @Column(name = "head_branch", length = 100)
    private String headBranch;

    private int additions = 0;
    private int deletions = 0;

    @Column(name = "changed_files")
    private int changedFiles = 0;

    @Column(name = "is_draft")
    private boolean isDraft = false;

    @Column(name = "is_merged")
    private boolean isMerged = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "merged_at")
    private Instant mergedAt;

    @Column(name = "first_review_at")
    private Instant firstReviewAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PullRequestReviewEntity> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PullRequestTimelineEventEntity> timelineEvents = new ArrayList<>();

    @OneToOne(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private PullRequestRiskPredictionEntity riskPrediction;

    public PullRequestEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public PullRequestEntity(RepositoryEntity repository, int prNumber, String title, String state, String authorLogin) {
        this();
        this.repository = repository;
        this.prNumber = prNumber;
        this.title = title;
        this.state = state;
        this.authorLogin = authorLogin;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

    public int getPrNumber() { return prNumber; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getAuthorLogin() { return authorLogin; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }

    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }

    public String getBaseBranch() { return baseBranch; }
    public void setBaseBranch(String baseBranch) { this.baseBranch = baseBranch; }

    public String getHeadBranch() { return headBranch; }
    public void setHeadBranch(String headBranch) { this.headBranch = headBranch; }

    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }

    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }

    public int getChangedFiles() { return changedFiles; }
    public void setChangedFiles(int changedFiles) { this.changedFiles = changedFiles; }

    public boolean isDraft() { return isDraft; }
    public void setDraft(boolean draft) { isDraft = draft; }

    public boolean isMerged() { return isMerged || mergedAt != null; }
    public void setMerged(boolean merged) { isMerged = merged; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public Instant getMergedAt() { return mergedAt; }
    public void setMergedAt(Instant mergedAt) { this.mergedAt = mergedAt; }

    public Instant getFirstReviewAt() { return firstReviewAt; }
    public void setFirstReviewAt(Instant firstReviewAt) { this.firstReviewAt = firstReviewAt; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public List<PullRequestReviewEntity> getReviews() { return reviews; }
    public void setReviews(List<PullRequestReviewEntity> reviews) { this.reviews = reviews; }

    public List<PullRequestTimelineEventEntity> getTimelineEvents() { return timelineEvents; }
    public void setTimelineEvents(List<PullRequestTimelineEventEntity> timelineEvents) { this.timelineEvents = timelineEvents; }

    public PullRequestRiskPredictionEntity getRiskPrediction() { return riskPrediction; }
    public void setRiskPrediction(PullRequestRiskPredictionEntity riskPrediction) { this.riskPrediction = riskPrediction; }
}

package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pr_reviews")
public class PullRequestReviewEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;

    @Column(name = "github_review_id")
    private Long githubReviewId;

    @Column(name = "reviewer_login", nullable = false, length = 100)
    private String reviewerLogin;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(columnDefinition = "TEXT")
    private String body;

    public PullRequestReviewEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public PullRequestReviewEntity(PullRequestEntity pullRequest, String reviewerLogin, String state, Instant submittedAt) {
        this();
        this.pullRequest = pullRequest;
        this.reviewerLogin = reviewerLogin;
        this.state = state;
        this.submittedAt = submittedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public PullRequestEntity getPullRequest() { return pullRequest; }
    public void setPullRequest(PullRequestEntity pullRequest) { this.pullRequest = pullRequest; }

    public Long getGithubReviewId() { return githubReviewId; }
    public void setGithubReviewId(Long githubReviewId) { this.githubReviewId = githubReviewId; }

    public String getReviewerLogin() { return reviewerLogin; }
    public void setReviewerLogin(String reviewerLogin) { this.reviewerLogin = reviewerLogin; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}

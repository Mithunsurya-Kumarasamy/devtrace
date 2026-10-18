package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "issues", uniqueConstraints = {
        @UniqueConstraint(name = "uk_repo_issue_number", columnNames = {"repository_id", "issue_number"})
})
public class IssueEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(name = "issue_number", nullable = false)
    private int issueNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(name = "author_login", nullable = false, length = 100)
    private String authorLogin;

    @Column(columnDefinition = "TEXT")
    private String labels;

    @Column(name = "comments_count")
    private int commentsCount = 0;

    @Column(length = 50)
    private String category = "OTHER";

    @Column(name = "category_confidence")
    private double categoryConfidence = 0.0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    public IssueEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public IssueEntity(RepositoryEntity repository, int issueNumber, String title, String state, String authorLogin) {
        this();
        this.repository = repository;
        this.issueNumber = issueNumber;
        this.title = title;
        this.state = state;
        this.authorLogin = authorLogin;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

    public int getIssueNumber() { return issueNumber; }
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

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

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}

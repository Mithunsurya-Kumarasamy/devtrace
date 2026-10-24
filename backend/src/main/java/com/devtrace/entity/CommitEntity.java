package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commits", uniqueConstraints = {
        @UniqueConstraint(name = "uk_repo_commit_sha", columnNames = {"repository_id", "sha"})
})
public class CommitEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(nullable = false, length = 40)
    private String sha;

    @Column(name = "author_name", length = 150)
    private String authorName;

    @Column(name = "author_email", length = 150)
    private String authorEmail;

    @Column(name = "author_date")
    private Instant authorDate;

    @Column(name = "committer_name", length = 150)
    private String committerName;

    @Column(name = "committer_email", length = 150)
    private String committerEmail;

    @Column(name = "commit_date", nullable = false)
    private Instant commitDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "parent_shas", columnDefinition = "TEXT")
    private String parentShas;

    private int additions = 0;
    private int deletions = 0;

    @Column(name = "total_changes")
    private int totalChanges = 0;

    @Column(length = 100)
    private String branch;

    @Column(columnDefinition = "TEXT")
    private String branches;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommitFileEntity> files = new ArrayList<>();

    public void addBranch(String branchName) {
        if (branchName == null || branchName.isBlank()) return;
        String b = branchName.trim();
        if (this.branch == null || this.branch.isBlank()) {
            this.branch = b;
        }
        if (this.branches == null || this.branches.isBlank()) {
            this.branches = "," + b + ",";
        } else if (!this.branches.contains("," + b + ",")) {
            this.branches = this.branches + b + ",";
        }
    }

    public CommitEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public CommitEntity(RepositoryEntity repository, String sha, String message, Instant commitDate) {
        this();
        this.repository = repository;
        this.sha = sha;
        this.message = message;
        this.commitDate = commitDate;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

    public String getSha() { return sha; }
    public void setSha(String sha) { this.sha = sha; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public Instant getAuthorDate() { return authorDate; }
    public void setAuthorDate(Instant authorDate) { this.authorDate = authorDate; }

    public String getCommitterName() { return committerName; }
    public void setCommitterName(String committerName) { this.committerName = committerName; }

    public String getCommitterEmail() { return committerEmail; }
    public void setCommitterEmail(String committerEmail) { this.committerEmail = committerEmail; }

    public Instant getCommitDate() { return commitDate; }
    public void setCommitDate(Instant commitDate) { this.commitDate = commitDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getParentShas() { return parentShas; }
    public void setParentShas(String parentShas) { this.parentShas = parentShas; }

    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }

    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }

    public int getTotalChanges() { return totalChanges; }
    public void setTotalChanges(int totalChanges) { this.totalChanges = totalChanges; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<CommitFileEntity> getFiles() { return files; }
    public void setFiles(List<CommitFileEntity> files) { this.files = files; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getBranches() { return branches; }
    public void setBranches(String branches) { this.branches = branches; }
}

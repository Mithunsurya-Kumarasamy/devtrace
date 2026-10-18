package com.devtrace.dto;

import com.devtrace.entity.RepositoryEntity;
import java.time.Instant;

public class RepositoryResponse {
    private String id;
    private String owner;
    private String name;
    private String fullName;
    private String description;
    private String defaultBranch;
    private boolean isPrivate;
    private String htmlUrl;
    private Long githubRepoId;
    private String syncStatus;
    private Instant lastSyncedAt;
    private Instant createdAt;

    public RepositoryResponse() {}

    public static RepositoryResponse fromEntity(RepositoryEntity entity) {
        RepositoryResponse res = new RepositoryResponse();
        res.setId(entity.getId());
        res.setOwner(entity.getOwner());
        res.setName(entity.getName());
        res.setFullName(entity.getFullName());
        res.setDescription(entity.getDescription());
        res.setDefaultBranch(entity.getDefaultBranch());
        res.setPrivate(entity.isPrivate());
        res.setHtmlUrl(entity.getHtmlUrl());
        res.setGithubRepoId(entity.getGithubRepoId());
        res.setSyncStatus(entity.getSyncStatus());
        res.setLastSyncedAt(entity.getLastSyncedAt());
        res.setCreatedAt(entity.getCreatedAt());
        return res;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public Long getGithubRepoId() { return githubRepoId; }
    public void setGithubRepoId(Long githubRepoId) { this.githubRepoId = githubRepoId; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

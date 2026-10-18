package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "releases", uniqueConstraints = {
        @UniqueConstraint(name = "uk_repo_tag", columnNames = {"repository_id", "tag_name"})
})
public class ReleaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "is_draft")
    private boolean isDraft = false;

    @Column(name = "is_prerelease")
    private boolean isPrerelease = false;

    public ReleaseEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public ReleaseEntity(RepositoryEntity repository, String tagName, String name, Instant publishedAt) {
        this();
        this.repository = repository;
        this.tagName = tagName;
        this.name = name;
        this.publishedAt = publishedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public boolean isDraft() { return isDraft; }
    public void setDraft(boolean draft) { isDraft = draft; }

    public boolean isPrerelease() { return isPrerelease; }
    public void setPrerelease(boolean prerelease) { isPrerelease = prerelease; }
}

package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_checkpoints", uniqueConstraints = {
        @UniqueConstraint(name = "uk_repo_resource", columnNames = {"repository_id", "resource_type"})
})
public class SyncCheckpointEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "last_sync_timestamp")
    private Instant lastSyncTimestamp;

    @Column(name = "last_external_id", length = 100)
    private String lastExternalId;

    @Column(name = "checkpoint_data", columnDefinition = "TEXT")
    private String checkpointData;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SyncCheckpointEntity() {
        this.id = UUID.randomUUID().toString();
        this.updatedAt = Instant.now();
    }

    public SyncCheckpointEntity(RepositoryEntity repository, String resourceType) {
        this();
        this.repository = repository;
        this.resourceType = resourceType;
    }

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public Instant getLastSyncTimestamp() { return lastSyncTimestamp; }
    public void setLastSyncTimestamp(Instant lastSyncTimestamp) { this.lastSyncTimestamp = lastSyncTimestamp; }

    public String getLastExternalId() { return lastExternalId; }
    public void setLastExternalId(String lastExternalId) { this.lastExternalId = lastExternalId; }

    public String getCheckpointData() { return checkpointData; }
    public void setCheckpointData(String checkpointData) { this.checkpointData = checkpointData; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

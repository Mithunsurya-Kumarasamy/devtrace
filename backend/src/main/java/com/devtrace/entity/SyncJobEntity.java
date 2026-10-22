package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_jobs")
public class SyncJobEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "current_step", length = 100)
    private String currentStep = "INITIALIZING";

    @Column(name = "progress_percentage")
    private int progressPercentage = 0;

    @Column(name = "items_processed")
    private int itemsProcessed = 0;

    @Column(name = "total_items_estimated")
    private int totalItemsEstimated = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public SyncJobEntity() {
        this.id = UUID.randomUUID().toString();
        this.startedAt = Instant.now();
    }

    public SyncJobEntity(RepositoryEntity repository) {
        this();
        this.repository = repository;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

    public int getItemsProcessed() { return itemsProcessed; }
    public void setItemsProcessed(int itemsProcessed) { this.itemsProcessed = itemsProcessed; }

    public int getTotalItemsEstimated() { return totalItemsEstimated; }
    public void setTotalItemsEstimated(int totalItemsEstimated) { this.totalItemsEstimated = totalItemsEstimated; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

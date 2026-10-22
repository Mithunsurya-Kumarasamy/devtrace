package com.devtrace.dto;

import com.devtrace.entity.SyncJobEntity;
import java.time.Instant;

public class SyncJobResponse {
    private String jobId;
    private String repositoryId;
    private String status;
    private String currentStep;
    private int progressPercentage;
    private int itemsProcessed;
    private int totalItemsEstimated;
    private String errorMessage;
    private Instant startedAt;
    private Instant completedAt;

    public SyncJobResponse() {}

    public static SyncJobResponse fromEntity(SyncJobEntity entity) {
        SyncJobResponse res = new SyncJobResponse();
        res.setJobId(entity.getId());
        res.setRepositoryId(entity.getRepository().getId());
        res.setStatus(entity.getStatus());
        res.setCurrentStep(entity.getCurrentStep());
        res.setProgressPercentage(entity.getProgressPercentage());
        res.setItemsProcessed(entity.getItemsProcessed());
        res.setTotalItemsEstimated(entity.getTotalItemsEstimated());
        res.setErrorMessage(entity.getErrorMessage());
        res.setStartedAt(entity.getStartedAt());
        res.setCompletedAt(entity.getCompletedAt());
        return res;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
    public int getItemsProcessed() { return itemsProcessed; }
    public int getProcessedItems() { return itemsProcessed; }
    public void setItemsProcessed(int itemsProcessed) { this.itemsProcessed = itemsProcessed; }
    public int getTotalItemsEstimated() { return totalItemsEstimated; }
    public int getTotalEstimatedItems() { return totalItemsEstimated; }
    public void setTotalItemsEstimated(int totalItemsEstimated) { this.totalItemsEstimated = totalItemsEstimated; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

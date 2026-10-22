package com.devtrace.kafka.event;

public class SyncProgressEvent {
    private String jobId;
    private String repositoryId;
    private String status;
    private String currentStep;
    private int progressPercentage;
    private int itemsProcessed;
    private int totalItemsEstimated;
    private String errorMessage;

    public SyncProgressEvent() {}

    public SyncProgressEvent(String jobId, String repositoryId, String status, String currentStep,
                             int progressPercentage, int itemsProcessed, int totalItemsEstimated, String errorMessage) {
        this.jobId = jobId;
        this.repositoryId = repositoryId;
        this.status = status;
        this.currentStep = currentStep;
        this.progressPercentage = progressPercentage;
        this.itemsProcessed = itemsProcessed;
        this.totalItemsEstimated = totalItemsEstimated;
        this.errorMessage = errorMessage;
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
    public void setItemsProcessed(int itemsProcessed) { this.itemsProcessed = itemsProcessed; }
    public int getTotalItemsEstimated() { return totalItemsEstimated; }
    public void setTotalItemsEstimated(int totalItemsEstimated) { this.totalItemsEstimated = totalItemsEstimated; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

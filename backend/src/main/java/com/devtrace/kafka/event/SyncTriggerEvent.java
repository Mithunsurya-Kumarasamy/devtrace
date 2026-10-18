package com.devtrace.kafka.event;

public class SyncTriggerEvent {
    private String jobId;
    private String repositoryId;
    private String owner;
    private String name;
    private boolean forceFullSync;

    public SyncTriggerEvent() {}

    public SyncTriggerEvent(String jobId, String repositoryId, String owner, String name, boolean forceFullSync) {
        this.jobId = jobId;
        this.repositoryId = repositoryId;
        this.owner = owner;
        this.name = name;
        this.forceFullSync = forceFullSync;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isForceFullSync() { return forceFullSync; }
    public void setForceFullSync(boolean forceFullSync) { this.forceFullSync = forceFullSync; }
}

package com.devtrace.kafka.event;

import com.devtrace.dto.GitHubCommitDto;

public class RawCommitEvent {
    private String jobId;
    private String repositoryId;
    private String owner;
    private String name;
    private GitHubCommitDto commit;

    public RawCommitEvent() {}

    public RawCommitEvent(String jobId, String repositoryId, String owner, String name, GitHubCommitDto commit) {
        this.jobId = jobId;
        this.repositoryId = repositoryId;
        this.owner = owner;
        this.name = name;
        this.commit = commit;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GitHubCommitDto getCommit() { return commit; }
    public void setCommit(GitHubCommitDto commit) { this.commit = commit; }
}

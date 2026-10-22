package com.devtrace.kafka.event;

import com.devtrace.dto.GitHubIssueDto;

public class RawIssueEvent {
    private String jobId;
    private String repositoryId;
    private String owner;
    private String name;
    private GitHubIssueDto issue;

    public RawIssueEvent() {}

    public RawIssueEvent(String jobId, String repositoryId, String owner, String name, GitHubIssueDto issue) {
        this.jobId = jobId;
        this.repositoryId = repositoryId;
        this.owner = owner;
        this.name = name;
        this.issue = issue;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GitHubIssueDto getIssue() { return issue; }
    public void setIssue(GitHubIssueDto issue) { this.issue = issue; }
}

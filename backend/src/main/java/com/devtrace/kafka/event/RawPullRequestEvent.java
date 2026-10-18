package com.devtrace.kafka.event;

import com.devtrace.dto.GitHubPullRequestDto;

public class RawPullRequestEvent {
    private String jobId;
    private String repositoryId;
    private String owner;
    private String name;
    private GitHubPullRequestDto pullRequest;

    public RawPullRequestEvent() {}

    public RawPullRequestEvent(String jobId, String repositoryId, String owner, String name, GitHubPullRequestDto pullRequest) {
        this.jobId = jobId;
        this.repositoryId = repositoryId;
        this.owner = owner;
        this.name = name;
        this.pullRequest = pullRequest;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GitHubPullRequestDto getPullRequest() { return pullRequest; }
    public void setPullRequest(GitHubPullRequestDto pullRequest) { this.pullRequest = pullRequest; }
}

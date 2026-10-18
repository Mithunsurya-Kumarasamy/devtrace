package com.devtrace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubIssueDto {
    private int number;
    private String title;
    private String body;
    private String state;
    private GitHubUser user;
    private List<LabelInfo> labels;
    private int comments;
    
    @JsonProperty("pull_request")
    private Object pullRequest; // When present, GitHub treats PR as an issue; we can distinguish pure issues!
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("closed_at")
    private String closedAt;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubUser {
        private String login;
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LabelInfo {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public boolean isPullRequest() {
        return pullRequest != null;
    }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public GitHubUser getUser() { return user; }
    public void setUser(GitHubUser user) { this.user = user; }
    public List<LabelInfo> getLabels() { return labels; }
    public void setLabels(List<LabelInfo> labels) { this.labels = labels; }
    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }
    public Object getPullRequest() { return pullRequest; }
    public void setPullRequest(Object pullRequest) { this.pullRequest = pullRequest; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getClosedAt() { return closedAt; }
    public void setClosedAt(String closedAt) { this.closedAt = closedAt; }
}

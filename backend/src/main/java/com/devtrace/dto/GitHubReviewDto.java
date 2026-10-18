package com.devtrace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubReviewDto {
    private Long id;
    private GitHubUser user;
    private String state;
    
    @JsonProperty("submitted_at")
    private String submittedAt;
    
    private String body;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubUser {
        private String login;
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GitHubUser getUser() { return user; }
    public void setUser(GitHubUser user) { this.user = user; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}

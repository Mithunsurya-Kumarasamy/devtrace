package com.devtrace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequestDto {
    private int number;
    private String title;
    private String body;
    private String state;
    private GitHubUser user;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("closed_at")
    private String closedAt;
    
    @JsonProperty("merged_at")
    private String mergedAt;
    
    private BranchInfo head;
    private BranchInfo base;
    
    private boolean draft;
    private boolean merged;
    
    private int additions;
    private int deletions;
    
    @JsonProperty("changed_files")
    private int changedFiles;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubUser {
        private String login;
        
        @JsonProperty("avatar_url")
        private String avatarUrl;

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchInfo {
        private String ref;
        private String sha;

        public String getRef() { return ref; }
        public void setRef(String ref) { this.ref = ref; }
        public String getSha() { return sha; }
        public void setSha(String sha) { this.sha = sha; }
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
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getClosedAt() { return closedAt; }
    public void setClosedAt(String closedAt) { this.closedAt = closedAt; }
    public String getMergedAt() { return mergedAt; }
    public void setMergedAt(String mergedAt) { this.mergedAt = mergedAt; }
    public BranchInfo getHead() { return head; }
    public void setHead(BranchInfo head) { this.head = head; }
    public BranchInfo getBase() { return base; }
    public void setBase(BranchInfo base) { this.base = base; }
    public boolean isDraft() { return draft; }
    public void setDraft(boolean draft) { this.draft = draft; }
    public boolean isMerged() { return merged; }
    public void setMerged(boolean merged) { this.merged = merged; }
    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }
    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
    public int getChangedFiles() { return changedFiles; }
    public void setChangedFiles(int changedFiles) { this.changedFiles = changedFiles; }
}

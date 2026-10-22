package com.devtrace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubReleaseDto {
    @JsonProperty("tag_name")
    private String tagName;
    
    private String name;
    private String body;
    
    @JsonProperty("published_at")
    private String publishedAt;
    
    private boolean draft;
    private boolean prerelease;

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public boolean isDraft() { return draft; }
    public void setDraft(boolean draft) { this.draft = draft; }
    public boolean isPrerelease() { return prerelease; }
    public void setPrerelease(boolean prerelease) { this.prerelease = prerelease; }
}

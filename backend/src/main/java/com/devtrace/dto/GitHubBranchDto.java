package com.devtrace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubBranchDto {
    private String name;

    @JsonProperty("protected")
    private boolean isProtected;

    public GitHubBranchDto() {}

    public GitHubBranchDto(String name) {
        this.name = name;
    }

    public GitHubBranchDto(String name, boolean isProtected) {
        this.name = name;
        this.isProtected = isProtected;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isProtected() { return isProtected; }
    public void setProtected(boolean aProtected) { isProtected = aProtected; }
}

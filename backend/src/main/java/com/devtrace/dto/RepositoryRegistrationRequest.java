package com.devtrace.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public class RepositoryRegistrationRequest {

    @Size(max = 100, message = "Owner name must be between 1 and 100 characters")
    private String owner;

    @Size(max = 100, message = "Repository name must be between 1 and 100 characters")
    private String name;

    private String url;
    private String branch;

    public RepositoryRegistrationRequest() {}

    public RepositoryRegistrationRequest(String owner, String name) {
        this.owner = owner;
        this.name = name;
        parseUrl();
    }

    public RepositoryRegistrationRequest(String owner, String name, String branch) {
        this.owner = owner;
        this.name = name;
        this.branch = branch;
        parseUrl();
    }

    public RepositoryRegistrationRequest(String url) {
        this.url = url;
        parseUrl();
    }

    public void parseUrl() {
        if (this.url != null && !this.url.isBlank()) {
            String clean = this.url.trim()
                    .replaceFirst("^(?i)https?://", "")
                    .replaceFirst("^(?i)git@github\\.com:", "")
                    .replaceFirst("^(?i)github\\.com/", "")
                    .replaceAll("^/+", "").replaceAll("/+$", "");

            String[] parts = clean.split("/");
            if (parts.length >= 2) {
                if (this.owner == null || this.owner.isBlank()) {
                    this.owner = parts[0].trim();
                }
                if (this.name == null || this.name.isBlank()) {
                    String repoName = parts[1].trim();
                    if (repoName.endsWith(".git")) {
                        repoName = repoName.substring(0, repoName.length() - 4);
                    }
                    this.name = repoName;
                }
                if ((this.branch == null || this.branch.isBlank()) && parts.length >= 4 && "tree".equalsIgnoreCase(parts[2])) {
                    StringBuilder b = new StringBuilder();
                    for (int i = 3; i < parts.length; i++) {
                        if (b.length() > 0) b.append("/");
                        b.append(parts[i]);
                    }
                    this.branch = b.toString().trim();
                }
            }
        }
    }

    @AssertTrue(message = "Owner and repository name must be valid GitHub identifiers, or a valid GitHub URL must be provided")
    public boolean isValid() {
        parseUrl();
        if (this.owner == null || this.owner.isBlank() || this.name == null || this.name.isBlank()) {
            return false;
        }
        return this.owner.matches("^[a-zA-Z0-9_.-]+$") && this.name.matches("^[a-zA-Z0-9_.-]+$");
    }

    public String getOwner() {
        parseUrl();
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
        parseUrl();
    }

    public String getName() {
        parseUrl();
        return name;
    }

    public void setName(String name) {
        this.name = name;
        parseUrl();
    }

    public String getUrl() { return url; }
    public void setUrl(String url) {
        this.url = url;
        parseUrl();
    }

    public String getBranch() {
        parseUrl();
        return branch;
    }

    public void setBranch(String branch) { this.branch = branch; }
}

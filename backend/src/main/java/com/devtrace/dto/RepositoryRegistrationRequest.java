package com.devtrace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RepositoryRegistrationRequest {

    @NotBlank(message = "Owner must not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Invalid GitHub owner name format")
    private String owner;

    @NotBlank(message = "Repository name must not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Invalid GitHub repository name format")
    private String name;

    public RepositoryRegistrationRequest() {}

    public RepositoryRegistrationRequest(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

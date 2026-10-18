package com.devtrace.controller;

import com.devtrace.dto.RepositoryRegistrationRequest;
import com.devtrace.dto.RepositoryResponse;
import com.devtrace.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@Tag(name = "Repositories", description = "Repository registration, validation, and metadata management")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    @Operation(summary = "Register repository", description = "Connects a GitHub repository by owner and name, validates against GitHub API, and persists in DevTrace.")
    public ResponseEntity<RepositoryResponse> registerRepository(
            @Valid @RequestBody RepositoryRegistrationRequest request) {
        RepositoryResponse response = repositoryService.registerRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List repositories", description = "Retrieves all registered GitHub repositories in DevTrace.")
    public ResponseEntity<List<RepositoryResponse>> getAllRepositories() {
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repository by ID", description = "Retrieves metadata and synchronization status for a specific repository.")
    public ResponseEntity<RepositoryResponse> getRepositoryById(@PathVariable String id) {
        return ResponseEntity.ok(repositoryService.getRepositoryById(id));
    }
}

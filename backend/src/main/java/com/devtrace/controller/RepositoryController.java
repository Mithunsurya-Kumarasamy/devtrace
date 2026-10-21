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
    private final com.devtrace.service.SyncJobService syncJobService;

    public RepositoryController(RepositoryService repositoryService, com.devtrace.service.SyncJobService syncJobService) {
        this.repositoryService = repositoryService;
        this.syncJobService = syncJobService;
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

    @GetMapping("/{id}/branches")
    @Operation(summary = "Get repository branches", description = "Retrieves all available branches from GitHub for the registered repository.")
    public ResponseEntity<List<String>> getRepositoryBranches(@PathVariable String id) {
        return ResponseEntity.ok(repositoryService.getRepositoryBranches(id));
    }

    @PatchMapping("/{id}/branch")
    @Operation(summary = "Update repository target branch", description = "Switches the active branch used for synchronization and analysis.")
    public ResponseEntity<RepositoryResponse> updateRepositoryBranch(
            @PathVariable String id,
            @RequestBody(required = false) java.util.Map<String, String> request,
            @RequestParam(required = false) String branch) {
        String targetBranch = branch;
        if ((targetBranch == null || targetBranch.isBlank()) && request != null) {
            targetBranch = request.get("branch");
        }
        if (targetBranch == null || targetBranch.isBlank()) {
            throw new IllegalArgumentException("Branch name must be provided in request body or 'branch' query parameter");
        }
        return ResponseEntity.ok(repositoryService.updateRepositoryBranch(id, targetBranch));
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "Trigger repository synchronization", description = "Asynchronously initiates repository ingestion via Kafka event pipeline and returns a tracking job ID.")
    public ResponseEntity<com.devtrace.dto.SyncJobResponse> triggerSync(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        return ResponseEntity.accepted().body(syncJobService.triggerSync(id, force));
    }

    @GetMapping("/{id}/sync-jobs")
    @Operation(summary = "Get synchronization jobs history", description = "Retrieves all synchronization jobs for a repository ordered by start time descending.")
    public ResponseEntity<List<com.devtrace.dto.SyncJobResponse>> getSyncJobs(@PathVariable String id) {
        return ResponseEntity.ok(syncJobService.getSyncJobsForRepository(id));
    }

    @GetMapping("/{id}/sync-jobs/{jobId}")
    @Operation(summary = "Get synchronization job status", description = "Retrieves real-time status, progress percentage, items processed, and diagnostics for an active or past sync job.")
    public ResponseEntity<com.devtrace.dto.SyncJobResponse> getSyncJob(
            @PathVariable String id,
            @PathVariable String jobId) {
        return ResponseEntity.ok(syncJobService.getSyncJob(id, jobId));
    }
}

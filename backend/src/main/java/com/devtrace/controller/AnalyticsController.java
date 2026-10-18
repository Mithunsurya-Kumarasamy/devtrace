package com.devtrace.controller;

import com.devtrace.dto.analytics.*;
import com.devtrace.entity.ReleaseEntity;
import com.devtrace.service.analytics.CoreAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repositories/{id}")
@Tag(name = "Analytics", description = "Repository overview, activity trends, PR metrics, and engineering health indicators")
public class AnalyticsController {

    private final CoreAnalyticsService analyticsService;

    public AnalyticsController(CoreAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics/overview")
    @Operation(summary = "Get repository analytics overview", description = "Calculates cycle time percentiles, review wait times, activity trends, and transparent engineering health scores.")
    public ResponseEntity<RepositoryOverviewDto> getOverview(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getRepositoryOverview(id, days));
    }

    @GetMapping("/commits")
    @Operation(summary = "Get repository commits", description = "Retrieves paginated commits with author and line churn details.")
    public ResponseEntity<Page<CommitSummaryDto>> getCommits(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(analyticsService.getCommits(id, page, size));
    }

    @GetMapping("/pull-requests")
    @Operation(summary = "Get repository pull requests", description = "Retrieves paginated pull requests with lifecycle and review wait time calculations.")
    public ResponseEntity<Page<PullRequestSummaryDto>> getPullRequests(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(analyticsService.getPullRequests(id, page, size));
    }

    @GetMapping("/issues")
    @Operation(summary = "Get repository issues", description = "Retrieves paginated issues with categorization and resolution times.")
    public ResponseEntity<Page<IssueSummaryDto>> getIssues(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(analyticsService.getIssues(id, page, size));
    }

    @GetMapping("/releases")
    @Operation(summary = "Get repository releases", description = "Retrieves paginated releases and publication history.")
    public ResponseEntity<Page<ReleaseEntity>> getReleases(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(analyticsService.getReleases(id, page, size));
    }
}

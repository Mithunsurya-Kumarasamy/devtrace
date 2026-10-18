package com.devtrace.controller;

import com.devtrace.dto.analytics.*;
import com.devtrace.dto.churn.CodebaseHotspotsReportDto;
import com.devtrace.dto.churn.RepositoryChurnReportDto;
import com.devtrace.dto.processmining.ProcessMiningReportDto;
import com.devtrace.entity.ReleaseEntity;
import com.devtrace.service.analytics.CoreAnalyticsService;
import com.devtrace.service.churn.ChurnAnalyticsService;
import com.devtrace.service.processmining.ProcessMiningService;
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
    private final ProcessMiningService processMiningService;
    private final ChurnAnalyticsService churnAnalyticsService;

    public AnalyticsController(CoreAnalyticsService analyticsService,
                               ProcessMiningService processMiningService,
                               ChurnAnalyticsService churnAnalyticsService) {
        this.analyticsService = analyticsService;
        this.processMiningService = processMiningService;
        this.churnAnalyticsService = churnAnalyticsService;
    }

    @GetMapping("/analytics/churn")
    @Operation(summary = "Get repository code churn analysis", description = "Calculates additions, deletions, net growth, and file/directory churn across a time window.")
    public ResponseEntity<RepositoryChurnReportDto> getChurn(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "30") int days) {
        return ResponseEntity.ok(churnAnalyticsService.calculateChurn(id, days));
    }

    @GetMapping("/analytics/hotspots")
    @Operation(summary = "Get codebase hotspots", description = "Identifies architectural hotspots by combining churn volume, modification frequency, defect association, and recency weighting.")
    public ResponseEntity<CodebaseHotspotsReportDto> getHotspots(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "90") int days,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return ResponseEntity.ok(churnAnalyticsService.calculateHotspots(id, days, limit));
    }

    @GetMapping("/analytics/process-mining")
    @Operation(summary = "Get PR lifecycle process mining report", description = "Analyzes PR stage transitions, bottleneck identification, process variants, and rework cycles.")
    public ResponseEntity<ProcessMiningReportDto> getProcessMining(@PathVariable String id) {
        return ResponseEntity.ok(processMiningService.analyzeProcessMining(id));
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

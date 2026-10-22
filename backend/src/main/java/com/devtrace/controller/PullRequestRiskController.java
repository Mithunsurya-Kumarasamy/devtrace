package com.devtrace.controller;

import com.devtrace.dto.risk.PullRequestRiskAssessmentDto;
import com.devtrace.service.risk.PullRequestRiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pull-requests/{id}/risk")
@Tag(name = "Pull Request Risk", description = "Explainable pull request risk estimation and quality guardrails")
public class PullRequestRiskController {

    private final PullRequestRiskService riskService;

    public PullRequestRiskController(PullRequestRiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    @Operation(summary = "Get pull request risk estimation", description = "Returns explainable risk estimation based on changeset size, author experience, hotspot exposure, review coverage, and submission timing.")
    public ResponseEntity<PullRequestRiskAssessmentDto> getRisk(@PathVariable String id) {
        return ResponseEntity.ok(riskService.getRiskAssessment(id));
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate and persist pull request risk", description = "Forces re-evaluation of PR risk factors, persists prediction to database, and applies quality guardrails.")
    public ResponseEntity<PullRequestRiskAssessmentDto> evaluateRisk(@PathVariable String id) {
        return ResponseEntity.ok(riskService.evaluateAndPersistRisk(id));
    }
}

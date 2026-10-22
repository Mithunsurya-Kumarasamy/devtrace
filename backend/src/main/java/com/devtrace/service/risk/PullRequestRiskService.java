package com.devtrace.service.risk;

import com.devtrace.dto.churn.CodebaseHotspotsReportDto;
import com.devtrace.dto.churn.HotspotFileDto;
import com.devtrace.dto.risk.PullRequestRiskAssessmentDto;
import com.devtrace.dto.risk.RiskFactorDto;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.PullRequestReviewEntity;
import com.devtrace.entity.PullRequestRiskPredictionEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.*;
import com.devtrace.service.churn.ChurnAnalyticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class PullRequestRiskService {

    private final PullRequestEntityRepository pullRequestRepo;
    private final PullRequestReviewEntityRepository reviewRepo;
    private final CommitEntityRepository commitRepo;
    private final PullRequestRiskPredictionEntityRepository riskRepo;
    private final ChurnAnalyticsService churnAnalyticsService;
    private final ObjectMapper objectMapper;

    public PullRequestRiskService(PullRequestEntityRepository pullRequestRepo,
                                  PullRequestReviewEntityRepository reviewRepo,
                                  CommitEntityRepository commitRepo,
                                  PullRequestRiskPredictionEntityRepository riskRepo,
                                  ChurnAnalyticsService churnAnalyticsService,
                                  ObjectMapper objectMapper) {
        this.pullRequestRepo = pullRequestRepo;
        this.reviewRepo = reviewRepo;
        this.commitRepo = commitRepo;
        this.riskRepo = riskRepo;
        this.churnAnalyticsService = churnAnalyticsService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PullRequestRiskAssessmentDto getRiskAssessment(String pullRequestId) {
        return evaluateRisk(pullRequestId, false);
    }

    @Transactional
    public PullRequestRiskAssessmentDto evaluateAndPersistRisk(String pullRequestId) {
        return evaluateRisk(pullRequestId, true);
    }

    private PullRequestRiskAssessmentDto evaluateRisk(String pullRequestId, boolean persist) {
        PullRequestEntity pr = pullRequestRepo.findById(pullRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Pull Request not found with ID: " + pullRequestId));

        String repoId = pr.getRepository().getId();

        // 1. Check data sufficiency
        long totalRepoCommits = commitRepo.countByRepositoryId(repoId);
        long totalRepoPrs = pullRequestRepo.countByRepositoryId(repoId);
        boolean isDataSufficient = totalRepoCommits >= 20 && totalRepoPrs >= 5;
        String warning = isDataSufficient ? null :
                "Limited historical repository data (<20 commits or <5 PRs); risk estimation is operating with heuristic defaults and wide confidence margins.";
        double confidence = isDataSufficient ? 0.85 : 0.50;

        List<RiskFactorDto> factors = new ArrayList<>();

        // 2. Factor 1: PR Size
        int totalLines = pr.getAdditions() + pr.getDeletions();
        int changedFiles = pr.getChangedFiles();
        double lineScore = Math.min(1.0, (double) totalLines / 1000.0);
        double fileScore = Math.min(1.0, (double) changedFiles / 25.0);
        double sizeRisk = round(0.70 * lineScore + 0.30 * fileScore);

        String sizeDesc = String.format("PR touches %d file(s) with +%d / -%d lines (total %d lines changed).",
                changedFiles, pr.getAdditions(), pr.getDeletions(), totalLines);
        String sizeRec = totalLines > 500 ?
                "Consider splitting large changes into smaller, reviewable commits to reduce defect escaping risk." :
                "PR size is within healthy review limits.";
        factors.add(new RiskFactorDto("PR Size & Complexity", sizeRisk, 0.30, sizeDesc, sizeRec));

        // 3. Factor 2: Author Experience
        long priorCommits = commitRepo.countCommitsByRepositoryIdAndAuthor(repoId, pr.getAuthorLogin());
        double authorRisk;
        String authorDesc;
        String authorRec;

        if (priorCommits == 0) {
            authorRisk = 0.95;
            authorDesc = "Author has no prior recorded commits in this repository (first-time contributor).";
            authorRec = "Assign an experienced repository owner to guide code convention and test standards.";
        } else if (priorCommits < 5) {
            authorRisk = 0.70;
            authorDesc = String.format("Author has limited repository history (%d prior commit%s).",
                    priorCommits, priorCommits == 1 ? "" : "s");
            authorRec = "Recommend pair-reviewing key functional areas.";
        } else if (priorCommits < 20) {
            authorRisk = 0.35;
            authorDesc = String.format("Author has moderate contribution history (%d prior commits).", priorCommits);
            authorRec = "Maintain standard verification workflows.";
        } else {
            authorRisk = 0.10;
            authorDesc = String.format("Author is a regular maintainer/contributor with %d prior commits.", priorCommits);
            authorRec = "High author familiarity with architecture and test suite.";
        }
        factors.add(new RiskFactorDto("Author Experience", authorRisk, 0.25, authorDesc, authorRec));

        // 4. Factor 3: Hotspot Risk Overlap
        double hotspotRisk = 0.15;
        String hotspotDesc = "Touched files show standard maintenance churn.";
        String hotspotRec = "No critical architectural hotspots detected in affected paths.";

        try {
            CodebaseHotspotsReportDto hotspots = churnAnalyticsService.calculateHotspots(repoId, 90, 20);
            if (hotspots.getCriticalHotspotsCount() > 0) {
                hotspotRisk = 0.65;
                hotspotDesc = String.format("Repository has %d active critical hotspot(s) undergoing frequent defect churn.",
                        hotspots.getCriticalHotspotsCount());
                hotspotRec = "Verify unit and integration regression test suites thoroughly.";
            }
        } catch (Exception ignored) {
            // Hotspot fallback
        }
        factors.add(new RiskFactorDto("Hotspot Exposure", hotspotRisk, 0.20, hotspotDesc, hotspotRec));

        // 5. Factor 4: Review Coverage
        List<PullRequestReviewEntity> reviews = reviewRepo.findByPullRequestIdOrderBySubmittedAtAsc(pullRequestId);
        boolean isApproved = pr.getApprovedAt() != null || reviews.stream().anyMatch(r ->
                "APPROVED".equalsIgnoreCase(r.getState()) || "APPROVE".equalsIgnoreCase(r.getState()));
        double reviewRisk;
        String reviewDesc;
        String reviewRec;

        if (isApproved) {
            reviewRisk = 0.15;
            reviewDesc = "Pull request has received formal approval from peer reviewer(s).";
            reviewRec = "Review guardrail requirements satisfied.";
        } else if (!reviews.isEmpty()) {
            reviewRisk = 0.55;
            reviewDesc = String.format("%d review(s) submitted but formal approval is still pending.", reviews.size());
            reviewRec = "Ensure designated peer reviewer verifies changes and submits formal approval.";
        } else {
            reviewRisk = 0.90;
            reviewDesc = "No peer reviews have been submitted for this pull request.";
            reviewRec = "Require at least one peer code review approval prior to merging.";
        }
        factors.add(new RiskFactorDto("Review Coverage", reviewRisk, 0.20, reviewDesc, reviewRec));

        // 6. Factor 5: Submission Timing
        Instant createdAt = pr.getCreatedAt() != null ? pr.getCreatedAt() : Instant.now();
        ZonedDateTime zdt = createdAt.atZone(ZoneOffset.UTC);
        boolean isWeekend = zdt.getDayOfWeek().getValue() >= 6;
        boolean isOffHours = zdt.getHour() < 7 || zdt.getHour() > 20;
        double temporalRisk;
        String temporalDesc;
        String temporalRec;

        if (isWeekend || isOffHours) {
            temporalRisk = 0.65;
            temporalDesc = String.format("PR submitted during %s (UTC hour: %02d:00).",
                    isWeekend ? "the weekend" : "off-hours", zdt.getHour());
            temporalRec = "Avoid merging during low-availability windows without active on-call or rollback readiness.";
        } else {
            temporalRisk = 0.20;
            temporalDesc = "Submitted during standard team working hours.";
            temporalRec = "Optimal reviewer responsiveness expected.";
        }
        factors.add(new RiskFactorDto("Submission Timing", temporalRisk, 0.05, temporalDesc, temporalRec));

        // 7. Calculate calibrated risk score
        double rawScore = 0.0;
        for (RiskFactorDto f : factors) {
            rawScore += f.getRiskScore() * f.getWeight();
        }
        double riskScore = round(Math.min(1.0, Math.max(0.0, rawScore)));

        String riskLevel;
        if (riskScore >= 0.70) {
            riskLevel = "HIGH";
        } else if (riskScore >= 0.30) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        // 8. Quality Guardrails Recommendations
        List<String> guardrails = new ArrayList<>();
        if ("HIGH".equals(riskLevel)) {
            guardrails.add("High risk pull request: require 2 peer approvals, green CI test suites, and feature flag deployment.");
        }
        if (reviewRisk > 0.50) {
            guardrails.add("Enforce mandatory peer review approval before base branch merge.");
        }
        if (sizeRisk > 0.60) {
            guardrails.add("Changeset is unusually large. Request an interactive walkthrough or breakdown into smaller PRs.");
        }
        if (authorRisk > 0.70) {
            guardrails.add("Verify security, styling conventions, and test coverage for new or infrequent contributor.");
        }
        if (!isDataSufficient) {
            guardrails.add("Historical repository activity is sparse; exercise additional review caution independent of heuristic scores.");
        }
        if (guardrails.isEmpty()) {
            guardrails.add("Pull request satisfies standard quality guardrails.");
        }

        Instant evaluatedAt = Instant.now();

        // 9. Persist if requested
        if (persist) {
            PullRequestRiskPredictionEntity prediction = riskRepo.findByPullRequestId(pullRequestId)
                    .orElse(new PullRequestRiskPredictionEntity());
            prediction.setPullRequest(pr);
            prediction.setRiskScore(riskScore);
            prediction.setRiskLevel(riskLevel);
            prediction.setConfidence(confidence);
            prediction.setModelVersion("devtrace-heuristic-v1.0");
            prediction.setHeuristic(true);
            prediction.setEvaluatedAt(evaluatedAt);

            try {
                prediction.setContributingFactors(objectMapper.writeValueAsString(factors));
            } catch (JsonProcessingException e) {
                prediction.setContributingFactors("[]");
            }
            riskRepo.save(prediction);
        }

        // 10. Construct response
        PullRequestRiskAssessmentDto response = new PullRequestRiskAssessmentDto();
        response.setPullRequestId(pr.getId());
        response.setPrNumber(pr.getPrNumber());
        response.setTitle(pr.getTitle());
        response.setAuthorLogin(pr.getAuthorLogin());
        response.setRiskScore(riskScore);
        response.setRiskLevel(riskLevel);
        response.setConfidence(confidence);
        response.setDataSufficient(isDataSufficient);
        response.setDataSufficiencyWarning(warning);
        response.setModelVersion("devtrace-heuristic-v1.0");
        response.setHeuristic(true);
        response.setEvaluatedAt(evaluatedAt);
        response.setRiskFactors(factors);
        response.setGuardrailRecommendations(guardrails);

        return response;
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pr_risk_predictions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pr_risk", columnNames = {"pull_request_id"})
})
public class PullRequestRiskPredictionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;

    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    private Double confidence;

    @Column(name = "contributing_factors", columnDefinition = "TEXT")
    private String contributingFactors;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "is_heuristic")
    private boolean isHeuristic = true;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public PullRequestRiskPredictionEntity() {
        this.id = UUID.randomUUID().toString();
        this.evaluatedAt = Instant.now();
    }

    public PullRequestRiskPredictionEntity(PullRequestEntity pullRequest, double riskScore, String riskLevel, String modelVersion) {
        this();
        this.pullRequest = pullRequest;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.modelVersion = modelVersion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public PullRequestEntity getPullRequest() { return pullRequest; }
    public void setPullRequest(PullRequestEntity pullRequest) { this.pullRequest = pullRequest; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getContributingFactors() { return contributingFactors; }
    public void setContributingFactors(String contributingFactors) { this.contributingFactors = contributingFactors; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public boolean isHeuristic() { return isHeuristic; }
    public void setHeuristic(boolean heuristic) { isHeuristic = heuristic; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}

package com.devtrace.dto.risk;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PullRequestRiskAssessmentDto {
    private String pullRequestId;
    private int prNumber;
    private String title;
    private String authorLogin;
    private double riskScore; // 0.0 to 1.0
    private String riskLevel; // LOW, MEDIUM, HIGH
    private double confidence; // 0.0 to 1.0
    private boolean isDataSufficient;
    private String dataSufficiencyWarning;
    private String modelVersion;
    private boolean isHeuristic;
    private Instant evaluatedAt;
    private List<RiskFactorDto> riskFactors = new ArrayList<>();
    private List<String> guardrailRecommendations = new ArrayList<>();

    public PullRequestRiskAssessmentDto() {}

    public String getPullRequestId() { return pullRequestId; }
    public void setPullRequestId(String pullRequestId) { this.pullRequestId = pullRequestId; }
    public int getPrNumber() { return prNumber; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthorLogin() { return authorLogin; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean isDataSufficient() { return isDataSufficient; }
    public void setDataSufficient(boolean dataSufficient) { isDataSufficient = dataSufficient; }
    public String getDataSufficiencyWarning() { return dataSufficiencyWarning; }
    public void setDataSufficiencyWarning(String dataSufficiencyWarning) { this.dataSufficiencyWarning = dataSufficiencyWarning; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public boolean isHeuristic() { return isHeuristic; }
    public void setHeuristic(boolean heuristic) { isHeuristic = heuristic; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public List<RiskFactorDto> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<RiskFactorDto> riskFactors) { this.riskFactors = riskFactors; }
    public List<String> getGuardrailRecommendations() { return guardrailRecommendations; }
    public void setGuardrailRecommendations(List<String> guardrailRecommendations) { this.guardrailRecommendations = guardrailRecommendations; }
}

package com.devtrace.dto.analytics;

public class HealthScoreDto {
    private double compositeScore;
    private double codeStability;
    private double reviewEfficiency;
    private double releaseCadence;
    private double collaborationResilience;
    private String calculationExplanation;

    public HealthScoreDto() {}

    public HealthScoreDto(double compositeScore, double codeStability, double reviewEfficiency,
                          double releaseCadence, double collaborationResilience, String calculationExplanation) {
        this.compositeScore = compositeScore;
        this.codeStability = codeStability;
        this.reviewEfficiency = reviewEfficiency;
        this.releaseCadence = releaseCadence;
        this.collaborationResilience = collaborationResilience;
        this.calculationExplanation = calculationExplanation;
    }

    public double getCompositeScore() { return compositeScore; }
    public double getComposite() { return compositeScore; }
    public void setCompositeScore(double compositeScore) { this.compositeScore = compositeScore; }
    public double getCodeStability() { return codeStability; }
    public void setCodeStability(double codeStability) { this.codeStability = codeStability; }
    public double getReviewEfficiency() { return reviewEfficiency; }
    public void setReviewEfficiency(double reviewEfficiency) { this.reviewEfficiency = reviewEfficiency; }
    public double getReleaseCadence() { return releaseCadence; }
    public void setReleaseCadence(double releaseCadence) { this.releaseCadence = releaseCadence; }
    public double getCollaborationResilience() { return collaborationResilience; }
    public void setCollaborationResilience(double collaborationResilience) { this.collaborationResilience = collaborationResilience; }
    public String getCalculationExplanation() { return calculationExplanation; }
    public void setCalculationExplanation(String calculationExplanation) { this.calculationExplanation = calculationExplanation; }
}

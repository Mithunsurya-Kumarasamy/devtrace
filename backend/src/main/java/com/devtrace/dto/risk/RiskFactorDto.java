package com.devtrace.dto.risk;

public class RiskFactorDto {
    private String factorName;
    private double riskScore;
    private double weight;
    private String description;
    private String recommendation;

    public RiskFactorDto() {}

    public RiskFactorDto(String factorName, double riskScore, double weight, String description, String recommendation) {
        this.factorName = factorName;
        this.riskScore = riskScore;
        this.weight = weight;
        this.description = description;
        this.recommendation = recommendation;
    }

    public String getFactorName() { return factorName; }
    public void setFactorName(String factorName) { this.factorName = factorName; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}

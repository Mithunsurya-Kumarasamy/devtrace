package com.devtrace.dto.processmining;

public class BottleneckAnalysisDto {
    private String stageKey;
    private String stageName;
    private double percentageOfCycleTime;
    private double medianDurationHours;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String impactSummary;
    private String recommendation;

    public BottleneckAnalysisDto() {}

    public BottleneckAnalysisDto(String stageKey, String stageName, double percentageOfCycleTime,
                                 double medianDurationHours, String severity, String impactSummary,
                                 String recommendation) {
        this.stageKey = stageKey;
        this.stageName = stageName;
        this.percentageOfCycleTime = percentageOfCycleTime;
        this.medianDurationHours = medianDurationHours;
        this.severity = severity;
        this.impactSummary = impactSummary;
        this.recommendation = recommendation;
    }

    public String getStageKey() { return stageKey; }
    public void setStageKey(String stageKey) { this.stageKey = stageKey; }
    public String getStageName() { return stageName; }
    public String getStage() { return stageName != null ? stageName : stageKey; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public double getPercentageOfCycleTime() { return percentageOfCycleTime; }
    public double getRatioOfCycleTime() { return percentageOfCycleTime / 100.0; }
    public void setPercentageOfCycleTime(double percentageOfCycleTime) { this.percentageOfCycleTime = percentageOfCycleTime; }
    public double getMedianDurationHours() { return medianDurationHours; }
    public double getMedianWaitHours() { return medianDurationHours; }
    public void setMedianDurationHours(double medianDurationHours) { this.medianDurationHours = medianDurationHours; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getImpactSummary() { return impactSummary; }
    public void setImpactSummary(String impactSummary) { this.impactSummary = impactSummary; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}

package com.devtrace.dto.processmining;

public class ProcessMiningStageDto {
    private String stageKey;
    private String stageName;
    private String description;
    private double medianDurationHours;
    private double p75DurationHours;
    private double p90DurationHours;
    private double averageDurationHours;
    private double percentageOfTotalCycle;
    private int sampleCount;
    private boolean isBottleneck;

    public ProcessMiningStageDto() {}

    public ProcessMiningStageDto(String stageKey, String stageName, String description, double medianDurationHours,
                                 double p75DurationHours, double p90DurationHours, double averageDurationHours,
                                 double percentageOfTotalCycle, int sampleCount, boolean isBottleneck) {
        this.stageKey = stageKey;
        this.stageName = stageName;
        this.description = description;
        this.medianDurationHours = medianDurationHours;
        this.p75DurationHours = p75DurationHours;
        this.p90DurationHours = p90DurationHours;
        this.averageDurationHours = averageDurationHours;
        this.percentageOfTotalCycle = percentageOfTotalCycle;
        this.sampleCount = sampleCount;
        this.isBottleneck = isBottleneck;
    }

    public String getStageKey() { return stageKey; }
    public String getStage() { return stageKey; }
    public void setStageKey(String stageKey) { this.stageKey = stageKey; }
    public String getStageName() { return stageName; }
    public String getDisplayName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMedianDurationHours() { return medianDurationHours; }
    public void setMedianDurationHours(double medianDurationHours) { this.medianDurationHours = medianDurationHours; }
    public double getP75DurationHours() { return p75DurationHours; }
    public void setP75DurationHours(double p75DurationHours) { this.p75DurationHours = p75DurationHours; }
    public double getP90DurationHours() { return p90DurationHours; }
    public void setP90DurationHours(double p90DurationHours) { this.p90DurationHours = p90DurationHours; }
    public double getAverageDurationHours() { return averageDurationHours; }
    public void setAverageDurationHours(double averageDurationHours) { this.averageDurationHours = averageDurationHours; }
    public double getPercentageOfTotalCycle() { return percentageOfTotalCycle; }
    public void setPercentageOfTotalCycle(double percentageOfTotalCycle) { this.percentageOfTotalCycle = percentageOfTotalCycle; }
    public int getSampleCount() { return sampleCount; }
    public int getTransitionCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
    public boolean isBottleneck() { return isBottleneck; }
    public void setBottleneck(boolean bottleneck) { isBottleneck = bottleneck; }
}

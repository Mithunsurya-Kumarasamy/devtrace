package com.devtrace.dto.analytics;

public class PercentileDistributionDto {
    private double min;
    private double p50; // Median
    private double p75;
    private double p90;
    private double max;
    private double average;

    public PercentileDistributionDto() {}

    public PercentileDistributionDto(double min, double p50, double p75, double p90, double max, double average) {
        this.min = min;
        this.p50 = p50;
        this.p75 = p75;
        this.p90 = p90;
        this.max = max;
        this.average = average;
    }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }
    public double getP50() { return p50; }
    public void setP50(double p50) { this.p50 = p50; }
    public double getMedian() { return p50; }
    public void setMedian(double median) { this.p50 = median; }
    public double getP75() { return p75; }
    public void setP75(double p75) { this.p75 = p75; }
    public double getP90() { return p90; }
    public void setP90(double p90) { this.p90 = p90; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }
}

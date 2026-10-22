package com.devtrace.dto.anomaly;

public class MetricBaselineDto {
    private String metricName;
    private double mean;
    private double standardDeviation;
    private double median;
    private double q1;
    private double q3;
    private double iqr;
    private double lowerBound;
    private double upperBound;
    private int sampleCount;

    public MetricBaselineDto() {}

    public MetricBaselineDto(String metricName, double mean, double standardDeviation, double median,
                             double q1, double q3, double iqr, double lowerBound, double upperBound,
                             int sampleCount) {
        this.metricName = metricName;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.median = median;
        this.q1 = q1;
        this.q3 = q3;
        this.iqr = iqr;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.sampleCount = sampleCount;
    }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public double getMean() { return mean; }
    public void setMean(double mean) { this.mean = mean; }
    public double getStandardDeviation() { return standardDeviation; }
    public void setStandardDeviation(double standardDeviation) { this.standardDeviation = standardDeviation; }
    public double getMedian() { return median; }
    public void setMedian(double median) { this.median = median; }
    public double getQ1() { return q1; }
    public void setQ1(double q1) { this.q1 = q1; }
    public double getQ3() { return q3; }
    public void setQ3(double q3) { this.q3 = q3; }
    public double getIqr() { return iqr; }
    public void setIqr(double iqr) { this.iqr = iqr; }
    public double getLowerBound() { return lowerBound; }
    public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }
    public double getUpperBound() { return upperBound; }
    public void setUpperBound(double upperBound) { this.upperBound = upperBound; }
    public int getSampleCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
}

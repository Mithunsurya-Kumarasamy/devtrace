package com.devtrace.dto.anomaly;

import java.time.Instant;

public class AnomalyRecordDto {
    private String id;
    private String metricName;
    private double observedValue;
    private double expectedLower;
    private double expectedUpper;
    private Double baselineMean;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String detectionMethod; // Z_SCORE, IQR, ROLLING_BASELINE
    private String explanation;
    private Instant detectionTimestamp;

    public AnomalyRecordDto() {}

    public AnomalyRecordDto(String id, String metricName, double observedValue, double expectedLower,
                            double expectedUpper, Double baselineMean, String severity,
                            String detectionMethod, String explanation, Instant detectionTimestamp) {
        this.id = id;
        this.metricName = metricName;
        this.observedValue = observedValue;
        this.expectedLower = expectedLower;
        this.expectedUpper = expectedUpper;
        this.baselineMean = baselineMean;
        this.severity = severity;
        this.detectionMethod = detectionMethod;
        this.explanation = explanation;
        this.detectionTimestamp = detectionTimestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public double getObservedValue() { return observedValue; }
    public void setObservedValue(double observedValue) { this.observedValue = observedValue; }
    public double getExpectedLower() { return expectedLower; }
    public void setExpectedLower(double expectedLower) { this.expectedLower = expectedLower; }
    public double getExpectedUpper() { return expectedUpper; }
    public void setExpectedUpper(double expectedUpper) { this.expectedUpper = expectedUpper; }
    public Double getBaselineMean() { return baselineMean; }
    public void setBaselineMean(Double baselineMean) { this.baselineMean = baselineMean; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDetectionMethod() { return detectionMethod; }
    public void setDetectionMethod(String detectionMethod) { this.detectionMethod = detectionMethod; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public Instant getDetectionTimestamp() { return detectionTimestamp; }
    public void setDetectionTimestamp(Instant detectionTimestamp) { this.detectionTimestamp = detectionTimestamp; }
}

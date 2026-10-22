package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomaly_records")
public class AnomalyRecordEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "observed_value", nullable = false)
    private double observedValue;

    @Column(name = "expected_lower", nullable = false)
    private double expectedLower;

    @Column(name = "expected_upper", nullable = false)
    private double expectedUpper;

    @Column(name = "baseline_mean")
    private Double baselineMean;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "detection_method", nullable = false, length = 50)
    private String detectionMethod;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "detection_timestamp", nullable = false)
    private Instant detectionTimestamp;

    public AnomalyRecordEntity() {
        this.id = UUID.randomUUID().toString();
        this.detectionTimestamp = Instant.now();
    }

    public AnomalyRecordEntity(RepositoryEntity repository, String metricName, double observedValue,
                               double expectedLower, double expectedUpper, String severity,
                               String detectionMethod, String explanation) {
        this();
        this.repository = repository;
        this.metricName = metricName;
        this.observedValue = observedValue;
        this.expectedLower = expectedLower;
        this.expectedUpper = expectedUpper;
        this.severity = severity;
        this.detectionMethod = detectionMethod;
        this.explanation = explanation;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public RepositoryEntity getRepository() { return repository; }
    public void setRepository(RepositoryEntity repository) { this.repository = repository; }

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

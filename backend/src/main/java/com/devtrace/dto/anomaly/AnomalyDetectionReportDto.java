package com.devtrace.dto.anomaly;

import java.util.ArrayList;
import java.util.List;

public class AnomalyDetectionReportDto {
    private String repositoryId;
    private int totalAnomaliesCount;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private List<MetricBaselineDto> baselines = new ArrayList<>();
    private List<AnomalyRecordDto> recentAnomalies = new ArrayList<>();

    public AnomalyDetectionReportDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTotalAnomaliesCount() { return totalAnomaliesCount; }
    public void setTotalAnomaliesCount(int totalAnomaliesCount) { this.totalAnomaliesCount = totalAnomaliesCount; }
    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }
    public int getHighCount() { return highCount; }
    public void setHighCount(int highCount) { this.highCount = highCount; }
    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int mediumCount) { this.mediumCount = mediumCount; }
    public int getLowCount() { return lowCount; }
    public void setLowCount(int lowCount) { this.lowCount = lowCount; }
    public List<MetricBaselineDto> getBaselines() { return baselines; }
    public void setBaselines(List<MetricBaselineDto> baselines) { this.baselines = baselines; }
    public List<AnomalyRecordDto> getRecentAnomalies() { return recentAnomalies; }
    public void setRecentAnomalies(List<AnomalyRecordDto> recentAnomalies) { this.recentAnomalies = recentAnomalies; }
}

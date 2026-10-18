package com.devtrace.dto.churn;

import java.util.ArrayList;
import java.util.List;

public class CodebaseHotspotsReportDto {
    private String repositoryId;
    private int timeWindowDays;
    private int totalAnalyzedFiles;
    private int criticalHotspotsCount;
    private int highHotspotsCount;
    private int mediumHotspotsCount;
    private int lowHotspotsCount;
    private List<HotspotFileDto> hotspots = new ArrayList<>();
    private List<String> architecturalRecommendations = new ArrayList<>();

    public CodebaseHotspotsReportDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTimeWindowDays() { return timeWindowDays; }
    public void setTimeWindowDays(int timeWindowDays) { this.timeWindowDays = timeWindowDays; }
    public int getTotalAnalyzedFiles() { return totalAnalyzedFiles; }
    public void setTotalAnalyzedFiles(int totalAnalyzedFiles) { this.totalAnalyzedFiles = totalAnalyzedFiles; }
    public int getCriticalHotspotsCount() { return criticalHotspotsCount; }
    public void setCriticalHotspotsCount(int criticalHotspotsCount) { this.criticalHotspotsCount = criticalHotspotsCount; }
    public int getHighHotspotsCount() { return highHotspotsCount; }
    public void setHighHotspotsCount(int highHotspotsCount) { this.highHotspotsCount = highHotspotsCount; }
    public int getMediumHotspotsCount() { return mediumHotspotsCount; }
    public void setMediumHotspotsCount(int mediumHotspotsCount) { this.mediumHotspotsCount = mediumHotspotsCount; }
    public int getLowHotspotsCount() { return lowHotspotsCount; }
    public void setLowHotspotsCount(int lowHotspotsCount) { this.lowHotspotsCount = lowHotspotsCount; }
    public List<HotspotFileDto> getHotspots() { return hotspots; }
    public void setHotspots(List<HotspotFileDto> hotspots) { this.hotspots = hotspots; }
    public List<String> getArchitecturalRecommendations() { return architecturalRecommendations; }
    public void setArchitecturalRecommendations(List<String> architecturalRecommendations) { this.architecturalRecommendations = architecturalRecommendations; }
}

package com.devtrace.dto.churn;

import java.time.Instant;

public class HotspotFileDto {
    private String filename;
    private String directory;
    private double hotspotScore;
    private String riskLevel; // CRITICAL, HIGH, MEDIUM, LOW
    private long modificationCount;
    private long additions;
    private long deletions;
    private long totalChurn;
    private int defectCommitCount;
    private double defectRatio;
    private double recencyWeightedScore;
    private String primaryContributor;
    private int distinctAuthorsCount;
    private String explanation;
    private Instant lastModifiedAt;

    public HotspotFileDto() {}

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
    public double getHotspotScore() { return hotspotScore; }
    public void setHotspotScore(double hotspotScore) { this.hotspotScore = hotspotScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public long getModificationCount() { return modificationCount; }
    public void setModificationCount(long modificationCount) { this.modificationCount = modificationCount; }
    public long getAdditions() { return additions; }
    public void setAdditions(long additions) { this.additions = additions; }
    public long getDeletions() { return deletions; }
    public void setDeletions(long deletions) { this.deletions = deletions; }
    public long getTotalChurn() { return totalChurn; }
    public void setTotalChurn(long totalChurn) { this.totalChurn = totalChurn; }
    public int getDefectCommitCount() { return defectCommitCount; }
    public void setDefectCommitCount(int defectCommitCount) { this.defectCommitCount = defectCommitCount; }
    public double getDefectRatio() { return defectRatio; }
    public void setDefectRatio(double defectRatio) { this.defectRatio = defectRatio; }
    public double getRecencyWeightedScore() { return recencyWeightedScore; }
    public void setRecencyWeightedScore(double recencyWeightedScore) { this.recencyWeightedScore = recencyWeightedScore; }
    public String getPrimaryContributor() { return primaryContributor; }
    public void setPrimaryContributor(String primaryContributor) { this.primaryContributor = primaryContributor; }
    public int getDistinctAuthorsCount() { return distinctAuthorsCount; }
    public void setDistinctAuthorsCount(int distinctAuthorsCount) { this.distinctAuthorsCount = distinctAuthorsCount; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public Instant getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(Instant lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}

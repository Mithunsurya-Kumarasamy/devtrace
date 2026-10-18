package com.devtrace.dto.collaboration;

public class KnowledgeSiloDto {
    private String path;
    private String siloType; // SINGLE_CONTRIBUTOR_FILE, ISOLATED_MODULE
    private String soleContributor;
    private long commitCount;
    private long totalChanges;
    private String riskSeverity; // CRITICAL, HIGH, MEDIUM
    private String recommendation;

    public KnowledgeSiloDto() {}

    public KnowledgeSiloDto(String path, String siloType, String soleContributor, long commitCount,
                            long totalChanges, String riskSeverity, String recommendation) {
        this.path = path;
        this.siloType = siloType;
        this.soleContributor = soleContributor;
        this.commitCount = commitCount;
        this.totalChanges = totalChanges;
        this.riskSeverity = riskSeverity;
        this.recommendation = recommendation;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getSiloType() { return siloType; }
    public void setSiloType(String siloType) { this.siloType = siloType; }
    public String getSoleContributor() { return soleContributor; }
    public void setSoleContributor(String soleContributor) { this.soleContributor = soleContributor; }
    public long getCommitCount() { return commitCount; }
    public void setCommitCount(long commitCount) { this.commitCount = commitCount; }
    public long getTotalChanges() { return totalChanges; }
    public void setTotalChanges(long totalChanges) { this.totalChanges = totalChanges; }
    public String getRiskSeverity() { return riskSeverity; }
    public void setRiskSeverity(String riskSeverity) { this.riskSeverity = riskSeverity; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}

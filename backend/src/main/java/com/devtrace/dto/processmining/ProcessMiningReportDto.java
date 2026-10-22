package com.devtrace.dto.processmining;

import java.util.ArrayList;
import java.util.List;

public class ProcessMiningReportDto {
    private String repositoryId;
    private int totalAnalyzedPrs;
    private int mergedPrsCount;
    private int closedUnmergedPrsCount;
    private int openPrsCount;
    private double medianTotalCycleTimeHours;
    private List<ProcessMiningStageDto> stages = new ArrayList<>();
    private BottleneckAnalysisDto bottleneck;
    private List<ProcessVariantDto> variants = new ArrayList<>();
    private ChangesRequestedSummaryDto changesRequestedSummary;
    private List<PullRequestLifecycleTraceDto> recentTraces = new ArrayList<>();
    private List<String> missingDataLimitations = new ArrayList<>();

    public ProcessMiningReportDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTotalAnalyzedPrs() { return totalAnalyzedPrs; }
    public void setTotalAnalyzedPrs(int totalAnalyzedPrs) { this.totalAnalyzedPrs = totalAnalyzedPrs; }
    public int getMergedPrsCount() { return mergedPrsCount; }
    public void setMergedPrsCount(int mergedPrsCount) { this.mergedPrsCount = mergedPrsCount; }
    public int getClosedUnmergedPrsCount() { return closedUnmergedPrsCount; }
    public void setClosedUnmergedPrsCount(int closedUnmergedPrsCount) { this.closedUnmergedPrsCount = closedUnmergedPrsCount; }
    public int getOpenPrsCount() { return openPrsCount; }
    public void setOpenPrsCount(int openPrsCount) { this.openPrsCount = openPrsCount; }
    public double getMedianTotalCycleTimeHours() { return medianTotalCycleTimeHours; }
    public void setMedianTotalCycleTimeHours(double medianTotalCycleTimeHours) { this.medianTotalCycleTimeHours = medianTotalCycleTimeHours; }
    public List<ProcessMiningStageDto> getStages() { return stages; }
    public void setStages(List<ProcessMiningStageDto> stages) { this.stages = stages; }
    public BottleneckAnalysisDto getBottleneck() { return bottleneck; }
    public void setBottleneck(BottleneckAnalysisDto bottleneck) { this.bottleneck = bottleneck; }
    public List<ProcessVariantDto> getVariants() { return variants; }
    public void setVariants(List<ProcessVariantDto> variants) { this.variants = variants; }
    public ChangesRequestedSummaryDto getChangesRequestedSummary() { return changesRequestedSummary; }
    public void setChangesRequestedSummary(ChangesRequestedSummaryDto changesRequestedSummary) { this.changesRequestedSummary = changesRequestedSummary; }
    public List<PullRequestLifecycleTraceDto> getRecentTraces() { return recentTraces; }
    public void setRecentTraces(List<PullRequestLifecycleTraceDto> recentTraces) { this.recentTraces = recentTraces; }
    public List<String> getMissingDataLimitations() { return missingDataLimitations; }
    public void setMissingDataLimitations(List<String> missingDataLimitations) { this.missingDataLimitations = missingDataLimitations; }

    public List<BottleneckAnalysisDto> getBottlenecks() {
        if (bottleneck != null && !"NONE".equalsIgnoreCase(bottleneck.getStageKey())) {
            return List.of(bottleneck);
        }
        return java.util.Collections.emptyList();
    }

    public List<String> getLimitations() {
        return missingDataLimitations;
    }

    public int getFastTrackPrs() {
        if (variants != null) {
            for (ProcessVariantDto v : variants) {
                if ("FAST_TRACK".equalsIgnoreCase(v.getVariantKey())) {
                    return v.getCount();
                }
            }
        }
        return 0;
    }
}

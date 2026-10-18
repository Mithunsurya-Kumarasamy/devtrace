package com.devtrace.dto.processmining;

public class ChangesRequestedSummaryDto {
    private int totalPrsWithChangesRequested;
    private double percentageWithChangesRequested;
    private double averageIterations;
    private int maxIterations;

    public ChangesRequestedSummaryDto() {}

    public ChangesRequestedSummaryDto(int totalPrsWithChangesRequested, double percentageWithChangesRequested,
                                      double averageIterations, int maxIterations) {
        this.totalPrsWithChangesRequested = totalPrsWithChangesRequested;
        this.percentageWithChangesRequested = percentageWithChangesRequested;
        this.averageIterations = averageIterations;
        this.maxIterations = maxIterations;
    }

    public int getTotalPrsWithChangesRequested() { return totalPrsWithChangesRequested; }
    public void setTotalPrsWithChangesRequested(int totalPrsWithChangesRequested) { this.totalPrsWithChangesRequested = totalPrsWithChangesRequested; }
    public double getPercentageWithChangesRequested() { return percentageWithChangesRequested; }
    public void setPercentageWithChangesRequested(double percentageWithChangesRequested) { this.percentageWithChangesRequested = percentageWithChangesRequested; }
    public double getAverageIterations() { return averageIterations; }
    public void setAverageIterations(double averageIterations) { this.averageIterations = averageIterations; }
    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
}

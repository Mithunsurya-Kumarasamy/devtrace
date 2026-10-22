package com.devtrace.dto.churn;

public class DirectoryChurnDto {
    private String directoryPath;
    private int filesCount;
    private long totalModifications;
    private long additions;
    private long deletions;
    private long totalChanges;
    private double percentageOfTotalChurn;

    public DirectoryChurnDto() {}

    public DirectoryChurnDto(String directoryPath, int filesCount, long totalModifications,
                             long additions, long deletions, long totalChanges, double percentageOfTotalChurn) {
        this.directoryPath = directoryPath;
        this.filesCount = filesCount;
        this.totalModifications = totalModifications;
        this.additions = additions;
        this.deletions = deletions;
        this.totalChanges = totalChanges;
        this.percentageOfTotalChurn = percentageOfTotalChurn;
    }

    public String getDirectoryPath() { return directoryPath; }
    public void setDirectoryPath(String directoryPath) { this.directoryPath = directoryPath; }
    public int getFilesCount() { return filesCount; }
    public void setFilesCount(int filesCount) { this.filesCount = filesCount; }
    public long getTotalModifications() { return totalModifications; }
    public void setTotalModifications(long totalModifications) { this.totalModifications = totalModifications; }
    public long getAdditions() { return additions; }
    public void setAdditions(long additions) { this.additions = additions; }
    public long getDeletions() { return deletions; }
    public void setDeletions(long deletions) { this.deletions = deletions; }
    public long getTotalChanges() { return totalChanges; }
    public void setTotalChanges(long totalChanges) { this.totalChanges = totalChanges; }
    public double getPercentageOfTotalChurn() { return percentageOfTotalChurn; }
    public void setPercentageOfTotalChurn(double percentageOfTotalChurn) { this.percentageOfTotalChurn = percentageOfTotalChurn; }
}

package com.devtrace.dto.churn;

import java.time.Instant;

public class FileChurnDto {
    private String filename;
    private String directory;
    private long modificationCount;
    private long additions;
    private long deletions;
    private long totalChanges;
    private int distinctAuthorsCount;
    private Instant lastModifiedAt;

    public FileChurnDto() {}

    public FileChurnDto(String filename, String directory, long modificationCount, long additions,
                        long deletions, long totalChanges, int distinctAuthorsCount, Instant lastModifiedAt) {
        this.filename = filename;
        this.directory = directory;
        this.modificationCount = modificationCount;
        this.additions = additions;
        this.deletions = deletions;
        this.totalChanges = totalChanges;
        this.distinctAuthorsCount = distinctAuthorsCount;
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
    public long getModificationCount() { return modificationCount; }
    public void setModificationCount(long modificationCount) { this.modificationCount = modificationCount; }
    public long getAdditions() { return additions; }
    public void setAdditions(long additions) { this.additions = additions; }
    public long getDeletions() { return deletions; }
    public void setDeletions(long deletions) { this.deletions = deletions; }
    public long getTotalChanges() { return totalChanges; }
    public void setTotalChanges(long totalChanges) { this.totalChanges = totalChanges; }
    public int getDistinctAuthorsCount() { return distinctAuthorsCount; }
    public void setDistinctAuthorsCount(int distinctAuthorsCount) { this.distinctAuthorsCount = distinctAuthorsCount; }
    public Instant getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(Instant lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}

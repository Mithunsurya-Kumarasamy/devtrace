package com.devtrace.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "commit_files")
public class CommitFileEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_id", nullable = false)
    private CommitEntity commit;

    @Column(nullable = false, length = 1000)
    private String filename;

    @Column(length = 50)
    private String status;

    private int additions = 0;
    private int deletions = 0;
    private int changes = 0;

    @Column(name = "raw_patch", columnDefinition = "TEXT")
    private String rawPatch;

    public CommitFileEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public CommitFileEntity(CommitEntity commit, String filename, String status, int additions, int deletions, int changes) {
        this();
        this.commit = commit;
        this.filename = filename;
        this.status = status;
        this.additions = additions;
        this.deletions = deletions;
        this.changes = changes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CommitEntity getCommit() { return commit; }
    public void setCommit(CommitEntity commit) { this.commit = commit; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }

    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }

    public int getChanges() { return changes; }
    public void setChanges(int changes) { this.changes = changes; }

    public String getRawPatch() { return rawPatch; }
    public void setRawPatch(String rawPatch) { this.rawPatch = rawPatch; }
}

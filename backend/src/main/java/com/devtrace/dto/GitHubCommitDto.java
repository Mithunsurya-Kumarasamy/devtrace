package com.devtrace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommitDto {
    private String sha;
    private CommitInfo commit;
    private List<ParentInfo> parents;
    private CommitStats stats;
    private List<CommitFileInfo> files;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitInfo {
        private String message;
        private GitUser author;
        private GitUser committer;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public GitUser getAuthor() { return author; }
        public void setAuthor(GitUser author) { this.author = author; }
        public GitUser getCommitter() { return committer; }
        public void setCommitter(GitUser committer) { this.committer = committer; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitUser {
        private String name;
        private String email;
        private String date;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentInfo {
        private String sha;
        public String getSha() { return sha; }
        public void setSha(String sha) { this.sha = sha; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitStats {
        private int total;
        private int additions;
        private int deletions;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getAdditions() { return additions; }
        public void setAdditions(int additions) { this.additions = additions; }
        public int getDeletions() { return deletions; }
        public void setDeletions(int deletions) { this.deletions = deletions; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitFileInfo {
        private String filename;
        private String status;
        private int additions;
        private int deletions;
        private int changes;
        private String patch;

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
        public String getPatch() { return patch; }
        public void setPatch(String patch) { this.patch = patch; }
    }

    public String getSha() { return sha; }
    public void setSha(String sha) { this.sha = sha; }
    public CommitInfo getCommit() { return commit; }
    public void setCommit(CommitInfo commit) { this.commit = commit; }
    public List<ParentInfo> getParents() { return parents; }
    public void setParents(List<ParentInfo> parents) { this.parents = parents; }
    public CommitStats getStats() { return stats; }
    public void setStats(CommitStats stats) { this.stats = stats; }
    public List<CommitFileInfo> getFiles() { return files; }
    public void setFiles(List<CommitFileInfo> files) { this.files = files; }
}

package com.devtrace.service;

import com.devtrace.dto.*;
import com.devtrace.entity.*;
import com.devtrace.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IngestionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(IngestionOrchestrator.class);

    private final CommitEntityRepository commitRepo;
    private final CommitFileEntityRepository commitFileRepo;
    private final PullRequestEntityRepository pullRequestRepo;
    private final PullRequestReviewEntityRepository prReviewRepo;
    private final IssueEntityRepository issueRepo;
    private final ReleaseEntityRepository releaseRepo;
    private final SyncCheckpointEntityRepository checkpointRepo;

    public IngestionOrchestrator(
            CommitEntityRepository commitRepo,
            CommitFileEntityRepository commitFileRepo,
            PullRequestEntityRepository pullRequestRepo,
            PullRequestReviewEntityRepository prReviewRepo,
            IssueEntityRepository issueRepo,
            ReleaseEntityRepository releaseRepo,
            SyncCheckpointEntityRepository checkpointRepo) {
        this.commitRepo = commitRepo;
        this.commitFileRepo = commitFileRepo;
        this.pullRequestRepo = pullRequestRepo;
        this.prReviewRepo = prReviewRepo;
        this.issueRepo = issueRepo;
        this.releaseRepo = releaseRepo;
        this.checkpointRepo = checkpointRepo;
    }

    @Transactional
    public CommitEntity processCommit(RepositoryEntity repository, GitHubCommitDto dto) {
        return processCommit(repository, dto, repository.getDefaultBranch());
    }

    @Transactional
    public CommitEntity processCommit(RepositoryEntity repository, GitHubCommitDto dto, String branch) {
        String sha = dto.getSha();
        Optional<CommitEntity> existing = commitRepo.findByRepositoryIdAndSha(repository.getId(), sha);

        CommitEntity commit = existing.orElseGet(() -> {
            CommitEntity c = new CommitEntity();
            c.setRepository(repository);
            c.setSha(sha);
            return c;
        });

        String effectiveBranch = (branch != null && !branch.isBlank()) ? branch : repository.getDefaultBranch();
        if (effectiveBranch != null && !effectiveBranch.isBlank()) {
            commit.addBranch(effectiveBranch);
        }

        if (dto.getCommit() != null) {
            commit.setMessage(dto.getCommit().getMessage() != null ? dto.getCommit().getMessage() : "No message");
            if (dto.getCommit().getAuthor() != null) {
                commit.setAuthorName(dto.getCommit().getAuthor().getName());
                commit.setAuthorEmail(dto.getCommit().getAuthor().getEmail());
                if (dto.getCommit().getAuthor().getDate() != null) {
                    commit.setAuthorDate(Instant.parse(dto.getCommit().getAuthor().getDate()));
                }
            }
            if (dto.getCommit().getCommitter() != null) {
                commit.setCommitterName(dto.getCommit().getCommitter().getName());
                commit.setCommitterEmail(dto.getCommit().getCommitter().getEmail());
                if (dto.getCommit().getCommitter().getDate() != null) {
                    commit.setCommitDate(Instant.parse(dto.getCommit().getCommitter().getDate()));
                }
            }
        }
        if (commit.getCommitDate() == null) {
            commit.setCommitDate(commit.getAuthorDate() != null ? commit.getAuthorDate() : Instant.now());
        }

        if (dto.getParents() != null && !dto.getParents().isEmpty()) {
            List<String> parents = new ArrayList<>();
            for (GitHubCommitDto.ParentInfo p : dto.getParents()) {
                parents.add(p.getSha());
            }
            commit.setParentShas(String.join(",", parents));
        }

        if (dto.getStats() != null) {
            commit.setAdditions(dto.getStats().getAdditions());
            commit.setDeletions(dto.getStats().getDeletions());
            commit.setTotalChanges(dto.getStats().getTotal());
        }

        CommitEntity savedCommit = commitRepo.save(commit);

        // Process files if present
        if (dto.getFiles() != null) {
            commitFileRepo.deleteByCommitId(savedCommit.getId());
            for (GitHubCommitDto.CommitFileInfo f : dto.getFiles()) {
                CommitFileEntity fileEntity = new CommitFileEntity(
                        savedCommit,
                        f.getFilename(),
                        f.getStatus(),
                        f.getAdditions(),
                        f.getDeletions(),
                        f.getChanges()
                );
                fileEntity.setRawPatch(f.getPatch());
                commitFileRepo.save(fileEntity);
            }
        }

        return savedCommit;
    }

    @Transactional
    public PullRequestEntity processPullRequest(RepositoryEntity repository, GitHubPullRequestDto dto, List<GitHubReviewDto> reviews) {
        int prNumber = dto.getNumber();
        Optional<PullRequestEntity> existing = pullRequestRepo.findByRepositoryIdAndPrNumber(repository.getId(), prNumber);

        PullRequestEntity pr = existing.orElseGet(() -> {
            PullRequestEntity entity = new PullRequestEntity();
            entity.setRepository(repository);
            entity.setPrNumber(prNumber);
            return entity;
        });

        pr.setTitle(dto.getTitle() != null ? dto.getTitle() : "Untitled PR");
        pr.setBody(dto.getBody());
        pr.setState(dto.getState());
        pr.setAuthorLogin(dto.getUser() != null ? dto.getUser().getLogin() : "unknown");
        pr.setAuthorAvatarUrl(dto.getUser() != null ? dto.getUser().getAvatarUrl() : null);

        if (dto.getBase() != null) pr.setBaseBranch(dto.getBase().getRef());
        if (dto.getHead() != null) pr.setHeadBranch(dto.getHead().getRef());

        pr.setDraft(dto.isDraft());
        pr.setMerged(dto.isMerged());
        pr.setAdditions(dto.getAdditions());
        pr.setDeletions(dto.getDeletions());
        pr.setChangedFiles(dto.getChangedFiles());

        if (dto.getCreatedAt() != null) pr.setCreatedAt(Instant.parse(dto.getCreatedAt()));
        if (dto.getUpdatedAt() != null) pr.setUpdatedAt(Instant.parse(dto.getUpdatedAt()));
        if (dto.getClosedAt() != null) pr.setClosedAt(Instant.parse(dto.getClosedAt()));
        if (dto.getMergedAt() != null) pr.setMergedAt(Instant.parse(dto.getMergedAt()));

        // Process reviews and calculate review lifecycle timestamps
        pr.getReviews().clear();
        if (reviews != null && !reviews.isEmpty()) {
            Instant firstReview = null;
            Instant approval = null;

            for (GitHubReviewDto r : reviews) {
                if (r.getSubmittedAt() != null) {
                    Instant submitted = Instant.parse(r.getSubmittedAt());
                    if (firstReview == null || submitted.isBefore(firstReview)) {
                        firstReview = submitted;
                    }
                    if ("APPROVED".equalsIgnoreCase(r.getState())) {
                        if (approval == null || submitted.isBefore(approval)) {
                            approval = submitted;
                        }
                    }

                    PullRequestReviewEntity reviewEntity = new PullRequestReviewEntity();
                    reviewEntity.setPullRequest(pr);
                    reviewEntity.setGithubReviewId(r.getId());
                    reviewEntity.setReviewerLogin(r.getUser() != null ? r.getUser().getLogin() : "reviewer");
                    reviewEntity.setState(r.getState());
                    reviewEntity.setSubmittedAt(submitted);
                    reviewEntity.setBody(r.getBody());
                    pr.getReviews().add(reviewEntity);
                }
            }
            pr.setFirstReviewAt(firstReview);
            pr.setApprovedAt(approval);
        }

        return pullRequestRepo.save(pr);
    }

    @Transactional
    public IssueEntity processIssue(RepositoryEntity repository, GitHubIssueDto dto) {
        if (dto.isPullRequest()) {
            return null; // Ignore PRs appearing in issues endpoint
        }

        int number = dto.getNumber();
        Optional<IssueEntity> existing = issueRepo.findByRepositoryIdAndIssueNumber(repository.getId(), number);

        IssueEntity issue = existing.orElseGet(() -> {
            IssueEntity entity = new IssueEntity();
            entity.setRepository(repository);
            entity.setIssueNumber(number);
            return entity;
        });

        issue.setTitle(dto.getTitle() != null ? dto.getTitle() : "Untitled Issue");
        issue.setBody(dto.getBody());
        issue.setState(dto.getState());
        issue.setAuthorLogin(dto.getUser() != null ? dto.getUser().getLogin() : "unknown");
        issue.setCommentsCount(dto.getComments());

        if (dto.getCreatedAt() != null) issue.setCreatedAt(Instant.parse(dto.getCreatedAt()));
        if (dto.getUpdatedAt() != null) issue.setUpdatedAt(Instant.parse(dto.getUpdatedAt()));
        if (dto.getClosedAt() != null) issue.setClosedAt(Instant.parse(dto.getClosedAt()));

        // Labels
        if (dto.getLabels() != null && !dto.getLabels().isEmpty()) {
            List<String> labelNames = dto.getLabels().stream()
                    .map(GitHubIssueDto.LabelInfo::getName)
                    .toList();
            issue.setLabels(String.join(",", labelNames));
            issue.setCategory(inferCategoryFromLabels(labelNames, issue.getTitle()));
            issue.setCategoryConfidence(0.85);
        } else {
            issue.setCategory(inferCategoryFromTitle(issue.getTitle()));
            issue.setCategoryConfidence(0.65);
        }

        return issueRepo.save(issue);
    }

    @Transactional
    public ReleaseEntity processRelease(RepositoryEntity repository, GitHubReleaseDto dto) {
        String tagName = dto.getTagName();
        Optional<ReleaseEntity> existing = releaseRepo.findByRepositoryIdAndTagName(repository.getId(), tagName);

        ReleaseEntity release = existing.orElseGet(() -> {
            ReleaseEntity entity = new ReleaseEntity();
            entity.setRepository(repository);
            entity.setTagName(tagName);
            return entity;
        });

        release.setName(dto.getName() != null ? dto.getName() : tagName);
        release.setBody(dto.getBody());
        release.setDraft(dto.isDraft());
        release.setPrerelease(dto.isPrerelease());
        if (dto.getPublishedAt() != null) {
            release.setPublishedAt(Instant.parse(dto.getPublishedAt()));
        } else {
            release.setPublishedAt(Instant.now());
        }

        return releaseRepo.save(release);
    }

    @Transactional
    public void updateCheckpoint(RepositoryEntity repository, String resourceType, Instant timestamp, String lastId) {
        SyncCheckpointEntity checkpoint = checkpointRepo.findByRepositoryIdAndResourceType(repository.getId(), resourceType)
                .orElseGet(() -> new SyncCheckpointEntity(repository, resourceType));

        checkpoint.setLastSyncTimestamp(timestamp);
        checkpoint.setLastExternalId(lastId);
        checkpoint.setUpdatedAt(Instant.now());
        checkpointRepo.save(checkpoint);
    }

    public Optional<SyncCheckpointEntity> getCheckpoint(RepositoryEntity repository, String resourceType) {
        return checkpointRepo.findByRepositoryIdAndResourceType(repository.getId(), resourceType);
    }

    @Transactional
    public void resetCheckpoint(RepositoryEntity repository, String resourceType) {
        checkpointRepo.findByRepositoryIdAndResourceType(repository.getId(), resourceType)
                .ifPresent(checkpointRepo::delete);
    }

    private String inferCategoryFromLabels(List<String> labels, String title) {
        String combined = (String.join(" ", labels) + " " + (title != null ? title : "")).toLowerCase();
        if (combined.contains("bug") || combined.contains("fix") || combined.contains("defect") || combined.contains("error")) {
            return "BUG";
        }
        if (combined.contains("security") || combined.contains("vulnerability") || combined.contains("cve")) {
            return "SECURITY";
        }
        if (combined.contains("perf") || combined.contains("performance") || combined.contains("slow") || combined.contains("latency")) {
            return "PERFORMANCE";
        }
        if (combined.contains("doc") || combined.contains("documentation") || combined.contains("guide") || combined.contains("readme")) {
            return "DOCUMENTATION";
        }
        if (combined.contains("feature") || combined.contains("feat") || combined.contains("proposal") || combined.contains("rfc")) {
            return "FEATURE";
        }
        if (combined.contains("enhancement") || combined.contains("improve") || combined.contains("refactor")) {
            return "ENHANCEMENT";
        }
        return "OTHER";
    }

    private String inferCategoryFromTitle(String title) {
        if (title == null) return "OTHER";
        String lower = title.toLowerCase();
        if (lower.contains("fix") || lower.contains("bug") || lower.contains("error") || lower.contains("crash") || lower.contains("fail")) {
            return "BUG";
        }
        if (lower.contains("sec") || lower.contains("auth") || lower.contains("leak")) {
            return "SECURITY";
        }
        if (lower.contains("doc") || lower.contains("guide")) {
            return "DOCUMENTATION";
        }
        if (lower.contains("speed") || lower.contains("fast") || lower.contains("memory") || lower.contains("slow")) {
            return "PERFORMANCE";
        }
        if (lower.contains("add") || lower.contains("new") || lower.contains("support")) {
            return "FEATURE";
        }
        return "OTHER";
    }
}

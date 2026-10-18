package com.devtrace.repository;

import com.devtrace.entity.PullRequestReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PullRequestReviewEntityRepository extends JpaRepository<PullRequestReviewEntity, String> {
    List<PullRequestReviewEntity> findByPullRequestId(String pullRequestId);
    List<PullRequestReviewEntity> findByPullRequestIdOrderBySubmittedAtAsc(String pullRequestId);
}

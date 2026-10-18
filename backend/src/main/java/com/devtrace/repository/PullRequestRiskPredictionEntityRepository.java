package com.devtrace.repository;

import com.devtrace.entity.PullRequestRiskPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PullRequestRiskPredictionEntityRepository extends JpaRepository<PullRequestRiskPredictionEntity, String> {
    Optional<PullRequestRiskPredictionEntity> findByPullRequestId(String pullRequestId);
}

package com.devtrace.repository;

import com.devtrace.entity.PullRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PullRequestEntityRepository extends JpaRepository<PullRequestEntity, String> {
    Optional<PullRequestEntity> findByRepositoryIdAndPrNumber(String repositoryId, int prNumber);
    boolean existsByRepositoryIdAndPrNumber(String repositoryId, int prNumber);
    Page<PullRequestEntity> findByRepositoryIdOrderByCreatedAtDesc(String repositoryId, Pageable pageable);
    List<PullRequestEntity> findByRepositoryIdOrderByCreatedAtAsc(String repositoryId);
    List<PullRequestEntity> findByRepositoryIdAndState(String repositoryId, String state);
    long countByRepositoryId(String repositoryId);
}

package com.devtrace.repository;

import com.devtrace.entity.IssueEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueEntityRepository extends JpaRepository<IssueEntity, String> {
    Optional<IssueEntity> findByRepositoryIdAndIssueNumber(String repositoryId, int issueNumber);
    boolean existsByRepositoryIdAndIssueNumber(String repositoryId, int issueNumber);
    Page<IssueEntity> findByRepositoryIdOrderByCreatedAtDesc(String repositoryId, Pageable pageable);
    List<IssueEntity> findByRepositoryId(String repositoryId);
    List<IssueEntity> findByRepositoryIdAndCategory(String repositoryId, String category);
    long countByRepositoryId(String repositoryId);
}

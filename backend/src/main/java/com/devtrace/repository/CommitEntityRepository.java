package com.devtrace.repository;

import com.devtrace.entity.CommitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommitEntityRepository extends JpaRepository<CommitEntity, String> {
    Optional<CommitEntity> findByRepositoryIdAndSha(String repositoryId, String sha);
    boolean existsByRepositoryIdAndSha(String repositoryId, String sha);
    Page<CommitEntity> findByRepositoryIdOrderByCommitDateDesc(String repositoryId, Pageable pageable);
    List<CommitEntity> findByRepositoryIdAndCommitDateBetweenOrderByCommitDateAsc(String repositoryId, Instant start, Instant end);
    long countByRepositoryId(String repositoryId);

    @Query("SELECT COUNT(DISTINCT c.authorEmail) FROM CommitEntity c WHERE c.repository.id = :repoId")
    long countDistinctAuthorsByRepositoryId(@Param("repoId") String repoId);

    @Query("SELECT COUNT(c) FROM CommitEntity c WHERE c.repository.id = :repoId AND (LOWER(c.authorName) = LOWER(:login) OR LOWER(c.authorEmail) LIKE LOWER(CONCAT('%', :login, '%')))")
    long countCommitsByRepositoryIdAndAuthor(@Param("repoId") String repoId, @Param("login") String login);
}

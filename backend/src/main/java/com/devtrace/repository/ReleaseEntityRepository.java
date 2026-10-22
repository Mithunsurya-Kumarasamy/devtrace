package com.devtrace.repository;

import com.devtrace.entity.ReleaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseEntityRepository extends JpaRepository<ReleaseEntity, String> {
    Optional<ReleaseEntity> findByRepositoryIdAndTagName(String repositoryId, String tagName);
    boolean existsByRepositoryIdAndTagName(String repositoryId, String tagName);
    Page<ReleaseEntity> findByRepositoryIdOrderByPublishedAtDesc(String repositoryId, Pageable pageable);
    long countByRepositoryId(String repositoryId);
}

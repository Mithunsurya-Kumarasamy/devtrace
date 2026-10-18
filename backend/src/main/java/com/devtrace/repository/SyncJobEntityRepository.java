package com.devtrace.repository;

import com.devtrace.entity.SyncJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyncJobEntityRepository extends JpaRepository<SyncJobEntity, String> {
    List<SyncJobEntity> findByRepositoryIdOrderByStartedAtDesc(String repositoryId);
    Optional<SyncJobEntity> findFirstByRepositoryIdAndStatus(String repositoryId, String status);
}

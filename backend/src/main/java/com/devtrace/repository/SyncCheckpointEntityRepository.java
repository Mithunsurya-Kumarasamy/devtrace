package com.devtrace.repository;

import com.devtrace.entity.SyncCheckpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SyncCheckpointEntityRepository extends JpaRepository<SyncCheckpointEntity, String> {
    Optional<SyncCheckpointEntity> findByRepositoryIdAndResourceType(String repositoryId, String resourceType);
}

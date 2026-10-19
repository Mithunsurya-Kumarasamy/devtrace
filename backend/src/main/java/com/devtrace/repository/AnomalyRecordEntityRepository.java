package com.devtrace.repository;

import com.devtrace.entity.AnomalyRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyRecordEntityRepository extends JpaRepository<AnomalyRecordEntity, String> {
    List<AnomalyRecordEntity> findByRepositoryIdOrderByDetectionTimestampDesc(String repositoryId);
    Page<AnomalyRecordEntity> findByRepositoryIdOrderByDetectionTimestampDesc(String repositoryId, Pageable pageable);
    List<AnomalyRecordEntity> findByRepositoryIdAndMetricName(String repositoryId, String metricName);
    void deleteByRepositoryId(String repositoryId);
}

package com.devtrace.repository;

import com.devtrace.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, String> {
    Optional<RepositoryEntity> findByFullName(String fullName);
    Optional<RepositoryEntity> findByOwnerAndName(String owner, String name);
    boolean existsByFullName(String fullName);
}

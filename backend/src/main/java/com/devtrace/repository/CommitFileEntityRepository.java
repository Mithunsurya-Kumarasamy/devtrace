package com.devtrace.repository;

import com.devtrace.entity.CommitFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitFileEntityRepository extends JpaRepository<CommitFileEntity, String> {
    List<CommitFileEntity> findByCommitId(String commitId);

    @Query("SELECT cf.filename, COUNT(cf), SUM(cf.additions), SUM(cf.deletions) " +
           "FROM CommitFileEntity cf JOIN cf.commit c " +
           "WHERE c.repository.id = :repoId " +
           "GROUP BY cf.filename ORDER BY COUNT(cf) DESC")
    List<Object[]> findFileChurnMetricsByRepositoryId(@Param("repoId") String repoId);
}

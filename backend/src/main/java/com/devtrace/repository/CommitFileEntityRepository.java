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

    @Query("SELECT cf.filename, c.sha, c.message, c.commitDate, c.authorName, cf.additions, cf.deletions " +
           "FROM CommitFileEntity cf JOIN cf.commit c " +
           "WHERE c.repository.id = :repoId AND c.commitDate >= :since " +
           "ORDER BY c.commitDate DESC")
    List<Object[]> findFileCommitDetailsSince(@Param("repoId") String repoId, @Param("since") java.time.Instant since);

    @Query("SELECT cf.filename, c.sha, c.message, c.commitDate, c.authorName, cf.additions, cf.deletions " +
           "FROM CommitFileEntity cf JOIN cf.commit c " +
           "WHERE c.repository.id = :repoId " +
           "ORDER BY c.commitDate DESC")
    List<Object[]> findFileCommitDetailsAllTime(@Param("repoId") String repoId);
}

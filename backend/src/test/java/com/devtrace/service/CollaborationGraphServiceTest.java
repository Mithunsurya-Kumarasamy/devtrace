package com.devtrace.service;

import com.devtrace.dto.collaboration.*;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.PullRequestReviewEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.*;
import com.devtrace.service.collaboration.CollaborationGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollaborationGraphServiceTest {

    @Mock
    private RepositoryEntityRepository repositoryRepo;

    @Mock
    private CommitEntityRepository commitRepo;

    @Mock
    private CommitFileEntityRepository commitFileRepo;

    @Mock
    private PullRequestEntityRepository pullRequestRepo;

    @Mock
    private PullRequestReviewEntityRepository reviewRepo;

    @InjectMocks
    private CollaborationGraphService collaborationGraphService;

    private RepositoryEntity testRepo;
    private final String repoId = "repo-collab-test-123";

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity("devtrace-org", "test-collab-repo");
        testRepo.setId(repoId);
    }

    @Test
    @DisplayName("getCollaborationNetwork throws ResourceNotFoundException if repository does not exist")
    void testRepoNotFound() {
        when(repositoryRepo.existsById("invalid-id")).thenReturn(false);

        assertThatThrownBy(() -> collaborationGraphService.getCollaborationNetwork("invalid-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @DisplayName("getCollaborationNetwork handles empty repository gracefully")
    void testEmptyRepository() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);
        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(Collections.emptyList());
        when(reviewRepo.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repoId)).thenReturn(Collections.emptyList());
        when(commitFileRepo.findFileCommitDetailsAllTime(repoId)).thenReturn(Collections.emptyList());

        CollaborationNetworkDto network = collaborationGraphService.getCollaborationNetwork(repoId);

        assertThat(network).isNotNull();
        assertThat(network.getRepositoryId()).isEqualTo(repoId);
        assertThat(network.getTotalContributors()).isEqualTo(0);
        assertThat(network.getTotalInteractions()).isEqualTo(0);
        assertThat(network.getNetworkDensity()).isEqualTo(0.0);
        assertThat(network.getNodes()).isEmpty();
        assertThat(network.getEdges()).isEmpty();
        assertThat(network.getKnowledgeSilos()).isEmpty();
    }

    @Test
    @DisplayName("getCollaborationNetwork constructs nodes, review/co-edit edges, centralities, and detects knowledge silos")
    void testCollaborationNetworkAndSilos() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        Instant now = Instant.now();

        // 1. Pull requests and reviews
        PullRequestEntity pr1 = new PullRequestEntity(testRepo, 1, "Feature A", "merged", "alice");
        pr1.setId("pr-1");
        PullRequestEntity pr2 = new PullRequestEntity(testRepo, 2, "Feature B", "merged", "bob");
        pr2.setId("pr-2");

        // Bob reviews Alice
        PullRequestReviewEntity rev1 = new PullRequestReviewEntity(pr1, "bob", "APPROVED", now);
        // Charlie reviews Bob
        PullRequestReviewEntity rev2 = new PullRequestReviewEntity(pr2, "charlie", "APPROVED", now);

        when(pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repoId)).thenReturn(List.of(pr1, pr2));
        when(reviewRepo.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repoId)).thenReturn(List.of(rev1, rev2));

        // 2. Commit file modifications
        // Shared file: modified by alice and bob -> co-edit edge
        // Silo file: modified ONLY by david (6 commits, 350 changes) -> knowledge silo
        List<Object[]> fileRows = List.of(
                new Object[]{"src/shared/Common.java", "sha1", "feat common", now, "alice", 50, 10},
                new Object[]{"src/shared/Common.java", "sha2", "update common", now, "bob", 30, 5},
                new Object[]{"src/isolated/SoloWorker.java", "sha3", "init worker", now, "david", 100, 20},
                new Object[]{"src/isolated/SoloWorker.java", "sha4", "extend worker", now, "david", 150, 30},
                new Object[]{"src/isolated/SoloWorker.java", "sha5", "fix worker", now, "david", 50, 10}
        );

        when(commitFileRepo.findFileCommitDetailsAllTime(repoId)).thenReturn(fileRows);

        CollaborationNetworkDto network = collaborationGraphService.getCollaborationNetwork(repoId);

        assertThat(network.getTotalContributors()).isEqualTo(4); // alice, bob, charlie, david
        assertThat(network.getNodes()).hasSize(4);

        // Check node existence
        List<String> logins = network.getNodes().stream().map(CollaborationNodeDto::getId).toList();
        assertThat(logins).contains("alice", "bob", "charlie", "david");

        // Check edges (alice-bob review + coedit, bob-charlie review)
        assertThat(network.getEdges()).isNotEmpty();
        CollaborationEdgeDto aliceBobEdge = network.getEdges().stream()
                .filter(e -> (e.getSource().equals("alice") && e.getTarget().equals("bob")) ||
                             (e.getSource().equals("bob") && e.getTarget().equals("alice")))
                .findFirst()
                .orElse(null);
        assertThat(aliceBobEdge).isNotNull();
        assertThat(aliceBobEdge.getInteractionTypes()).contains("REVIEW", "CO_EDIT");

        // Check network density and centrality
        assertThat(network.getNetworkDensity()).isGreaterThan(0.0);
        assertThat(network.getNodes().stream().anyMatch(n -> n.getDegreeCentrality() > 0.0)).isTrue();

        // Check Knowledge Silos
        assertThat(network.getKnowledgeSilos()).hasSize(1);
        KnowledgeSiloDto soloSilo = network.getKnowledgeSilos().get(0);
        assertThat(soloSilo.getPath()).isEqualTo("src/isolated/SoloWorker.java");
        assertThat(soloSilo.getSoleContributor()).isEqualTo("david");
        assertThat(soloSilo.getCommitCount()).isEqualTo(3);
        assertThat(soloSilo.getTotalChanges()).isEqualTo(360);
        assertThat(soloSilo.getRiskSeverity()).isIn("CRITICAL", "HIGH");
        assertThat(soloSilo.getRecommendation()).contains("cross-training");
    }
}

package com.devtrace.service;

import com.devtrace.dto.dependency.*;
import com.devtrace.entity.CommitEntity;
import com.devtrace.entity.CommitFileEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.CommitFileEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import com.devtrace.service.dependency.DependencyGraphService;
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
class DependencyGraphServiceTest {

    @Mock
    private RepositoryEntityRepository repositoryRepo;

    @Mock
    private CommitFileEntityRepository commitFileRepo;

    @InjectMocks
    private DependencyGraphService dependencyGraphService;

    private RepositoryEntity testRepo;
    private final String repoId = "repo-dep-test-123";

    @BeforeEach
    void setUp() {
        testRepo = new RepositoryEntity("devtrace-org", "test-dep-repo");
        testRepo.setId(repoId);
    }

    @Test
    @DisplayName("analyzeDependencyGraph throws ResourceNotFoundException if repository does not exist")
    void testRepoNotFound() {
        when(repositoryRepo.existsById("non-existent")).thenReturn(false);

        assertThatThrownBy(() -> dependencyGraphService.analyzeDependencyGraph("non-existent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    @DisplayName("analyzeDependencyGraph handles empty repository gracefully")
    void testEmptyRepository() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);
        when(commitFileRepo.findByCommitRepositoryId(repoId)).thenReturn(Collections.emptyList());

        DependencyGraphReportDto report = dependencyGraphService.analyzeDependencyGraph(repoId);

        assertThat(report).isNotNull();
        assertThat(report.getRepositoryId()).isEqualTo(repoId);
        assertThat(report.getTotalModules()).isEqualTo(0);
        assertThat(report.getTotalDependencies()).isEqualTo(0);
        assertThat(report.isHasCircularDependencies()).isFalse();
        assertThat(report.getNodes()).isEmpty();
        assertThat(report.getEdges()).isEmpty();
    }

    @Test
    @DisplayName("analyzeDependencyGraph parses imports, computes coupling and instability, and detects circular dependencies")
    void testDependencyCouplingAndCycles() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        CommitEntity commit = new CommitEntity(testRepo, "sha1", "feat: architecture setup", Instant.now());

        // File 1: com.devtrace.service.ServiceA imports com.devtrace.repository
        CommitFileEntity fileA = new CommitFileEntity(commit,
                "backend/src/main/java/com/devtrace/service/ServiceA.java", "modified", 300, 50, 350);
        fileA.setRawPatch("+package com.devtrace.service;\n+import com.devtrace.repository.RepoB;\n+public class ServiceA {}");

        // File 2: com.devtrace.controller.ControllerC imports com.devtrace.service
        CommitFileEntity fileC = new CommitFileEntity(commit,
                "backend/src/main/java/com/devtrace/controller/ControllerC.java", "modified", 100, 20, 120);
        fileC.setRawPatch("+package com.devtrace.controller;\n+import com.devtrace.service.ServiceA;\n+public class ControllerC {}");

        // File 3: com.devtrace.repository.RepoB imports com.devtrace.service (introduces a cycle between service and repository!)
        CommitFileEntity fileB = new CommitFileEntity(commit,
                "backend/src/main/java/com/devtrace/repository/RepoB.java", "modified", 250, 40, 290);
        fileB.setRawPatch("+package com.devtrace.repository;\n+import com.devtrace.service.ServiceA;\n+public class RepoB {}");

        when(commitFileRepo.findByCommitRepositoryId(repoId)).thenReturn(List.of(fileA, fileC, fileB));

        DependencyGraphReportDto report = dependencyGraphService.analyzeDependencyGraph(repoId);

        assertThat(report.getTotalModules()).isGreaterThanOrEqualTo(3);
        assertThat(report.getTotalDependencies()).isGreaterThanOrEqualTo(3);

        // Verify nodes exist
        List<String> moduleNames = report.getNodes().stream().map(DependencyNodeDto::getName).toList();
        assertThat(moduleNames).contains("com.devtrace.service", "com.devtrace.repository", "com.devtrace.controller");

        // Check service node: has incoming from controller and repository (Ca = 2), outgoing to repository (Ce = 1)
        DependencyNodeDto serviceNode = report.getNodes().stream()
                .filter(n -> "com.devtrace.service".equals(n.getName()))
                .findFirst()
                .orElse(null);
        assertThat(serviceNode).isNotNull();
        assertThat(serviceNode.getAfferentCoupling()).isEqualTo(2); // Controller & Repo depend on Service
        assertThat(serviceNode.getEfferentCoupling()).isEqualTo(1); // Service depends on Repo
        assertThat(serviceNode.getInstability()).isGreaterThan(0.0).isLessThan(1.0);
        assertThat(serviceNode.getTotalChurn()).isEqualTo(350);
        assertThat(serviceNode.getArchitecturalRole()).isEqualTo("HIGH_BLAST_RADIUS");

        // Verify circular dependency between service and repository
        assertThat(report.isHasCircularDependencies()).isTrue();
        assertThat(report.getCircularDependenciesCount()).isGreaterThanOrEqualTo(1);
        assertThat(serviceNode.isInCycle()).isTrue();

        CircularDependencyDto cycle = report.getCircularDependencies().get(0);
        assertThat(cycle.getModules()).contains("com.devtrace.service", "com.devtrace.repository");
        assertThat(cycle.getRecommendation()).containsIgnoringCase("dependency inversion");

        assertThat(report.getArchitecturalSummary()).contains("HIGH_BLAST_RADIUS");
    }

    @Test
    @DisplayName("analyzeDependencyGraph normalizes Windows backslash file paths correctly")
    void testWindowsPathSeparatorsNormalized() {
        when(repositoryRepo.existsById(repoId)).thenReturn(true);

        CommitEntity commit = new CommitEntity(testRepo, "shaWin", "feat: windows path support", Instant.now());
        CommitFileEntity winFile = new CommitFileEntity(commit,
                "backend\\src\\main\\java\\com\\devtrace\\service\\WindowsService.java", "modified", 100, 10, 110);
        winFile.setRawPatch("+package com.devtrace.service;\n+import com.devtrace.repository.WindowsRepo;\n+public class WindowsService {}");

        when(commitFileRepo.findByCommitRepositoryId(repoId)).thenReturn(List.of(winFile));

        DependencyGraphReportDto report = dependencyGraphService.analyzeDependencyGraph(repoId);

        assertThat(report.getTotalModules()).isGreaterThanOrEqualTo(1);
        List<String> moduleNames = report.getNodes().stream().map(DependencyNodeDto::getName).toList();
        assertThat(moduleNames).contains("com.devtrace.service");
    }
}

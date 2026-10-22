package com.devtrace;

import com.devtrace.dto.*;
import com.devtrace.dto.anomaly.AnomalyDetectionReportDto;
import com.devtrace.dto.dependency.DependencyGraphReportDto;
import com.devtrace.dto.issuenlp.IssueNlpReportDto;
import com.devtrace.dto.processmining.ProcessMiningReportDto;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.repository.RepositoryEntityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevTraceE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    @Test
    @DisplayName("E2E Integration: Repository registration, sync pipeline, and complete intelligence suite")
    void testCompleteDevTraceIntelligenceLifecycle() throws Exception {
        // 1. Set up test repository directly in persistent database
        RepositoryEntity repo = new RepositoryEntity("devtrace-e2e", "core-platform");
        repo.setHtmlUrl("https://github.com/devtrace-e2e/core-platform");
        repo.setDefaultBranch("main");
        repo.setSyncStatus("COMPLETED");
        repo.setLastSyncedAt(Instant.now());
        repo = repositoryRepo.save(repo);
        String repoId = repo.getId();

        // 2. Query Repository Metadata
        mockMvc.perform(get("/api/repositories/" + repoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("devtrace-e2e/core-platform"))
                .andExpect(jsonPath("$.syncStatus").value("COMPLETED"));

        // 3. Analytics Overview & Health Score
        mockMvc.perform(get("/api/repositories/" + repoId + "/analytics/overview?days=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("devtrace-e2e/core-platform"))
                .andExpect(jsonPath("$.healthScore.compositeScore").isNumber());

        // 4. Process Mining & Bottlenecks
        mockMvc.perform(get("/api/repositories/" + repoId + "/analytics/process-mining?days=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId))
                .andExpect(jsonPath("$.stages").isArray())
                .andExpect(jsonPath("$.variants").isArray());

        // 5. Hotspots & Churn Analytics
        mockMvc.perform(get("/api/repositories/" + repoId + "/analytics/hotspots?days=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId));

        mockMvc.perform(get("/api/repositories/" + repoId + "/analytics/churn?days=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId));

        // 6. Collaboration Graph & Knowledge Silos
        mockMvc.perform(get("/api/repositories/" + repoId + "/collaboration-network?days=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId));

        // 7. Issue NLP Categorization
        mockMvc.perform(get("/api/repositories/" + repoId + "/analytics/issues-nlp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId));

        // 8. Statistical Anomaly Detection
        mockMvc.perform(get("/api/repositories/" + repoId + "/anomalies?days=90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId))
                .andExpect(jsonPath("$.baselines").isArray());

        // 9. Architectural Coupling & Dependency Graph
        mockMvc.perform(get("/api/repositories/" + repoId + "/dependency-graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repoId))
                .andExpect(jsonPath("$.architecturalSummary").isNotEmpty());
    }
}

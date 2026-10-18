package com.devtrace.controller;

import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.RepositoryEntity;
import com.devtrace.repository.PullRequestEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PullRequestRiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    @Autowired
    private PullRequestEntityRepository pullRequestRepo;

    private PullRequestEntity pr;

    @BeforeEach
    void setUp() {
        RepositoryEntity repo = repositoryRepo.save(new RepositoryEntity("risk-controller-org", "risk-controller-repo"));
        pr = new PullRequestEntity(repo, 42, "Add risk controller", "open", "developer");
        pr.setAdditions(120);
        pr.setDeletions(30);
        pr.setChangedFiles(4);
        pr.setCreatedAt(Instant.now());
        pr = pullRequestRepo.save(pr);
    }

    @Test
    @DisplayName("GET /api/pull-requests/{id}/risk returns 200 and risk assessment schema")
    void testGetRisk() throws Exception {
        mockMvc.perform(get("/api/pull-requests/" + pr.getId() + "/risk")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pullRequestId").value(pr.getId()))
                .andExpect(jsonPath("$.prNumber").value(42))
                .andExpect(jsonPath("$.riskScore").isNumber())
                .andExpect(jsonPath("$.riskLevel").isString())
                .andExpect(jsonPath("$.riskFactors").isArray())
                .andExpect(jsonPath("$.guardrailRecommendations").isArray());
    }

    @Test
    @DisplayName("POST /api/pull-requests/{id}/risk/evaluate evaluates and returns persisted risk assessment")
    void testEvaluateRisk() throws Exception {
        mockMvc.perform(post("/api/pull-requests/" + pr.getId() + "/risk/evaluate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pullRequestId").value(pr.getId()))
                .andExpect(jsonPath("$.modelVersion").value("devtrace-heuristic-v1.0"));
    }
}

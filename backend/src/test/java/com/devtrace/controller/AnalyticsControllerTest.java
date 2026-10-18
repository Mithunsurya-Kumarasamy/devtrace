package com.devtrace.controller;

import com.devtrace.entity.RepositoryEntity;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositoryEntityRepository repositoryRepo;

    private RepositoryEntity repo;

    @BeforeEach
    void setUp() {
        repo = repositoryRepo.save(new RepositoryEntity("controller-org", "analytics-api-repo"));
    }

    @Test
    @DisplayName("GET /api/repositories/{id}/analytics/overview returns 200 and overview schema")
    void testGetOverview() throws Exception {
        mockMvc.perform(get("/api/repositories/" + repo.getId() + "/analytics/overview?days=30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(repo.getId()))
                .andExpect(jsonPath("$.fullName").value(repo.getFullName()))
                .andExpect(jsonPath("$.healthScore.compositeScore").isNumber())
                .andExpect(jsonPath("$.prCycleTimeHours").exists());
    }

    @Test
    @DisplayName("GET /api/repositories/{id}/commits returns 200 and paginated commit structure")
    void testGetCommits() throws Exception {
        mockMvc.perform(get("/api/repositories/" + repo.getId() + "/commits?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("GET /api/repositories/{id}/pull-requests returns 200 and paginated PR structure")
    void testGetPullRequests() throws Exception {
        mockMvc.perform(get("/api/repositories/" + repo.getId() + "/pull-requests?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/repositories/{id}/issues returns 200 and paginated issues structure")
    void testGetIssues() throws Exception {
        mockMvc.perform(get("/api/repositories/" + repo.getId() + "/issues?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/repositories/{id}/releases returns 200 and paginated releases structure")
    void testGetReleases() throws Exception {
        mockMvc.perform(get("/api/repositories/" + repo.getId() + "/releases?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

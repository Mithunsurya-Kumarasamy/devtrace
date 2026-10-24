package com.devtrace.controller;

import com.devtrace.dto.GitHubRepoDto;
import com.devtrace.dto.RepositoryRegistrationRequest;
import com.devtrace.service.GitHubApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GitHubApiClient gitHubApiClient;

    @Test
    @DisplayName("POST /api/repositories with valid data registers repository and returns 201")
    void testRegisterRepositorySuccess() throws Exception {
        GitHubRepoDto gitHubDto = new GitHubRepoDto();
        gitHubDto.setId(998877L);
        gitHubDto.setName("devtrace");
        gitHubDto.setFullName("Mithunsurya-Kumarasamy/devtrace");
        gitHubDto.setDescription("Software Engineering Intelligence Platform");
        gitHubDto.setDefaultBranch("master");
        gitHubDto.setPrivate(false);
        gitHubDto.setHtmlUrl("https://github.com/Mithunsurya-Kumarasamy/devtrace");

        when(gitHubApiClient.fetchRepository(eq("Mithunsurya-Kumarasamy"), eq("devtrace")))
                .thenReturn(gitHubDto);

        RepositoryRegistrationRequest request = new RepositoryRegistrationRequest("Mithunsurya-Kumarasamy", "devtrace");

        mockMvc.perform(post("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("devtrace"))
                .andExpect(jsonPath("$.fullName").value("Mithunsurya-Kumarasamy/devtrace"))
                .andExpect(jsonPath("$.defaultBranch").value("master"))
                .andExpect(jsonPath("$.syncStatus").value("IDLE"));
    }

    @Test
    @DisplayName("POST /api/repositories with invalid payload returns 400 Bad Request")
    void testRegisterRepositoryValidationFailure() throws Exception {
        RepositoryRegistrationRequest invalidRequest = new RepositoryRegistrationRequest("", "devtrace");

        mockMvc.perform(post("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("GET /api/repositories returns repository list")
    void testGetAllRepositories() throws Exception {
        mockMvc.perform(get("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/repositories/{id} returns 404 for unknown repository")
    void testGetRepositoryNotFound() throws Exception {
        mockMvc.perform(get("/api/repositories/nonexistent-id-000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/repositories with full GitHub URL and branch registers repository")
    void testRegisterRepositoryWithUrlAndBranch() throws Exception {
        GitHubRepoDto gitHubDto = new GitHubRepoDto();
        gitHubDto.setId(112233L);
        gitHubDto.setName("react");
        gitHubDto.setFullName("facebook/react");
        gitHubDto.setDescription("The library for web and native user interfaces");
        gitHubDto.setDefaultBranch("main");
        gitHubDto.setPrivate(false);
        gitHubDto.setHtmlUrl("https://github.com/facebook/react");

        when(gitHubApiClient.fetchRepository(eq("facebook"), eq("react")))
                .thenReturn(gitHubDto);

        RepositoryRegistrationRequest request = new RepositoryRegistrationRequest("https://github.com/facebook/react/tree/v18.2.0");

        mockMvc.perform(post("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("react"))
                .andExpect(jsonPath("$.fullName").value("facebook/react"))
                .andExpect(jsonPath("$.defaultBranch").value("v18.2.0"));
    }

    @Test
    @DisplayName("GET /api/repositories/{id}/branches and PATCH /api/repositories/{id}/branch")
    void testBranchesLifecycle() throws Exception {
        GitHubRepoDto gitHubDto = new GitHubRepoDto();
        gitHubDto.setId(554433L);
        gitHubDto.setName("devtrace");
        gitHubDto.setFullName("Mithunsurya-Kumarasamy/devtrace");
        gitHubDto.setDefaultBranch("master");

        when(gitHubApiClient.fetchRepository(eq("Mithunsurya-Kumarasamy"), eq("devtrace")))
                .thenReturn(gitHubDto);
        when(gitHubApiClient.fetchBranches(eq("Mithunsurya-Kumarasamy"), eq("devtrace")))
                .thenReturn(java.util.List.of(new com.devtrace.dto.GitHubBranchDto("master"), new com.devtrace.dto.GitHubBranchDto("Miskyy")));

        RepositoryRegistrationRequest request = new RepositoryRegistrationRequest("Mithunsurya-Kumarasamy", "devtrace");
        String responseJson = mockMvc.perform(post("/api/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String repoId = objectMapper.readTree(responseJson).get("id").asText();

        // GET branches
        mockMvc.perform(get("/api/repositories/" + repoId + "/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("master"))
                .andExpect(jsonPath("$[1]").value("Miskyy"));

        // PATCH branch
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/repositories/" + repoId + "/branch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"branch\":\"Miskyy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultBranch").value("Miskyy"));

        // DELETE repository
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/repositories/" + repoId))
                .andExpect(status().isNoContent());

        // Verify 404 after deletion
        mockMvc.perform(get("/api/repositories/" + repoId))
                .andExpect(status().isNotFound());
    }
}

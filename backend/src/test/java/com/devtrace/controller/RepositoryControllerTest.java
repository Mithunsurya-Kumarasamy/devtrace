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
}

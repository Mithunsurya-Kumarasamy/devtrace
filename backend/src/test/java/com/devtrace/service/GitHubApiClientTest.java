package com.devtrace.service;

import com.devtrace.dto.GitHubCommitDto;
import com.devtrace.dto.GitHubPullRequestDto;
import com.devtrace.dto.GitHubRepoDto;
import com.devtrace.exception.RateLimitExceededException;
import com.devtrace.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GitHubApiClientTest {

    private GitHubApiClient gitHubApiClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        RestTemplateBuilder builder = new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };
        mockServer = MockRestServiceServer.createServer(restTemplate);
        gitHubApiClient = new GitHubApiClient(builder, "https://api.github.com", "dummy-test-token");

        // Set the restTemplate field if builder created a new instance
        Field field = GitHubApiClient.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(gitHubApiClient, restTemplate);
    }

    @Test
    @DisplayName("Should fetch repository metadata successfully")
    void testFetchRepositorySuccess() {
        String json = """
            {
                "id": 12345678,
                "name": "devtrace",
                "full_name": "Mithunsurya-Kumarasamy/devtrace",
                "description": "Software Engineering Intelligence Platform",
                "private": false,
                "html_url": "https://github.com/Mithunsurya-Kumarasamy/devtrace",
                "default_branch": "master"
            }
            """;

        mockServer.expect(requestTo("https://api.github.com/repos/Mithunsurya-Kumarasamy/devtrace"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer dummy-test-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        GitHubRepoDto repo = gitHubApiClient.fetchRepository("Mithunsurya-Kumarasamy", "devtrace");

        assertNotNull(repo);
        assertEquals("devtrace", repo.getName());
        assertEquals("Mithunsurya-Kumarasamy/devtrace", repo.getFullName());
        assertEquals("master", repo.getDefaultBranch());
        assertFalse(repo.isPrivate());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when repository does not exist")
    void testFetchRepositoryNotFound() {
        mockServer.expect(requestTo("https://api.github.com/repos/nonexistent-org/unknown-repo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        assertThrows(ResourceNotFoundException.class, () ->
                gitHubApiClient.fetchRepository("nonexistent-org", "unknown-repo"));

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when rate limit is 0 on 403 response")
    void testRateLimitExceeded() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Remaining", "0");
        headers.set("X-RateLimit-Reset", "1790000000");

        mockServer.expect(requestTo("https://api.github.com/repos/rate-limited-org/busy-repo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .headers(headers)
                        .body("{\"message\":\"API rate limit exceeded\"}"));

        assertThrows(RateLimitExceededException.class, () ->
                gitHubApiClient.fetchRepository("rate-limited-org", "busy-repo"));

        mockServer.verify();
    }

    @Test
    @DisplayName("Should fetch paginated commits with since parameter")
    void testFetchCommitsWithPagination() {
        String json = """
            [
                {
                    "sha": "c1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0",
                    "commit": {
                        "message": "feat: process mining pipeline",
                        "author": { "name": "Mithun", "email": "mithun@devtrace.internal", "date": "2026-10-18T10:00:00Z" },
                        "committer": { "name": "Mithun", "email": "mithun@devtrace.internal", "date": "2026-10-18T10:00:00Z" }
                    }
                }
            ]
            """;

        mockServer.expect(requestTo("https://api.github.com/repos/org/repo/commits?page=1&per_page=100&since=2026-10-01T00:00:00Z"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<GitHubCommitDto> commits = gitHubApiClient.fetchCommits("org", "repo", "2026-10-01T00:00:00Z", 1, 100);

        assertEquals(1, commits.size());
        assertEquals("c1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0", commits.get(0).getSha());
        assertEquals("feat: process mining pipeline", commits.get(0).getCommit().getMessage());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should fetch paginated pull requests")
    void testFetchPullRequests() {
        String json = """
            [
                {
                    "number": 12,
                    "title": "Add Kafka asynchronous event ingestion",
                    "state": "open",
                    "user": { "login": "engineer-1" },
                    "created_at": "2026-10-18T11:00:00Z",
                    "updated_at": "2026-10-18T12:00:00Z",
                    "draft": false,
                    "merged": false
                }
            ]
            """;

        mockServer.expect(requestTo("https://api.github.com/repos/org/repo/pulls?state=all&page=1&per_page=50&sort=created&direction=asc"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<GitHubPullRequestDto> prs = gitHubApiClient.fetchPullRequests("org", "repo", "all", 1, 50);

        assertEquals(1, prs.size());
        assertEquals(12, prs.get(0).getNumber());
        assertEquals("engineer-1", prs.get(0).getUser().getLogin());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should fetch branches for repository")
    void testFetchBranches() {
        String json = """
            [
                { "name": "main", "protected": true },
                { "name": "dev", "protected": false }
            ]
            """;

        mockServer.expect(requestTo("https://api.github.com/repos/org/repo/branches?per_page=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<com.devtrace.dto.GitHubBranchDto> branches = gitHubApiClient.fetchBranches("org", "repo");

        assertEquals(2, branches.size());
        assertEquals("main", branches.get(0).getName());
        assertTrue(branches.get(0).isProtected());
        assertEquals("dev", branches.get(1).getName());
        assertFalse(branches.get(1).isProtected());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should fetch commits on specific branch using sha parameter")
    void testFetchCommitsOnSpecificBranch() {
        String json = """
            [
                {
                    "sha": "branchsha12345",
                    "commit": {
                        "message": "feat: branch commit",
                        "author": { "name": "Mithun", "email": "m@d.io", "date": "2026-10-18T10:00:00Z" }
                    }
                }
            ]
            """;

        mockServer.expect(requestTo("https://api.github.com/repos/org/repo/commits?page=1&per_page=50&sha=Miskyy"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<GitHubCommitDto> commits = gitHubApiClient.fetchCommits("org", "repo", "Miskyy", null, 1, 50);

        assertEquals(1, commits.size());
        assertEquals("branchsha12345", commits.get(0).getSha());

        mockServer.verify();
    }
}

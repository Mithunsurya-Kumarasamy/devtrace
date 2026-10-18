package com.devtrace.service;

import com.devtrace.dto.*;
import com.devtrace.exception.GitHubApiException;
import com.devtrace.exception.RateLimitExceededException;
import com.devtrace.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class GitHubApiClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubApiClient.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String githubToken;

    public GitHubApiClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${devtrace.github.api-url:https://api.github.com}") String apiUrl,
            @Value("${devtrace.github.token:}") String githubToken) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.apiUrl = apiUrl.replaceAll("/+$", "");
        this.githubToken = githubToken;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        headers.set("User-Agent", "DevTrace-Intelligence-Platform/1.0");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.setBearerAuth(githubToken.trim());
        }
        return headers;
    }

    public GitHubRepoDto fetchRepository(String owner, String name) {
        String url = String.format("%s/repos/%s/%s", apiUrl, owner, name);
        return executeRequest(url, HttpMethod.GET, GitHubRepoDto.class);
    }

    public List<GitHubCommitDto> fetchCommits(String owner, String name, String since, int page, int perPage) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/repos/%s/%s/commits", apiUrl, owner, name))
                .queryParam("page", page)
                .queryParam("per_page", perPage);
        if (since != null && !since.isBlank()) {
            builder.queryParam("since", since);
        }

        List<GitHubCommitDto> list = executeListRequest(builder.toUriString(), new ParameterizedTypeReference<List<GitHubCommitDto>>() {});
        return list != null ? list : Collections.emptyList();
    }

    public GitHubCommitDto fetchCommitDetails(String owner, String name, String sha) {
        String url = String.format("%s/repos/%s/%s/commits/%s", apiUrl, owner, name, sha);
        return executeRequest(url, HttpMethod.GET, GitHubCommitDto.class);
    }

    public List<GitHubPullRequestDto> fetchPullRequests(String owner, String name, String state, int page, int perPage) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/repos/%s/%s/pulls", apiUrl, owner, name))
                .queryParam("state", state != null ? state : "all")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("sort", "created")
                .queryParam("direction", "asc");

        List<GitHubPullRequestDto> list = executeListRequest(builder.toUriString(), new ParameterizedTypeReference<List<GitHubPullRequestDto>>() {});
        return list != null ? list : Collections.emptyList();
    }

    public List<GitHubReviewDto> fetchPullRequestReviews(String owner, String name, int prNumber) {
        String url = String.format("%s/repos/%s/%s/pulls/%d/reviews", apiUrl, owner, name, prNumber);
        List<GitHubReviewDto> list = executeListRequest(url, new ParameterizedTypeReference<List<GitHubReviewDto>>() {});
        return list != null ? list : Collections.emptyList();
    }

    public List<GitHubIssueDto> fetchIssues(String owner, String name, String state, String since, int page, int perPage) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("%s/repos/%s/%s/issues", apiUrl, owner, name))
                .queryParam("state", state != null ? state : "all")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .queryParam("sort", "created")
                .queryParam("direction", "asc");
        if (since != null && !since.isBlank()) {
            builder.queryParam("since", since);
        }

        List<GitHubIssueDto> list = executeListRequest(builder.toUriString(), new ParameterizedTypeReference<List<GitHubIssueDto>>() {});
        return list != null ? list : Collections.emptyList();
    }

    public List<GitHubReleaseDto> fetchReleases(String owner, String name, int page, int perPage) {
        String url = UriComponentsBuilder.fromHttpUrl(String.format("%s/repos/%s/%s/releases", apiUrl, owner, name))
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .toUriString();

        List<GitHubReleaseDto> list = executeListRequest(url, new ParameterizedTypeReference<List<GitHubReleaseDto>>() {});
        return list != null ? list : Collections.emptyList();
    }

    private <T> T executeRequest(String url, HttpMethod method, Class<T> responseType) {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
            checkRateLimitHeaders(response.getHeaders());
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("GitHub resource not found at " + url);
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.TooManyRequests ex) {
            handleRateLimitOrForbidden(ex);
            throw new GitHubApiException(ex.getMessage(), ex.getStatusCode().value(), ex);
        } catch (HttpClientErrorException ex) {
            throw new GitHubApiException("GitHub API Client Error: " + ex.getResponseBodyAsString(), ex.getStatusCode().value(), ex);
        } catch (HttpServerErrorException ex) {
            throw new GitHubApiException("GitHub API Server Error (" + ex.getStatusCode().value() + ")", ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            throw new GitHubApiException("Failed to communicate with GitHub API: " + ex.getMessage(), 500, ex);
        }
    }

    private <T> List<T> executeListRequest(String url, ParameterizedTypeReference<List<T>> typeRef) {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<List<T>> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, typeRef);
            checkRateLimitHeaders(response.getHeaders());
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            return Collections.emptyList();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.TooManyRequests ex) {
            handleRateLimitOrForbidden(ex);
            throw new GitHubApiException(ex.getMessage(), ex.getStatusCode().value(), ex);
        } catch (HttpClientErrorException ex) {
            throw new GitHubApiException("GitHub API Client Error: " + ex.getResponseBodyAsString(), ex.getStatusCode().value(), ex);
        } catch (Exception ex) {
            throw new GitHubApiException("Failed to fetch list from GitHub API: " + ex.getMessage(), 500, ex);
        }
    }

    private void checkRateLimitHeaders(HttpHeaders headers) {
        String remaining = headers.getFirst("X-RateLimit-Remaining");
        if (remaining != null && Integer.parseInt(remaining) < 5) {
            log.warn("GitHub API rate limit is critically low: {} requests remaining", remaining);
        }
    }

    private void handleRateLimitOrForbidden(HttpClientErrorException ex) {
        HttpHeaders headers = ex.getResponseHeaders();
        if (headers != null) {
            String remaining = headers.getFirst("X-RateLimit-Remaining");
            String reset = headers.getFirst("X-RateLimit-Reset");
            if ("0".equals(remaining) && reset != null) {
                Instant resetInstant = Instant.ofEpochSecond(Long.parseLong(reset));
                throw new RateLimitExceededException("GitHub API rate limit exhausted. Resets at " + resetInstant, resetInstant);
            }
        }
    }
}

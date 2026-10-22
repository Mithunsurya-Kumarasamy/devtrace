package com.devtrace.kafka.consumer;

import com.devtrace.entity.RepositoryEntity;
import com.devtrace.kafka.KafkaTopicConfig;
import com.devtrace.kafka.event.RawPullRequestEvent;
import com.devtrace.service.GitHubApiClient;
import com.devtrace.service.IngestionOrchestrator;
import com.devtrace.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Profile("!test")
public class PullRequestEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PullRequestEventConsumer.class);

    private final IngestionOrchestrator ingestionOrchestrator;
    private final RepositoryService repositoryService;
    private final GitHubApiClient gitHubApiClient;

    public PullRequestEventConsumer(
            IngestionOrchestrator ingestionOrchestrator,
            RepositoryService repositoryService,
            GitHubApiClient gitHubApiClient) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.repositoryService = repositoryService;
        this.gitHubApiClient = gitHubApiClient;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_PULL_REQUESTS_RAW, groupId = "devtrace-pr-workers", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleRawPullRequest(RawPullRequestEvent event) {
        log.debug("Received raw PR event: prNumber={}", event.getPullRequest() != null ? event.getPullRequest().getNumber() : 0);
        try {
            RepositoryEntity repo = repositoryService.getEntityById(event.getRepositoryId());
            if (event.getPullRequest() != null) {
                var reviews = gitHubApiClient.fetchPullRequestReviews(
                        event.getOwner(), event.getName(), event.getPullRequest().getNumber());
                ingestionOrchestrator.processPullRequest(repo, event.getPullRequest(), reviews);
            }
        } catch (Exception e) {
            log.error("Failed to process PR event: {}", e.getMessage(), e);
        }
    }
}

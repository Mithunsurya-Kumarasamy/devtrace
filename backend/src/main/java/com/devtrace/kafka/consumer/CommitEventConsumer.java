package com.devtrace.kafka.consumer;

import com.devtrace.entity.RepositoryEntity;
import com.devtrace.kafka.KafkaTopicConfig;
import com.devtrace.kafka.event.RawCommitEvent;
import com.devtrace.service.IngestionOrchestrator;
import com.devtrace.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class CommitEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CommitEventConsumer.class);

    private final IngestionOrchestrator ingestionOrchestrator;
    private final RepositoryService repositoryService;

    public CommitEventConsumer(IngestionOrchestrator ingestionOrchestrator, RepositoryService repositoryService) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.repositoryService = repositoryService;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_COMMITS_RAW, groupId = "devtrace-commit-workers", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleRawCommit(RawCommitEvent event) {
        log.debug("Received raw commit event: sha={}", event.getCommit() != null ? event.getCommit().getSha() : "null");
        try {
            RepositoryEntity repo = repositoryService.getEntityById(event.getRepositoryId());
            if (event.getCommit() != null) {
                ingestionOrchestrator.processCommit(repo, event.getCommit());
            }
        } catch (Exception e) {
            log.error("Failed to process commit event: {}", e.getMessage(), e);
        }
    }
}

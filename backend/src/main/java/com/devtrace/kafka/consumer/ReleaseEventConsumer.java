package com.devtrace.kafka.consumer;

import com.devtrace.entity.RepositoryEntity;
import com.devtrace.kafka.KafkaTopicConfig;
import com.devtrace.kafka.event.RawReleaseEvent;
import com.devtrace.service.IngestionOrchestrator;
import com.devtrace.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ReleaseEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReleaseEventConsumer.class);

    private final IngestionOrchestrator ingestionOrchestrator;
    private final RepositoryService repositoryService;

    public ReleaseEventConsumer(IngestionOrchestrator ingestionOrchestrator, RepositoryService repositoryService) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.repositoryService = repositoryService;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_RELEASES_RAW, groupId = "devtrace-release-workers", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleRawRelease(RawReleaseEvent event) {
        log.debug("Received raw release event: tag={}", event.getRelease() != null ? event.getRelease().getTagName() : "null");
        try {
            RepositoryEntity repo = repositoryService.getEntityById(event.getRepositoryId());
            if (event.getRelease() != null) {
                ingestionOrchestrator.processRelease(repo, event.getRelease());
            }
        } catch (Exception e) {
            log.error("Failed to process release event: {}", e.getMessage(), e);
        }
    }
}

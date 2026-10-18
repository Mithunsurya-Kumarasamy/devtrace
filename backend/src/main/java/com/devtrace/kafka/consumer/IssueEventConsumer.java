package com.devtrace.kafka.consumer;

import com.devtrace.entity.RepositoryEntity;
import com.devtrace.kafka.KafkaTopicConfig;
import com.devtrace.kafka.event.RawIssueEvent;
import com.devtrace.service.IngestionOrchestrator;
import com.devtrace.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class IssueEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(IssueEventConsumer.class);

    private final IngestionOrchestrator ingestionOrchestrator;
    private final RepositoryService repositoryService;

    public IssueEventConsumer(IngestionOrchestrator ingestionOrchestrator, RepositoryService repositoryService) {
        this.ingestionOrchestrator = ingestionOrchestrator;
        this.repositoryService = repositoryService;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_ISSUES_RAW, groupId = "devtrace-issue-workers", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleRawIssue(RawIssueEvent event) {
        log.debug("Received raw issue event: issueNumber={}", event.getIssue() != null ? event.getIssue().getNumber() : 0);
        try {
            RepositoryEntity repo = repositoryService.getEntityById(event.getRepositoryId());
            if (event.getIssue() != null) {
                ingestionOrchestrator.processIssue(repo, event.getIssue());
            }
        } catch (Exception e) {
            log.error("Failed to process issue event: {}", e.getMessage(), e);
        }
    }
}

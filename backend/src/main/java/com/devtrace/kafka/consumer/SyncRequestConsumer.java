package com.devtrace.kafka.consumer;

import com.devtrace.kafka.KafkaTopicConfig;
import com.devtrace.kafka.event.SyncTriggerEvent;
import com.devtrace.service.SyncJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SyncRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(SyncRequestConsumer.class);

    private final SyncJobService syncJobService;

    public SyncRequestConsumer(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_SYNC_REQUESTS, groupId = "devtrace-sync-coordinator", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void handleSyncTrigger(SyncTriggerEvent event) {
        log.info("Received Kafka sync request: jobId={}, repoId={}", event.getJobId(), event.getRepositoryId());
        try {
            syncJobService.executeSync(event.getJobId(), event.getRepositoryId(), event.isForceFullSync());
        } catch (Exception e) {
            log.error("Error executing Kafka sync task: {}", e.getMessage(), e);
        }
    }
}

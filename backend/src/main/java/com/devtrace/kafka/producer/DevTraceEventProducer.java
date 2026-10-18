package com.devtrace.kafka.producer;

import com.devtrace.kafka.KafkaTopicConfig;
import com.devtrace.kafka.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DevTraceEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DevTraceEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DevTraceEventProducer(@org.springframework.beans.factory.annotation.Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSyncTrigger(SyncTriggerEvent event) {
        log.info("Emitting sync trigger: repoId={}, jobId={}", event.getRepositoryId(), event.getJobId());
        if (kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_SYNC_REQUESTS, event.getRepositoryId(), event);
        }
    }

    public void sendRawCommit(RawCommitEvent event) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_COMMITS_RAW, event.getRepositoryId(), event);
        }
    }

    public void sendRawPullRequest(RawPullRequestEvent event) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_PULL_REQUESTS_RAW, event.getRepositoryId(), event);
        }
    }

    public void sendRawIssue(RawIssueEvent event) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_ISSUES_RAW, event.getRepositoryId(), event);
        }
    }

    public void sendRawRelease(RawReleaseEvent event) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_RELEASES_RAW, event.getRepositoryId(), event);
        }
    }

    public void sendSyncProgress(SyncProgressEvent event) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_SYNC_PROGRESS, event.getJobId(), event);
        }
    }
}

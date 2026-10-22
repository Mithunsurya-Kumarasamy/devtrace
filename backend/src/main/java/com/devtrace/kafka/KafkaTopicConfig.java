package com.devtrace.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
public class KafkaTopicConfig {

    public static final String TOPIC_SYNC_REQUESTS = "devtrace.sync.requests";
    public static final String TOPIC_COMMITS_RAW = "devtrace.commits.raw";
    public static final String TOPIC_PULL_REQUESTS_RAW = "devtrace.pull-requests.raw";
    public static final String TOPIC_ISSUES_RAW = "devtrace.issues.raw";
    public static final String TOPIC_RELEASES_RAW = "devtrace.releases.raw";
    public static final String TOPIC_SYNC_PROGRESS = "devtrace.sync.progress";

    @Bean
    public NewTopic syncRequestsTopic() {
        return TopicBuilder.name(TOPIC_SYNC_REQUESTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic commitsRawTopic() {
        return TopicBuilder.name(TOPIC_COMMITS_RAW).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic pullRequestsRawTopic() {
        return TopicBuilder.name(TOPIC_PULL_REQUESTS_RAW).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic issuesRawTopic() {
        return TopicBuilder.name(TOPIC_ISSUES_RAW).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic releasesRawTopic() {
        return TopicBuilder.name(TOPIC_RELEASES_RAW).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic syncProgressTopic() {
        return TopicBuilder.name(TOPIC_SYNC_PROGRESS).partitions(3).replicas(1).build();
    }
}

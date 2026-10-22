package com.devtrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pr_timeline_events")
public class PullRequestTimelineEventEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequestEntity pullRequest;

    @Column(name = "actor_login", length = 100)
    private String actorLogin;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    public PullRequestTimelineEventEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public PullRequestTimelineEventEntity(PullRequestEntity pullRequest, String actorLogin, String eventType, Instant eventTimestamp) {
        this();
        this.pullRequest = pullRequest;
        this.actorLogin = actorLogin;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public PullRequestEntity getPullRequest() { return pullRequest; }
    public void setPullRequest(PullRequestEntity pullRequest) { this.pullRequest = pullRequest; }

    public String getActorLogin() { return actorLogin; }
    public void setActorLogin(String actorLogin) { this.actorLogin = actorLogin; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

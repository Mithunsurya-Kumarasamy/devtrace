package com.devtrace.exception;

import java.time.Instant;

public class RateLimitExceededException extends GitHubApiException {
    private final Instant resetTime;

    public RateLimitExceededException(String message, Instant resetTime) {
        super(message, 429);
        this.resetTime = resetTime;
    }

    public Instant getResetTime() {
        return resetTime;
    }
}

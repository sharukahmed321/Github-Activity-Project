package com.savant.labs.activity.tracker.exception;

import lombok.Getter;

import java.time.LocalDateTime;

import static com.savant.labs.activity.tracker.constants.GitHubApiConstants.RATE_LIMIT_EXCEEDED;

@Getter
public class RateLimitExceededException extends GitHubConnectorException {
    private final LocalDateTime resetTime;
    private final int remainingRequests;

    public RateLimitExceededException(String message, LocalDateTime resetTime, int remainingRequests) {
        super(message, RATE_LIMIT_EXCEEDED, 429);
        this.resetTime = resetTime;
        this.remainingRequests = remainingRequests;
    }

}

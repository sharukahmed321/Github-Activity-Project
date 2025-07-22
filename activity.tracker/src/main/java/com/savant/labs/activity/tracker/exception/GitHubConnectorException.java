package com.savant.labs.activity.tracker.exception;

import lombok.Getter;

@Getter
public class GitHubConnectorException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public GitHubConnectorException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR";
        this.httpStatus = 500;
    }

    public GitHubConnectorException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN_ERROR";
        this.httpStatus = 500;
    }

    public GitHubConnectorException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public GitHubConnectorException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}

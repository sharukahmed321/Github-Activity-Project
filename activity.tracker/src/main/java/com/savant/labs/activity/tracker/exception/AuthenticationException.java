package com.savant.labs.activity.tracker.exception;

public class AuthenticationException extends GitHubConnectorException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED", 401);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", 401, cause);
    }
}

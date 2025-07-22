package com.savant.labs.activity.tracker.exception;

public class UserNotFoundException extends GitHubConnectorException {

    public UserNotFoundException(String username) {
        super("GitHub user not found: " + username, "USER_NOT_FOUND", 404);
    }
}

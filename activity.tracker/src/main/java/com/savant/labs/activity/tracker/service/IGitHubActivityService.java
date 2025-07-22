package com.savant.labs.activity.tracker.service;

import com.savant.labs.activity.tracker.exception.GitHubConnectorException;
import com.savant.labs.activity.tracker.models.RepositoryActivity;

public interface IGitHubActivityService {
    RepositoryActivity fetchUserActivity(String username) throws GitHubConnectorException;
}


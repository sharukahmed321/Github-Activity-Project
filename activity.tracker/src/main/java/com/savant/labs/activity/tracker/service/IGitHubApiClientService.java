package com.savant.labs.activity.tracker.service;

import com.savant.labs.activity.tracker.models.GitHubCommit;
import com.savant.labs.activity.tracker.models.GitHubRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IGitHubApiClientService {
    CompletableFuture<List<GitHubRepository>> fetchRepositoriesAsync(String username);
    CompletableFuture<List<GitHubCommit>> fetchCommitsAsync(String repoFullName, String branch);
}


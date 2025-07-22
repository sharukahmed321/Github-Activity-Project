package com.savant.labs.activity.tracker.service.impl;

import com.savant.labs.activity.tracker.exception.GitHubConnectorException;
import com.savant.labs.activity.tracker.models.GitHubCommit;
import com.savant.labs.activity.tracker.models.GitHubRepository;
import com.savant.labs.activity.tracker.models.RepositoryActivity;
import com.savant.labs.activity.tracker.service.IGitHubActivityService;
import com.savant.labs.activity.tracker.service.IGitHubApiClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class GitHubActivityService implements IGitHubActivityService {

    private final IGitHubApiClientService apiClient;

    @Autowired
    public GitHubActivityService(IGitHubApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    @Timed(value = "github.activity.fetch", description = "Time taken to fetch GitHub activity")
    @Counted(value = "github.activity.requests", description = "Number of GitHub activity requests")
    @CircuitBreaker(name = "github-api", fallbackMethod = "fetchActivityFallback")
    @Retry(name = "github-api")
    @Cacheable(value = "github-activity", key = "#username")
    public RepositoryActivity fetchUserActivity(String username) throws GitHubConnectorException {
        log.info("Fetching GitHub activity for user: {}", username);

        try {
            List<GitHubRepository> repositories = apiClient.fetchRepositoriesAsync(username).join();

            if (repositories.isEmpty()) {
                log.warn("No repositories found for user: {}", username);
                return new RepositoryActivity(username, Collections.emptyList());
            }

            List<CompletableFuture<GitHubRepository>> commitFutures = repositories.stream()
                    .map(this::fetchCommitsForRepository)
                    .toList();

            CompletableFuture<Void> allDone = CompletableFuture.allOf(
                    commitFutures.toArray(new CompletableFuture[0]));

            List<GitHubRepository> enrichedRepos = allDone
                    .thenApply(v -> commitFutures.stream()
                            .map(CompletableFuture::join)
                            .toList())
                    .join();

            RepositoryActivity activity = new RepositoryActivity(username, enrichedRepos);

            log.info("Fetched GitHub activity for user: {}. Repositories: {}, Total commits: {}",
                    username, activity.getTotalRepositories(), activity.getTotalCommitsFetched());

            return activity;

        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof GitHubConnectorException githubConnectorException) {
                throw githubConnectorException;
            }
            throw new GitHubConnectorException("Failure while fetching activity", cause);
        } catch (Exception e) {
            log.error("Unexpected error during GitHub activity fetch", e);
            throw new GitHubConnectorException("Unexpected error", e);
        }
    }

    private CompletableFuture<GitHubRepository> fetchCommitsForRepository(GitHubRepository repo) {
        return CompletableFuture.supplyAsync(() -> {
            String branch = Optional.ofNullable(repo.getDefaultBranch()).orElse("main");
            try {
                List<GitHubCommit> commits = apiClient.fetchCommitsAsync(repo.getFullName(), branch).join();
                repo.setRecentCommits(commits);
                log.debug("Fetched {} commits for repository: {}", commits.size(), repo.getName());
            } catch (Exception ex) {
                log.warn("Failed to fetch commits for {}: {}", repo.getName(), ex.getMessage());
                repo.setRecentCommits(Collections.emptyList());
            }
            return repo;
        });
    }

    public RepositoryActivity fetchActivityFallback(String username, Exception ex) {
        log.error("Fallback triggered for user: {} due to {}", username, ex.getMessage(), ex);
        return new RepositoryActivity(username, Collections.emptyList());
    }
}

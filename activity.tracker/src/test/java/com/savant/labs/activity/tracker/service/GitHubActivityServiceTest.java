package com.savant.labs.activity.tracker.service;

import com.savant.labs.activity.tracker.exception.GitHubConnectorException;
import com.savant.labs.activity.tracker.models.AuthorInfo;
import com.savant.labs.activity.tracker.models.CommitDetails;
import com.savant.labs.activity.tracker.models.GitHubCommit;
import com.savant.labs.activity.tracker.models.GitHubRepository;
import com.savant.labs.activity.tracker.models.RepositoryActivity;
import com.savant.labs.activity.tracker.service.impl.GitHubActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubActivityServiceTest {

    @Mock
    private IGitHubApiClientService apiClient;

    @InjectMocks
    private GitHubActivityService activityService;

    private GitHubRepository repo1;
    private GitHubRepository repo2;

    @BeforeEach
    void setup() {
        repo1 = new GitHubRepository();
        repo1.setName("Repo1");
        repo1.setFullName("user/Repo1");
        repo1.setDefaultBranch("main");

        repo2 = new GitHubRepository();
        repo2.setName("Repo2");
        repo2.setFullName("user/Repo2");
        repo2.setDefaultBranch("master");
    }

    @Test
    void shouldReturnRepositoryActivity_whenRepositoriesAndCommitsExist() throws Exception {
        List<GitHubRepository> repositories = List.of(repo1, repo2);

        CommitDetails commitDetails1 = new CommitDetails();
        commitDetails1.setMessage("Initial commit");
        commitDetails1.setAuthor(new AuthorInfo("Alice", "alice@example.com", LocalDateTime.parse("2023-07-01T10:00:00")));

        CommitDetails commitDetails2 = new CommitDetails();
        commitDetails2.setMessage("Add README");
        commitDetails2.setAuthor(new AuthorInfo("Bob", "bob@example.com", LocalDateTime.parse("2023-07-01T11:00:00")));

        CommitDetails commitDetails3 = new CommitDetails();
        commitDetails3.setMessage("Fix issue");
        commitDetails3.setAuthor(new AuthorInfo("Charlie", "charlie@example.com", LocalDateTime.parse("2023-07-02T09:00:00")));

        List<GitHubCommit> commits1 = List.of(
                new GitHubCommit("c1", commitDetails1),
                new GitHubCommit("c2", commitDetails2)
        );

        List<GitHubCommit> commits2 = List.of(
                new GitHubCommit("c3", commitDetails3)
        );


        when(apiClient.fetchRepositoriesAsync("user"))
                .thenReturn(CompletableFuture.completedFuture(repositories));

        when(apiClient.fetchCommitsAsync(eq("user/Repo1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(commits1));

        when(apiClient.fetchCommitsAsync(eq("user/Repo2"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(commits2));

        RepositoryActivity result = activityService.fetchUserActivity("user");

        assertEquals("user", result.getUsername());
        assertEquals(2, result.getTotalRepositories());
        assertEquals(3, result.getTotalCommitsFetched());
    }

    @Test
    void shouldReturnEmptyActivity_whenNoRepositoriesFound() throws Exception {
        when(apiClient.fetchRepositoriesAsync("user"))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        RepositoryActivity result = activityService.fetchUserActivity("user");

        assertEquals("user", result.getUsername());
        assertTrue(result.getRepositories().isEmpty());
    }

    @Test
    void shouldFallbackWithEmptyActivity_onException() {
        RepositoryActivity fallback = activityService.fetchActivityFallback("user", new RuntimeException("Error"));

        assertEquals("user", fallback.getUsername());
        assertTrue(fallback.getRepositories().isEmpty());
    }

    @Test
    void shouldThrowGitHubConnectorException_onWrappedException() {
        when(apiClient.fetchRepositoriesAsync("user"))
                .thenReturn(CompletableFuture.failedFuture(new GitHubConnectorException("API error")));

        GitHubConnectorException ex = assertThrows(GitHubConnectorException.class,
                () -> activityService.fetchUserActivity("user"));

        assertEquals("API error", ex.getMessage());
    }

    @Test
    void shouldSetEmptyCommits_whenCommitFetchFails() throws Exception {
        when(apiClient.fetchRepositoriesAsync("user"))
                .thenReturn(CompletableFuture.completedFuture(List.of(repo1)));

        when(apiClient.fetchCommitsAsync(anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Git error")));

        RepositoryActivity result = activityService.fetchUserActivity("user");

        assertEquals("user", result.getUsername());
        assertEquals(1, result.getRepositories().size());
        assertTrue(result.getRepositories().getFirst().getRecentCommits().isEmpty());
    }
}

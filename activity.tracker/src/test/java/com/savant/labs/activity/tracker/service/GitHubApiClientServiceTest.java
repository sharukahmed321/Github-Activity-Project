package com.savant.labs.activity.tracker.service;

import com.savant.labs.activity.tracker.config.GitHubConfig;
import com.savant.labs.activity.tracker.service.impl.GitHubApiClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubApiClientServiceTest {

    @Mock(lenient = true)
    private WebClient webClient;
    @Mock(lenient = true)
    private GitHubConfig config;
    @Mock(lenient = true)
    private com.savant.labs.activity.tracker.service.IRateLimitService rateLimitService;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GitHubApiClientService gitHubApiClientService;

    @BeforeEach
    void setUp() {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(rateLimitService.checkRateLimit()).thenReturn(Mono.empty());
    }

    private void setupUrlBuildingConfig() {
        lenient().when(config.getBaseUrl()).thenReturn("https://api.github.com");
        lenient().when(config.getPerPage()).thenReturn(30);
        lenient().when(config.getMaxCommits()).thenReturn(10);

        GitHubConfig.Api apiConfig = new GitHubConfig.Api();
        apiConfig.setUsersReposEndpoint("/users/{username}/repos?type=public&sort=updated&per_page={perPage}&page={page}");
        apiConfig.setRepoCommitsEndpoint("/repos/{repoFullName}/commits?sha={branch}&per_page={maxCommits}&page={page}");
        lenient().when(config.getApi()).thenReturn(apiConfig);
    }

    @Test
    void buildRepositoriesUrl_CorrectlyBuildsUrl() {
        setupUrlBuildingConfig();
        String username = "testuser";
        int page = 2;
        String expectedUrl = "https://api.github.com/users/testuser/repos?type=public&sort=updated&per_page=30&page=2";

        String actualUrl = gitHubApiClientService.buildRepositoriesUrl(username, page);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void buildCommitsUrl_CorrectlyBuildsUrl() {
        setupUrlBuildingConfig();
        String repoFullName = "testuser/testrepo";
        String branch = "develop";
        int page = 3;
        String expectedUrl = "https://api.github.com/repos/testuser/testrepo/commits?sha=develop&per_page=10&page=3";

        String actualUrl = gitHubApiClientService.buildCommitsUrl(repoFullName, branch, page);
        assertEquals(expectedUrl, actualUrl);
    }
}
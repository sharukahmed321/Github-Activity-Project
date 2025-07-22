package com.savant.labs.activity.tracker.service.impl;

import com.savant.labs.activity.tracker.config.GitHubConfig;
import com.savant.labs.activity.tracker.constants.GitHubApiConstants;
import com.savant.labs.activity.tracker.exception.AuthenticationException;
import com.savant.labs.activity.tracker.exception.GitHubConnectorException;
import com.savant.labs.activity.tracker.exception.RateLimitExceededException;
import com.savant.labs.activity.tracker.exception.UserNotFoundException;
import com.savant.labs.activity.tracker.models.GitHubCommit;
import com.savant.labs.activity.tracker.models.GitHubRepository;
import com.savant.labs.activity.tracker.service.IGitHubApiClientService;
import com.savant.labs.activity.tracker.service.IRateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class GitHubApiClientService implements IGitHubApiClientService {

    private final WebClient webClient;
    private final GitHubConfig config;
    private final IRateLimitService rateLimitService;

    @Autowired
    public GitHubApiClientService(WebClient webClient, GitHubConfig config, IRateLimitService rateLimitService) {
        this.webClient = webClient;
        this.config = config;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public CompletableFuture<List<GitHubRepository>> fetchRepositoriesAsync(String username) {
        return fetchRepositories(username, 1)
                .toFuture();
    }

    @Override
    public CompletableFuture<List<GitHubCommit>> fetchCommitsAsync(String repoFullName, String branch) {
        return fetchCommits(repoFullName, branch, 1)
                .toFuture();
    }

    private Mono<List<GitHubRepository>> fetchRepositories(String username, int page) {
        String url = buildRepositoriesUrl(username, page);

        log.debug(config.getLog().getFetchingRepositories(), username, page);

        return rateLimitService.checkRateLimit()
                .then(makeRequest(url, new ParameterizedTypeReference<List<GitHubRepository>>() {}))
                .doOnNext(repos -> log.info(config.getLog().getFetchedRepositories(), repos.size(), username))
                .onErrorMap(this::mapException);
    }

    private Mono<List<GitHubCommit>> fetchCommits(String repoFullName, String branch, int page) {
        String url = buildCommitsUrl(repoFullName, branch, page);

        log.debug(config.getLog().getFetchingCommits(), repoFullName, branch, page);

        return rateLimitService.checkRateLimit()
                .then(makeRequest(url, new ParameterizedTypeReference<List<GitHubCommit>>() {}))
                .doOnNext(commits -> log.debug(config.getLog().getFetchedCommits(), commits.size(), repoFullName))
                .onErrorMap(this::mapException);
    }

    public String buildRepositoriesUrl(String username, int page) {
        String endpoint = config.getApi().getUsersReposEndpoint();
        Map<String, Object> params = new HashMap<>();
        params.put(GitHubApiConstants.PARAM_USERNAME, username);
        params.put(GitHubApiConstants.PARAM_PER_PAGE, config.getPerPage());
        params.put(GitHubApiConstants.PARAM_PAGE, page);

        return config.getBaseUrl() + replaceUrlParameters(endpoint, params);
    }

    public String buildCommitsUrl(String repoFullName, String branch, int page) {
        String endpoint = config.getApi().getRepoCommitsEndpoint();
        Map<String, Object> params = new HashMap<>();
        params.put(GitHubApiConstants.PARAM_REPO_FULL_NAME, repoFullName);
        params.put(GitHubApiConstants.PARAM_BRANCH, branch);
        params.put(GitHubApiConstants.PARAM_MAX_COMMITS, config.getMaxCommits());
        params.put(GitHubApiConstants.PARAM_PAGE, page);

        return config.getBaseUrl() + replaceUrlParameters(endpoint, params);
    }

    private String replaceUrlParameters(String endpoint, Map<String, Object> params) {
        String result = endpoint;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }

    private <T> Mono<T> makeRequest(String url, ParameterizedTypeReference<T> responseType) {
        return webClient.get()
                .uri(url)
                .header(GitHubApiConstants.AUTHORIZATION_HEADER,
                        GitHubApiConstants.BEARER_TOKEN_PREFIX + config.getToken())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(config.getMaxRetries(), Duration.ofMillis(config.getRetryDelayMs()))
                        .filter(throwable -> !(throwable instanceof AuthenticationException) &&
                                !(throwable instanceof UserNotFoundException)))
                .doOnNext(response -> updateRateLimit());
    }

    private Mono<? extends Throwable> handle4xxError(ClientResponse response) {
        return response.toEntity(String.class)
                .flatMap(entity -> {
                    HttpHeaders headers = response.headers().asHttpHeaders();
                    updateRateLimitFromHeaders(headers);

                    String body = entity.getBody();

                    return switch (response.statusCode().value()) {
                        case GitHubApiConstants.HTTP_UNAUTHORIZED ->
                                Mono.error(new AuthenticationException(config.getError().getAuthenticationFailed() + ": " + body));
                        case GitHubApiConstants.HTTP_FORBIDDEN -> {
                            if (isRateLimitExceeded(headers)) {
                                LocalDateTime resetTime = getRateLimitResetTime(headers);
                                int remaining = getRemainingRequests(headers);
                                yield Mono.error(new RateLimitExceededException(
                                        config.getError().getRateLimitExceeded(), resetTime, remaining));
                            }
                            yield Mono.error(new GitHubConnectorException(
                                    config.getError().getAccessForbidden() + ": " + body,
                                    GitHubApiConstants.ERROR_CODE_ACCESS_FORBIDDEN,
                                    GitHubApiConstants.HTTP_FORBIDDEN));
                        }
                        case GitHubApiConstants.HTTP_NOT_FOUND ->
                                Mono.error(new UserNotFoundException(extractUsernameFromError(body)));
                        default ->
                                Mono.error(new GitHubConnectorException(
                                        config.getError().getClientError() + ": " + body,
                                        GitHubApiConstants.ERROR_CODE_CLIENT_ERROR,
                                        response.statusCode().value()));
                    };
                });
    }

    private Mono<? extends Throwable> handle5xxError(ClientResponse response) {
        return response.toEntity(String.class)
                .flatMap(entity -> Mono.error(new GitHubConnectorException(
                        config.getError().getServerError() + ": " + entity.getBody(),
                        GitHubApiConstants.ERROR_CODE_SERVER_ERROR,
                        response.statusCode().value())));
    }

    private Throwable mapException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return switch (ex.getStatusCode().value()) {
                case GitHubApiConstants.HTTP_UNAUTHORIZED ->
                        new AuthenticationException(config.getError().getAuthenticationFailed(), ex);
                case GitHubApiConstants.HTTP_FORBIDDEN -> {
                    if (isRateLimitExceeded(ex.getHeaders())) {
                        LocalDateTime resetTime = getRateLimitResetTime(ex.getHeaders());
                        int remaining = getRemainingRequests(ex.getHeaders());
                        yield new RateLimitExceededException(config.getError().getRateLimitExceeded(), resetTime, remaining);
                    }
                    yield new GitHubConnectorException(
                            config.getError().getAccessForbidden(),
                            GitHubApiConstants.ERROR_CODE_ACCESS_FORBIDDEN,
                            GitHubApiConstants.HTTP_FORBIDDEN, ex);
                }
                case GitHubApiConstants.HTTP_NOT_FOUND ->
                        new UserNotFoundException(extractUsernameFromError(ex.getResponseBodyAsString()));
                default ->
                        new GitHubConnectorException(
                                config.getError().getApiError() + ": " + ex.getMessage(),
                                GitHubApiConstants.ERROR_CODE_API_ERROR,
                                ex.getStatusCode().value(), ex);
            };
        }
        return new GitHubConnectorException(config.getError().getUnexpectedError() + ": " + throwable.getMessage(), throwable);
    }

    private void updateRateLimit() {
        log.debug(config.getLog().getRequestSuccessful());
    }

    private void updateRateLimitFromHeaders(HttpHeaders headers) {
        try {
            String remaining = headers.getFirst(config.getHeaders().getRateLimitRemaining());
            String reset = headers.getFirst(config.getHeaders().getRateLimitReset());

            if (remaining != null && reset != null) {
                rateLimitService.updateRateLimit(Integer.parseInt(remaining), Long.parseLong(reset));
            }
        } catch (NumberFormatException e) {
            log.warn(config.getError().getParseRateLimitHeaders(), e);
        }
    }

    private boolean isRateLimitExceeded(HttpHeaders headers) {
        String remaining = headers.getFirst(config.getHeaders().getRateLimitRemaining());
        return GitHubApiConstants.RATE_LIMIT_ZERO.equals(remaining);
    }

    private LocalDateTime getRateLimitResetTime(HttpHeaders headers) {
        String reset = headers.getFirst(config.getHeaders().getRateLimitReset());
        if (reset != null) {
            try {
                return LocalDateTime.from(ZonedDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(Long.parseLong(reset)),
                        java.time.ZoneId.systemDefault()));
            } catch (NumberFormatException e) {
                log.warn(config.getError().getParseRateLimitResetTime(), e);
            }
        }
        return LocalDateTime.now().plusHours(GitHubApiConstants.RATE_LIMIT_RESET_BUFFER_HOURS);
    }

    private int getRemainingRequests(HttpHeaders headers) {
        String remaining = headers.getFirst(config.getHeaders().getRateLimitRemaining());
        return remaining != null ? Integer.parseInt(remaining) : GitHubApiConstants.DEFAULT_RATE_LIMIT_REMAINING;
    }

    private String extractUsernameFromError(String errorBody) {
        // Simple extraction logic - in production, you might want more sophisticated parsing
        return GitHubApiConstants.UNKNOWN_USERNAME;
    }

}

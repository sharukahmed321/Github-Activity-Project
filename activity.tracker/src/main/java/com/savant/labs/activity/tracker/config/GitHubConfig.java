package com.savant.labs.activity.tracker.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "github")
@Validated
@Setter
@Getter
public class GitHubConfig {

    @NotBlank(message = "GitHub token must not be blank")
    private String token;

    @NotBlank(message = "GitHub base URL must not be blank")
    private String baseUrl;

    @Positive(message = "Per page must be positive")
    private int perPage = 30;

    @Positive(message = "Max commits must be positive")
    private int maxCommits = 10;

    @Positive(message = "Max retries must be positive")
    private int maxRetries = 3;

    @Positive(message = "Retry delay must be positive")
    private int retryDelayMs = 1000;

    // API Endpoints
    private Api api = new Api();

    // Headers
    private Headers headers = new Headers();

    // Error Messages
    private Error error = new Error();

    // Log Messages
    private Log log = new Log();

    @Getter
    @Setter
    public static class Api {
        private String usersReposEndpoint;
        private String repoCommitsEndpoint;
    }

    @Getter
    @Setter
    public static class Headers {
        private String rateLimitRemaining;
        private String rateLimitReset;
    }

    @Getter
    @Setter
    public static class Error {
        private String authenticationFailed;
        private String accessForbidden;
        private String rateLimitExceeded;
        private String clientError;
        private String serverError;
        private String apiError;
        private String unexpectedError;
        private String parseRateLimitHeaders;
        private String parseRateLimitResetTime;
    }

    @Getter
    @Setter
    public static class Log {
        private String fetchingRepositories;
        private String fetchedRepositories;
        private String fetchingCommits;
        private String fetchedCommits;
        private String requestSuccessful;
        private String rateLimitLow;
        private String rateLimitUpdated;
    }
}


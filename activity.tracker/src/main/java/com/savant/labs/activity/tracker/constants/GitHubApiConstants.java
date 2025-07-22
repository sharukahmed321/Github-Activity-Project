package com.savant.labs.activity.tracker.constants;

public final class GitHubApiConstants {

    private GitHubApiConstants() {}

    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000; // 5 seconds
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";

    // HTTP Status Codes
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;

    // Error Codes
    public static final String ERROR_CODE_ACCESS_FORBIDDEN = "ACCESS_FORBIDDEN";
    public static final String ERROR_CODE_CLIENT_ERROR = "CLIENT_ERROR";
    public static final String ERROR_CODE_SERVER_ERROR = "SERVER_ERROR";
    public static final String ERROR_CODE_API_ERROR = "API_ERROR";

    // Rate Limit
    public static final String RATE_LIMIT_ZERO = "0";
    public static final int RATE_LIMIT_WARNING_THRESHOLD = 100;
    public static final int DEFAULT_RATE_LIMIT_REMAINING = 0;
    public static final long RATE_LIMIT_RESET_BUFFER_HOURS = 1L;

    // Default Values
    public static final String DEFAULT_BRANCH_FALLBACK = "main";
    public static final String UNKNOWN_USERNAME = "unknown";

    // URL Parameters
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PER_PAGE = "perPage";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_REPO_FULL_NAME = "repoFullName";
    public static final String PARAM_BRANCH = "branch";
    public static final String PARAM_MAX_COMMITS = "maxCommits";

    // Authorization
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";
}


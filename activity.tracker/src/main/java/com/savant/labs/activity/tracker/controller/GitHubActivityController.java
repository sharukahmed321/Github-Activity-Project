package com.savant.labs.activity.tracker.controller;

import com.savant.labs.activity.tracker.exception.GitHubApiException;
import com.savant.labs.activity.tracker.exception.GitHubConnectorException;
import com.savant.labs.activity.tracker.models.RepositoryActivity;
import com.savant.labs.activity.tracker.service.IGitHubActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/github")
@Validated
@Tag(name = "GitHub Activity", description = "GitHub repository activity operations")
@Slf4j
public class GitHubActivityController {

    private final IGitHubActivityService activityService;

    @Autowired
    public GitHubActivityController(IGitHubActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/activity/{username}")
    @Operation(summary = "Fetch GitHub user activity",
            description = "Retrieves public repositories and recent commits for a GitHub user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user activity",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RepositoryActivity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username format"),
            @ApiResponse(responseCode = "404", description = "GitHub user not found"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RepositoryActivity> fetchUserActivity(
            @Parameter(description = "GitHub username", example = "username")
            @PathVariable
            @NotBlank(message = "Username cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$",
                    message = "Invalid GitHub username format")
            String username) {

        try {
            log.info("REST API: Fetching activity for user: {}", username);

            RepositoryActivity activity = activityService.fetchUserActivity(username);

            return ResponseEntity.ok(activity);

        } catch (GitHubConnectorException e) {
            log.error("Failed to fetch activity for user: {}. Error: {}", username, e.getMessage());
            throw new GitHubApiException(e.getMessage(), e.getErrorCode(), e.getHttpStatus(), e);
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the GitHub connector is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GitHub Activity Connector is running");
    }
}


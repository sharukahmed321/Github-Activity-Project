package com.savant.labs.activity.tracker.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RepositoryActivity {

    @NotBlank
    private String username;

    @NotNull
    private LocalDateTime fetchedAt;

    @NotNull
    private List<GitHubRepository> repositories;

    private Integer totalRepositories;
    private Integer totalCommitsFetched;

    public RepositoryActivity() {
        this.fetchedAt = LocalDateTime.now();
    }

    public RepositoryActivity(String username, List<GitHubRepository> repositories) {
        this();
        this.username = username;
        this.repositories = repositories;
        this.totalRepositories = repositories.size();
        this.totalCommitsFetched = repositories.stream()
                .mapToInt(repo -> repo.getRecentCommits() != null ? repo.getRecentCommits().size() : 0)
                .sum();
    }
}

package com.savant.labs.activity.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
public class GitHubRepository {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("pushed_at")
    private LocalDateTime pushedAt;

    private String language;

    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    @JsonProperty("watchers_count")
    private Integer watchersCount;

    @JsonProperty("forks_count")
    private Integer forksCount;

    @JsonProperty("default_branch")
    private String defaultBranch;

    private Owner owner;

    private List<GitHubCommit> recentCommits;

    public GitHubRepository() {
    }

    public GitHubRepository(Long id, String name, String fullName) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "GitHubRepository{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", language='" + language + '\'' +
                ", stargazersCount=" + stargazersCount +
                '}';
    }
}

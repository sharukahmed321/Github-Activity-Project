package com.savant.labs.activity.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class GitHubCommit {

    @NotBlank
    private String sha;

    @NotNull
    private CommitDetails commit;

    private CommitAuthor author;

    @JsonProperty("html_url")
    private String htmlUrl;

    public GitHubCommit() {
    }

    public GitHubCommit(String sha, CommitDetails commit) {
        this.sha = sha;
        this.commit = commit;
    }

    @Override
    public String toString() {
        return "GitHubCommit{" +
                "sha='" + sha + '\'' +
                ", message='" + (commit != null ? commit.getMessage() : null) + '\'' +
                ", author='" + (commit != null && commit.getAuthor() != null ? commit.getAuthor().getName() : null) + '\'' +
                '}';
    }
}

package com.savant.labs.activity.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CommitAuthor {

    private Long id;
    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;

    public CommitAuthor() {
    }

    public CommitAuthor(Long id, String login) {
        this.id = id;
        this.login = login;
    }
}

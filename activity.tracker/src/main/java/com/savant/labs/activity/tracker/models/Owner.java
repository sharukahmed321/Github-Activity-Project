package com.savant.labs.activity.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Owner {

    private Long id;
    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String type;

    // Constructors
    public Owner() {
    }

    public Owner(Long id, String login, String type) {
        this.id = id;
        this.login = login;
        this.type = type;
    }
}

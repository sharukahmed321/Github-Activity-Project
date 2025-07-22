package com.savant.labs.activity.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CommitDetails {
    private String message;
    private AuthorInfo author;
    private AuthorInfo committer;

    // Constructors
    public CommitDetails() {}

    public CommitDetails(String message, AuthorInfo author) {
        this.message = message;
        this.author = author;
    }
}
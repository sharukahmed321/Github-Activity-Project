package com.savant.labs.activity.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AuthorInfo {
    private String name;
    private String email;
    private LocalDateTime date;

    public AuthorInfo() {
    }

    public AuthorInfo(String name, String email, LocalDateTime date) {
        this.name = name;
        this.email = email;
        this.date = date;
    }
}

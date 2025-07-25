package io.github.rosestack.app.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppWhiteList {
    private String id;

    // PHONE,USERNAME,EMAIL
    private String type;

    private String value;

    private LocalDateTime createdAt;
}

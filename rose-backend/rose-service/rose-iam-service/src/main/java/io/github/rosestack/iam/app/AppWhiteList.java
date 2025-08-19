package io.github.rosestack.iam.app;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AppWhiteList {
    private String id;

    // PHONE,USERNAME,EMAIL
    private String type;

    private String value;

    private LocalDateTime createdAt;
}

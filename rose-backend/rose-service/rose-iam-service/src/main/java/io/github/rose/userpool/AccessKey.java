package io.github.rose.userpool;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessKey {
    private Long id;
    private String accessKeyId;
    private String accessKeySecret;
    private String status; // activated,staging,revoked
    private String type; // userpool
    private String userId;
    private String userPoolId;
    private Boolean enabled;
    private String tenantId;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
}

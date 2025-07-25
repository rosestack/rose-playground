package io.github.rose.tenant;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantPermission {
    private Long id;

    private String resourceId;

    private String tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

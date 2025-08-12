package io.github.rosestack.iam.tenant;

import java.time.LocalDateTime;
import lombok.Data;

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

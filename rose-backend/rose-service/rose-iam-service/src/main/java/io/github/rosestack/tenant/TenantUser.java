package io.github.rosestack.tenant;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantUser {
    private Long id;

    private String tenantId;

    private String userId;

    /**
     * 用户原始的租户 ID
     */
    private String originTenantId;

    private Boolean isTenantAdmin;

    /**
     * 用户主部门 ID
     */
    private String mainDepartmentId;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

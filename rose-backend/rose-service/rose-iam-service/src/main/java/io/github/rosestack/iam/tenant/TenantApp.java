package io.github.rosestack.iam.tenant;

import lombok.Data;

/** 租户协助者 */
@Data
public class TenantApp {
    private Long id;

    private String tenantId;

    private String appId;

    private String userPoolId;

    /** 创建时间 */
    private String createdAt;
}

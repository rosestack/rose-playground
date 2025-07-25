package io.github.rosestack.tenant;

import lombok.Data;

/**
 * 租户协助者：只有本地用户才可以添加到多租户管理员。租户的创建者自动成为多租户管理员。
 */
@Data
public class TenantCooperator {
    private Long id;

    //租户用户
    private String tenantUserId;

    //用户池用户
    private String userId;

    //为外部用户时，userId不为空；内部用户时，tenantUserId不为空
    private Boolean external;

    //creatorUser、tenantUser、externalUser
    private String type;

}

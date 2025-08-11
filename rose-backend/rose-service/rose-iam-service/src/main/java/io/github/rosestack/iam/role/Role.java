package io.github.rosestack.iam.role;

import lombok.Data;

/**
 * 超级管理员
 * 系统全局管理员：system_all_admin
 * 系统身份源管理员：
 * 系统岗位管理员
 * 系统用户组管理员
 * 系统成员管理员
 * 系统组织机构管理员
 * 系统应用管理员
 */
@Data
public class Role {
    private String id;
    private String name;
    private String code;
    private String description;

    private Boolean isSystem;

    private String userPoolId;

    private String appId;

    /**
     * 租户 ID，可以为空
     */
    private String tenantId;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private String createdAt;
    /**
     * 修改时间
     */
    private String updatedAt;
}

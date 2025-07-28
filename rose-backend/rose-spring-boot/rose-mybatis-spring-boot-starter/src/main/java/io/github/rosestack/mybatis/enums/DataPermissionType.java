package io.github.rosestack.mybatis.enums;

/**
 * 数据权限类型枚举
 */
public enum DataPermissionType {
    /**
     * 用户级权限
     */
    USER,

    /**
     * 部门级权限
     */
    DEPT,

    /**
     * 组织级权限
     */
    ORG,

    /**
     * 角色级权限
     */
    ROLE,

    /**
     * 自定义权限
     */
    CUSTOM
}
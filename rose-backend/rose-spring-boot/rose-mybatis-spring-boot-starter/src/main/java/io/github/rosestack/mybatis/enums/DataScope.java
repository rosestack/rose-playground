package io.github.rosestack.mybatis.enums;

/**
 * 数据权限范围枚举
 */
public enum DataScope {
    SELF,

    PARENT,

    /**
     * 本部门及下级部门数据
     */
    PARENT_AND_CHILD,

    /**
     * 本组织数据
     */
    PARENT_PARENT,

    /**
     * 本组织及下级组织数据
     */
    PARENT_PARENT_AND_CHILD,

    /**
     * 全部数据
     */
    ALL,

    /**
     * 自定义数据范围
     */
    CUSTOM
}
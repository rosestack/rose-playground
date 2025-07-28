package io.github.rosestack.mybatis.enums;

/**
 * 数据权限范围枚举
 */
public enum DataScope {
    /**
     * 仅本人数据
     */
    SELF,

    /**
     * 本部门数据
     */
    DEPT,

    /**
     * 本部门及下级部门数据
     */
    DEPT_AND_CHILD,

    /**
     * 本组织数据
     */
    ORG,

    /**
     * 本组织及下级组织数据
     */
    ORG_AND_CHILD,

    /**
     * 全部数据
     */
    ALL,

    /**
     * 自定义数据范围
     */
    CUSTOM
}
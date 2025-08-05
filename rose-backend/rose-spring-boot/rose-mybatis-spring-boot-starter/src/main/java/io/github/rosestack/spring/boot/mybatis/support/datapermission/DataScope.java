package io.github.rosestack.spring.boot.mybatis.support.datapermission;

/**
 * 数据权限范围枚举
 */
public enum DataScope {
    SELF,

    PARENT,

    PARENT_AND_CHILD,

    PARENT_PARENT,

    PARENT_PARENT_AND_CHILD,

    ALL,

    CUSTOM
}
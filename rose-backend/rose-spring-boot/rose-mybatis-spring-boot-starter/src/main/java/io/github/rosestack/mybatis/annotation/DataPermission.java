package io.github.rosestack.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 用于标记需要进行数据权限控制的方法或类。
 * 支持基于用户、部门、角色等维度的数据权限控制。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 数据权限字段
     * <p>
     * 指定用于数据权限控制的字段名，如：user_id、dept_id、org_id 等。
     * 支持表名前缀，如：u.user_id、dept.dept_id
     * </p>
     *
     * @return 数据权限字段名
     */
    String field() default "user_id";

    /**
     * 表别名
     * <p>
     * 在多表 JOIN 查询中指定表别名，如：u、main、t1 等。
     * 如果不指定，将尝试自动识别主表。
     * </p>
     *
     * @return 表别名
     */
    String tableAlias() default "";

    /**
     * 数据权限类型
     *
     * @return 数据权限类型
     */
    DataPermissionType type() default DataPermissionType.USER;

    /**
     * 数据权限范围
     *
     * @return 数据权限范围
     */
    DataScope scope() default DataScope.SELF;

    /**
     * 数据权限类型枚举
     */
    enum DataPermissionType {
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

    /**
     * 数据权限范围枚举
     */
    enum DataScope {
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
}

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
     * </p>
     *
     * @return 数据权限字段名
     */
    String field() default "user_id";

    String name();

    /**
     * 字段数据类型
     * <p>
     * 指定权限字段的数据类型，用于正确构建 SQL 表达式。
     * 支持：STRING、Number 等类型。
     * </p>
     *
     * @return 字段数据类型
     */
    FieldType fieldType() default FieldType.STRING;

    /**
     * 字段数据类型枚举
     */
    enum FieldType {
        /**
         * 字符串类型, 包括 UUID -  使用引号
         */
        STRING,

        /**
         * 长整型 - 不使用引号
         */
        NUMBER,
    }
}

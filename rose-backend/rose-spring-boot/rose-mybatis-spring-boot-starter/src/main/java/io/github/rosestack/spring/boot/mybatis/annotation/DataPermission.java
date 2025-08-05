package io.github.rosestack.spring.boot.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 用于标记需要进行数据权限控制的方法或类。
 * 通过字段名自动匹配对应的权限提供者，权限范围由提供者内部决定。
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
     *
     * @return 数据权限字段名
     */
    String field();

    /**
     * 字段数据类型
     * <p>
     * 指定权限字段的数据类型，用于正确构建 SQL 表达式。
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
         * 字符串类型，包括 UUID - 使用单引号包围
         */
        STRING,

        /**
         * 数值类型，包括 LONG、INTEGER - 不使用引号
         */
        NUMBER,
    }
}

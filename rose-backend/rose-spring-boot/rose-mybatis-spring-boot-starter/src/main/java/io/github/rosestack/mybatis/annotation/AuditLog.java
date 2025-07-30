package io.github.rosestack.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 数据变更日志注解
 * <p>
 * 用于标记需要记录变更日志的实体类或方法。
 * 记录数据变更前后的值，支持字段级别的变更追踪。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 业务模块名称
     *
     * @return 业务模块名称
     */
    String module() default "";

    /**
     * 业务操作名称
     *
     * @return 业务操作名称
     */
    String value() default "";

    /**
     * 忽略的字段列表
     * <p>
     * 这些字段的变更不会被记录，如：password、version 等。
     * </p>
     *
     * @return 忽略的字段列表
     */
    String[] ignoreFields() default {"password", "version"};
}

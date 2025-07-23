package io.github.rose.security.rbac.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * <p>
 * 用于方法级别的权限控制，支持单个权限检查。
 * 可以与Spring Security的@PreAuthorize配合使用。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @HasPermission("user:create")
 * public User createUser(UserCreateRequest request) {
 *     // 方法实现
 * }
 * }</pre>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    /**
     * 权限名称
     * <p>
     * 支持权限表达式，如：user:create、user:read、system:config等
     * </p>
     *
     * @return 权限名称
     */
    String value();

    /**
     * 权限描述
     * <p>
     * 用于文档生成和权限管理界面显示
     * </p>
     *
     * @return 权限描述
     */
    String description() default "";

    /**
     * 是否必须拥有权限
     * <p>
     * true: 必须拥有权限才能访问（默认）
     * false: 不拥有权限也可以访问，但会记录日志
     * </p>
     *
     * @return 是否必须拥有权限
     */
    boolean required() default true;

    /**
     * 权限检查失败时的错误消息
     *
     * @return 错误消息
     */
    String errorMessage() default "权限不足";
}
package io.github.rose.security.rbac.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任一权限检查注解
 * <p>
 * 用于方法级别的权限控制，支持多个权限中任一满足即可。
 * 当用户拥有指定权限中的任意一个时，即可访问该方法。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @HasAnyPermission({"user:create", "user:update"})
 * public User saveUser(UserSaveRequest request) {
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
public @interface HasAnyPermission {

    /**
     * 权限名称数组
     * <p>
     * 用户拥有其中任意一个权限即可访问
     * </p>
     *
     * @return 权限名称数组
     */
    String[] value();

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
     * true: 必须拥有任一权限才能访问（默认）
     * false: 不拥有任何权限也可以访问，但会记录日志
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
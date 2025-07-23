package io.github.rose.security.rbac.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色检查注解
 * <p>
 * 用于方法级别的角色控制，支持单个角色检查。
 * 当用户拥有指定角色时，即可访问该方法。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @HasRole("ADMIN")
 * public void deleteUser(Long userId) {
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
public @interface HasRole {

    /**
     * 角色名称
     * <p>
     * 支持角色表达式，如：ADMIN、MANAGER、USER等
     * </p>
     *
     * @return 角色名称
     */
    String value();

    /**
     * 角色描述
     * <p>
     * 用于文档生成和权限管理界面显示
     * </p>
     *
     * @return 角色描述
     */
    String description() default "";

    /**
     * 是否必须拥有角色
     * <p>
     * true: 必须拥有角色才能访问（默认）
     * false: 不拥有角色也可以访问，但会记录日志
     * </p>
     *
     * @return 是否必须拥有角色
     */
    boolean required() default true;

    /**
     * 角色检查失败时的错误消息
     *
     * @return 错误消息
     */
    String errorMessage() default "角色权限不足";
}
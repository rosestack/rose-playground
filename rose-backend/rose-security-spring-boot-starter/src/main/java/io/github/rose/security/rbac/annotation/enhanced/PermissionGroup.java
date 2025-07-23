package io.github.rose.security.rbac.annotation.enhanced;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限组注解
 * <p>
 * 用于定义权限组，支持权限的分组管理和批量操作。
 * 可以在类或方法上使用，提供更灵活的权限控制。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @PermissionGroup(
 *     name = "user-management",
 *     permissions = {"user:create", "user:read", "user:update"},
 *     description = "用户管理权限组"
 * )
 * @RestController
 * public class UserController {
 *     // 控制器方法
 * }
 * }</pre>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionGroup {

    /**
     * 权限组名称
     *
     * @return 权限组名称
     */
    String name();

    /**
     * 权限组包含的权限列表
     *
     * @return 权限数组
     */
    String[] permissions();

    /**
     * 权限组描述
     *
     * @return 权限组描述
     */
    String description() default "";

    /**
     * 权限检查模式
     *
     * @return 检查模式
     */
    CheckMode mode() default CheckMode.ANY;

    /**
     * 是否必须拥有权限
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

    /**
     * 权限检查模式枚举
     */
    enum CheckMode {
        /**
         * 拥有任一权限即可
         */
        ANY,
        
        /**
         * 必须拥有所有权限
         */
        ALL,
        
        /**
         * 至少拥有指定数量的权限
         */
        AT_LEAST
    }

    /**
     * 当模式为AT_LEAST时，需要的最少权限数量
     *
     * @return 最少权限数量
     */
    int minRequired() default 1;
}
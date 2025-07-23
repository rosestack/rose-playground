package io.github.rose.security.rbac.annotation.enhanced;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 条件权限注解
 * <p>
 * 支持基于条件的权限检查，可以根据方法参数、用户属性、
 * 时间条件等动态判断权限。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @ConditionalPermission(
 *     permission = "user:update",
 *     condition = "@userService.isOwner(#userId, authentication.principal.id)",
 *     fallbackPermission = "user:update:all"
 * )
 * public User updateUser(Long userId, UserUpdateRequest request) {
 *     // 方法实现
 * }
 * }</pre>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalPermission {

    /**
     * 主要权限
     *
     * @return 权限名称
     */
    String permission();

    /**
     * 权限检查条件（SpEL表达式）
     * <p>
     * 支持的变量：
     * - authentication: 当前认证信息
     * - principal: 当前用户
     * - method parameters: 方法参数（通过#参数名访问）
     * - Spring beans: 通过@beanName访问
     * </p>
     *
     * @return SpEL条件表达式
     */
    String condition();

    /**
     * 条件不满足时的备用权限
     *
     * @return 备用权限名称
     */
    String fallbackPermission() default "";

    /**
     * 权限描述
     *
     * @return 权限描述
     */
    String description() default "";

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
     * 条件评估失败时的处理策略
     *
     * @return 处理策略
     */
    ConditionFailureStrategy onConditionFailure() default ConditionFailureStrategy.DENY;

    /**
     * 条件评估失败处理策略
     */
    enum ConditionFailureStrategy {
        /**
         * 拒绝访问
         */
        DENY,
        
        /**
         * 允许访问
         */
        ALLOW,
        
        /**
         * 使用备用权限
         */
        USE_FALLBACK
    }
}
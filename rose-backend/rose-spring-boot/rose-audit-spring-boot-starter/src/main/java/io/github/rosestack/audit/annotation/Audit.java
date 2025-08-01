package io.github.rosestack.audit.annotation;

import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.AuditRiskLevel;
import io.github.rosestack.mybatis.support.encryption.EncryptType;

import java.lang.annotation.*;

/**
 * 审计注解
 * <p>
 * 用于标记需要记录审计日志的方法。支持自动收集HTTP请求信息、用户信息、执行时间等。
 * 与现有的 @AuditLog 注解兼容，提供更丰富的审计功能。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    /**
     * 操作名称
     * <p>
     * 具体的业务操作描述，如："创建用户"、"删除订单"等。
     * 如果不指定，将使用方法名作为操作名称。
     * </p>
     *
     * @return 操作名称
     */
    String value() default "";

    /**
     * 事件类型
     * <p>
     * 技术分类，如：认证、数据、安全等。
     * 如果不指定，将根据方法特征自动推断。
     * </p>
     *
     * @return 事件类型
     */
    AuditEventType eventType() default AuditEventType.DATA_OTHER;

    /**
     * 风险等级
     * <p>
     * 操作的风险等级，如：LOW、MEDIUM、HIGH、CRITICAL。
     * 如果不指定，将根据事件类型自动推断。
     * </p>
     *
     * @return 风险等级
     */
    AuditRiskLevel riskLevel() default AuditRiskLevel.LOW;

    /**
     * 是否记录请求参数
     * <p>
     * 是否将方法参数记录到审计详情中。
     * 注意：敏感参数会自动脱敏处理。
     * </p>
     *
     * @return 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回值
     * <p>
     * 是否将方法返回值记录到审计详情中。
     * 注意：敏感返回值会自动脱敏处理。
     * </p>
     *
     * @return 是否记录返回值
     */
    boolean recordResult() default false;

    EncryptType encryptType() default EncryptType.AES;

    /**
     * 是否加密结果
     */
    boolean encryptResult() default false;

    String[] maskFields() default {};

    /**
     * 条件表达式
     * <p>
     * 使用SpEL表达式定义记录条件，只有满足条件时才记录审计日志。
     * 例如：#result != null 表示只有返回值不为空时才记录。
     * </p>
     *
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 是否记录异常信息
     * <p>
     * 当方法执行出现异常时，是否记录异常信息到审计日志中。
     * </p>
     *
     * @return 是否记录异常信息
     */
    boolean recordException() default true;

}
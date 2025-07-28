package io.github.rosestack.mybatis.annotation;

import io.github.rosestack.mybatis.desensitization.SensitiveType;

import java.lang.annotation.*;

/**
 * 敏感字段脱敏注解
 * <p>
 * 用于标记需要脱敏显示的敏感字段，如手机号、身份证号、邮箱等。
 * 在查询结果返回时自动进行脱敏处理。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveField {

    /**
     * 脱敏类型
     *
     * @return 脱敏类型
     */
    SensitiveType value() default SensitiveType.CUSTOM;

    /**
     * 自定义脱敏规则（当类型为 CUSTOM 时使用）
     * 格式：保留前N位,保留后N位
     * 例如：3,4 表示保留前3位和后4位
     *
     * @return 自定义脱敏规则
     */
    String customRule() default "3,4";
}

package io.github.rosestack.mybatis.annotation;

import io.github.rosestack.mybatis.support.encryption.EncryptType;

import java.lang.annotation.*;

/**
 * 敏感字段加密注解
 * <p>
 * 用于标记需要加密存储的敏感字段，如手机号、身份证号、银行卡号等。
 * 在插入和更新时自动加密，在查询时自动解密。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptField {

    /**
     * 加密算法类型
     *
     * @return 加密算法类型
     */
    EncryptType value() default EncryptType.AES;
}

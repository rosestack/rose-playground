package io.github.rosestack.spring.boot.common.encryption.annotation;

import io.github.rosestack.spring.boot.common.encryption.enums.EncryptType;
import io.github.rosestack.spring.boot.common.encryption.enums.HashType;

import java.lang.annotation.*;

/**
 * 敏感字段加密注解
 * <p>
 * 用于标记需要加密存储的敏感字段，如手机号、身份证号、银行卡号等。
 * 在插入和更新时自动加密，在查询时自动解密。
 * 支持可选的哈希查询功能，在保证安全性的同时提供查询能力。
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

    /**
     * 是否支持哈希查询
     * <p>
     * 如果启用，会自动生成对应的哈希字段用于查询。
     * 例如：phone 字段会生成 phone_hash 字段。
     * 哈希字段使用单向哈希算法，不可逆，只能用于精确匹配查询。
     * </p>
     *
     * @return 是否支持查询
     */
    boolean searchable() default false;

    /**
     * 哈希算法类型
     * <p>
     * 当 searchable = true 时生效，指定用于生成哈希字段的算法。
     * </p>
     *
     * @return 哈希算法类型
     */
    HashType hashType() default HashType.SHA256;

    /**
     * 哈希字段名
     * <p>
     * 自定义哈希字段的名称，如果不指定则自动生成：原字段名 + "_hash"。
     * 例如：phone 字段的哈希字段名为 phone_hash。
     * </p>
     *
     * @return 哈希字段名
     */
    String hashField() default "";
}

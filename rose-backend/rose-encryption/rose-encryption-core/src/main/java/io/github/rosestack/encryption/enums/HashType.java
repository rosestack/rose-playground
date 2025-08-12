package io.github.rosestack.encryption.enums;

/**
 * 哈希算法类型枚举
 *
 * <p>用于支持加密字段的哈希查询功能，提供多种安全的哈希算法选择。
 *
 * @author Rose Team
 * @since 1.0.0
 */
public enum HashType {

    /**
     * SHA-256 哈希算法
     *
     * <p>使用 SHA-256 算法配合盐值进行哈希计算，安全性较高。
     */
    SHA256("SHA-256"),

    /**
     * SHA-512 哈希算法
     *
     * <p>使用 SHA-512 算法配合盐值进行哈希计算，安全性更高但计算开销较大。
     */
    SHA512("SHA-512"),

    /**
     * HMAC-SHA256 哈希算法
     *
     * <p>使用 HMAC-SHA256 算法进行哈希计算，提供更强的安全保证。 推荐用于高安全要求的场景。
     */
    HMAC_SHA256("HmacSHA256"),

    /**
     * HMAC-SHA512 哈希算法
     *
     * <p>使用 HMAC-SHA512 算法进行哈希计算，提供最强的安全保证。
     */
    HMAC_SHA512("HmacSHA512");

    private final String algorithm;

    HashType(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * 获取算法名称
     *
     * @return 算法名称
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 是否为 HMAC 算法
     *
     * @return 如果是 HMAC 算法返回 true，否则返回 false
     */
    public boolean isHmac() {
        return this == HMAC_SHA256 || this == HMAC_SHA512;
    }
}

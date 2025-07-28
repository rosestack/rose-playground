package io.github.rosestack.mybatis.encryption;

/**
 * 加密算法类型枚举
 */
public enum EncryptType {
    /**
     * AES 对称加密算法
     */
    AES,

    /**
     * DES 对称加密算法（不推荐，安全性较低）
     */
    DES,

    /**
     * 3DES 对称加密算法
     */
    DES3,

    /**
     * SM4 国密算法
     */
    SM4
}
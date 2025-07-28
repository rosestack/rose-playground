package io.github.rosestack.mybatis.enums;

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
     * SM4 国密对称加密算法
     */
    SM4,

    /**
     * SM2 国密非对称加密算法
     */
    SM2,

    /**
     * RSA 非对称加密算法
     */
    RSA,

    /**
     * ECC 椭圆曲线加密算法
     */
    ECC
}
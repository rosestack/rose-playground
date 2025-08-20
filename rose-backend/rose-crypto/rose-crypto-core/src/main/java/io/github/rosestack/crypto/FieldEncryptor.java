package io.github.rosestack.crypto;

import io.github.rosestack.crypto.enums.EncryptType;

/**
 * 字段加密器接口
 *
 * <p>定义字段加密和解密的标准接口，支持多种加密算法。
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface FieldEncryptor {

    /**
     * 加密字段值
     *
     * @param plainText   明文
     * @param encryptType 加密类型
     * @return 密文
     */
    String encrypt(String plainText, EncryptType encryptType);

    /**
     * 解密字段值
     *
     * @param cipherText  密文
     * @param encryptType 加密类型
     * @return 明文
     */
    String decrypt(String cipherText, EncryptType encryptType);
}

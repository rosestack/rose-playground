package io.github.rosestack.spring.boot.common.encryption;

import io.github.rosestack.spring.boot.common.encryption.enums.EncryptType;

public class NoopFieldEncryptor implements FieldEncryptor {
    @Override
    public String encrypt(String plainText, EncryptType encryptType) {
        return plainText;
    }

    @Override
    public String decrypt(String cipherText, EncryptType encryptType) {
        return cipherText;
    }
}

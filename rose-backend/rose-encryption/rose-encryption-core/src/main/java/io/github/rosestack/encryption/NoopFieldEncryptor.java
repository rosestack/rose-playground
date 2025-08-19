package io.github.rosestack.encryption;

import io.github.rosestack.encryption.enums.EncryptType;

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

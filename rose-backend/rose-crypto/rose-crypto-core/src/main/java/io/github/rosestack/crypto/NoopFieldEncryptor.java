package io.github.rosestack.crypto;

import io.github.rosestack.crypto.enums.EncryptType;

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

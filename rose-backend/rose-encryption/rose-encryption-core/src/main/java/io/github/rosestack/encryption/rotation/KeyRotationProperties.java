package io.github.rosestack.encryption.rotation;

import lombok.Data;

/** 密钥轮换配置 */
@Data
public class KeyRotationProperties {
  /** 是否启用密钥轮换 */
  private boolean enabled = false;

  /** 自动轮换间隔（天） */
  private int autoRotationDays = 90;

  /** 密钥保留期（天）- 旧密钥保留多久用于解密 */
  private int keyRetentionDays = 30;

  /** 是否启用自动清理过期密钥 */
  private boolean autoCleanup = true;

  /** 默认密钥长度配置 */
  private KeyLength keyLength = new KeyLength();

  @Data
  public static class KeyLength {
    /** AES密钥长度（位） */
    private int aes = 256;

    /** RSA密钥长度（位） */
    private int rsa = 2048;
  }
}

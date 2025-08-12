package io.github.rosestack.encryption.rotation;

import io.github.rosestack.encryption.enums.EncryptType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密钥规格定义
 *
 * <p>定义不同算法的密钥要求和规格，支持密钥轮换
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeySpec {

  /** 密钥版本号 */
  private String version;

  /** 加密算法类型 */
  private EncryptType encryptType;

  /** 对称加密密钥（Base64编码） */
  private String secretKey;

  /** RSA公钥（用于RSA加密） */
  private String publicKey;

  /** RSA私钥（用于RSA解密） */
  private String privateKey;

  /** SM2公钥 */
  private String sm2PublicKey;

  /** SM2私钥 */
  private String sm2PrivateKey;

  /** 密钥创建时间 */
  private LocalDateTime createdTime;

  /** 密钥激活时间 */
  private LocalDateTime activeTime;

  /** 密钥过期时间 */
  private LocalDateTime expireTime;

  /** 是否为当前活跃版本 */
  private boolean active;

  /** 是否已废弃 */
  private boolean deprecated;

  /** 扩展属性 */
  private Map<String, String> properties;

  /** 密钥描述 */
  private String description;

  /** 检查是否可用于加密 */
  public boolean canEncrypt() {
    return active
        && !deprecated
        && (expireTime == null || LocalDateTime.now().isBefore(expireTime));
  }

  /** 检查是否可用于解密 */
  public boolean canDecrypt() {
    return !deprecated
        && (expireTime == null || LocalDateTime.now().isBefore(expireTime.plusDays(30)));
  }

  /** 获取对称加密密钥字节数组 */
  public byte[] getSecretKeyBytes() {
    if (secretKey == null) {
      return null;
    }
    return java.util.Base64.getDecoder().decode(secretKey);
  }

  /** 验证密钥规格是否符合算法要求 */
  public boolean isValidForAlgorithm() {
    if (encryptType == null) {
      return false;
    }

    switch (encryptType) {
      case AES:
        return validateAESKey();
      case DES:
        return validateDESKey();
      case DES3:
        return validate3DESKey();
      case SM4:
        return validateSM4Key();
      case SM2:
        return validateSM2Key();
      case RSA:
        return validateRSAKey();
      default:
        return false;
    }
  }

  private boolean validateAESKey() {
    if (secretKey == null) return false;
    byte[] keyBytes = getSecretKeyBytes();
    int length = keyBytes.length;
    return length == 16 || length == 24 || length == 32; // 128, 192, 256 bits
  }

  private boolean validateDESKey() {
    if (secretKey == null) return false;
    return getSecretKeyBytes().length == 8; // 64 bits
  }

  private boolean validate3DESKey() {
    if (secretKey == null) return false;
    return getSecretKeyBytes().length == 24; // 192 bits
  }

  private boolean validateSM4Key() {
    if (secretKey == null) return false;
    return getSecretKeyBytes().length == 16; // 128 bits
  }

  private boolean validateSM2Key() {
    return sm2PublicKey != null && sm2PrivateKey != null;
  }

  private boolean validateRSAKey() {
    return publicKey != null && privateKey != null;
  }
}

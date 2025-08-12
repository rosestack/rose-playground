package io.github.rosestack.encryption;

import io.github.rosestack.encryption.enums.EncryptType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 优化的字段加密器实现
 *
 * <p>基于通用加密工具类 EncryptionUtils 实现，提供以下特性： 1. 统一的加密解密逻辑 2. 高性能缓存机制 3. 线程安全 4. 完善的错误处理和监控
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultFieldEncryptor implements FieldEncryptor {
  private final String secretKey;
  private final Boolean failOnError;

  @Override
  public String encrypt(String plainText, EncryptType encryptType) {
    try {
      String secretKey = getEncryptionKey();
      return EncryptionUtils.encrypt(plainText, encryptType, secretKey);
    } catch (Exception e) {
      if (!failOnError) {
        return plainText;
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public String decrypt(String cipherText, EncryptType encryptType) {
    try {
      String secretKey = getEncryptionKey();
      return EncryptionUtils.decrypt(cipherText, encryptType, secretKey);
    } catch (Exception e) {
      if (!failOnError) {
        return cipherText;
      }
      throw new RuntimeException(e);
    }
  }

  /** 获取加密密钥 */
  private String getEncryptionKey() {
    if (StringUtils.isBlank(secretKey)) {
      throw new IllegalStateException("未配置加密密钥");
    }
    return secretKey;
  }

  /** 获取性能统计 */
  public static Map<String, Long> getPerformanceStats() {
    return EncryptionUtils.getPerformanceStats();
  }

  /** 获取缓存统计信息 */
  public static String getCacheStats() {
    return EncryptionUtils.getCacheStats();
  }

  /** 清空缓存 */
  public static void clearCache() {
    EncryptionUtils.clearCache();
    log.info("已清空字段加密器缓存");
  }
}

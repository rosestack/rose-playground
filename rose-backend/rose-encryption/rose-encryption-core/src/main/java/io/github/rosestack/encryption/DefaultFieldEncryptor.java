package io.github.rosestack.encryption;

import io.github.rosestack.encryption.enums.EncryptType;
import io.github.rosestack.encryption.exception.EncryptionException;

import java.util.Map;

import io.github.rosestack.encryption.monitor.EncryptionMonitorManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 安全的字段加密器实现
 *
 * <p>基于通用加密工具类 EncryptionUtils 实现，提供以下特性：
 * <ul>
 *   <li>统一的加密解密逻辑</li>
 *   <li>安全的错误处理机制</li>
 *   <li>完善的日志记录</li>
 *   <li>性能监控统计</li>
 * </ul>
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
		if (StringUtils.isBlank(plainText)) {
			return plainText;
		}

		try {
			String result = EncryptionUtils.encrypt(plainText, encryptType, secretKey);
			log.debug("字段加密成功: encryptType={}, length={}", encryptType, plainText.length());
			return result;
		} catch (EncryptionException e) {
			log.error("字段加密失败: encryptType={}, error={}", encryptType, e.getMessage());
			if (!failOnError) {
				log.warn("加密失败但配置为不抛出异常，返回原始值");
				return plainText;
			}
			throw e;
		} catch (Exception e) {
			log.error("字段加密发生未知错误: encryptType={}", encryptType, e);
			if (!failOnError) {
				log.warn("加密失败但配置为不抛出异常，返回原始值");
				return plainText;
			}
			throw new EncryptionException("字段加密失败", e);
		}
	}

	@Override
	public String decrypt(String cipherText, EncryptType encryptType) {
		if (StringUtils.isBlank(cipherText)) {
			return cipherText;
		}

		try {
			String result = EncryptionUtils.decrypt(cipherText, encryptType, secretKey);
			log.debug("字段解密成功: encryptType={}, length={}", encryptType, cipherText.length());
			return result;
		} catch (EncryptionException e) {
			log.error("字段解密失败: encryptType={}, error={}", encryptType, e.getMessage());
			if (!failOnError) {
				log.warn("解密失败但配置为不抛出异常，返回原始值");
				return cipherText;
			}
			throw e;
		} catch (Exception e) {
			log.error("字段解密发生未知错误: encryptType={}", encryptType, e);
			if (!failOnError) {
				log.warn("解密失败但配置为不抛出异常，返回原始值");
				return cipherText;
			}
			throw new EncryptionException("字段解密失败", e);
		}
	}
}

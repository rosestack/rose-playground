package io.github.rosestack.crypto;

import com.antherd.smcrypto.sm2.Sm2;
import com.antherd.smcrypto.sm4.Sm4;
import io.github.rosestack.crypto.enums.EncryptType;
import io.github.rosestack.crypto.exception.CryptoException;
import io.github.rosestack.crypto.monitor.CryptoMonitorManager;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 安全的通用加密工具类
 *
 * <p>提供统一的加密解密功能，支持多种加密算法。
 * <p>安全特性：
 * <ul>
 *   <li>使用安全的加密模式（GCM/CBC，避免ECB）</li>
 *   <li>每次加密生成随机IV</li>
 *   <li>完善的异常处理和日志记录</li>
 *   <li>性能监控和统计</li>
 * </ul>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public final class CryptoUtils {

	/**
	 * 安全的加密算法配置
	 */
	private static final Map<EncryptType, String> TRANSFORMATIONS = Map.of(
		EncryptType.AES, "AES/GCM/NoPadding",
		EncryptType.DES, "DES/CBC/PKCS5Padding",
		EncryptType.DES3, "DESede/CBC/PKCS5Padding"
	);

	/**
	 * IV 长度配置
	 */
	private static final Map<EncryptType, Integer> IV_LENGTHS = Map.of(
		EncryptType.AES, 12,  // GCM 推荐 12 字节
		EncryptType.DES, 8,   // DES 块大小
		EncryptType.DES3, 8   // 3DES 块大小
	);

	/**
	 * GCM 标签长度
	 */
	private static final int GCM_TAG_LENGTH = 128;

	/**
	 * 安全随机数生成器
	 */
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/**
	 * 监控管理器
	 */
	private static final CryptoMonitorManager MONITORING_MANAGER = CryptoMonitorManager.getInstance();

	private CryptoUtils() {
	}

	/**
	 * 安全加密数据
	 *
	 * @param plainText   明文
	 * @param encryptType 加密类型
	 * @param secretKey   密钥
	 * @return 加密后的密文（Base64编码，包含IV）
	 * @throws CryptoException 加密失败时抛出
	 */
	public static String encrypt(String plainText, EncryptType encryptType, String secretKey) {
		return executeWithMonitoring("encrypt", plainText, encryptType, secretKey,
			() -> doEncrypt(plainText, encryptType, secretKey));
	}

	/**
	 * 安全解密数据
	 *
	 * @param cipherText  密文（Base64编码，包含IV）
	 * @param encryptType 加密类型
	 * @param secretKey   密钥
	 * @return 解密后的明文
	 * @throws CryptoException 解密失败时抛出
	 */
	public static String decrypt(String cipherText, EncryptType encryptType, String secretKey) {
		return executeWithMonitoring("decrypt", cipherText, encryptType, secretKey,
			() -> doDecrypt(cipherText, encryptType, secretKey));
	}

	/**
	 * 通用的加密解密执行方法，包含监控和错误处理
	 */
	private static String executeWithMonitoring(String operation, String inputData,
												EncryptType encryptType, String secretKey, CryptoOperation cryptoOperation) {
		// 输入验证
		if (StringUtils.isBlank(inputData)) {
			return inputData;
		}

		validateSecretKey(secretKey, operation);

		// 执行操作并监控
		long startTime = System.nanoTime();
		try {
			String result = cryptoOperation.execute();
			MONITORING_MANAGER.recordPerformance(operation, encryptType.name(), startTime, true, inputData.length());
			return result;
		} catch (Exception e) {
			MONITORING_MANAGER.recordPerformance(operation, encryptType.name(), startTime, false, inputData.length());
			MONITORING_MANAGER.recordError(operation, encryptType.name(), e.getClass().getSimpleName());
			log.error("数据{}失败: encryptType={}, error={}", operation, encryptType, e.getMessage());
			throw new CryptoException("数据" + operation + "失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 验证密钥
	 */
	private static void validateSecretKey(String secretKey, String operation) {
		if (StringUtils.isBlank(secretKey)) {
			throw new CryptoException(operation + "密钥不能为空");
		}

		if (secretKey.length() < 16) {
			throw new CryptoException(operation + "密钥长度不足，至少需要16个字符");
		}
	}

	/**
	 * 加密解密操作的函数式接口
	 */
	@FunctionalInterface
	private interface CryptoOperation {
		String execute() throws Exception;
	}

	/**
	 * 批量加密
	 *
	 * @param plainTexts  明文数组
	 * @param encryptType 加密类型
	 * @param secretKey   密钥
	 * @return 加密后的密文数组
	 */
	public static String[] encryptBatch(String[] plainTexts, EncryptType encryptType, String secretKey) {
		if (plainTexts == null) {
			return null;
		}

		String[] results = new String[plainTexts.length];
		for (int i = 0; i < plainTexts.length; i++) {
			results[i] = encrypt(plainTexts[i], encryptType, secretKey);
		}
		return results;
	}

	/**
	 * 批量解密
	 *
	 * @param cipherTexts 密文数组
	 * @param encryptType 加密类型
	 * @param secretKey   密钥
	 * @return 解密后的明文数组
	 */
	public static String[] decryptBatch(String[] cipherTexts, EncryptType encryptType, String secretKey) {
		if (cipherTexts == null) {
			return null;
		}

		String[] results = new String[cipherTexts.length];
		for (int i = 0; i < cipherTexts.length; i++) {
			results[i] = decrypt(cipherTexts[i], encryptType, secretKey);
		}
		return results;
	}

	/**
	 * 安全加密实现
	 */
	private static String doEncrypt(String plainText, EncryptType encryptType, String secretKey) throws Exception {
		switch (encryptType) {
			case AES:
				return encryptAES(plainText, secretKey);
			case SM4:
				return Sm4.encrypt(plainText, secretKey);
			case SM2:
				return Sm2.doEncrypt(plainText, secretKey);
			case DES:
			case DES3:
			case RSA:
				return encryptWithCipher(plainText, encryptType, secretKey);
			default:
				throw new CryptoException("不支持的加密类型: " + encryptType);
		}
	}

	/**
	 * 安全解密实现
	 */
	private static String doDecrypt(String cipherText, EncryptType encryptType, String secretKey) throws Exception {
		switch (encryptType) {
			case AES:
				return decryptAES(cipherText, secretKey);
			case SM4:
				return Sm4.decrypt(cipherText, secretKey);
			case SM2:
				return Sm2.doDecrypt(cipherText, secretKey);
			case DES:
			case DES3:
			case RSA:
				return decryptWithCipher(cipherText, encryptType, secretKey);
			default:
				throw new CryptoException("不支持的解密类型: " + encryptType);
		}
	}

	/**
	 * AES-GCM 加密
	 */
	private static String encryptAES(String plainText, String secretKey) throws Exception {
		byte[] keyBytes = adjustKeyLength(secretKey.getBytes(StandardCharsets.UTF_8), getKeyLength(EncryptType.AES));
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

		// 使用配置的 IV 长度生成随机 IV
		int ivLength = IV_LENGTHS.get(EncryptType.AES);
		byte[] iv = new byte[ivLength];
		SECURE_RANDOM.nextBytes(iv);

		String transformation = TRANSFORMATIONS.get(EncryptType.AES);
		Cipher cipher = Cipher.getInstance(transformation);
		GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

		byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

		// 将 IV 和密文组合
		byte[] result = new byte[iv.length + cipherBytes.length];
		System.arraycopy(iv, 0, result, 0, iv.length);
		System.arraycopy(cipherBytes, 0, result, iv.length, cipherBytes.length);

		return Base64.getEncoder().encodeToString(result);
	}

	/**
	 * AES-GCM 解密
	 */
	private static String decryptAES(String cipherText, String secretKey) throws Exception {
		byte[] data = Base64.getDecoder().decode(cipherText);

		int ivLength = IV_LENGTHS.get(EncryptType.AES);
		if (data.length < ivLength) {
			throw new CryptoException("密文格式错误：长度不足");
		}

		// 提取 IV 和密文
		byte[] iv = new byte[ivLength];
		byte[] cipherBytes = new byte[data.length - ivLength];
		System.arraycopy(data, 0, iv, 0, ivLength);
		System.arraycopy(data, ivLength, cipherBytes, 0, cipherBytes.length);

		byte[] keyBytes = adjustKeyLength(secretKey.getBytes(StandardCharsets.UTF_8), getKeyLength(EncryptType.AES));
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

		String transformation = TRANSFORMATIONS.get(EncryptType.AES);
		Cipher cipher = Cipher.getInstance(transformation);
		GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

		byte[] plainBytes = cipher.doFinal(cipherBytes);
		return new String(plainBytes, StandardCharsets.UTF_8);
	}

	/**
	 * 获取加密算法的变换字符串
	 */
	private static String getTransformation(EncryptType encryptType) {
		// 优先使用配置的变换字符串
		String transformation = TRANSFORMATIONS.get(encryptType);
		if (transformation != null) {
			return transformation;
		}

		// 兼容其他算法
		switch (encryptType) {
			case RSA:
				return "RSA/ECB/PKCS1Padding";
			default:
				throw new CryptoException("不支持的加密类型: " + encryptType);
		}
	}

	/**
	 * 获取算法名称
	 */
	private static String getAlgorithm(EncryptType encryptType) {
		switch (encryptType) {
			case DES:
				return "DES";
			case DES3:
				return "DESede";
			case RSA:
				return "RSA";
			default:
				return encryptType.name();
		}
	}

	/**
	 * 获取密钥长度
	 */
	private static int getKeyLength(EncryptType encryptType) {
		switch (encryptType) {
			case DES:
				return 8;
			case DES3:
				return 24;
			case AES:
				return 32; // AES-256
			default:
				return 16; // 默认
		}
	}

	/**
	 * 调整密钥长度
	 */
	private static byte[] adjustKeyLength(byte[] key, int targetLength) {
		if (key.length == targetLength) {
			return key;
		}

		byte[] adjustedKey = new byte[targetLength];
		if (key.length > targetLength) {
			System.arraycopy(key, 0, adjustedKey, 0, targetLength);
		} else {
			System.arraycopy(key, 0, adjustedKey, 0, key.length);
			// 用零填充剩余部分
			Arrays.fill(adjustedKey, key.length, targetLength, (byte) 0);
		}
		return adjustedKey;
	}

	/**
	 * 使用 Cipher 加密（DES/3DES/RSA）
	 */
	private static String encryptWithCipher(String plainText, EncryptType encryptType, String secretKey) throws Exception {
		return processCipher(plainText, encryptType, secretKey, true);
	}

	/**
	 * 使用 Cipher 解密（DES/3DES/RSA）
	 */
	private static String decryptWithCipher(String cipherText, EncryptType encryptType, String secretKey) throws Exception {
		return processCipher(cipherText, encryptType, secretKey, false);
	}

	/**
	 * 通用的 Cipher 处理方法
	 */
	private static String processCipher(String inputData, EncryptType encryptType, String secretKey, boolean isEncrypt) throws Exception {
		String transformation = getTransformation(encryptType);
		SecretKeySpec keySpec = new SecretKeySpec(
			adjustKeyLength(secretKey.getBytes(StandardCharsets.UTF_8), getKeyLength(encryptType)),
			getAlgorithm(encryptType)
		);

		Cipher cipher = Cipher.getInstance(transformation);

		if (transformation.contains("CBC")) {
			return isEncrypt ?
				encryptWithCBC(cipher, keySpec, inputData) :
				decryptWithCBC(cipher, keySpec, inputData);
		} else {
			return isEncrypt ?
				encryptWithoutIV(cipher, keySpec, inputData) :
				decryptWithoutIV(cipher, keySpec, inputData);
		}
	}

	/**
	 * CBC 模式加密
	 */
	private static String encryptWithCBC(Cipher cipher, SecretKeySpec keySpec, String plainText) throws Exception {
		// 生成随机 IV
		byte[] iv = new byte[cipher.getBlockSize()];
		SECURE_RANDOM.nextBytes(iv);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

		// 将 IV 和密文组合
		byte[] result = new byte[iv.length + encrypted.length];
		System.arraycopy(iv, 0, result, 0, iv.length);
		System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

		return Base64.getEncoder().encodeToString(result);
	}

	/**
	 * CBC 模式解密
	 */
	private static String decryptWithCBC(Cipher cipher, SecretKeySpec keySpec, String cipherText) throws Exception {
		byte[] data = Base64.getDecoder().decode(cipherText);
		int blockSize = cipher.getBlockSize();

		if (data.length < blockSize) {
			throw new CryptoException("密文格式错误：长度不足");
		}

		// 提取 IV 和密文
		byte[] iv = new byte[blockSize];
		byte[] encrypted = new byte[data.length - blockSize];
		System.arraycopy(data, 0, iv, 0, blockSize);
		System.arraycopy(data, blockSize, encrypted, 0, encrypted.length);

		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	/**
	 * 无 IV 模式加密
	 */
	private static String encryptWithoutIV(Cipher cipher, SecretKeySpec keySpec, String plainText) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encrypted);
	}

	/**
	 * 无 IV 模式解密
	 */
	private static String decryptWithoutIV(Cipher cipher, SecretKeySpec keySpec, String cipherText) throws Exception {
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		byte[] data = Base64.getDecoder().decode(cipherText);
		byte[] decrypted = cipher.doFinal(data);
		return new String(decrypted, StandardCharsets.UTF_8);
	}
}

package io.github.rosestack.spring.boot.security.mfa.totp;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * TOTP (Time-based One-Time Password) 生成器
 * <p>
 * 基于RFC 6238标准实现的时间基于一次性密码算法。
 * 使用HMAC-SHA1算法和30秒时间窗口生成6位数字密码。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
public class TotpGenerator {

    /** HMAC算法 */
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    /** 默认密码位数 */
    private static final int DEFAULT_DIGITS = 6;

    /** 默认时间步长（秒） */
    private static final int DEFAULT_TIME_STEP = 30;

    /** 默认密钥长度（字节） */
    private static final int DEFAULT_KEY_LENGTH = 20;

    /** 时间步长（秒） */
    private final int timeStep;

    /** 密码位数 */
    private final int digits;

    /** 时间窗容忍度 */
    private final int windowSize;

    /**
     * 使用默认参数构造TOTP生成器
     */
    public TotpGenerator() {
        this(DEFAULT_TIME_STEP, DEFAULT_DIGITS, 1);
    }

    /**
     * 构造TOTP生成器
     *
     * @param timeStep 时间步长（秒）
     * @param digits 密码位数
     * @param windowSize 时间窗容忍度
     */
    public TotpGenerator(int timeStep, int digits, int windowSize) {
        this.timeStep = timeStep;
        this.digits = digits;
        this.windowSize = windowSize;
    }

    /**
     * 生成随机密钥
     *
     * @return Base32编码的密钥
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[DEFAULT_KEY_LENGTH];
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * 使用当前时间生成TOTP密码
     *
     * @param secret Base32编码的密钥
     * @return TOTP密码
     */
    public String generateCode(String secret) {
        return generateCode(secret, Instant.now().getEpochSecond());
    }

    /**
     * 使用指定时间生成TOTP密码
     *
     * @param secret Base32编码的密钥
     * @param timestamp 时间戳（秒）
     * @return TOTP密码
     */
    public String generateCode(String secret, long timestamp) {
        long timeCounter = timestamp / timeStep;
        return generateCodeForCounter(secret, timeCounter);
    }

    /**
     * 验证TOTP密码
     *
     * @param secret Base32编码的密钥
     * @param code 待验证的密码
     * @return 验证是否成功
     */
    public boolean verifyCode(String secret, String code) {
        return verifyCode(secret, code, Instant.now().getEpochSecond());
    }

    /**
     * 验证TOTP密码（指定时间）
     *
     * @param secret Base32编码的密钥
     * @param code 待验证的密码
     * @param timestamp 时间戳（秒）
     * @return 验证是否成功
     */
    public boolean verifyCode(String secret, String code, long timestamp) {
        if (secret == null || code == null || code.trim().isEmpty()) {
            return false;
        }

        long timeCounter = timestamp / timeStep;

        // 检查当前时间窗口及前后窗口
        for (int i = -windowSize; i <= windowSize; i++) {
            String expectedCode = generateCodeForCounter(secret, timeCounter + i);
            if (code.equals(expectedCode)) {
                log.debug("TOTP验证成功，时间偏移: {} 窗口", i);
                return true;
            }
        }

        log.debug("TOTP验证失败，密码: {}", code);
        return false;
    }

    /**
     * 生成QR码数据
     *
     * @param secret Base32编码的密钥
     * @param accountName 账户名称
     * @param issuer 发行者
     * @return QR码数据URL
     */
    public String generateQrCodeData(String secret, String accountName, String issuer) {
        return String.format(
                "otpauth://totp/%s?secret=%s&issuer=%s&digits=%d&period=%d",
                accountName, secret, issuer, digits, timeStep);
    }

    /**
     * 根据计数器生成TOTP密码
     *
     * @param secret Base32编码的密钥
     * @param counter 时间计数器
     * @return TOTP密码
     */
    private String generateCodeForCounter(String secret, long counter) {
        try {
            // 解码密钥
            byte[] keyBytes = Base64.getDecoder().decode(secret);

            // 转换计数器为字节数组
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(counter);
            byte[] counterBytes = buffer.array();

            // 计算HMAC
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(counterBytes);

            // 动态截取
            int offset = hash[hash.length - 1] & 0x0F;
            int code = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            // 取模得到指定位数的密码
            code = code % (int) Math.pow(10, digits);

            // 格式化为指定位数的字符串（左侧补零）
            return String.format("%0" + digits + "d", code);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("TOTP密码生成失败", e);
        }
    }

    /**
     * 获取当前时间窗口的剩余时间（秒）
     *
     * @return 剩余时间
     */
    public int getRemainingTime() {
        return timeStep - (int) (Instant.now().getEpochSecond() % timeStep);
    }

    /**
     * 获取时间步长
     *
     * @return 时间步长（秒）
     */
    public int getTimeStep() {
        return timeStep;
    }

    /**
     * 获取密码位数
     *
     * @return 密码位数
     */
    public int getDigits() {
        return digits;
    }

    /**
     * 获取时间窗容忍度
     *
     * @return 时间窗容忍度
     */
    public int getWindowSize() {
        return windowSize;
    }
}

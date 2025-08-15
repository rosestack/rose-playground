package io.github.rosestack.spring.boot.security.mfa.totp;

import io.github.rosestack.spring.boot.security.mfa.MfaChallenge;
import io.github.rosestack.spring.boot.security.mfa.MfaContext;
import io.github.rosestack.spring.boot.security.mfa.MfaProvider;
import io.github.rosestack.spring.boot.security.mfa.MfaResult;
import io.github.rosestack.spring.boot.security.mfa.exception.MfaException;
import io.github.rosestack.spring.boot.security.mfa.exception.MfaVerificationFailedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * TOTP认证提供商实现
 * <p>
 * 基于时间的一次性密码认证提供商，实现了完整的TOTP认证流程，
 * 包括密钥生成、二维码生成、验证码验证等功能。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TotpProvider implements MfaProvider {

    /**
     * 提供商类型
     */
    public static final String PROVIDER_TYPE = "totp";

    /**
     * TOTP生成器
     */
    private final TotpGenerator totpGenerator;

    /**
     * TOTP密钥存储（生产环境应使用数据库或Redis）
     */
    private final Map<String, TotpSecret> secretStore = new ConcurrentHashMap<>();

    /**
     * 挑战信息存储（生产环境应使用数据库或Redis）
     */
    private final Map<String, MfaChallenge> challengeStore = new ConcurrentHashMap<>();

    /**
     * 默认发行者名称
     */
    private final String defaultIssuer;

    /**
     * 最大失败次数
     */
    private final int maxFailureAttempts;

    /**
     * 锁定时间（分钟）
     */
    private final int lockoutMinutes;

    @Override
    public String getType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean isSetup(String userId) {
        TotpSecret secret = secretStore.get(userId);
        return secret != null && secret.isVerified() && secret.isUsable();
    }

    @Override
    public MfaChallenge initSetup(String userId, MfaContext context) {
        log.info("初始化用户 {} 的TOTP设置", userId);

        try {
            // 生成新的密钥
            String secret = totpGenerator.generateSecret();
            String accountName = context.getUsername() != null ? context.getUsername() : userId;
            String issuer = defaultIssuer;

            // 生成QR码数据
            String qrCodeUrl = totpGenerator.generateQrCodeData(secret, accountName, issuer);

            // 创建密钥信息
            TotpSecret totpSecret = TotpSecret.create(userId, secret, accountName, issuer);
            totpSecret.setQrCodeUrl(qrCodeUrl);

            // 暂存密钥（等待验证）
            secretStore.put(userId, totpSecret);

            // 创建挑战信息
            String challengeId = UUID.randomUUID().toString();
            MfaChallenge challenge = MfaChallenge.builder()
                    .challengeId(challengeId)
                    .providerType(PROVIDER_TYPE)
                    .userId(userId)
                    .challengeType("setup")
                    .challengeData(secret)
                    .displayText("请使用认证器应用扫描二维码或手动输入密钥")
                    .expiresAt(LocalDateTime.now().plusMinutes(10)) // 10分钟过期
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            challenge.setProperty("qrCodeUrl", qrCodeUrl);
            challenge.setProperty("accountName", accountName);
            challenge.setProperty("issuer", issuer);

            // 存储挑战信息
            challengeStore.put(challengeId, challenge);

            log.info("用户 {} 的TOTP设置初始化完成，挑战ID: {}", userId, challengeId);
            return challenge;

        } catch (Exception e) {
            log.error("初始化用户 {} 的TOTP设置失败", userId, e);
            throw new MfaException("TOTP_SETUP_INIT_FAILED", "TOTP设置初始化失败", userId, e);
        }
    }

    @Override
    public MfaResult completeSetup(String userId, MfaChallenge challenge, String verificationCode, MfaContext context) {
        log.info("完成用户 {} 的TOTP设置", userId);

        try {
            // 验证挑战信息
            if (challenge == null || challenge.isExpired() || challenge.isUsed()) {
                return MfaResult.failure(userId, PROVIDER_TYPE, "setup", "INVALID_CHALLENGE", "设置挑战已过期或无效");
            }

            // 验证用户ID
            if (!userId.equals(challenge.getUserId())) {
                return MfaResult.failure(userId, PROVIDER_TYPE, "setup", "USER_MISMATCH", "用户身份不匹配");
            }

            // 获取密钥
            TotpSecret secret = secretStore.get(userId);
            if (secret == null || !secret.isUsable()) {
                return MfaResult.failure(userId, PROVIDER_TYPE, "setup", "SECRET_NOT_FOUND", "TOTP密钥不存在或无效");
            }

            // 验证验证码
            if (!totpGenerator.verifyCode(secret.getSecret(), verificationCode)) {
                secret.incrementFailureCount();
                return MfaResult.failure(userId, PROVIDER_TYPE, "setup", "INVALID_CODE", "验证码错误，请检查您的认证器应用");
            }

            // 标记为已验证
            secret.setVerified(true);
            secret.updateLastVerified(System.currentTimeMillis() / (totpGenerator.getTimeStep() * 1000));

            // 标记挑战为已使用
            challenge.setUsed(true);

            // 生成备用恢复码（可选）
            List<String> backupCodes = generateBackupCodes();

            MfaResult result = MfaResult.success(userId, PROVIDER_TYPE, "setup");
            result.setSuccessMessage("TOTP多因子认证设置成功");
            result.setData("backupCodes", backupCodes);

            log.info("用户 {} 的TOTP设置完成", userId);
            return result;

        } catch (Exception e) {
            log.error("完成用户 {} 的TOTP设置失败", userId, e);
            return MfaResult.failure(
                    userId, PROVIDER_TYPE, "setup", "SETUP_COMPLETION_FAILED", "TOTP设置完成失败: " + e.getMessage());
        }
    }

    @Override
    public MfaResult verify(String userId, String verificationCode, MfaContext context) {
        log.debug("验证用户 {} 的TOTP码", userId);

        try {
            // 获取密钥
            TotpSecret secret = secretStore.get(userId);
            if (secret == null || !secret.isVerified() || !secret.isUsable()) {
                throw MfaVerificationFailedException.mfaNotSetup(userId);
            }

            // 检查是否被锁定
            if (isLockedOut(secret)) {
                long lockoutRemaining = getLockoutRemainingMinutes(secret);
                return MfaResult.failure(
                        userId,
                        PROVIDER_TYPE,
                        "verify",
                        "ACCOUNT_LOCKED",
                        String.format("账号已被锁定，请 %d 分钟后重试", lockoutRemaining));
            }

            // 验证输入
            if (!StringUtils.hasText(verificationCode)) {
                return MfaResult.failure(userId, PROVIDER_TYPE, "verify", "EMPTY_CODE", "验证码不能为空");
            }

            // 计算当前时间窗口
            long currentTimeWindow = System.currentTimeMillis() / (totpGenerator.getTimeStep() * 1000);

            // 防重放检查
            if (secret.isWindowUsed(currentTimeWindow)) {
                return MfaResult.failure(userId, PROVIDER_TYPE, "verify", "CODE_ALREADY_USED", "此验证码已被使用，请等待下一个验证码");
            }

            // 验证TOTP码
            if (!totpGenerator.verifyCode(secret.getSecret(), verificationCode)) {
                secret.incrementFailureCount();

                int remaining = maxFailureAttempts - secret.getFailureCount();
                if (remaining <= 0) {
                    return MfaResult.failure(userId, PROVIDER_TYPE, "verify", "TOO_MANY_ATTEMPTS", "验证失败次数过多，账号已被锁定");
                }

                return MfaResult.failure(
                                userId,
                                PROVIDER_TYPE,
                                "verify",
                                "INVALID_CODE",
                                String.format("验证码错误，剩余重试次数: %d", remaining))
                        .builder()
                        .remainingAttempts(remaining)
                        .build();
            }

            // 验证成功
            secret.updateLastVerified(currentTimeWindow);

            MfaResult result = MfaResult.success(userId, PROVIDER_TYPE, "verify");
            result.setSuccessMessage("TOTP验证成功");
            result.setVerificationToken(generateVerificationToken(userId));

            log.info("用户 {} 的TOTP验证成功", userId);
            return result;

        } catch (MfaVerificationFailedException e) {
            log.warn("用户 {} 的TOTP验证失败: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户 {} 的TOTP验证异常", userId, e);
            return MfaResult.failure(
                    userId, PROVIDER_TYPE, "verify", "VERIFICATION_ERROR", "验证过程发生异常: " + e.getMessage());
        }
    }

    @Override
    public MfaResult removeSetup(String userId, MfaContext context) {
        log.info("移除用户 {} 的TOTP设置", userId);

        try {
            TotpSecret secret = secretStore.remove(userId);
            if (secret == null) {
                return MfaResult.failure(userId, PROVIDER_TYPE, "remove", "NOT_SETUP", "用户尚未设置TOTP");
            }

            // 清理相关挑战信息
            challengeStore
                    .entrySet()
                    .removeIf(entry -> userId.equals(entry.getValue().getUserId()));

            MfaResult result = MfaResult.success(userId, PROVIDER_TYPE, "remove");
            result.setSuccessMessage("TOTP设置已移除");

            log.info("用户 {} 的TOTP设置已移除", userId);
            return result;

        } catch (Exception e) {
            log.error("移除用户 {} 的TOTP设置失败", userId, e);
            return MfaResult.failure(userId, PROVIDER_TYPE, "remove", "REMOVE_FAILED", "移除TOTP设置失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否被锁定
     */
    private boolean isLockedOut(TotpSecret secret) {
        if (secret.getFailureCount() < maxFailureAttempts) {
            return false;
        }

        if (secret.getLastFailureAt() == null) {
            return false;
        }

        return secret.getLastFailureAt().plusMinutes(lockoutMinutes).isAfter(LocalDateTime.now());
    }

    /**
     * 获取锁定剩余时间（分钟）
     */
    private long getLockoutRemainingMinutes(TotpSecret secret) {
        if (!isLockedOut(secret)) {
            return 0;
        }

        LocalDateTime unlockTime = secret.getLastFailureAt().plusMinutes(lockoutMinutes);
        return java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
    }

    /**
     * 生成备用恢复码
     */
    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codes.add(UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase());
        }
        return codes;
    }

    /**
     * 生成验证令牌
     */
    private String generateVerificationToken(String userId) {
        return "mfa_" + userId + "_" + System.currentTimeMillis();
    }
}

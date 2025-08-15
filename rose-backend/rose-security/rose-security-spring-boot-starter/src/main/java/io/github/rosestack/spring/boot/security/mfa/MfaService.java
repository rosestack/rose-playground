package io.github.rosestack.spring.boot.security.mfa;

import io.github.rosestack.spring.boot.security.mfa.exception.MfaException;
import io.github.rosestack.spring.boot.security.mfa.exception.MfaRequiredException;
import io.github.rosestack.spring.boot.security.mfa.exception.MfaVerificationFailedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MFA服务
 * <p>
 * 提供统一的MFA管理接口，封装了多种MFA提供商的操作。
 * 支持MFA设置、验证、管理等完整流程。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    /** MFA提供商注册表 */
    private final MfaRegistry mfaRegistry;

    /**
     * 检查用户是否设置了MFA
     *
     * @param userId 用户ID
     * @return 如果设置了MFA返回true
     */
    public boolean isUserMfaSetup(String userId) {
        return mfaRegistry.hasUserSetupAnyMfa(userId);
    }

    /**
     * 检查用户是否设置了指定类型的MFA
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @return 如果设置了指定类型的MFA返回true
     */
    public boolean isUserMfaSetup(String userId, String providerType) {
        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.isSetup(userId);
        } catch (Exception e) {
            log.warn("检查用户 {} 的 {} MFA设置状态失败", userId, providerType, e);
            return false;
        }
    }

    /**
     * 获取用户已设置的MFA提供商
     *
     * @param userId 用户ID
     * @return 已设置的提供商类型列表
     */
    public List<String> getUserSetupProviders(String userId) {
        return mfaRegistry.getUserSetupProviders(userId);
    }

    /**
     * 获取所有可用的MFA提供商
     *
     * @return 可用的提供商类型列表
     */
    public List<String> getAvailableProviders() {
        return mfaRegistry.getAvailableProviderTypes();
    }

    /**
     * 初始化MFA设置
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @param context MFA上下文
     * @return MFA挑战信息
     * @throws MfaException MFA操作异常
     */
    public MfaChallenge initMfaSetup(String userId, String providerType, MfaContext context) {
        log.info("初始化用户 {} 的 {} MFA设置", userId, providerType);

        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.initSetup(userId, context);
        } catch (Exception e) {
            log.error("初始化用户 {} 的 {} MFA设置失败", userId, providerType, e);
            throw new MfaException("MFA_SETUP_INIT_FAILED", "初始化MFA设置失败: " + e.getMessage(), userId, e);
        }
    }

    /**
     * 完成MFA设置
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @param challenge 挑战信息
     * @param verificationCode 验证码
     * @param context MFA上下文
     * @return MFA操作结果
     * @throws MfaException MFA操作异常
     */
    public MfaResult completeMfaSetup(
            String userId, String providerType, MfaChallenge challenge, String verificationCode, MfaContext context) {
        log.info("完成用户 {} 的 {} MFA设置", userId, providerType);

        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.completeSetup(userId, challenge, verificationCode, context);
        } catch (Exception e) {
            log.error("完成用户 {} 的 {} MFA设置失败", userId, providerType, e);
            if (e instanceof MfaVerificationFailedException) {
                throw e;
            }
            throw new MfaException("MFA_SETUP_COMPLETION_FAILED", "完成MFA设置失败: " + e.getMessage(), userId, e);
        }
    }

    /**
     * 验证MFA
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @param verificationCode 验证码
     * @param context MFA上下文
     * @return MFA验证结果
     * @throws MfaVerificationFailedException MFA验证失败异常
     */
    public MfaResult verifyMfa(String userId, String providerType, String verificationCode, MfaContext context) {
        log.debug("验证用户 {} 的 {} MFA", userId, providerType);

        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.verify(userId, verificationCode, context);
        } catch (MfaVerificationFailedException e) {
            log.warn("用户 {} 的 {} MFA验证失败: {}", userId, providerType, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户 {} 的 {} MFA验证异常", userId, providerType, e);
            throw new MfaVerificationFailedException(
                    MfaVerificationFailedException.FailureType.OTHER, "MFA验证过程发生异常: " + e.getMessage(), userId);
        }
    }

    /**
     * 自动选择提供商进行MFA验证
     *
     * @param userId 用户ID
     * @param verificationCode 验证码
     * @param context MFA上下文
     * @return MFA验证结果
     * @throws MfaVerificationFailedException MFA验证失败异常
     */
    public MfaResult verifyMfaAuto(String userId, String verificationCode, MfaContext context) {
        List<String> setupProviders = getUserSetupProviders(userId);

        if (setupProviders.isEmpty()) {
            throw MfaVerificationFailedException.mfaNotSetup(userId);
        }

        // 默认使用第一个已设置的提供商
        String providerType = setupProviders.get(0);
        return verifyMfa(userId, providerType, verificationCode, context);
    }

    /**
     * 移除MFA设置
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @param context MFA上下文
     * @return MFA操作结果
     * @throws MfaException MFA操作异常
     */
    public MfaResult removeMfaSetup(String userId, String providerType, MfaContext context) {
        log.info("移除用户 {} 的 {} MFA设置", userId, providerType);

        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.removeSetup(userId, context);
        } catch (Exception e) {
            log.error("移除用户 {} 的 {} MFA设置失败", userId, providerType, e);
            throw new MfaException("MFA_REMOVE_FAILED", "移除MFA设置失败: " + e.getMessage(), userId, e);
        }
    }

    /**
     * 检查是否需要MFA验证
     *
     * @param userId 用户ID
     * @param requireMfa 是否强制要求MFA
     * @throws MfaRequiredException 需要MFA验证异常
     */
    public void checkMfaRequirement(String userId, boolean requireMfa) {
        if (!requireMfa) {
            return;
        }

        List<String> availableProviders = getAvailableProviders();
        if (availableProviders.isEmpty()) {
            throw MfaRequiredException.mfaDisabled(userId);
        }

        List<String> setupProviders = getUserSetupProviders(userId);
        if (setupProviders.isEmpty()) {
            throw MfaRequiredException.setupRequired(userId, availableProviders);
        }
    }

    /**
     * 生成备用恢复码
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @param context MFA上下文
     * @return 包含备用恢复码的挑战信息
     * @throws MfaException MFA操作异常
     */
    public MfaChallenge generateBackupCodes(String userId, String providerType, MfaContext context) {
        log.info("生成用户 {} 的 {} 备用恢复码", userId, providerType);

        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.generateBackupCodes(userId, context);
        } catch (UnsupportedOperationException e) {
            throw new MfaException("BACKUP_CODES_NOT_SUPPORTED", "该MFA提供商不支持备用恢复码", userId);
        } catch (Exception e) {
            log.error("生成用户 {} 的 {} 备用恢复码失败", userId, providerType, e);
            throw new MfaException("BACKUP_CODES_GENERATION_FAILED", "生成备用恢复码失败: " + e.getMessage(), userId, e);
        }
    }

    /**
     * 验证备用恢复码
     *
     * @param userId 用户ID
     * @param providerType 提供商类型
     * @param backupCode 备用恢复码
     * @param context MFA上下文
     * @return MFA验证结果
     * @throws MfaException MFA操作异常
     */
    public MfaResult verifyBackupCode(String userId, String providerType, String backupCode, MfaContext context) {
        log.info("验证用户 {} 的 {} 备用恢复码", userId, providerType);

        try {
            MfaProvider provider = mfaRegistry.getProvider(providerType);
            return provider.verifyBackupCode(userId, backupCode, context);
        } catch (UnsupportedOperationException e) {
            throw new MfaException("BACKUP_CODES_NOT_SUPPORTED", "该MFA提供商不支持备用恢复码验证", userId);
        } catch (Exception e) {
            log.error("验证用户 {} 的 {} 备用恢复码失败", userId, providerType, e);
            throw new MfaException("BACKUP_CODE_VERIFICATION_FAILED", "备用恢复码验证失败: " + e.getMessage(), userId, e);
        }
    }
}

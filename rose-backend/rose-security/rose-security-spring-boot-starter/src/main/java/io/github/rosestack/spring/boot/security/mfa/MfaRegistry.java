package io.github.rosestack.spring.boot.security.mfa;

import io.github.rosestack.spring.boot.security.mfa.exception.MfaException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MFA提供商注册表
 * <p>
 * 管理所有可用的MFA提供商，提供注册、查找、列表等功能。
 * 支持动态注册和注销MFA提供商。
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Slf4j
@Component
public class MfaRegistry {

    /** 提供商存储 */
    private final Map<String, MfaProvider> providers = new ConcurrentHashMap<>();

    /**
     * 注册MFA提供商
     *
     * @param provider MFA提供商
     */
    public void registerProvider(MfaProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("MFA提供商不能为空");
        }

        String type = provider.getType();
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("MFA提供商类型不能为空");
        }

        providers.put(type, provider);
        log.info("注册MFA提供商: {}", type);
    }

    /**
     * 注销MFA提供商
     *
     * @param type 提供商类型
     * @return 被注销的提供商，如果不存在返回null
     */
    public MfaProvider unregisterProvider(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }

        MfaProvider removed = providers.remove(type);
        if (removed != null) {
            log.info("注销MFA提供商: {}", type);
        }
        return removed;
    }

    /**
     * 获取指定类型的MFA提供商
     *
     * @param type 提供商类型
     * @return MFA提供商
     * @throws MfaException 如果提供商不存在
     */
    public MfaProvider getProvider(String type) {
        return getProviderOptional(type)
                .orElseThrow(() -> new MfaException("MFA_PROVIDER_NOT_FOUND", "未找到类型为 " + type + " 的MFA提供商"));
    }

    /**
     * 获取指定类型的MFA提供商（可选）
     *
     * @param type 提供商类型
     * @return MFA提供商的Optional包装
     */
    public Optional<MfaProvider> getProviderOptional(String type) {
        if (type == null || type.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(type));
    }

    /**
     * 检查是否存在指定类型的提供商
     *
     * @param type 提供商类型
     * @return 如果存在返回true
     */
    public boolean hasProvider(String type) {
        return type != null && providers.containsKey(type);
    }

    /**
     * 获取所有可用的提供商类型
     *
     * @return 提供商类型列表
     */
    public List<String> getAvailableProviderTypes() {
        return providers.keySet().stream().sorted().collect(Collectors.toList());
    }

    /**
     * 获取所有注册的提供商
     *
     * @return 提供商映射（类型 -> 提供商）
     */
    public Map<String, MfaProvider> getAllProviders() {
        return Map.copyOf(providers);
    }

    /**
     * 获取注册的提供商数量
     *
     * @return 提供商数量
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * 清空所有提供商
     */
    public void clear() {
        providers.clear();
        log.info("清空所有MFA提供商");
    }

    /**
     * 检查是否有任何提供商注册
     *
     * @return 如果有提供商返回true
     */
    public boolean hasAnyProvider() {
        return !providers.isEmpty();
    }

    /**
     * 获取用户已设置的MFA提供商
     *
     * @param userId 用户ID
     * @return 已设置的提供商类型列表
     */
    public List<String> getUserSetupProviders(String userId) {
        return providers.entrySet().stream()
                .filter(entry -> {
                    try {
                        return entry.getValue().isSetup(userId);
                    } catch (Exception e) {
                        log.warn("检查用户 {} 的MFA设置状态失败，提供商: {}", userId, entry.getKey(), e);
                        return false;
                    }
                })
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否设置了任何MFA
     *
     * @param userId 用户ID
     * @return 如果设置了任何MFA返回true
     */
    public boolean hasUserSetupAnyMfa(String userId) {
        return providers.values().stream().anyMatch(provider -> {
            try {
                return provider.isSetup(userId);
            } catch (Exception e) {
                log.warn("检查用户 {} 的MFA设置状态失败，提供商: {}", userId, provider.getType(), e);
                return false;
            }
        });
    }
}

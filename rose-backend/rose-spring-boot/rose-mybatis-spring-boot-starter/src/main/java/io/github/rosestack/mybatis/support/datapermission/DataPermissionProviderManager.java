package io.github.rosestack.mybatis.support.datapermission;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据权限提供者管理器
 * <p>
 * 负责管理和选择合适的数据权限提供者：
 * 1. 自动发现和注册权限提供者
 * 2. 根据字段名选择合适的提供者
 * 3. 提供者缓存和优先级管理
 * 4. 字段唯一性校验
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.mybatis.data-permission", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataPermissionProviderManager {

    // 字段支持缓存：字段名 -> 支持的提供者列表（按优先级排序）
    private final Map<String, List<DataPermissionProvider>> fieldSupportCache = new ConcurrentHashMap<>();

    // 所有注册的权限提供者
    private final List<DataPermissionProvider> allProviders = new ArrayList<>();

    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        discoverAndRegisterProviders();
        validateFieldUniqueness();
        log.info("数据权限提供者管理器初始化完成，共注册 {} 个提供者", allProviders.size());
    }

    /**
     * 自动发现和注册权限提供者
     */
    private void discoverAndRegisterProviders() {
        Map<String, DataPermissionProvider> providers = applicationContext.getBeansOfType(DataPermissionProvider.class);

        for (DataPermissionProvider provider : providers.values()) {
            if (provider.isEnabled()) {
                allProviders.add(provider);
                fieldSupportCache.computeIfAbsent(provider.getSupportedField(), k -> new ArrayList<>()).add(provider);
                log.debug("注册权限提供者: {} - {}", provider.getClass().getSimpleName(), provider.getDescription());
            } else {
                log.debug("权限提供者 {} 已禁用，跳过注册", provider.getClass().getSimpleName());
            }
        }
    }

    /**
     * 校验字段唯一性
     * <p>
     * 检查是否有多个提供者支持同一字段，如果有则记录警告信息
     * </p>
     */
    private void validateFieldUniqueness() {
        Map<String, List<DataPermissionProvider>> fieldConflicts = new HashMap<>();


        fieldSupportCache.forEach((field, providers) -> {
            if (providers.size() > 1) {
                fieldConflicts.put(field, providers);
            }
        });

        if (!fieldConflicts.isEmpty()) {
            fieldConflicts.forEach((field, providers) -> {
                String providerNames = providers.stream()
                        .map(p -> p.getClass().getSimpleName() + "(优先级:" + p.getPriority() + ")")
                        .collect(Collectors.joining(", "));
                log.warn("字段 '{}' 被多个提供者支持: {}", field, providerNames);
            });
        }
    }

    /**
     * 根据字段名获取合适的权限提供者
     */
    public DataPermissionProvider getProvider(String field) {
        List<DataPermissionProvider> supportedProviders = fieldSupportCache.get(field).stream()
                .sorted(Comparator.comparingInt(DataPermissionProvider::getPriority))
                .collect(Collectors.toList());

        if (!supportedProviders.isEmpty()) {
            DataPermissionProvider provider = supportedProviders.get(0); // 使用优先级最高的
            return provider;
        }

        log.warn("未找到支持字段 '{}' 的权限提供者", field);
        return null;
    }

    /**
     * 获取权限值
     */
    public List<String> getPermissionValues(String field) {
        DataPermissionProvider provider = getProvider(field);
        if (provider != null) {
            try {
                List<String> values = provider.getPermissionValues(field);
                log.debug("权限提供者 {} 为字段 '{}' 返回权限值: {}",
                        provider.getClass().getSimpleName(), field, values);
                return values;
            } catch (Exception e) {
                log.error("获取权限值失败，提供者: {}, 字段: {}",
                        provider.getClass().getSimpleName(), field, e);
            }
        }

        return Collections.emptyList();
    }

    /**
     * 获取所有注册的权限提供者
     */
    public List<DataPermissionProvider> getAllProviders() {
        return allProviders;
    }

    /**
     * 获取字段支持情况
     */
    public Map<String, String> getFieldSupportMapping() {
        Map<String, String> mapping = new HashMap<>();
        fieldSupportCache.forEach((field, providers) -> {
            String providerNames = providers.stream()
                    .map(p -> p.getClass().getSimpleName() + "(优先级:" + p.getPriority() + ")")
                    .collect(Collectors.joining(", "));
            mapping.put(field, providerNames);
        });

        return mapping;
    }

    /**
     * 获取权限提供者统计信息
     */
    public Map<String, Object> getProviderStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProviders", allProviders.size());
        stats.put("enabledProviders", allProviders.stream()
                .mapToLong(p -> p.isEnabled() ? 1 : 0).sum());
        stats.put("fieldSupportCacheSize", fieldSupportCache.size());

        // 提供者详情
        List<Map<String, Object>> providerDetails = allProviders.stream()
                .map(provider -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("class", provider.getClass().getSimpleName());
                    detail.put("priority", provider.getPriority());
                    detail.put("enabled", provider.isEnabled());
                    detail.put("description", provider.getDescription());
                    return detail;
                })
                .collect(Collectors.toList());

        stats.put("providers", providerDetails);
        stats.put("fieldSupportMapping", getFieldSupportMapping());
        return stats;
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        fieldSupportCache.clear();
        log.info("权限提供者缓存已清空");
    }

    /**
     * 重新加载权限提供者
     */
    public void reload() {
        allProviders.clear();
        fieldSupportCache.clear();

        discoverAndRegisterProviders();
        validateFieldUniqueness();
        log.info("权限提供者已重新加载");
    }
}

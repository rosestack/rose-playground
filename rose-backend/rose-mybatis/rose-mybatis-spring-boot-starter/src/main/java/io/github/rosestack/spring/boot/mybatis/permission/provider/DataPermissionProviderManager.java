package io.github.rosestack.spring.boot.mybatis.permission.provider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限提供者管理器
 *
 * <p>负责管理和选择合适的数据权限提供者： 1. 自动发现和注册权限提供者 2. 根据字段名选择合适的提供者 3. 提供者缓存和优先级管理 4. 字段唯一性校验
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DataPermissionProviderManager extends AbstractDataPermissionProviderManager {

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
    @Override
    protected void discoverAndRegisterProviders() {
        Map<String, DataPermissionProvider> providers = applicationContext.getBeansOfType(DataPermissionProvider.class);
        for (DataPermissionProvider provider : providers.values()) {
            if (!provider.isEnabled()) {
                continue;
            }
            this.getAllProviders().add(provider);
            this.fieldSupportCache
                    .computeIfAbsent(provider.getSupportedField(), k -> new ArrayList<>())
                    .add(provider);
            log.debug("注册权限提供者: {} - {}", provider.getClass().getSimpleName(), provider.getDescription());
        }
    }

    /**
     * 校验字段唯一性
     *
     * <p>检查是否有多个提供者支持同一字段，如果有则记录警告信息
     */
    @Override
    protected void validateFieldUniqueness() {
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
     * 获取权限值
     */
    public List<String> getPermissionValues(String field) {
        DataPermissionProvider provider = getProvider(field);
        if (provider != null) {
            try {
                List<String> values = provider.getPermissionValues(field);
                log.debug("权限提供者 {} 为字段 '{}' 返回权限值: {}", provider.getClass().getSimpleName(), field, values);
                return values;
            } catch (Exception e) {
                log.error("获取权限值失败，提供者: {}, 字段: {}", provider.getClass().getSimpleName(), field, e);
            }
        }

        return Collections.emptyList();
    }
}

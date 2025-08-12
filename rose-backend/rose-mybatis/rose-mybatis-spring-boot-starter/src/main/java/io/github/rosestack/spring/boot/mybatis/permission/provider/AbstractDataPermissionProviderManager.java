package io.github.rosestack.spring.boot.mybatis.permission.provider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 核心层的权限提供者管理抽象，Starter 实现具体发现与装配。
 */
public abstract class AbstractDataPermissionProviderManager {
    protected final Map<String, List<DataPermissionProvider>> fieldSupportCache = new ConcurrentHashMap<>();
    protected final List<DataPermissionProvider> allProviders = new ArrayList<>();

    /** Starter 负责发现并填充 allProviders 与 fieldSupportCache */
    protected abstract void discoverAndRegisterProviders();

    /** 简单校验：同一字段是否被多个 Provider 支持（Starter 可扩展日志输出） */
    protected void validateFieldUniqueness() {}

    public DataPermissionProvider getProvider(String field) {
        List<DataPermissionProvider> list = fieldSupportCache.get(field);
        if (list == null || list.isEmpty()) return null;
        return list.stream().min(Comparator.comparingInt(DataPermissionProvider::getPriority)).orElse(null);
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

    public List<DataPermissionProvider> getAllProviders() { return allProviders; }
}



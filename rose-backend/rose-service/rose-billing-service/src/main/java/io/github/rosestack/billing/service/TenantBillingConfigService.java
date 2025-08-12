package io.github.rosestack.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.rosestack.billing.entity.BillingConfig;
import io.github.rosestack.billing.repository.BillingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 租户计费配置读取
 */
@Service
@RequiredArgsConstructor
public class TenantBillingConfigService {

    private final BillingConfigRepository billingConfigRepository;

    /**
     * 获取租户默认币种，先读租户级配置，找不到再回退全局配置，最后回退默认
     */
    public String getCurrency(String tenantId) {
        return getEffective(tenantId, "billing.currency")
                .orElse(DEFAULTS.getOrDefault("currency", "CNY"));
    }

    public int getIntOrDefault(String tenantId, String key, int defaultVal) {
        return getEffective(tenantId, key).map(v -> {
            try { return Integer.parseInt(v.trim()); } catch (Exception ignore) { return defaultVal; }
        }).orElse(defaultVal);
    }

    public long getLongOrDefault(String tenantId, String key, long defaultVal) {
        return getEffective(tenantId, key).map(v -> {
            try { return Long.parseLong(v.trim()); } catch (Exception ignore) { return defaultVal; }
        }).orElse(defaultVal);
    }

    public boolean getBoolOrDefault(String tenantId, String key, boolean defaultVal) {
        return getEffective(tenantId, key).map(v -> {
            String s = v.trim().toLowerCase();
            if ("true".equals(s) || "1".equals(s) || "yes".equals(s)) return true;
            if ("false".equals(s) || "0".equals(s) || "no".equals(s)) return false;
            return defaultVal;
        }).orElse(defaultVal);
    }

    /**
     * 读取租户级配置；若不存在，回退读取全局配置（tenant_id IS NULL）
     */
    public Optional<String> getEffective(String tenantId, String key) {
        if (tenantId != null) {
            Optional<String> t = get(tenantId, key);
            if (t.isPresent()) return t;
        }
        BillingConfig global = billingConfigRepository.selectOne(new LambdaQueryWrapper<BillingConfig>()
                .isNull(BillingConfig::getTenantId)
                .eq(BillingConfig::getConfigKey, key)
                .last("limit 1"));
        return Optional.ofNullable(global == null ? null : global.getConfigValue());
    }

    /**
     * 严格按 (tenantId, key) 读取，不做全局回退
     */
    public Optional<String> get(String tenantId, String key) {
        BillingConfig cfg = billingConfigRepository.selectOne(new LambdaQueryWrapper<BillingConfig>()
                .eq(BillingConfig::getTenantId, tenantId)
                .eq(BillingConfig::getConfigKey, key)
                .last("limit 1"));
        return Optional.ofNullable(cfg == null ? null : cfg.getConfigValue());
    }

    private static final Map<String, String> DEFAULTS = Map.of(
            "currency", "CNY"
    );
}

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
     * 获取租户默认币种，找不到返回全局默认
     */
    public String getCurrency(String tenantId) {
        String v = get(tenantId, "billing.currency")
                .orElse(DEFAULTS.getOrDefault("currency", "CNY"));
        return v;
    }

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

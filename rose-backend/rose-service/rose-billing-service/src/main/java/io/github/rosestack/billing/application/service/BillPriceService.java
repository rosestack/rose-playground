package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.PriceType;
import io.github.rosestack.billing.domain.enums.TargetType;
import io.github.rosestack.billing.domain.price.BillPrice;
import io.github.rosestack.billing.domain.price.BillPriceMapper;
import io.github.rosestack.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定价管理服务
 *
 * 提供统一的定价管理功能，支持标准定价和租户专属定价
 * 实现定价策略的灵活配置和动态调整
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillPriceService {

    private final BillPriceMapper priceMapper;

    /**
     * 创建定价
     */
    @Transactional(rollbackFor = Exception.class)
    public BillPrice createPrice(BillPrice price) {
        log.info("Creating new price for target: {} {}", price.getTargetType(), price.getTargetId());

        // 验证定价数据
        validatePriceData(price);

        // 检查是否已存在相同的定价
        if (priceMapper.existsPrice(price.getTargetType(), price.getTargetId(), 
                                   price.getBillingCycle(), price.getTenantId())) {
            throw new BusinessException("定价已存在");
        }

        // 设置默认值
        if (price.getEffectiveTime() == null) {
            price.setEffectiveTime(LocalDateTime.now());
        }
        if (price.getCurrency() == null) {
            price.setCurrency("CNY");
        }

        priceMapper.insert(price);
        log.info("Created price with ID: {}", price.getId());
        return price;
    }

    /**
     * 更新定价
     */
    @Transactional(rollbackFor = Exception.class)
    public BillPrice updatePrice(BillPrice price) {
        log.info("Updating price: {}", price.getId());

        BillPrice existingPrice = priceMapper.selectById(price.getId());
        if (existingPrice == null) {
            throw new BusinessException("定价不存在: " + price.getId());
        }

        // 验证定价数据
        validatePriceData(price);

        priceMapper.updateById(price);
        log.info("Updated price: {}", price.getId());
        return price;
    }

    /**
     * 获取最佳定价
     */
    public BillPrice getBestPrice(String tenantId, TargetType targetType, 
                                 Long targetId, BillingCycle cycle) {
        log.debug("Getting best price for tenant: {}, target: {} {}, cycle: {}", 
                 tenantId, targetType, targetId, cycle);

        BillPrice price = priceMapper.findBestPrice(tenantId, targetType, targetId, cycle);
        
        if (price == null) {
            log.warn("No price found for target: {} {}, cycle: {}", targetType, targetId, cycle);
            throw new BusinessException("未找到定价信息");
        }

        log.debug("Found price: {} for target: {} {}", price.getPrice(), targetType, targetId);
        return price;
    }

    /**
     * 获取标准定价
     */
    public BillPrice getStandardPrice(TargetType targetType, Long targetId, BillingCycle cycle) {
        log.debug("Getting standard price for target: {} {}, cycle: {}", targetType, targetId, cycle);

        BillPrice price = priceMapper.findEffectiveStandardPrice(targetType, targetId, cycle);
        
        if (price == null) {
            throw new BusinessException("未找到标准定价信息");
        }

        return price;
    }

    /**
     * 获取租户专属定价
     */
    public BillPrice getTenantSpecificPrice(String tenantId, TargetType targetType, 
                                          Long targetId, BillingCycle cycle) {
        log.debug("Getting tenant specific price for tenant: {}, target: {} {}, cycle: {}", 
                 tenantId, targetType, targetId, cycle);

        return priceMapper.findTenantSpecificPrice(tenantId, targetType, targetId, cycle);
    }

    /**
     * 创建租户专属定价
     */
    @Transactional(rollbackFor = Exception.class)
    public BillPrice createTenantPrice(String tenantId, TargetType targetType, Long targetId,
                                      BigDecimal price, String currency, BillingCycle cycle,
                                      String pricingConfig) {
        log.info("Creating tenant specific price for tenant: {}, target: {} {}", 
                tenantId, targetType, targetId);

        // 检查是否已存在租户专属定价
        if (priceMapper.existsPrice(targetType, targetId, cycle, tenantId)) {
            throw new BusinessException("租户专属定价已存在");
        }

        BillPrice billPrice = new BillPrice();
        billPrice.setType(targetType == TargetType.PLAN ? PriceType.TENANT_PLAN : PriceType.TENANT_FEATURE);
        billPrice.setTargetType(targetType);
        billPrice.setTargetId(targetId);
        billPrice.setTenantId(tenantId);
        billPrice.setPrice(price);
        billPrice.setCurrency(currency != null ? currency : "CNY");
        billPrice.setBillingCycle(cycle);
        billPrice.setPricingConfig(pricingConfig);
        billPrice.setEffectiveTime(LocalDateTime.now());

        return createPrice(billPrice);
    }

    /**
     * 查询租户的所有定价
     */
    public List<BillPrice> getTenantPrices(String tenantId) {
        log.debug("Getting all prices for tenant: {}", tenantId);
        return priceMapper.findPricesByTenant(tenantId);
    }

    /**
     * 查询所有有效的套餐定价
     */
    public List<BillPrice> getEffectivePlanPrices() {
        log.debug("Getting all effective plan prices");
        return priceMapper.findEffectivePlanPrices();
    }

    /**
     * 查询所有有效的功能定价
     */
    public List<BillPrice> getEffectiveFeaturePrices() {
        log.debug("Getting all effective feature prices");
        return priceMapper.findEffectiveFeaturePrices();
    }

    /**
     * 使定价过期
     */
    @Transactional(rollbackFor = Exception.class)
    public void expirePrice(Long priceId) {
        log.info("Expiring price: {}", priceId);

        BillPrice price = priceMapper.selectById(priceId);
        if (price == null) {
            throw new BusinessException("定价不存在: " + priceId);
        }

        price.setExpireTime(LocalDateTime.now());
        priceMapper.updateById(price);
        
        log.info("Expired price: {}", priceId);
    }

    /**
     * 批量使目标的所有定价过期
     */
    @Transactional(rollbackFor = Exception.class)
    public void expireTargetPrices(TargetType targetType, Long targetId) {
        log.info("Expiring all prices for target: {} {}", targetType, targetId);
        priceMapper.expirePrices(targetType, targetId);
        log.info("Expired all prices for target: {} {}", targetType, targetId);
    }

    /**
     * 验证定价数据
     */
    private void validatePriceData(BillPrice price) {
        if (price.getTargetType() == null) {
            throw new BusinessException("目标类型不能为空");
        }
        if (price.getTargetId() == null) {
            throw new BusinessException("目标ID不能为空");
        }
        if (price.getPrice() == null || price.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("价格必须大于等于0");
        }
        if (price.getBillingCycle() == null) {
            throw new BusinessException("计费周期不能为空");
        }
        if (price.getType() == null) {
            throw new BusinessException("定价类型不能为空");
        }

        // 租户专属定价必须有租户ID
        if ((price.getType() == PriceType.TENANT_PLAN || price.getType() == PriceType.TENANT_FEATURE) 
            && (price.getTenantId() == null || price.getTenantId().trim().isEmpty())) {
            throw new BusinessException("租户专属定价必须指定租户ID");
        }

        // 标准定价不能有租户ID
        if ((price.getType() == PriceType.PLAN || price.getType() == PriceType.FEATURE) 
            && price.getTenantId() != null) {
            throw new BusinessException("标准定价不能指定租户ID");
        }
    }
}
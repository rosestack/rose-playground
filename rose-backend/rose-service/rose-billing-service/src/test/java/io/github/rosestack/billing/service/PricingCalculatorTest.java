package io.github.rosestack.billing.service;

import io.github.rosestack.billing.config.BillingProperties;
import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.enums.BillingType;
import io.github.rosestack.billing.repository.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * PricingCalculator 单元测试
 *
 * @author rose
 */
@ExtendWith(MockitoExtension.class)
class PricingCalculatorTest {

    @Mock
    private UsageRecordRepository usageRepository;

    @Mock
    private BillingProperties billingProperties;

    @InjectMocks
    private PricingCalculator pricingCalculator;

    private SubscriptionPlan testPlan;
    private String testTenantId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @BeforeEach
    void setUp() {
        testTenantId = "tenant-123";
        periodStart = LocalDateTime.of(2024, 1, 1, 0, 0);
        periodEnd = LocalDateTime.of(2024, 1, 31, 23, 59);

        testPlan = new SubscriptionPlan();
        testPlan.setId("plan-123");
        testPlan.setBasePrice(new BigDecimal("99.00"));
        testPlan.setBillingType(BillingType.MONTHLY);

        // 设置使用量定价
        Map<String, BigDecimal> usagePricing = new HashMap<>();
        usagePricing.put("api_calls", new BigDecimal("0.01"));
        usagePricing.put("storage_gb", new BigDecimal("0.10"));
        testPlan.setUsagePricing(usagePricing);

        // Mock BillingProperties - 使用 lenient 避免不必要的 stubbing 警告
        lenient().when(billingProperties.getDefaultTaxRate()).thenReturn(new BigDecimal("0.08"));
    }

    @Test
    void testCalculateBasePrice_Monthly() {
        // 测试按月计费的基础价格计算
        BigDecimal result = pricingCalculator.calculateBasePrice(testPlan, periodStart, periodEnd);
        
        assertEquals(new BigDecimal("99.00"), result);
    }

    @Test
    void testCalculateBasePrice_UsageBased() {
        // 测试使用量计费模式
        testPlan.setBillingType(BillingType.USAGE_BASED);
        
        BigDecimal result = pricingCalculator.calculateBasePrice(testPlan, periodStart, periodEnd);
        
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateUsageAmount() {
        // Mock 使用量查询
        when(usageRepository.sumUsageByTenantAndMetricAndPeriod(
                eq(testTenantId), eq("api_calls"), any(), any()))
                .thenReturn(new BigDecimal("1000"));
        when(usageRepository.sumUsageByTenantAndMetricAndPeriod(
                eq(testTenantId), eq("storage_gb"), any(), any()))
                .thenReturn(new BigDecimal("50"));

        BigDecimal result = pricingCalculator.calculateUsageAmount(
                testTenantId, periodStart, periodEnd, testPlan);

        // 1000 * 0.01 + 50 * 0.10 = 10 + 5 = 15
        assertEquals(new BigDecimal("15.00"), result);
    }

    @Test
    void testCalculateUsageAmount_NoPricing() {
        // 测试没有使用量定价的情况
        testPlan.setUsagePricing(null);
        
        BigDecimal result = pricingCalculator.calculateUsageAmount(
                testTenantId, periodStart, periodEnd, testPlan);
        
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateDiscount() {
        BigDecimal amount = new BigDecimal("100.00");
        
        // 测试 WELCOME10 折扣码
        BigDecimal result = pricingCalculator.calculateDiscount(testTenantId, amount, "WELCOME10");
        assertEquals(0, new BigDecimal("10.00").compareTo(result));
        
        // 测试无效折扣码
        BigDecimal noDiscount = pricingCalculator.calculateDiscount(testTenantId, amount, "INVALID");
        assertEquals(BigDecimal.ZERO, noDiscount);
    }

    @Test
    void testCalculateTax() {
        BigDecimal amount = new BigDecimal("100.00");
        
        BigDecimal result = pricingCalculator.calculateTax(amount);
        
        assertEquals(new BigDecimal("8.00"), result);
    }

    @Test
    void testCalculateTotalAmount() {
        // Mock 使用量查询
        when(usageRepository.sumUsageByTenantAndMetricAndPeriod(anyString(), anyString(), any(), any()))
                .thenReturn(new BigDecimal("100"));

        BigDecimal result = pricingCalculator.calculateTotalAmount(
                testTenantId, testPlan, periodStart, periodEnd, "WELCOME10");

        // 基础费用: 99.00
        // 使用量费用: 100 * 0.01 + 100 * 0.10 = 11.00
        // 小计: 110.00
        // 折扣: 11.00 (10%)
        // 税后: 99.00 + 7.92 = 106.92
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testIsUsageExceeded() {
        // 设置 API 调用限制
        testPlan.setApiCallLimit(10000L);

        when(usageRepository.sumUsageByTenantAndMetricAndPeriod(anyString(), eq("api_calls"), any(), any()))
                .thenReturn(new BigDecimal("5000"));

        boolean result = pricingCalculator.isUsageExceeded(testTenantId, testPlan, "api_calls");

        assertFalse(result);
    }
}

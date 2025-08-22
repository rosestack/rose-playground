package io.github.rosestack.billing.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.domain.enums.FeatureType;
import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.billing.domain.feature.BillFeatureMapper;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanFeature;
import io.github.rosestack.billing.domain.plan.BillPlanFeatureMapper;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.billing.domain.usage.BillUsageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 计费引擎服务单元测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BillingEngineServiceTest {

    @Mock
    private BillSubscriptionMapper subscriptionMapper;

    @Mock
    private BillPlanMapper planMapper;

    @Mock
    private BillFeatureMapper featureMapper;

    @Mock
    private BillPlanFeatureMapper planFeatureMapper;

    @Mock
    private BillUsageMapper usageMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BillingEngineService billingEngineService;

    private BillSubscription testSubscription;
    private BillPlan testPlan;
    private BillFeature testFeature;
    private BillPlanFeature testPlanFeature;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testSubscription = new BillSubscription();
        testSubscription.setId(1L);
        testSubscription.setPlanId(1L);
        testSubscription.setTenantId("tenant001");
        testSubscription.setQuantity(1);

        testPlan = new BillPlan();
        testPlan.setId(1L);
        testPlan.setCode("PRO");
        testPlan.setName("专业版");

        testFeature = new BillFeature();
        testFeature.setId(1L);
        testFeature.setCode("API_CALLS");
        testFeature.setName("API调用");
        testFeature.setType(FeatureType.USAGE);
        testFeature.setUnit("次");

        testPlanFeature = new BillPlanFeature();
        testPlanFeature.setPlanId(1L);
        testPlanFeature.setFeatureId(1L);
        testPlanFeature.setFeatureValue("{\"enabled\":true,\"basePrice\":10.0,\"unitPrice\":0.01,\"quota\":1000,\"freeQuota\":100}");
    }

    @Test
    void testCalculateBilling_Success() {
        // Given
        Long subscriptionId = 1L;
        LocalDate periodStart = LocalDate.of(2024, 1, 1);
        LocalDate periodEnd = LocalDate.of(2024, 1, 31);

        when(subscriptionMapper.selectById(subscriptionId)).thenReturn(testSubscription);
        when(planMapper.selectById(testSubscription.getPlanId())).thenReturn(testPlan);
        when(planFeatureMapper.findByPlanId(testSubscription.getPlanId()))
                .thenReturn(Arrays.asList(testPlanFeature));
        when(featureMapper.selectById(testPlanFeature.getFeatureId())).thenReturn(testFeature);
        when(usageMapper.sumUsageBySubscriptionFeatureAndPeriod(subscriptionId, testFeature.getId(), periodStart))
                .thenReturn(BigDecimal.valueOf(150)); // 使用量150次

        // When
        BillingEngineService.BillingResult result = billingEngineService.calculateBilling(
                subscriptionId, periodStart, periodEnd);

        // Then
        assertNotNull(result);
        assertEquals(subscriptionId, result.getSubscriptionId());
        assertEquals(testPlan.getId(), result.getPlanId());
        assertEquals(periodStart, result.getPeriodStart());
        assertEquals(periodEnd, result.getPeriodEnd());
        assertEquals(1, result.getQuantity());
        assertTrue(result.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        // 验证调用次数
        verify(subscriptionMapper).selectById(subscriptionId);
        verify(planMapper).selectById(testSubscription.getPlanId());
        verify(planFeatureMapper).findByPlanId(testSubscription.getPlanId());
        verify(featureMapper).selectById(testPlanFeature.getFeatureId());
        verify(usageMapper).sumUsageBySubscriptionFeatureAndPeriod(subscriptionId, testFeature.getId(), periodStart);
    }

    @Test
    void testCalculateBilling_SubscriptionNotFound() {
        // Given
        Long subscriptionId = 999L;
        LocalDate periodStart = LocalDate.of(2024, 1, 1);
        LocalDate periodEnd = LocalDate.of(2024, 1, 31);

        when(subscriptionMapper.selectById(subscriptionId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            billingEngineService.calculateBilling(subscriptionId, periodStart, periodEnd);
        });

        assertEquals("订阅不存在: " + subscriptionId, exception.getMessage());
        verify(subscriptionMapper).selectById(subscriptionId);
        verifyNoInteractions(planMapper, planFeatureMapper, featureMapper, usageMapper);
    }

    @Test
    void testCalculateBilling_PlanNotFound() {
        // Given
        Long subscriptionId = 1L;
        LocalDate periodStart = LocalDate.of(2024, 1, 1);
        LocalDate periodEnd = LocalDate.of(2024, 1, 31);

        when(subscriptionMapper.selectById(subscriptionId)).thenReturn(testSubscription);
        when(planMapper.selectById(testSubscription.getPlanId())).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            billingEngineService.calculateBilling(subscriptionId, periodStart, periodEnd);
        });

        assertEquals("套餐不存在: " + testSubscription.getPlanId(), exception.getMessage());
        verify(subscriptionMapper).selectById(subscriptionId);
        verify(planMapper).selectById(testSubscription.getPlanId());
        verifyNoInteractions(planFeatureMapper, featureMapper, usageMapper);
    }

    @Test
    void testCheckQuota_Success() {
        // Given
        Long subscriptionId = 1L;
        Long featureId = 1L;
        BigDecimal requestedAmount = BigDecimal.valueOf(50);

        List<BillPlanFeature> planFeatures = Arrays.asList(testPlanFeature);
        when(planFeatureMapper.findBySubscriptionAndFeature(subscriptionId, featureId))
                .thenReturn(planFeatures);
        when(usageMapper.sumUsageBySubscriptionFeatureAndPeriod(eq(subscriptionId), eq(featureId), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(200)); // 当前使用量200

        // When
        BillingEngineService.QuotaCheckResult result = billingEngineService.checkQuota(
                subscriptionId, featureId, requestedAmount);

        // Then
        assertNotNull(result);
        assertTrue(result.isAllowed()); // 配额1000 - 当前使用200 = 可用800，请求50应该允许
        assertEquals("配额充足", result.getMessage());
        assertTrue(result.getAvailableQuota().compareTo(requestedAmount) >= 0);

        verify(planFeatureMapper).findBySubscriptionAndFeature(subscriptionId, featureId);
        verify(usageMapper).sumUsageBySubscriptionFeatureAndPeriod(eq(subscriptionId), eq(featureId), any(LocalDate.class));
    }

    @Test
    void testCheckQuota_ExceedsLimit() {
        // Given
        Long subscriptionId = 1L;
        Long featureId = 1L;
        BigDecimal requestedAmount = BigDecimal.valueOf(900); // 请求量900

        List<BillPlanFeature> planFeatures = Arrays.asList(testPlanFeature);
        when(planFeatureMapper.findBySubscriptionAndFeature(subscriptionId, featureId))
                .thenReturn(planFeatures);
        when(usageMapper.sumUsageBySubscriptionFeatureAndPeriod(eq(subscriptionId), eq(featureId), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(200)); // 当前使用量200

        // When
        BillingEngineService.QuotaCheckResult result = billingEngineService.checkQuota(
                subscriptionId, featureId, requestedAmount);

        // Then
        assertNotNull(result);
        assertFalse(result.isAllowed()); // 配额1000 - 当前使用200 = 可用800，请求900应该拒绝
        assertEquals("配额不足", result.getMessage());
        assertTrue(result.getAvailableQuota().compareTo(requestedAmount) < 0);

        verify(planFeatureMapper).findBySubscriptionAndFeature(subscriptionId, featureId);
        verify(usageMapper).sumUsageBySubscriptionFeatureAndPeriod(eq(subscriptionId), eq(featureId), any(LocalDate.class));
    }

    @Test
    void testCheckQuota_FeatureNotConfigured() {
        // Given
        Long subscriptionId = 1L;
        Long featureId = 1L;
        BigDecimal requestedAmount = BigDecimal.valueOf(50);

        when(planFeatureMapper.findBySubscriptionAndFeature(subscriptionId, featureId))
                .thenReturn(Arrays.asList()); // 空列表，表示功能未配置

        // When
        BillingEngineService.QuotaCheckResult result = billingEngineService.checkQuota(
                subscriptionId, featureId, requestedAmount);

        // Then
        assertNotNull(result);
        assertFalse(result.isAllowed());
        assertEquals("功能未配置", result.getMessage());
        assertEquals(BigDecimal.ZERO, result.getAvailableQuota());
        assertEquals(BigDecimal.ZERO, result.getTotalQuota());

        verify(planFeatureMapper).findBySubscriptionAndFeature(subscriptionId, featureId);
        verifyNoInteractions(usageMapper);
    }
}
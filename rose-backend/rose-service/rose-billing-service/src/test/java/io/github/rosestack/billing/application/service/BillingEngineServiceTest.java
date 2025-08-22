package io.github.rosestack.billing.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.billing.domain.feature.BillFeatureMapper;
import io.github.rosestack.billing.domain.plan.BillPlan;
import io.github.rosestack.billing.domain.plan.BillPlanFeature;
import io.github.rosestack.billing.domain.plan.BillPlanFeatureMapper;
import io.github.rosestack.billing.domain.plan.BillPlanMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.billing.domain.usage.BillUsageMapper;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

    @Mock
    private Timer billingCalculationTimer;

    @InjectMocks
    private BillingEngineService billingEngineService;

    private BillSubscription subscription;
    private BillPlan plan;
    private BillFeature feature;
    private BillPlanFeature planFeature;

    @BeforeEach
    void setUp() {
        subscription = new BillSubscription();
        subscription.setId(1L);
        subscription.setPlanId(1L);
        subscription.setQuantity(1);
        subscription.setStartTime(LocalDateTime.now());
        subscription.setCurrentPeriodStartTime(LocalDateTime.now());
        subscription.setCurrentPeriodEndTime(LocalDateTime.now().plusMonths(1));

        plan = new BillPlan();
        plan.setId(1L);
        plan.setCode("PRO");
        plan.setName("专业版");

        feature = new BillFeature();
        feature.setId(1L);
        feature.setCode("api_calls");
        feature.setName("API调用");
        feature.setType(io.github.rosestack.billing.domain.enums.FeatureType.USAGE);

        planFeature = new BillPlanFeature();
        planFeature.setId(1L);
        planFeature.setPlanId(1L);
        planFeature.setFeatureId(1L);
        planFeature.setFeatureValue("{\"enabled\":true,\"freeQuota\":10000,\"unitPrice\":0.001}");
    }

    @Test
    void estimateBilling_Success() {
        // Given
        LocalDate periodStart = LocalDate.now();
        LocalDate periodEnd = periodStart.plusMonths(1);
        
        when(subscriptionMapper.selectById(1L)).thenReturn(subscription);
        when(planMapper.selectById(1L)).thenReturn(plan);
        when(planFeatureMapper.findByPlanId(1L)).thenReturn(Arrays.asList(planFeature));
        when(featureMapper.selectById(1L)).thenReturn(feature);
        when(usageMapper.sumUsageBySubscriptionFeatureAndPeriod(1L, 1L, periodStart)).thenReturn(BigDecimal.valueOf(15000));
        when(billingCalculationTimer.record(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0, Timer.SampleCallable.class).call();
        });

        // When
        BillingEngineService.BillingResult result = billingEngineService.estimateBilling(1L, periodStart, periodEnd);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getSubscriptionId());
        assertNotNull(result.getTotalAmount());
        verify(subscriptionMapper).selectById(1L);
    }

    @Test
    void getBillingPeriodInfo_Success() {
        // Given
        when(subscriptionMapper.selectById(1L)).thenReturn(subscription);

        // When
        BillingEngineService.BillingPeriodInfo info = billingEngineService.getBillingPeriodInfo(1L);

        // Then
        assertNotNull(info);
        assertEquals(1L, info.getSubscriptionId());
        assertEquals(subscription.getCurrentPeriodStartTime(), info.getCurrentPeriodStart());
        assertEquals(subscription.getCurrentPeriodEndTime(), info.getCurrentPeriodEnd());
    }

    @Test
    void getBillingPeriodInfo_SubscriptionNotFound_ThrowsException() {
        // Given
        when(subscriptionMapper.selectById(1L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            billingEngineService.getBillingPeriodInfo(1L);
        });

        assertTrue(exception.getMessage().contains("订阅不存在"));
    }

    @Test
    void calculateSeatChangeCost_Success() {
        // Given
        subscription.setQuantity(5);
        String pricingSnapshot = "{\"plan_pricing\":{\"price\":99.00}}";
        subscription.setPricingSnapshot(pricingSnapshot);
        
        when(subscriptionMapper.selectById(1L)).thenReturn(subscription);
        when(objectMapper.readTree(pricingSnapshot)).thenCallRealMethod();

        // When
        BillingEngineService.SeatChangeCalculation calculation = billingEngineService.calculateSeatChangeCost(1L, 10);

        // Then
        assertNotNull(calculation);
        assertEquals(1L, calculation.getSubscriptionId());
        assertEquals(5, calculation.getCurrentQuantity());
        assertEquals(10, calculation.getNewQuantity());
        assertEquals(5, calculation.getQuantityDiff());
        assertNotNull(calculation.getSeatChangeCost());
    }
}
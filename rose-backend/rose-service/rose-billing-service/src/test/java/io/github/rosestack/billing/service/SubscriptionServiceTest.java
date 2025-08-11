package io.github.rosestack.billing.service;

import io.github.rosestack.billing.entity.TenantSubscription;
import io.github.rosestack.billing.entity.SubscriptionPlan;
import io.github.rosestack.billing.enums.SubscriptionStatus;
import io.github.rosestack.billing.exception.PlanNotFoundException;
import io.github.rosestack.billing.exception.SubscriptionNotFoundException;
import io.github.rosestack.billing.repository.SubscriptionPlanRepository;
import io.github.rosestack.billing.repository.TenantSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SubscriptionService 单元测试
 *
 * @author rose
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private TenantSubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private PricingCalculator pricingCalculator;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private TenantSubscription testSubscription;
    private SubscriptionPlan testPlan;
    private String testTenantId;
    private String testSubscriptionId;

    @BeforeEach
    void setUp() {
        testTenantId = "tenant-123";
        testSubscriptionId = "subscription-123";

        testSubscription = new TenantSubscription();
        testSubscription.setId(testSubscriptionId);
        testSubscription.setTenantId(testTenantId);
        testSubscription.setPlanId("plan-123");
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);
        testSubscription.setCurrentPeriodAmount(new BigDecimal("99.00"));

        testPlan = new SubscriptionPlan();
        testPlan.setId("plan-123");
        testPlan.setBasePrice(new BigDecimal("99.00"));
    }

    @Test
    void testGetActiveSubscription_Success() {
        when(subscriptionRepository.findActiveByTenantId(testTenantId))
                .thenReturn(Optional.of(testSubscription));

        Optional<TenantSubscription> result = subscriptionService.getActiveSubscription(testTenantId);

        assertTrue(result.isPresent());
        assertEquals(testSubscriptionId, result.get().getId());
        verify(subscriptionRepository).findActiveByTenantId(testTenantId);
    }

    @Test
    void testGetActiveSubscription_NotFound() {
        when(subscriptionRepository.findActiveByTenantId(testTenantId))
                .thenReturn(Optional.empty());

        Optional<TenantSubscription> result = subscriptionService.getActiveSubscription(testTenantId);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetActiveSubscription_InvalidTenantId() {
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.getActiveSubscription(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.getActiveSubscription("");
        });
    }

    @Test
    void testHasActiveSubscription() {
        when(subscriptionRepository.findActiveByTenantId(testTenantId))
                .thenReturn(Optional.of(testSubscription));

        boolean result = subscriptionService.hasActiveSubscription(testTenantId);

        assertTrue(result);
    }

    @Test
    void testPauseSubscription_Success() {
        when(subscriptionRepository.selectById(testSubscriptionId))
                .thenReturn(testSubscription);

        subscriptionService.pauseSubscription(testSubscriptionId, "用户请求暂停");

        assertEquals(SubscriptionStatus.PAUSED, testSubscription.getStatus());
        assertNotNull(testSubscription.getPausedTime());
        assertEquals("用户请求暂停", testSubscription.getPauseReason());
        verify(subscriptionRepository).updateById(testSubscription);
    }

    @Test
    void testPauseSubscription_NotFound() {
        when(subscriptionRepository.selectById(testSubscriptionId))
                .thenReturn(null);

        assertThrows(SubscriptionNotFoundException.class, () -> {
            subscriptionService.pauseSubscription(testSubscriptionId, "测试");
        });
    }

    @Test
    void testResumeSubscription_Success() {
        testSubscription.setStatus(SubscriptionStatus.PAUSED);
        testSubscription.setPausedTime(LocalDateTime.now());
        testSubscription.setPauseReason("测试暂停");

        when(subscriptionRepository.selectById(testSubscriptionId))
                .thenReturn(testSubscription);

        subscriptionService.resumeSubscription(testSubscriptionId);

        assertEquals(SubscriptionStatus.ACTIVE, testSubscription.getStatus());
        assertNull(testSubscription.getPausedTime());
        assertNull(testSubscription.getPauseReason());
        verify(subscriptionRepository).updateById(testSubscription);
    }

    @Test
    void testUpgradeSubscription_Success() {
        String newPlanId = "plan-456";
        SubscriptionPlan newPlan = new SubscriptionPlan();
        newPlan.setId(newPlanId);
        newPlan.setBasePrice(new BigDecimal("199.00"));

        when(subscriptionRepository.selectById(testSubscriptionId))
                .thenReturn(testSubscription);
        when(planRepository.selectById(newPlanId))
                .thenReturn(newPlan);
        when(planRepository.selectById(testSubscription.getPlanId()))
                .thenReturn(testPlan);

        subscriptionService.upgradeSubscription(testSubscriptionId, newPlanId);

        assertEquals(newPlanId, testSubscription.getPlanId());
        assertEquals(new BigDecimal("199.00"), testSubscription.getCurrentPeriodAmount());
        assertNotNull(testSubscription.getUpgradedTime());
        verify(subscriptionRepository).updateById(testSubscription);
    }

    @Test
    void testUpgradeSubscription_PlanNotFound() {
        when(subscriptionRepository.selectById(testSubscriptionId))
                .thenReturn(testSubscription);
        when(planRepository.selectById("invalid-plan"))
                .thenReturn(null);

        assertThrows(PlanNotFoundException.class, () -> {
            subscriptionService.upgradeSubscription(testSubscriptionId, "invalid-plan");
        });
    }

    @Test
    void testValidateUsageLimit_Success() {
        when(subscriptionRepository.findActiveByTenantId(testTenantId))
                .thenReturn(Optional.of(testSubscription));
        when(planRepository.selectById(testSubscription.getPlanId()))
                .thenReturn(testPlan);
        when(pricingCalculator.isUsageExceeded(testTenantId, testPlan, "api_calls"))
                .thenReturn(false);

        boolean result = subscriptionService.validateUsageLimit(testTenantId, "api_calls");

        assertTrue(result);
    }

    @Test
    void testValidateUsageLimit_NoActiveSubscription() {
        when(subscriptionRepository.findActiveByTenantId(testTenantId))
                .thenReturn(Optional.empty());

        boolean result = subscriptionService.validateUsageLimit(testTenantId, "api_calls");

        assertFalse(result);
    }

    @Test
    void testValidateUsageLimit_InvalidParameters() {
        boolean result1 = subscriptionService.validateUsageLimit(null, "api_calls");
        boolean result2 = subscriptionService.validateUsageLimit(testTenantId, null);

        assertFalse(result1);
        assertFalse(result2);
    }
}

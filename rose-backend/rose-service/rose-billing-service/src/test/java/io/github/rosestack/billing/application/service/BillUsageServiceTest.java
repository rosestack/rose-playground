package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.feature.BillFeature;
import io.github.rosestack.billing.domain.feature.BillFeatureMapper;
import io.github.rosestack.billing.domain.subscription.BillSubscription;
import io.github.rosestack.billing.domain.subscription.BillSubscriptionMapper;
import io.github.rosestack.billing.domain.usage.BillUsage;
import io.github.rosestack.billing.domain.usage.BillUsageMapper;
import io.github.rosestack.core.exception.BusinessException;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillUsageServiceTest {

    @Mock
    private BillUsageMapper usageMapper;

    @Mock
    private BillingEngineService billingEngineService;

    @Mock
    private BillSubscriptionMapper subscriptionMapper;

    @Mock
    private BillFeatureMapper featureMapper;

    @Mock
    private Timer quotaCheckTimer;

    @InjectMocks
    private BillUsageService billUsageService;

    private BillUsage usage;
    private BillSubscription subscription;
    private BillFeature feature;

    @BeforeEach
    void setUp() {
        usage = new BillUsage();
        usage.setTenantId("tenant1");
        usage.setSubscriptionId(1L);
        usage.setFeatureId(1L);
        usage.setUsageAmount(BigDecimal.TEN);

        subscription = new BillSubscription();
        subscription.setId(1L);
        subscription.setTenantId("tenant1");

        feature = new BillFeature();
        feature.setId(1L);
        feature.setUnit("次");
    }

    @Test
    void recordUsage_Success() {
        // Given
        when(subscriptionMapper.selectById(1L)).thenReturn(subscription);
        when(featureMapper.selectById(1L)).thenReturn(feature);
        when(subscription.canProvideService()).thenReturn(true);
        when(feature.isActive()).thenReturn(true);

        // When
        BillUsage result = billUsageService.recordUsage(usage);

        // Then
        assertNotNull(result);
        assertEquals(usage, result);
        verify(usageMapper).insert(usage);
    }

    @Test
    void recordUsage_SubscriptionNotFound_ThrowsException() {
        // Given
        when(subscriptionMapper.selectById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billUsageService.recordUsage(usage);
        });

        assertEquals("subscription.not.found", exception.getMessageKey());
        verify(usageMapper, never()).insert(any());
    }

    @Test
    void recordUsage_FeatureNotFound_ThrowsException() {
        // Given
        when(subscriptionMapper.selectById(1L)).thenReturn(subscription);
        when(featureMapper.selectById(1L)).thenReturn(null);
        when(subscription.canProvideService()).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billUsageService.recordUsage(usage);
        });

        assertEquals("feature.not.found", exception.getMessageKey());
        verify(usageMapper, never()).insert(any());
    }

    @Test
    void recordUsageBatch_Success() {
        // Given
        BillUsage usage1 = new BillUsage();
        usage1.setTenantId("tenant1");
        usage1.setSubscriptionId(1L);
        usage1.setFeatureId(1L);
        usage1.setUsageAmount(BigDecimal.TEN);

        BillUsage usage2 = new BillUsage();
        usage2.setTenantId("tenant1");
        usage2.setSubscriptionId(1L);
        usage2.setFeatureId(1L);
        usage2.setUsageAmount(BigDecimal.ONE);

        List<BillUsage> usages = Arrays.asList(usage1, usage2);

        when(subscriptionMapper.selectById(1L)).thenReturn(subscription);
        when(featureMapper.selectById(1L)).thenReturn(feature);
        when(subscription.canProvideService()).thenReturn(true);
        when(feature.isActive()).thenReturn(true);

        // When
        billUsageService.recordUsageBatch(usages);

        // Then
        verify(usageMapper, times(2)).insert(any(BillUsage.class));
    }

    @Test
    void recordUsageBatch_EmptyList_DoesNothing() {
        // When
        billUsageService.recordUsageBatch(Arrays.asList());

        // Then
        verify(usageMapper, never()).insert(any());
    }
}
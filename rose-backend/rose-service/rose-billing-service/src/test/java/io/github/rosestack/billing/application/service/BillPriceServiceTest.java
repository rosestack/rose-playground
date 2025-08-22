package io.github.rosestack.billing.application.service;

import io.github.rosestack.billing.domain.enums.BillingCycle;
import io.github.rosestack.billing.domain.enums.PriceType;
import io.github.rosestack.billing.domain.enums.TargetType;
import io.github.rosestack.billing.domain.price.BillPrice;
import io.github.rosestack.billing.domain.price.BillPriceMapper;
import io.github.rosestack.core.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 定价服务单元测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BillPriceServiceTest {

    @Mock
    private BillPriceMapper priceMapper;

    @InjectMocks
    private BillPriceService priceService;

    private BillPrice standardPlanPrice;
    private BillPrice tenantSpecificPrice;

    @BeforeEach
    void setUp() {
        // 标准套餐定价
        standardPlanPrice = new BillPrice();
        standardPlanPrice.setId(1L);
        standardPlanPrice.setType(PriceType.PLAN);
        standardPlanPrice.setTargetType(TargetType.PLAN);
        standardPlanPrice.setTargetId(1L);
        standardPlanPrice.setPrice(BigDecimal.valueOf(99.00));
        standardPlanPrice.setCurrency("CNY");
        standardPlanPrice.setBillingCycle(BillingCycle.MONTHLY);
        standardPlanPrice.setEffectiveTime(LocalDateTime.now().minusDays(1));

        // 租户专属定价
        tenantSpecificPrice = new BillPrice();
        tenantSpecificPrice.setId(2L);
        tenantSpecificPrice.setType(PriceType.TENANT_PLAN);
        tenantSpecificPrice.setTargetType(TargetType.PLAN);
        tenantSpecificPrice.setTargetId(1L);
        tenantSpecificPrice.setTenantId("tenant001");
        tenantSpecificPrice.setPrice(BigDecimal.valueOf(79.00));
        tenantSpecificPrice.setCurrency("CNY");
        tenantSpecificPrice.setBillingCycle(BillingCycle.MONTHLY);
        tenantSpecificPrice.setEffectiveTime(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testCreatePrice_Success() {
        // Given
        BillPrice price = new BillPrice();
        price.setType(PriceType.PLAN);
        price.setTargetType(TargetType.PLAN);
        price.setTargetId(1L);
        price.setPrice(BigDecimal.valueOf(99.00));
        price.setBillingCycle(BillingCycle.MONTHLY);

        when(priceMapper.existsPrice(any(), any(), any(), any())).thenReturn(false);
        when(priceMapper.insert(price)).thenReturn(1);

        // When
        BillPrice result = priceService.createPrice(price);

        // Then
        assertNotNull(result);
        assertEquals("CNY", result.getCurrency()); // 默认货币
        assertNotNull(result.getEffectiveTime()); // 默认生效时间

        verify(priceMapper).existsPrice(TargetType.PLAN, 1L, BillingCycle.MONTHLY, null);
        verify(priceMapper).insert(price);
    }

    @Test
    void testCreatePrice_AlreadyExists() {
        // Given
        BillPrice price = new BillPrice();
        price.setType(PriceType.PLAN);
        price.setTargetType(TargetType.PLAN);
        price.setTargetId(1L);
        price.setPrice(BigDecimal.valueOf(99.00));
        price.setBillingCycle(BillingCycle.MONTHLY);

        when(priceMapper.existsPrice(any(), any(), any(), any())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            priceService.createPrice(price);
        });

        assertEquals("定价已存在", exception.getMessage());
        verify(priceMapper).existsPrice(TargetType.PLAN, 1L, BillingCycle.MONTHLY, null);
        verify(priceMapper, never()).insert(any());
    }

    @Test
    void testCreatePrice_InvalidData() {
        // Given
        BillPrice price = new BillPrice();
        price.setType(PriceType.PLAN);
        price.setTargetType(TargetType.PLAN);
        // targetId为null，应该验证失败
        price.setPrice(BigDecimal.valueOf(99.00));
        price.setBillingCycle(BillingCycle.MONTHLY);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            priceService.createPrice(price);
        });

        assertEquals("目标ID不能为空", exception.getMessage());
        verifyNoInteractions(priceMapper);
    }

    @Test
    void testGetBestPrice_TenantSpecificExists() {
        // Given
        String tenantId = "tenant001";
        TargetType targetType = TargetType.PLAN;
        Long targetId = 1L;
        BillingCycle cycle = BillingCycle.MONTHLY;

        when(priceMapper.findBestPrice(tenantId, targetType, targetId, cycle))
                .thenReturn(tenantSpecificPrice);

        // When
        BillPrice result = priceService.getBestPrice(tenantId, targetType, targetId, cycle);

        // Then
        assertNotNull(result);
        assertEquals(tenantSpecificPrice.getId(), result.getId());
        assertEquals(BigDecimal.valueOf(79.00), result.getPrice()); // 租户专属价格更低

        verify(priceMapper).findBestPrice(tenantId, targetType, targetId, cycle);
    }

    @Test
    void testGetBestPrice_StandardPriceOnly() {
        // Given
        String tenantId = "tenant002";
        TargetType targetType = TargetType.PLAN;
        Long targetId = 1L;
        BillingCycle cycle = BillingCycle.MONTHLY;

        when(priceMapper.findBestPrice(tenantId, targetType, targetId, cycle))
                .thenReturn(standardPlanPrice);

        // When
        BillPrice result = priceService.getBestPrice(tenantId, targetType, targetId, cycle);

        // Then
        assertNotNull(result);
        assertEquals(standardPlanPrice.getId(), result.getId());
        assertEquals(BigDecimal.valueOf(99.00), result.getPrice()); // 标准价格

        verify(priceMapper).findBestPrice(tenantId, targetType, targetId, cycle);
    }

    @Test
    void testGetBestPrice_NotFound() {
        // Given
        String tenantId = "tenant003";
        TargetType targetType = TargetType.PLAN;
        Long targetId = 999L;
        BillingCycle cycle = BillingCycle.MONTHLY;

        when(priceMapper.findBestPrice(tenantId, targetType, targetId, cycle))
                .thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            priceService.getBestPrice(tenantId, targetType, targetId, cycle);
        });

        assertEquals("未找到定价信息", exception.getMessage());
        verify(priceMapper).findBestPrice(tenantId, targetType, targetId, cycle);
    }

    @Test
    void testCreateTenantPrice_Success() {
        // Given
        String tenantId = "tenant001";
        TargetType targetType = TargetType.PLAN;
        Long targetId = 1L;
        BigDecimal price = BigDecimal.valueOf(79.00);
        String currency = "CNY";
        BillingCycle cycle = BillingCycle.MONTHLY;
        String pricingConfig = "{\"discount\":0.2}";

        when(priceMapper.existsPrice(targetType, targetId, cycle, tenantId)).thenReturn(false);
        when(priceMapper.insert(any(BillPrice.class))).thenReturn(1);

        // When
        BillPrice result = priceService.createTenantPrice(
                tenantId, targetType, targetId, price, currency, cycle, pricingConfig);

        // Then
        assertNotNull(result);
        assertEquals(PriceType.TENANT_PLAN, result.getType());
        assertEquals(tenantId, result.getTenantId());
        assertEquals(price, result.getPrice());
        assertEquals(currency, result.getCurrency());
        assertEquals(pricingConfig, result.getPricingConfig());

        verify(priceMapper).existsPrice(targetType, targetId, cycle, tenantId);
        verify(priceMapper).insert(any(BillPrice.class));
    }

    @Test
    void testCreateTenantPrice_AlreadyExists() {
        // Given
        String tenantId = "tenant001";
        TargetType targetType = TargetType.PLAN;
        Long targetId = 1L;
        BigDecimal price = BigDecimal.valueOf(79.00);
        BillingCycle cycle = BillingCycle.MONTHLY;

        when(priceMapper.existsPrice(targetType, targetId, cycle, tenantId)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            priceService.createTenantPrice(tenantId, targetType, targetId, price, "CNY", cycle, null);
        });

        assertEquals("租户专属定价已存在", exception.getMessage());
        verify(priceMapper).existsPrice(targetType, targetId, cycle, tenantId);
        verify(priceMapper, never()).insert(any());
    }

    @Test
    void testGetTenantPrices() {
        // Given
        String tenantId = "tenant001";
        List<BillPrice> expectedPrices = Arrays.asList(tenantSpecificPrice);

        when(priceMapper.findPricesByTenant(tenantId)).thenReturn(expectedPrices);

        // When
        List<BillPrice> result = priceService.getTenantPrices(tenantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tenantSpecificPrice.getId(), result.get(0).getId());

        verify(priceMapper).findPricesByTenant(tenantId);
    }

    @Test
    void testExpirePrice_Success() {
        // Given
        Long priceId = 1L;
        BillPrice existingPrice = new BillPrice();
        existingPrice.setId(priceId);

        when(priceMapper.selectById(priceId)).thenReturn(existingPrice);
        when(priceMapper.updateById(any(BillPrice.class))).thenReturn(1);

        // When
        priceService.expirePrice(priceId);

        // Then
        assertNotNull(existingPrice.getExpireTime());

        verify(priceMapper).selectById(priceId);
        verify(priceMapper).updateById(existingPrice);
    }

    @Test
    void testExpirePrice_NotFound() {
        // Given
        Long priceId = 999L;

        when(priceMapper.selectById(priceId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            priceService.expirePrice(priceId);
        });

        assertEquals("定价不存在: " + priceId, exception.getMessage());
        verify(priceMapper).selectById(priceId);
        verify(priceMapper, never()).updateById(any());
    }

    @Test
    void testExpireTargetPrices() {
        // Given
        TargetType targetType = TargetType.PLAN;
        Long targetId = 1L;

        // When
        priceService.expireTargetPrices(targetType, targetId);

        // Then
        verify(priceMapper).expirePrices(targetType, targetId);
    }
}
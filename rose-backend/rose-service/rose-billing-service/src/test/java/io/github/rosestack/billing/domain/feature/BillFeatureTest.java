package io.github.rosestack.billing.domain.feature;

import io.github.rosestack.billing.domain.enums.FeatureType;
import io.github.rosestack.billing.domain.enums.ResetPeriod;
import io.github.rosestack.billing.domain.enums.ValueScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BillFeature 实体测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("计费功能实体测试")
class BillFeatureTest {

    private BillFeature feature;

    @BeforeEach
    void setUp() {
        feature = new BillFeature();
        feature.setCode("api_calls");
        feature.setName("API调用");
        feature.setDescription("API调用功能");
        feature.setType(FeatureType.USAGE);
        feature.setUnit("次");
        feature.setResetPeriod(ResetPeriod.MONTH);
        feature.setValueScope(ValueScope.PER_SUBSCRIPTION);
        feature.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("测试功能状态检查")
    void testFeatureStatus() {
        // 测试激活状态
        assertTrue(feature.isActive());

        // 测试非激活状态
        feature.setStatus("INACTIVE");
        assertFalse(feature.isActive());
    }

    @Test
    @DisplayName("测试功能类型检查")
    void testFeatureType() {
        // 测试使用量类型
        feature.setType(FeatureType.USAGE);
        assertTrue(feature.isUsageType());
        assertFalse(feature.isQuotaType());
        assertFalse(feature.isSwitchType());

        // 测试配额类型
        feature.setType(FeatureType.QUOTA);
        assertTrue(feature.isQuotaType());
        assertFalse(feature.isUsageType());
        assertFalse(feature.isSwitchType());

        // 测试开关类型
        feature.setType(FeatureType.SWITCH);
        assertTrue(feature.isSwitchType());
        assertFalse(feature.isQuotaType());
        assertFalse(feature.isUsageType());
    }

    @Test
    @DisplayName("测试功能范围检查")
    void testValueScope() {
        // 测试按订阅共享
        feature.setValueScope(ValueScope.PER_SUBSCRIPTION);
        assertTrue(feature.isPerSubscription());
        assertFalse(feature.isPerSeat());

        // 测试按席位独立
        feature.setValueScope(ValueScope.PER_SEAT);
        assertTrue(feature.isPerSeat());
        assertFalse(feature.isPerSubscription());
    }

    @Test
    @DisplayName("测试重置周期检查")
    void testResetPeriod() {
        // 测试需要重置
        feature.setResetPeriod(ResetPeriod.MONTH);
        assertTrue(feature.needsReset());

        // 测试永不重置
        feature.setResetPeriod(ResetPeriod.NEVER);
        assertFalse(feature.needsReset());
    }

    @Test
    @DisplayName("测试功能激活和停用")
    void testActivateAndDeactivate() {
        // 测试激活
        feature.setStatus("INACTIVE");
        feature.activate();
        assertEquals("ACTIVE", feature.getStatus());
        assertTrue(feature.isActive());

        // 测试停用
        feature.deactivate();
        assertEquals("INACTIVE", feature.getStatus());
        assertFalse(feature.isActive());
    }

    @Test
    @DisplayName("测试功能基本属性")
    void testBasicProperties() {
        assertEquals("api_calls", feature.getCode());
        assertEquals("API调用", feature.getName());
        assertEquals("API调用功能", feature.getDescription());
        assertEquals(FeatureType.USAGE, feature.getType());
        assertEquals("次", feature.getUnit());
        assertEquals(ResetPeriod.MONTH, feature.getResetPeriod());
        assertEquals(ValueScope.PER_SUBSCRIPTION, feature.getValueScope());
    }

    @Test
    @DisplayName("测试不同重置周期的功能")
    void testDifferentResetPeriods() {
        // 日重置
        feature.setResetPeriod(ResetPeriod.DAY);
        assertTrue(feature.needsReset());
        assertEquals(ResetPeriod.DAY, feature.getResetPeriod());

        // 月重置
        feature.setResetPeriod(ResetPeriod.MONTH);
        assertTrue(feature.needsReset());
        assertEquals(ResetPeriod.MONTH, feature.getResetPeriod());

        // 年重置
        feature.setResetPeriod(ResetPeriod.YEAR);
        assertTrue(feature.needsReset());
        assertEquals(ResetPeriod.YEAR, feature.getResetPeriod());

        // 永不重置
        feature.setResetPeriod(ResetPeriod.NEVER);
        assertFalse(feature.needsReset());
        assertEquals(ResetPeriod.NEVER, feature.getResetPeriod());
    }

    @Test
    @DisplayName("测试功能类型和范围的组合")
    void testFeatureTypeAndScopeCombination() {
        // 配额类型 + 按订阅共享
        feature.setType(FeatureType.QUOTA);
        feature.setValueScope(ValueScope.PER_SUBSCRIPTION);
        assertTrue(feature.isQuotaType());
        assertTrue(feature.isPerSubscription());

        // 使用量类型 + 按席位独立
        feature.setType(FeatureType.USAGE);
        feature.setValueScope(ValueScope.PER_SEAT);
        assertTrue(feature.isUsageType());
        assertTrue(feature.isPerSeat());

        // 开关类型 + 按订阅共享
        feature.setType(FeatureType.SWITCH);
        feature.setValueScope(ValueScope.PER_SUBSCRIPTION);
        assertTrue(feature.isSwitchType());
        assertTrue(feature.isPerSubscription());
    }
}
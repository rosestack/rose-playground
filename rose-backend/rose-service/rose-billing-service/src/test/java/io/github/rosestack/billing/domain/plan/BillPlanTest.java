package io.github.rosestack.billing.domain.plan;

import io.github.rosestack.billing.domain.enums.BillingMode;
import io.github.rosestack.billing.domain.enums.PlanStatus;
import io.github.rosestack.billing.domain.enums.PlanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BillPlan 实体测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@DisplayName("套餐计划实体测试")
class BillPlanTest {

    private BillPlan plan;

    @BeforeEach
    void setUp() {
        plan = new BillPlan();
        plan.setCode("PRO");
        plan.setName("专业版");
        plan.setVersion("v1.0");
        plan.setDescription("专业版套餐");
        plan.setPlanType(PlanType.PRO);
        plan.setBillingMode(BillingMode.POSTPAID);
        plan.setTrialEnabled(true);
        plan.setTrialDays(15);
        plan.setTrialLimitPerUser(1);
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setEffectiveTime(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("测试套餐可用性检查")
    void testPlanAvailability() {
        // 测试正常可用套餐
        assertTrue(plan.isAvailable());

        // 测试非激活状态
        plan.setStatus(PlanStatus.DRAFT);
        assertFalse(plan.isAvailable());

        // 测试未生效
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setEffectiveTime(LocalDateTime.now().plusDays(1));
        assertFalse(plan.isAvailable());

        // 测试已失效
        plan.setEffectiveTime(LocalDateTime.now().minusDays(2));
        plan.setExpireTime(LocalDateTime.now().minusDays(1));
        assertFalse(plan.isAvailable());
    }

    @Test
    @DisplayName("测试试用功能")
    void testTrialFunctionality() {
        // 测试支持试用
        assertTrue(plan.supportsTriad());

        // 测试不支持试用 - 未启用
        plan.setTrialEnabled(false);
        assertFalse(plan.supportsTriad());

        // 测试不支持试用 - 天数为0
        plan.setTrialEnabled(true);
        plan.setTrialDays(0);
        assertFalse(plan.supportsTriad());

        // 测试不支持试用 - 天数为null
        plan.setTrialDays(null);
        assertFalse(plan.supportsTriad());
    }

    @Test
    @DisplayName("测试套餐类型检查")
    void testPlanType() {
        // 测试付费套餐
        plan.setPlanType(PlanType.PRO);
        assertTrue(plan.isPaid());
        assertFalse(plan.isFree());

        // 测试免费套餐
        plan.setPlanType(PlanType.FREE);
        assertTrue(plan.isFree());
        assertFalse(plan.isPaid());
    }

    @Test
    @DisplayName("测试计费模式检查")
    void testBillingMode() {
        // 测试预付费
        plan.setBillingMode(BillingMode.PREPAID);
        assertTrue(plan.isPrepaid());
        assertFalse(plan.isPostpaid());
        assertFalse(plan.isHybrid());

        // 测试后付费
        plan.setBillingMode(BillingMode.POSTPAID);
        assertTrue(plan.isPostpaid());
        assertFalse(plan.isPrepaid());
        assertFalse(plan.isHybrid());

        // 测试混合模式
        plan.setBillingMode(BillingMode.HYBRID);
        assertTrue(plan.isHybrid());
        assertFalse(plan.isPrepaid());
        assertFalse(plan.isPostpaid());
    }

    @Test
    @DisplayName("测试套餐状态管理")
    void testPlanStatusManagement() {
        // 测试激活
        plan.setStatus(PlanStatus.DRAFT);
        plan.activate();
        assertEquals(PlanStatus.ACTIVE.name(), plan.getStatus());
        assertEquals(PlanStatus.ACTIVE, plan.getStatus());
        assertNotNull(plan.getEffectiveTime());

        // 测试禁用
        plan.deactivate();
        assertEquals(PlanStatus.INACTIVE.name(), plan.getStatus());
        assertEquals(PlanStatus.INACTIVE, plan.getStatus());

        // 测试弃用
        plan.deprecate();
        assertEquals(PlanStatus.DEPRECATED.name(), plan.getStatus());
        assertEquals(PlanStatus.DEPRECATED, plan.getStatus());

        // 测试归档
        plan.archive();
        assertEquals(PlanStatus.ARCHIVED.name(), plan.getStatus());
        assertEquals(PlanStatus.ARCHIVED, plan.getStatus());
        assertNotNull(plan.getExpireTime());
    }

    @Test
    @DisplayName("测试试用配置设置")
    void testTrialConfigSetting() {
        // 测试启用试用
        plan.setTrialConfig(true, 30, 2);
        assertTrue(plan.getTrialEnabled());
        assertEquals(30, plan.getTrialDays());
        assertEquals(2, plan.getTrialLimitPerUser());

        // 测试禁用试用
        plan.setTrialConfig(false, 15, 1);
        assertFalse(plan.getTrialEnabled());
        assertEquals(0, plan.getTrialDays());
        assertEquals(0, plan.getTrialLimitPerUser());
    }

    @Test
    @DisplayName("测试套餐基本属性")
    void testBasicProperties() {
        assertEquals("PRO", plan.getCode());
        assertEquals("专业版", plan.getName());
        assertEquals("v1.0", plan.getVersion());
        assertEquals("专业版套餐", plan.getDescription());
        assertEquals(PlanType.PRO, plan.getPlanType());
        assertEquals(BillingMode.POSTPAID, plan.getBillingMode());
        assertEquals(15, plan.getTrialDays());
        assertEquals(1, plan.getTrialLimitPerUser());
    }

    @Test
    @DisplayName("测试套餐生效时间验证")
    void testEffectiveTimeValidation() {
        LocalDateTime now = LocalDateTime.now();

        // 测试已生效
        plan.setEffectiveTime(now.minusHours(1));
        plan.setExpireTime(null);
        assertTrue(plan.isAvailable());

        // 测试未生效
        plan.setEffectiveTime(now.plusHours(1));
        assertFalse(plan.isAvailable());

        // 测试已过期
        plan.setEffectiveTime(now.minusDays(2));
        plan.setExpireTime(now.minusHours(1));
        assertFalse(plan.isAvailable());

        // 测试在有效期内
        plan.setEffectiveTime(now.minusHours(2));
        plan.setExpireTime(now.plusHours(2));
        assertTrue(plan.isAvailable());
    }

    @Test
    @DisplayName("测试不同状态的套餐可用性")
    void testAvailabilityWithDifferentStatus() {
        LocalDateTime now = LocalDateTime.now();
        plan.setEffectiveTime(now.minusHours(1));
        plan.setExpireTime(null);

        // ACTIVE状态 - 可用
        plan.setStatus(PlanStatus.ACTIVE);
        assertTrue(plan.isAvailable());

        // DRAFT状态 - 不可用
        plan.setStatus(PlanStatus.DRAFT);
        assertFalse(plan.isAvailable());

        // INACTIVE状态 - 不可用
        plan.setStatus(PlanStatus.INACTIVE);
        assertFalse(plan.isAvailable());

        // DEPRECATED状态 - 不可用
        plan.setStatus(PlanStatus.DEPRECATED);
        assertFalse(plan.isAvailable());

        // ARCHIVED状态 - 不可用
        plan.setStatus(PlanStatus.ARCHIVED);
        assertFalse(plan.isAvailable());
    }
}

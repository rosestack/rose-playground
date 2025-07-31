package io.github.rosestack.audit.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 风险等级枚举测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class RiskLevelTest {

    @Test
    void testRiskLevelProperties() {
        assertThat(RiskLevel.LOW.getCode()).isEqualTo("LOW");
        assertThat(RiskLevel.LOW.getDescription()).isEqualTo("低风险");
        assertThat(RiskLevel.LOW.getLevel()).isEqualTo(1);
        
        assertThat(RiskLevel.CRITICAL.getCode()).isEqualTo("CRITICAL");
        assertThat(RiskLevel.CRITICAL.getDescription()).isEqualTo("严重风险");
        assertThat(RiskLevel.CRITICAL.getLevel()).isEqualTo(4);
    }

    @Test
    void testFromCode() {
        assertThat(RiskLevel.fromCode("LOW")).isEqualTo(RiskLevel.LOW);
        assertThat(RiskLevel.fromCode("HIGH")).isEqualTo(RiskLevel.HIGH);
        assertThat(RiskLevel.fromCode("INVALID")).isNull();
    }

    @Test
    void testFromEventType() {
        // 安全事件 -> 严重风险
        assertThat(RiskLevel.fromEventType(AuditEventType.SEC_ATTACK_DETECTION)).isEqualTo(RiskLevel.CRITICAL);
        assertThat(RiskLevel.fromEventType(AuditEventType.SEC_ABNORMAL_BEHAVIOR)).isEqualTo(RiskLevel.CRITICAL);
        
        // 高风险事件 -> 高风险
        assertThat(RiskLevel.fromEventType(AuditEventType.DATA_SENSITIVE_ACCESS)).isEqualTo(RiskLevel.HIGH);
        assertThat(RiskLevel.fromEventType(AuditEventType.DATA_DELETE)).isEqualTo(RiskLevel.HIGH);
        assertThat(RiskLevel.fromEventType(AuditEventType.SYS_CONFIG_CHANGE)).isEqualTo(RiskLevel.HIGH);
        
        // 数据事件 -> 中等风险
        assertThat(RiskLevel.fromEventType(AuditEventType.DATA_CREATE)).isEqualTo(RiskLevel.MEDIUM);
        assertThat(RiskLevel.fromEventType(AuditEventType.DATA_UPDATE)).isEqualTo(RiskLevel.MEDIUM);
        
        // 普通事件 -> 低风险
        assertThat(RiskLevel.fromEventType(AuditEventType.AUTH_LOGIN)).isEqualTo(RiskLevel.LOW);
        assertThat(RiskLevel.fromEventType(AuditEventType.SYS_SERVICE_CONTROL)).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void testHighRiskDetection() {
        assertThat(RiskLevel.LOW.isHighRisk()).isFalse();
        assertThat(RiskLevel.MEDIUM.isHighRisk()).isFalse();
        assertThat(RiskLevel.HIGH.isHighRisk()).isTrue();
        assertThat(RiskLevel.CRITICAL.isHighRisk()).isTrue();
    }

    @Test
    void testAlertNeeds() {
        assertThat(RiskLevel.LOW.needsAlert()).isFalse();
        assertThat(RiskLevel.MEDIUM.needsAlert()).isTrue();
        assertThat(RiskLevel.HIGH.needsAlert()).isTrue();
        assertThat(RiskLevel.CRITICAL.needsAlert()).isTrue();
    }

    @Test
    void testCompareTo() {
        assertThat(RiskLevel.LOW.compareTo(RiskLevel.HIGH)).isLessThan(0);
        assertThat(RiskLevel.HIGH.compareTo(RiskLevel.LOW)).isGreaterThan(0);
        assertThat(RiskLevel.MEDIUM.compareTo(RiskLevel.MEDIUM)).isEqualTo(0);
        
        // 验证排序
        assertThat(RiskLevel.LOW.getLevel()).isLessThan(RiskLevel.MEDIUM.getLevel());
        assertThat(RiskLevel.MEDIUM.getLevel()).isLessThan(RiskLevel.HIGH.getLevel());
        assertThat(RiskLevel.HIGH.getLevel()).isLessThan(RiskLevel.CRITICAL.getLevel());
    }
}
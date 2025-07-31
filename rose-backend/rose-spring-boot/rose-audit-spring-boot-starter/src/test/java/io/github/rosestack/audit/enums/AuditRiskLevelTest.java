package io.github.rosestack.audit.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 风险等级枚举测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AuditRiskLevelTest {

    @Test
    void testRiskLevelProperties() {
        assertThat(AuditRiskLevel.LOW.getCode()).isEqualTo("LOW");
        assertThat(AuditRiskLevel.LOW.getDescription()).isEqualTo("低风险");
        assertThat(AuditRiskLevel.LOW.getLevel()).isEqualTo(1);
        
        assertThat(AuditRiskLevel.CRITICAL.getCode()).isEqualTo("CRITICAL");
        assertThat(AuditRiskLevel.CRITICAL.getDescription()).isEqualTo("严重风险");
        assertThat(AuditRiskLevel.CRITICAL.getLevel()).isEqualTo(4);
    }

    @Test
    void testFromCode() {
        assertThat(AuditRiskLevel.fromCode("LOW")).isEqualTo(AuditRiskLevel.LOW);
        assertThat(AuditRiskLevel.fromCode("HIGH")).isEqualTo(AuditRiskLevel.HIGH);
        assertThat(AuditRiskLevel.fromCode("INVALID")).isNull();
    }

    @Test
    void testFromEventType() {
        // 安全事件 -> 严重风险
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.SEC_ATTACK_DETECTION)).isEqualTo(AuditRiskLevel.CRITICAL);
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.SEC_ABNORMAL_BEHAVIOR)).isEqualTo(AuditRiskLevel.CRITICAL);
        
        // 高风险事件 -> 高风险
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.DATA_SENSITIVE_ACCESS)).isEqualTo(AuditRiskLevel.HIGH);
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.DATA_DELETE)).isEqualTo(AuditRiskLevel.HIGH);
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.SYS_CONFIG_CHANGE)).isEqualTo(AuditRiskLevel.HIGH);
        
        // 数据事件 -> 中等风险
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.DATA_CREATE)).isEqualTo(AuditRiskLevel.MEDIUM);
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.DATA_UPDATE)).isEqualTo(AuditRiskLevel.MEDIUM);
        
        // 普通事件 -> 低风险
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.AUTH_LOGIN)).isEqualTo(AuditRiskLevel.LOW);
        assertThat(AuditRiskLevel.fromEventType(AuditEventType.SYS_SERVICE_CONTROL)).isEqualTo(AuditRiskLevel.LOW);
    }

    @Test
    void testHighRiskDetection() {
        assertThat(AuditRiskLevel.LOW.isHighRisk()).isFalse();
        assertThat(AuditRiskLevel.MEDIUM.isHighRisk()).isFalse();
        assertThat(AuditRiskLevel.HIGH.isHighRisk()).isTrue();
        assertThat(AuditRiskLevel.CRITICAL.isHighRisk()).isTrue();
    }

    @Test
    void testAlertNeeds() {
        assertThat(AuditRiskLevel.LOW.needsAlert()).isFalse();
        assertThat(AuditRiskLevel.MEDIUM.needsAlert()).isTrue();
        assertThat(AuditRiskLevel.HIGH.needsAlert()).isTrue();
        assertThat(AuditRiskLevel.CRITICAL.needsAlert()).isTrue();
    }

    @Test
    void testCompareTo() {
        assertThat(AuditRiskLevel.LOW.compareTo(AuditRiskLevel.HIGH)).isLessThan(0);
        assertThat(AuditRiskLevel.HIGH.compareTo(AuditRiskLevel.LOW)).isGreaterThan(0);
        assertThat(AuditRiskLevel.MEDIUM.compareTo(AuditRiskLevel.MEDIUM)).isEqualTo(0);
        
        // 验证排序
        assertThat(AuditRiskLevel.LOW.getLevel()).isLessThan(AuditRiskLevel.MEDIUM.getLevel());
        assertThat(AuditRiskLevel.MEDIUM.getLevel()).isLessThan(AuditRiskLevel.HIGH.getLevel());
        assertThat(AuditRiskLevel.HIGH.getLevel()).isLessThan(AuditRiskLevel.CRITICAL.getLevel());
    }
}
package io.github.rosestack.audit.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计事件类型枚举测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AuditEventTypeTest {

    @Test
    void testEventTypeAndSubType() {
        AuditEventType loginEvent = AuditEventType.AUTH_LOGIN;
        
        assertThat(loginEvent.getEventType()).isEqualTo("认证");
        assertThat(loginEvent.getEventSubType()).isEqualTo("用户登录");
        assertThat(loginEvent.getEventCode()).isEqualTo("AUTH_LOGIN");
        assertThat(loginEvent.getFullDescription()).isEqualTo("认证 - 用户登录");
    }

    @Test
    void testSecurityEventDetection() {
        assertThat(AuditEventType.SEC_ATTACK_DETECTION.isSecurityEvent()).isTrue();
        assertThat(AuditEventType.SEC_ABNORMAL_BEHAVIOR.isSecurityEvent()).isTrue();
        assertThat(AuditEventType.AUTH_LOGIN.isSecurityEvent()).isFalse();
        assertThat(AuditEventType.DATA_CREATE.isSecurityEvent()).isFalse();
    }

    @Test
    void testHighRiskEventDetection() {
        // 安全事件都是高风险
        assertThat(AuditEventType.SEC_ATTACK_DETECTION.isHighRiskEvent()).isTrue();
        assertThat(AuditEventType.SEC_ABNORMAL_BEHAVIOR.isHighRiskEvent()).isTrue();
        
        // 特定的高风险事件
        assertThat(AuditEventType.DATA_SENSITIVE_ACCESS.isHighRiskEvent()).isTrue();
        assertThat(AuditEventType.DATA_DELETE.isHighRiskEvent()).isTrue();
        assertThat(AuditEventType.SYS_CONFIG_CHANGE.isHighRiskEvent()).isTrue();
        assertThat(AuditEventType.AUTHZ_CHANGE.isHighRiskEvent()).isTrue();
        
        // 普通事件不是高风险
        assertThat(AuditEventType.AUTH_LOGIN.isHighRiskEvent()).isFalse();
        assertThat(AuditEventType.DATA_READ.isHighRiskEvent()).isFalse();
    }

    @Test
    void testAllEventTypesHaveValidProperties() {
        for (AuditEventType eventType : AuditEventType.values()) {
            assertThat(eventType.getEventType()).isNotBlank();
            assertThat(eventType.getEventSubType()).isNotBlank();
            assertThat(eventType.getEventCode()).isNotBlank();
            assertThat(eventType.getFullDescription()).isNotBlank();
        }
    }

    @Test
    void testEventTypeCategories() {
        // 认证类事件
        assertThat(AuditEventType.AUTH_LOGIN.getEventType()).isEqualTo("认证");
        assertThat(AuditEventType.AUTH_LOGOUT.getEventType()).isEqualTo("认证");
        
        // 授权类事件
        assertThat(AuditEventType.AUTHZ_PERMISSION_DENIED.getEventType()).isEqualTo("授权");
        assertThat(AuditEventType.AUTHZ_CHANGE.getEventType()).isEqualTo("授权");
        
        // 数据类事件
        assertThat(AuditEventType.DATA_CREATE.getEventType()).isEqualTo("数据");
        assertThat(AuditEventType.DATA_UPDATE.getEventType()).isEqualTo("数据");
        
        // 系统类事件
        assertThat(AuditEventType.SYS_CONFIG_CHANGE.getEventType()).isEqualTo("系统");
        assertThat(AuditEventType.SYS_SERVICE_CONTROL.getEventType()).isEqualTo("系统");
        
        // 安全类事件
        assertThat(AuditEventType.SEC_ATTACK_DETECTION.getEventType()).isEqualTo("安全");
        assertThat(AuditEventType.SEC_ABNORMAL_BEHAVIOR.getEventType()).isEqualTo("安全");
    }
}
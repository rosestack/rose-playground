package io.github.rosestack.audit.entity;

import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.AuditRiskLevel;
import io.github.rosestack.audit.enums.AuditStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计日志实体测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AuditLogTest {

    @Test
    void testBuilderPattern() {
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .eventTime(now)
                .eventType("认证")
                .eventSubtype("用户登录")
                .operationName("用户登录系统")
                .status("SUCCESS")
                .riskLevel("LOW")
                .userId("user123")
                .userName("张三")
                .requestUri("/api/auth/login")
                .httpMethod("POST")
                .httpStatus(200)
                .clientIp("192.168.1.100")
                .tenantId("tenant001")
                .build();

        assertThat(auditLog.getId()).isEqualTo(1L);
        assertThat(auditLog.getEventTime()).isEqualTo(now);
        assertThat(auditLog.getEventType()).isEqualTo("认证");
        assertThat(auditLog.getEventSubtype()).isEqualTo("用户登录");
        assertThat(auditLog.getOperationName()).isEqualTo("用户登录系统");
        assertThat(auditLog.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditLog.getRiskLevel()).isEqualTo("LOW");
    }

    @Test
    void testSetEventTypeFromEnum() {
        AuditLog auditLog = new AuditLog();
        auditLog.setEventType(AuditEventType.AUTH_LOGIN);

        assertThat(auditLog.getEventType()).isEqualTo("认证");
        assertThat(auditLog.getEventSubtype()).isEqualTo("用户登录");
    }

    @Test
    void testSetStatusFromEnum() {
        AuditLog auditLog = new AuditLog();
        auditLog.setStatus(AuditStatus.SUCCESS);

        assertThat(auditLog.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void testSetRiskLevelFromEnum() {
        AuditLog auditLog = new AuditLog();
        auditLog.setRiskLevel(AuditRiskLevel.HIGH);

        assertThat(auditLog.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    void testGetEventTypeEnum() {
        AuditLog auditLog = new AuditLog();
        auditLog.setEventType(AuditEventType.AUTH_LOGIN);

        AuditEventType eventType = auditLog.getEventTypeEnum();
        assertThat(eventType).isEqualTo(AuditEventType.AUTH_LOGIN);
    }

    @Test
    void testGetStatusEnum() {
        AuditLog auditLog = new AuditLog();
        auditLog.setStatus(AuditStatus.SUCCESS);

        AuditStatus status = auditLog.getStatusEnum();
        assertThat(status).isEqualTo(AuditStatus.SUCCESS);
    }

    @Test
    void testGetRiskLevelEnum() {
        AuditLog auditLog = new AuditLog();
        auditLog.setRiskLevel(AuditRiskLevel.HIGH);

        AuditRiskLevel auditRiskLevel = auditLog.getRiskLevelEnum();
        assertThat(auditRiskLevel).isEqualTo(AuditRiskLevel.HIGH);
    }

    @Test
    void testIsSuccess() {
        AuditLog successLog = new AuditLog();
        successLog.setStatus(AuditStatus.SUCCESS);
        assertThat(successLog.isSuccess()).isTrue();

        AuditLog failureLog = new AuditLog();
        failureLog.setStatus(AuditStatus.FAILURE);
        assertThat(failureLog.isSuccess()).isFalse();
    }

    @Test
    void testIsFailure() {
        AuditLog failureLog = new AuditLog();
        failureLog.setStatus(AuditStatus.FAILURE);
        assertThat(failureLog.isFailure()).isTrue();

        AuditLog successLog = new AuditLog();
        successLog.setStatus(AuditStatus.SUCCESS);
        assertThat(successLog.isFailure()).isFalse();
    }

    @Test
    void testIsHighRisk() {
        AuditLog highRiskLog = new AuditLog();
        highRiskLog.setRiskLevel(AuditRiskLevel.HIGH);
        assertThat(highRiskLog.isHighRisk()).isTrue();

        AuditLog lowRiskLog = new AuditLog();
        lowRiskLog.setRiskLevel(AuditRiskLevel.LOW);
        assertThat(lowRiskLog.isHighRisk()).isFalse();
    }

    @Test
    void testNeedsAlert() {
        AuditLog mediumRiskLog = new AuditLog();
        mediumRiskLog.setRiskLevel(AuditRiskLevel.MEDIUM);
        assertThat(mediumRiskLog.needsAlert()).isTrue();

        AuditLog lowRiskLog = new AuditLog();
        lowRiskLog.setRiskLevel(AuditRiskLevel.LOW);
        assertThat(lowRiskLog.needsAlert()).isFalse();
    }

    @Test
    void testNullSafetyInEnumMethods() {
        AuditLog auditLog = new AuditLog();
        
        // 测试空值安全性
        auditLog.setEventType((AuditEventType) null);
        assertThat(auditLog.getEventType()).isNull();
        assertThat(auditLog.getEventSubtype()).isNull();

        auditLog.setStatus((AuditStatus) null);
        assertThat(auditLog.getStatus()).isNull();

        auditLog.setRiskLevel((AuditRiskLevel) null);
        assertThat(auditLog.getRiskLevel()).isNull();
    }

    @Test
    void testCompleteAuditLogExample() {
        // 创建一个完整的审计日志示例
        AuditLog auditLog = AuditLog.builder()
                .eventTime(LocalDateTime.now())
                .operationName("用户登录系统")
                .userId("user123")
                .userName("张三")
                .requestUri("/api/auth/login")
                .httpMethod("POST")
                .httpStatus(200)
                .sessionId("SESS_789456123")
                .clientIp("192.168.1.100")
                .serverIp("10.0.0.5")
                .geoLocation("北京市")
                .userAgent("Chrome/120.0 Windows")
                .appName("UserApp")
                .tenantId("tenant001")
                .traceId("trace-abc-123")
                .executionTime(1200L)
                .build();

        // 使用枚举设置类型
        auditLog.setEventType(AuditEventType.AUTH_LOGIN);
        auditLog.setStatus(AuditStatus.SUCCESS);
        auditLog.setRiskLevel(AuditRiskLevel.LOW);

        // 验证设置结果
        assertThat(auditLog.getEventType()).isEqualTo("认证");
        assertThat(auditLog.getEventSubtype()).isEqualTo("用户登录");
        assertThat(auditLog.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditLog.getRiskLevel()).isEqualTo("LOW");
        
        // 验证业务方法
        assertThat(auditLog.isSuccess()).isTrue();
        assertThat(auditLog.isFailure()).isFalse();
        assertThat(auditLog.isHighRisk()).isFalse();
        assertThat(auditLog.needsAlert()).isFalse();
    }
}
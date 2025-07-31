package io.github.rosestack.audit.entity;

import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditDetailType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计日志详情实体测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AuditLogDetailTest {

    @Test
    void testBuilderPattern() {
        LocalDateTime now = LocalDateTime.now();
        
        AuditLogDetail detail = AuditLogDetail.builder()
                .id(1L)
                .auditLogId(1001L)
                .detailType("HTTP请求相关")
                .detailKey("REQUEST_PARAMS")
                .detailValue("{\"userId\":\"123\",\"pageSize\":20}")
                .isSensitive(true)
                .isEncrypted(false)
                .tenantId("tenant001")
                .createdAt(now)
                .build();

        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getAuditLogId()).isEqualTo(1001L);
        assertThat(detail.getDetailType()).isEqualTo("HTTP请求相关");
        assertThat(detail.getDetailKey()).isEqualTo("REQUEST_PARAMS");
        assertThat(detail.getDetailValue()).isEqualTo("{\"userId\":\"123\",\"pageSize\":20}");
        assertThat(detail.getIsSensitive()).isTrue();
        assertThat(detail.getIsEncrypted()).isFalse();
    }

    @Test
    void testSetDetailTypeFromEnum() {
        AuditLogDetail detail = new AuditLogDetail();
        detail.setDetailType(AuditDetailType.HTTP_REQUEST);

        assertThat(detail.getDetailType()).isEqualTo("HTTP请求相关");
    }

    @Test
    void testSetDetailKeyFromEnum() {
        AuditLogDetail detail = new AuditLogDetail();
        detail.setDetailKey(AuditDetailKey.REQUEST_PARAMS);

        assertThat(detail.getDetailKey()).isEqualTo("REQUEST_PARAMS");
        assertThat(detail.getIsSensitive()).isTrue(); // REQUEST_PARAMS 是敏感数据
    }

    @Test
    void testGetDetailTypeEnum() {
        AuditLogDetail detail = new AuditLogDetail();
        detail.setDetailType(AuditDetailType.HTTP_REQUEST);

        AuditDetailType detailType = detail.getDetailTypeEnum();
        assertThat(detailType).isEqualTo(AuditDetailType.HTTP_REQUEST);
    }

    @Test
    void testGetDetailKeyEnum() {
        AuditLogDetail detail = new AuditLogDetail();
        detail.setDetailKey(AuditDetailKey.REQUEST_PARAMS);

        AuditDetailKey detailKey = detail.getDetailKeyEnum();
        assertThat(detailKey).isEqualTo(AuditDetailKey.REQUEST_PARAMS);
    }

    @Test
    void testNeedsEncryption() {
        AuditLogDetail sensitiveDetail = new AuditLogDetail();
        sensitiveDetail.setDetailKey(AuditDetailKey.SECURITY_CONTEXT);
        assertThat(sensitiveDetail.needsEncryption()).isTrue();

        AuditLogDetail normalDetail = new AuditLogDetail();
        normalDetail.setDetailKey(AuditDetailKey.PERFORMANCE_METRICS);
        assertThat(normalDetail.needsEncryption()).isFalse();
    }

    @Test
    void testNeedsMasking() {
        AuditLogDetail sensitiveDetail = new AuditLogDetail();
        sensitiveDetail.setDetailKey(AuditDetailKey.REQUEST_PARAMS);
        assertThat(sensitiveDetail.needsMasking()).isTrue();

        AuditLogDetail normalDetail = new AuditLogDetail();
        normalDetail.setDetailKey(AuditDetailKey.RESPONSE_RESULT);
        assertThat(normalDetail.needsMasking()).isFalse();
    }

    @Test
    void testIsHttpRelated() {
        AuditLogDetail httpDetail = new AuditLogDetail();
        httpDetail.setDetailType(AuditDetailType.HTTP_REQUEST);
        assertThat(httpDetail.isHttpRelated()).isTrue();

        AuditLogDetail securityDetail = new AuditLogDetail();
        securityDetail.setDetailType(AuditDetailType.SECURITY);
        assertThat(securityDetail.isHttpRelated()).isFalse();
    }

    @Test
    void testIsDataChangeRelated() {
        AuditLogDetail dataChangeDetail = new AuditLogDetail();
        dataChangeDetail.setDetailType(AuditDetailType.DATA_CHANGE);
        assertThat(dataChangeDetail.isDataChangeRelated()).isTrue();

        AuditLogDetail httpDetail = new AuditLogDetail();
        httpDetail.setDetailType(AuditDetailType.HTTP_REQUEST);
        assertThat(httpDetail.isDataChangeRelated()).isFalse();
    }

    @Test
    void testIsSecurityRelated() {
        AuditLogDetail securityDetail = new AuditLogDetail();
        securityDetail.setDetailType(AuditDetailType.SECURITY);
        assertThat(securityDetail.isSecurityRelated()).isTrue();

        AuditLogDetail httpDetail = new AuditLogDetail();
        httpDetail.setDetailType(AuditDetailType.HTTP_REQUEST);
        assertThat(httpDetail.isSecurityRelated()).isFalse();
    }

    @Test
    void testCreateHttpDetail() {
        AuditLogDetail detail = AuditLogDetail.createHttpDetail(
                1001L, 
                AuditDetailKey.REQUEST_PARAMS, 
                "{\"userId\":\"123\"}"
        );

        assertThat(detail.getAuditLogId()).isEqualTo(1001L);
        assertThat(detail.getDetailType()).isEqualTo("HTTP请求相关");
        assertThat(detail.getDetailKey()).isEqualTo("REQUEST_PARAMS");
        assertThat(detail.getDetailValue()).isEqualTo("{\"userId\":\"123\"}");
        assertThat(detail.getIsSensitive()).isTrue();
        assertThat(detail.getIsEncrypted()).isFalse();
    }

    @Test
    void testCreateDataChangeDetail() {
        AuditLogDetail detail = AuditLogDetail.createDataChangeDetail(
                1001L, 
                AuditDetailKey.DATA_CHANGE_BEFORE, 
                "{\"status\":\"PENDING\"}"
        );

        assertThat(detail.getAuditLogId()).isEqualTo(1001L);
        assertThat(detail.getDetailType()).isEqualTo("数据变更相关");
        assertThat(detail.getDetailKey()).isEqualTo("DATA_CHANGE_BEFORE");
        assertThat(detail.getDetailValue()).isEqualTo("{\"status\":\"PENDING\"}");
        assertThat(detail.getIsSensitive()).isTrue();
        assertThat(detail.getIsEncrypted()).isFalse();
    }

    @Test
    void testCreateSecurityDetail() {
        AuditLogDetail detail = AuditLogDetail.createSecurityDetail(
                1001L, 
                AuditDetailKey.SECURITY_CONTEXT, 
                "{\"riskScore\":85}"
        );

        assertThat(detail.getAuditLogId()).isEqualTo(1001L);
        assertThat(detail.getDetailType()).isEqualTo("安全相关");
        assertThat(detail.getDetailKey()).isEqualTo("SECURITY_CONTEXT");
        assertThat(detail.getDetailValue()).isEqualTo("{\"riskScore\":85}");
        assertThat(detail.getIsSensitive()).isTrue();
        assertThat(detail.getIsEncrypted()).isTrue(); // 安全相关的敏感数据需要加密
    }

    @Test
    void testCreateSystemDetail() {
        AuditLogDetail detail = AuditLogDetail.createSystemDetail(
                1001L, 
                AuditDetailKey.PERFORMANCE_METRICS, 
                "{\"responseTime\":120}"
        );

        assertThat(detail.getAuditLogId()).isEqualTo(1001L);
        assertThat(detail.getDetailType()).isEqualTo("系统技术相关");
        assertThat(detail.getDetailKey()).isEqualTo("PERFORMANCE_METRICS");
        assertThat(detail.getDetailValue()).isEqualTo("{\"responseTime\":120}");
        assertThat(detail.getIsSensitive()).isFalse();
        assertThat(detail.getIsEncrypted()).isFalse();
    }

    @Test
    void testCreateOperationDetail() {
        AuditLogDetail detail = AuditLogDetail.createOperationDetail(
                1001L, 
                AuditDetailKey.TARGET_INFO, 
                "{\"tableName\":\"users\"}"
        );

        assertThat(detail.getAuditLogId()).isEqualTo(1001L);
        assertThat(detail.getDetailType()).isEqualTo("操作对象相关");
        assertThat(detail.getDetailKey()).isEqualTo("TARGET_INFO");
        assertThat(detail.getDetailValue()).isEqualTo("{\"tableName\":\"users\"}");
        assertThat(detail.getIsSensitive()).isFalse();
        assertThat(detail.getIsEncrypted()).isFalse();
    }

    @Test
    void testNullSafetyInEnumMethods() {
        AuditLogDetail detail = new AuditLogDetail();
        
        // 测试空值安全性
        detail.setDetailType((AuditDetailType) null);
        assertThat(detail.getDetailType()).isNull();

        detail.setDetailKey((AuditDetailKey) null);
        assertThat(detail.getDetailKey()).isNull();
        assertThat(detail.getIsSensitive()).isNull();
    }
}
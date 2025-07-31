package io.github.rosestack.audit.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计详情键枚举测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class AuditDetailKeyTest {

    @Test
    void testDetailKeyProperties() {
        AuditDetailKey requestParams = AuditDetailKey.REQUEST_PARAMS;
        
        assertThat(requestParams.getCode()).isEqualTo("REQUEST_PARAMS");
        assertThat(requestParams.getDescription()).isEqualTo("HTTP请求参数");
        assertThat(requestParams.getDetailType()).isEqualTo(AuditDetailType.HTTP_REQUEST);
        assertThat(requestParams.isSensitive()).isTrue();
    }

    @Test
    void testFromCode() {
        assertThat(AuditDetailKey.fromCode("REQUEST_PARAMS")).isEqualTo(AuditDetailKey.REQUEST_PARAMS);
        assertThat(AuditDetailKey.fromCode("INVALID_CODE")).isNull();
    }

    @Test
    void testGetKeysByType() {
        AuditDetailKey[] httpKeys = AuditDetailKey.getKeysByType(AuditDetailType.HTTP_REQUEST);
        
        assertThat(httpKeys).contains(
                AuditDetailKey.REQUEST_PARAMS,
                AuditDetailKey.REQUEST_BODY,
                AuditDetailKey.REQUEST_HEADERS,
                AuditDetailKey.RESPONSE_RESULT
        );
        
        // 验证所有返回的键都属于指定类型
        for (AuditDetailKey key : httpKeys) {
            assertThat(key.getDetailType()).isEqualTo(AuditDetailType.HTTP_REQUEST);
        }
    }

    @Test
    void testSensitiveDataDetection() {
        // 敏感数据
        assertThat(AuditDetailKey.REQUEST_PARAMS.isSensitive()).isTrue();
        assertThat(AuditDetailKey.REQUEST_BODY.isSensitive()).isTrue();
        assertThat(AuditDetailKey.SECURITY_CONTEXT.isSensitive()).isTrue();
        assertThat(AuditDetailKey.DATA_CHANGE_BEFORE.isSensitive()).isTrue();
        
        // 非敏感数据
        assertThat(AuditDetailKey.REQUEST_HEADERS.isSensitive()).isFalse();
        assertThat(AuditDetailKey.RESPONSE_RESULT.isSensitive()).isFalse();
        assertThat(AuditDetailKey.PERFORMANCE_METRICS.isSensitive()).isFalse();
    }

    @Test
    void testEncryptionAndMaskingNeeds() {
        AuditDetailKey sensitiveKey = AuditDetailKey.SECURITY_CONTEXT;
        AuditDetailKey normalKey = AuditDetailKey.PERFORMANCE_METRICS;
        
        assertThat(sensitiveKey.needsEncryption()).isTrue();
        assertThat(sensitiveKey.needsMasking()).isTrue();
        
        assertThat(normalKey.needsEncryption()).isFalse();
        assertThat(normalKey.needsMasking()).isFalse();
    }

    @Test
    void testAllDetailTypesHaveKeys() {
        for (AuditDetailType detailType : AuditDetailType.values()) {
            AuditDetailKey[] keys = AuditDetailKey.getKeysByType(detailType);
            assertThat(keys).isNotEmpty()
                    .as("详情类型 %s 应该有对应的详情键", detailType.getCode());
        }
    }

    @Test
    void testAllKeysHaveValidProperties() {
        for (AuditDetailKey key : AuditDetailKey.values()) {
            assertThat(key.getCode()).isNotBlank();
            assertThat(key.getDescription()).isNotBlank();
            assertThat(key.getDetailType()).isNotNull();
        }
    }
}
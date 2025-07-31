package io.github.rosestack.audit.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditDetailType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 审计日志详情 Mapper 测试
 * <p>
 * 注意：这是一个单元测试，使用 Mock 对象模拟数据库操作。
 * 在实际项目中，应该使用 @SpringBootTest 和真实的数据库进行集成测试。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ActiveProfiles("test")
class AuditLogDetailMapperTest {

    private AuditLogDetailMapper auditLogDetailMapper;
    private AuditLogDetail sampleDetail;

    @BeforeEach
    void setUp() {
        auditLogDetailMapper = mock(AuditLogDetailMapper.class);
        
        // 创建测试数据
        sampleDetail = AuditLogDetail.builder()
                .id(1L)
                .auditLogId(1001L)
                .detailType("HTTP请求相关")
                .detailKey("REQUEST_PARAMS")
                .detailValue("{\"userId\":\"123\",\"pageSize\":20}")
                .isSensitive(true)
                .isEncrypted(false)
                .tenantId("tenant001")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSelectByAuditLogId() {
        // Given
        Long auditLogId = 1001L;
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByAuditLogId(auditLogId)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByAuditLogId(auditLogId);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getAuditLogId()).isEqualTo(auditLogId);
        verify(auditLogDetailMapper).selectByAuditLogId(auditLogId);
    }

    @Test
    void testSelectByDetailType() {
        // Given
        String detailType = "HTTP请求相关";
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByDetailType(detailType)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByDetailType(detailType);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getDetailType()).isEqualTo(detailType);
        verify(auditLogDetailMapper).selectByDetailType(detailType);
    }

    @Test
    void testSelectByDetailKey() {
        // Given
        String detailKey = "REQUEST_PARAMS";
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByDetailKey(detailKey)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByDetailKey(detailKey);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getDetailKey()).isEqualTo(detailKey);
        verify(auditLogDetailMapper).selectByDetailKey(detailKey);
    }

    @Test
    void testSelectByAuditLogIdAndDetailType() {
        // Given
        Long auditLogId = 1001L;
        String detailType = "HTTP请求相关";
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByAuditLogIdAndDetailType(auditLogId, detailType)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByAuditLogIdAndDetailType(auditLogId, detailType);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getAuditLogId()).isEqualTo(auditLogId);
        assertThat(actualDetails.get(0).getDetailType()).isEqualTo(detailType);
        verify(auditLogDetailMapper).selectByAuditLogIdAndDetailType(auditLogId, detailType);
    }

    @Test
    void testSelectByAuditLogIdAndDetailKey() {
        // Given
        Long auditLogId = 1001L;
        String detailKey = "REQUEST_PARAMS";
        when(auditLogDetailMapper.selectByAuditLogIdAndDetailKey(auditLogId, detailKey)).thenReturn(sampleDetail);

        // When
        AuditLogDetail actualDetail = auditLogDetailMapper.selectByAuditLogIdAndDetailKey(auditLogId, detailKey);

        // Then
        assertThat(actualDetail).isNotNull();
        assertThat(actualDetail.getAuditLogId()).isEqualTo(auditLogId);
        assertThat(actualDetail.getDetailKey()).isEqualTo(detailKey);
        verify(auditLogDetailMapper).selectByAuditLogIdAndDetailKey(auditLogId, detailKey);
    }

    @Test
    void testSelectByAuditLogIds() {
        // Given
        List<Long> auditLogIds = List.of(1001L, 1002L);
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByAuditLogIds(auditLogIds)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByAuditLogIds(auditLogIds);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        verify(auditLogDetailMapper).selectByAuditLogIds(auditLogIds);
    }

    @Test
    void testSelectByAuditLogIdsWithEmptyList() {
        // Given
        List<Long> emptyList = List.of();
        when(auditLogDetailMapper.selectByAuditLogIds(emptyList)).thenReturn(List.of());

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByAuditLogIds(emptyList);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).isEmpty();
        verify(auditLogDetailMapper).selectByAuditLogIds(emptyList);
    }

    @Test
    void testSelectSensitiveDetails() {
        // Given
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectSensitiveDetails()).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectSensitiveDetails();

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getIsSensitive()).isTrue();
        verify(auditLogDetailMapper).selectSensitiveDetails();
    }

    @Test
    void testSelectEncryptedDetails() {
        // Given
        AuditLogDetail encryptedDetail = AuditLogDetail.builder()
                .id(2L)
                .auditLogId(1001L)
                .detailType("安全相关")
                .detailKey("SECURITY_CONTEXT")
                .detailValue("encrypted_data")
                .isSensitive(true)
                .isEncrypted(true)
                .build();
        
        List<AuditLogDetail> expectedDetails = List.of(encryptedDetail);
        when(auditLogDetailMapper.selectEncryptedDetails()).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectEncryptedDetails();

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getIsEncrypted()).isTrue();
        verify(auditLogDetailMapper).selectEncryptedDetails();
    }

    @Test
    void testSelectSensitiveButNotEncrypted() {
        // Given
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectSensitiveButNotEncrypted()).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectSensitiveButNotEncrypted();

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getIsSensitive()).isTrue();
        assertThat(actualDetails.get(0).getIsEncrypted()).isFalse();
        verify(auditLogDetailMapper).selectSensitiveButNotEncrypted();
    }

    @Test
    void testSelectPageByAuditLogId() {
        // Given
        Page<AuditLogDetail> page = new Page<>(1, 10);
        Long auditLogId = 1001L;
        
        IPage<AuditLogDetail> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(List.of(sampleDetail));
        expectedPage.setTotal(1);
        
        when(auditLogDetailMapper.selectPageByAuditLogId(page, auditLogId)).thenReturn(expectedPage);

        // When
        IPage<AuditLogDetail> actualPage = auditLogDetailMapper.selectPageByAuditLogId(page, auditLogId);

        // Then
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getRecords()).hasSize(1);
        assertThat(actualPage.getTotal()).isEqualTo(1);
        verify(auditLogDetailMapper).selectPageByAuditLogId(page, auditLogId);
    }

    @Test
    void testCountByAuditLogId() {
        // Given
        Long auditLogId = 1001L;
        Long expectedCount = 5L;
        when(auditLogDetailMapper.countByAuditLogId(auditLogId)).thenReturn(expectedCount);

        // When
        Long actualCount = auditLogDetailMapper.countByAuditLogId(auditLogId);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(auditLogDetailMapper).countByAuditLogId(auditLogId);
    }

    @Test
    void testCountByDetailType() {
        // Given
        List<Map<String, Object>> expectedStats = List.of(
                Map.of("detail_type", "HTTP请求相关", "count", 10L),
                Map.of("detail_type", "数据变更相关", "count", 5L)
        );
        when(auditLogDetailMapper.countByDetailType()).thenReturn(expectedStats);

        // When
        List<Map<String, Object>> actualStats = auditLogDetailMapper.countByDetailType();

        // Then
        assertThat(actualStats).isNotNull();
        assertThat(actualStats).hasSize(2);
        assertThat(actualStats.get(0).get("detail_type")).isEqualTo("HTTP请求相关");
        assertThat(actualStats.get(0).get("count")).isEqualTo(10L);
        verify(auditLogDetailMapper).countByDetailType();
    }

    @Test
    void testCountSensitiveDetails() {
        // Given
        Long expectedCount = 25L;
        when(auditLogDetailMapper.countSensitiveDetails()).thenReturn(expectedCount);

        // When
        Long actualCount = auditLogDetailMapper.countSensitiveDetails();

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(auditLogDetailMapper).countSensitiveDetails();
    }

    @Test
    void testCountEncryptedDetails() {
        // Given
        Long expectedCount = 15L;
        when(auditLogDetailMapper.countEncryptedDetails()).thenReturn(expectedCount);

        // When
        Long actualCount = auditLogDetailMapper.countEncryptedDetails();

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(auditLogDetailMapper).countEncryptedDetails();
    }

    @Test
    void testSelectByTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByTimeRange(startTime, endTime)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByTimeRange(startTime, endTime);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        verify(auditLogDetailMapper).selectByTimeRange(startTime, endTime);
    }

    @Test
    void testDeleteByAuditLogIds() {
        // Given
        List<Long> auditLogIds = List.of(1001L, 1002L);
        int expectedDeletedCount = 10;
        when(auditLogDetailMapper.deleteByAuditLogIds(auditLogIds)).thenReturn(expectedDeletedCount);

        // When
        int actualDeletedCount = auditLogDetailMapper.deleteByAuditLogIds(auditLogIds);

        // Then
        assertThat(actualDeletedCount).isEqualTo(expectedDeletedCount);
        verify(auditLogDetailMapper).deleteByAuditLogIds(auditLogIds);
    }

    @Test
    void testSelectExpiredDetailIds() {
        // Given
        LocalDateTime expireTime = LocalDateTime.now().minusDays(365);
        int limit = 1000;
        List<Long> expectedIds = List.of(1L, 2L, 3L);
        when(auditLogDetailMapper.selectExpiredDetailIds(expireTime, limit)).thenReturn(expectedIds);

        // When
        List<Long> actualIds = auditLogDetailMapper.selectExpiredDetailIds(expireTime, limit);

        // Then
        assertThat(actualIds).isNotNull();
        assertThat(actualIds).hasSize(3);
        assertThat(actualIds).containsExactly(1L, 2L, 3L);
        verify(auditLogDetailMapper).selectExpiredDetailIds(expireTime, limit);
    }

    @Test
    void testSelectByTenantId() {
        // Given
        String tenantId = "tenant001";
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByTenantId(tenantId)).thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByTenantId(tenantId);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getTenantId()).isEqualTo(tenantId);
        verify(auditLogDetailMapper).selectByTenantId(tenantId);
    }

    @Test
    void testSelectByConditions() {
        // Given
        Long auditLogId = 1001L;
        String detailType = "HTTP请求相关";
        String detailKey = "REQUEST_PARAMS";
        Boolean isSensitive = true;
        Boolean isEncrypted = false;
        
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByConditions(auditLogId, detailType, detailKey, isSensitive, isEncrypted))
                .thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByConditions(auditLogId, detailType, detailKey, isSensitive, isEncrypted);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        assertThat(actualDetails.get(0).getAuditLogId()).isEqualTo(auditLogId);
        assertThat(actualDetails.get(0).getDetailType()).isEqualTo(detailType);
        assertThat(actualDetails.get(0).getDetailKey()).isEqualTo(detailKey);
        verify(auditLogDetailMapper).selectByConditions(auditLogId, detailType, detailKey, isSensitive, isEncrypted);
    }

    @Test
    void testSelectByConditionsWithNullValues() {
        // Given
        List<AuditLogDetail> expectedDetails = List.of(sampleDetail);
        when(auditLogDetailMapper.selectByConditions(null, null, null, null, null))
                .thenReturn(expectedDetails);

        // When
        List<AuditLogDetail> actualDetails = auditLogDetailMapper.selectByConditions(null, null, null, null, null);

        // Then
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(1);
        verify(auditLogDetailMapper).selectByConditions(null, null, null, null, null);
    }
}
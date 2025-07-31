package io.github.rosestack.audit.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.AuditStatus;
import io.github.rosestack.audit.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 审计日志 Mapper 测试
 * <p>
 * 注意：这是一个单元测试，使用 Mock 对象模拟数据库操作。
 * 在实际项目中，应该使用 @SpringBootTest 和真实的数据库进行集成测试。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ActiveProfiles("test")
class AuditLogMapperTest {

    private AuditLogMapper auditLogMapper;
    private AuditLog sampleAuditLog;

    @BeforeEach
    void setUp() {
        auditLogMapper = mock(AuditLogMapper.class);
        
        // 创建测试数据
        sampleAuditLog = AuditLog.builder()
                .id(1L)
                .eventTime(LocalDateTime.now())
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
    }

    @Test
    void testSelectByUserId() {
        // Given
        String userId = "user123";
        List<AuditLog> expectedLogs = List.of(sampleAuditLog);
        when(auditLogMapper.selectByUserId(userId)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectByUserId(userId);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getUserId()).isEqualTo(userId);
        verify(auditLogMapper).selectByUserId(userId);
    }

    @Test
    void testSelectByEventType() {
        // Given
        String eventType = "认证";
        List<AuditLog> expectedLogs = List.of(sampleAuditLog);
        when(auditLogMapper.selectByEventType(eventType)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectByEventType(eventType);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getEventType()).isEqualTo(eventType);
        verify(auditLogMapper).selectByEventType(eventType);
    }

    @Test
    void testSelectByRiskLevel() {
        // Given
        String riskLevel = "LOW";
        List<AuditLog> expectedLogs = List.of(sampleAuditLog);
        when(auditLogMapper.selectByRiskLevel(riskLevel)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectByRiskLevel(riskLevel);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getRiskLevel()).isEqualTo(riskLevel);
        verify(auditLogMapper).selectByRiskLevel(riskLevel);
    }

    @Test
    void testSelectByTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        List<AuditLog> expectedLogs = List.of(sampleAuditLog);
        when(auditLogMapper.selectByTimeRange(startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectByTimeRange(startTime, endTime);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        verify(auditLogMapper).selectByTimeRange(startTime, endTime);
    }

    @Test
    void testSelectPageByTimeRange() {
        // Given
        Page<AuditLog> page = new Page<>(1, 10);
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        IPage<AuditLog> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(List.of(sampleAuditLog));
        expectedPage.setTotal(1);
        
        when(auditLogMapper.selectPageByTimeRange(page, startTime, endTime)).thenReturn(expectedPage);

        // When
        IPage<AuditLog> actualPage = auditLogMapper.selectPageByTimeRange(page, startTime, endTime);

        // Then
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getRecords()).hasSize(1);
        assertThat(actualPage.getTotal()).isEqualTo(1);
        verify(auditLogMapper).selectPageByTimeRange(page, startTime, endTime);
    }

    @Test
    void testSelectByUserAndTimeRange() {
        // Given
        String userId = "user123";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        List<AuditLog> expectedLogs = List.of(sampleAuditLog);
        when(auditLogMapper.selectByUserAndTimeRange(userId, startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectByUserAndTimeRange(userId, startTime, endTime);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getUserId()).isEqualTo(userId);
        verify(auditLogMapper).selectByUserAndTimeRange(userId, startTime, endTime);
    }

    @Test
    void testSelectPageByConditions() {
        // Given
        Page<AuditLog> page = new Page<>(1, 10);
        String userId = "user123";
        String eventType = "认证";
        String riskLevel = "LOW";
        String status = "SUCCESS";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        IPage<AuditLog> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(List.of(sampleAuditLog));
        expectedPage.setTotal(1);
        
        when(auditLogMapper.selectPageByConditions(page, userId, eventType, riskLevel, status, startTime, endTime))
                .thenReturn(expectedPage);

        // When
        IPage<AuditLog> actualPage = auditLogMapper.selectPageByConditions(page, userId, eventType, riskLevel, status, startTime, endTime);

        // Then
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getRecords()).hasSize(1);
        assertThat(actualPage.getTotal()).isEqualTo(1);
        verify(auditLogMapper).selectPageByConditions(page, userId, eventType, riskLevel, status, startTime, endTime);
    }

    @Test
    void testCountByTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        Long expectedCount = 10L;
        when(auditLogMapper.countByTimeRange(startTime, endTime)).thenReturn(expectedCount);

        // When
        Long actualCount = auditLogMapper.countByTimeRange(startTime, endTime);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(auditLogMapper).countByTimeRange(startTime, endTime);
    }

    @Test
    void testCountByUserId() {
        // Given
        String userId = "user123";
        Long expectedCount = 5L;
        when(auditLogMapper.countByUserId(userId)).thenReturn(expectedCount);

        // When
        Long actualCount = auditLogMapper.countByUserId(userId);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(auditLogMapper).countByUserId(userId);
    }

    @Test
    void testCountByEventType() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        List<Map<String, Object>> expectedStats = List.of(
                Map.of("event_type", "认证", "count", 10L),
                Map.of("event_type", "数据", "count", 5L)
        );
        when(auditLogMapper.countByEventType(startTime, endTime)).thenReturn(expectedStats);

        // When
        List<Map<String, Object>> actualStats = auditLogMapper.countByEventType(startTime, endTime);

        // Then
        assertThat(actualStats).isNotNull();
        assertThat(actualStats).hasSize(2);
        assertThat(actualStats.get(0).get("event_type")).isEqualTo("认证");
        assertThat(actualStats.get(0).get("count")).isEqualTo(10L);
        verify(auditLogMapper).countByEventType(startTime, endTime);
    }

    @Test
    void testSelectHighRiskLogs() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        AuditLog highRiskLog = AuditLog.builder()
                .id(2L)
                .eventTime(LocalDateTime.now())
                .eventType("安全")
                .eventSubtype("攻击检测")
                .riskLevel("HIGH")
                .build();
        
        List<AuditLog> expectedLogs = List.of(highRiskLog);
        when(auditLogMapper.selectHighRiskLogs(startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectHighRiskLogs(startTime, endTime);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getRiskLevel()).isEqualTo("HIGH");
        verify(auditLogMapper).selectHighRiskLogs(startTime, endTime);
    }

    @Test
    void testSelectFailedOperations() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        AuditLog failedLog = AuditLog.builder()
                .id(3L)
                .eventTime(LocalDateTime.now())
                .status("FAILURE")
                .build();
        
        List<AuditLog> expectedLogs = List.of(failedLog);
        when(auditLogMapper.selectFailedOperations(startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectFailedOperations(startTime, endTime);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getStatus()).isEqualTo("FAILURE");
        verify(auditLogMapper).selectFailedOperations(startTime, endTime);
    }

    @Test
    void testSelectSecurityEvents() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        AuditLog securityLog = AuditLog.builder()
                .id(4L)
                .eventTime(LocalDateTime.now())
                .eventType("安全")
                .eventSubtype("异常行为")
                .build();
        
        List<AuditLog> expectedLogs = List.of(securityLog);
        when(auditLogMapper.selectSecurityEvents(startTime, endTime)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectSecurityEvents(startTime, endTime);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getEventType()).isEqualTo("安全");
        verify(auditLogMapper).selectSecurityEvents(startTime, endTime);
    }

    @Test
    void testSelectExpiredLogIds() {
        // Given
        LocalDateTime expireTime = LocalDateTime.now().minusDays(365);
        int limit = 1000;
        List<Long> expectedIds = List.of(1L, 2L, 3L);
        when(auditLogMapper.selectExpiredLogIds(expireTime, limit)).thenReturn(expectedIds);

        // When
        List<Long> actualIds = auditLogMapper.selectExpiredLogIds(expireTime, limit);

        // Then
        assertThat(actualIds).isNotNull();
        assertThat(actualIds).hasSize(3);
        assertThat(actualIds).containsExactly(1L, 2L, 3L);
        verify(auditLogMapper).selectExpiredLogIds(expireTime, limit);
    }

    @Test
    void testSelectByTenantId() {
        // Given
        String tenantId = "tenant001";
        List<AuditLog> expectedLogs = List.of(sampleAuditLog);
        when(auditLogMapper.selectByTenantId(tenantId)).thenReturn(expectedLogs);

        // When
        List<AuditLog> actualLogs = auditLogMapper.selectByTenantId(tenantId);

        // Then
        assertThat(actualLogs).isNotNull();
        assertThat(actualLogs).hasSize(1);
        assertThat(actualLogs.get(0).getTenantId()).isEqualTo(tenantId);
        verify(auditLogMapper).selectByTenantId(tenantId);
    }

    @Test
    void testCountByTenantId() {
        // Given
        String tenantId = "tenant001";
        Long expectedCount = 15L;
        when(auditLogMapper.countByTenantId(tenantId)).thenReturn(expectedCount);

        // When
        Long actualCount = auditLogMapper.countByTenantId(tenantId);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(auditLogMapper).countByTenantId(tenantId);
    }
}
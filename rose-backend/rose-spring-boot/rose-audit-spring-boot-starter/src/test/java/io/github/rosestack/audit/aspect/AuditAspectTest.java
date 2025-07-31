package io.github.rosestack.audit.aspect;

import io.github.rosestack.audit.annotation.Audit;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.RiskLevel;
import io.github.rosestack.audit.properties.AuditProperties;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 审计切面测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuditLogDetailService auditLogDetailService;

    @Mock
    private AuditProperties auditProperties;

    @InjectMocks
    private AuditAspect auditAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = new TestService();
        
        // 设置默认配置
        when(auditProperties.isEnabled()).thenReturn(true);
        
        // 设置方法签名
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"testParam"});
    }

    @Test
    void testAuditAspectWithEnabledAudit() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("testResult");
        when(auditLogService.recordAuditLogAsync(any(AuditLog.class)))
                .thenReturn(CompletableFuture.completedFuture(new AuditLog()));

        // When
        Object result = auditAspect.around(joinPoint, audit);

        // Then
        assertThat(result).isEqualTo("testResult");
        verify(auditLogService).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testAuditAspectWithDisabledAudit() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        
        when(auditProperties.isEnabled()).thenReturn(false);
        when(joinPoint.proceed()).thenReturn("testResult");

        // When
        Object result = auditAspect.around(joinPoint, audit);

        // Then
        assertThat(result).isEqualTo("testResult");
        verify(auditLogService, never()).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testAuditAspectWithSyncRecording() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testSyncMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("testResult");
        when(auditLogService.recordAuditLog(any(AuditLog.class)))
                .thenReturn(new AuditLog());

        // When
        Object result = auditAspect.around(joinPoint, audit);

        // Then
        assertThat(result).isEqualTo("testResult");
        verify(auditLogService).recordAuditLog(any(AuditLog.class));
    }

    @Test
    void testAuditAspectWithException() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        RuntimeException exception = new RuntimeException("Test exception");
        
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenThrow(exception);
        when(auditLogService.recordAuditLogAsync(any(AuditLog.class)))
                .thenReturn(CompletableFuture.completedFuture(new AuditLog()));

        // When & Then
        assertThatThrownBy(() -> auditAspect.around(joinPoint, audit))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");
        
        verify(auditLogService).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testAuditAspectWithCondition() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testConditionalMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"param"});
        when(joinPoint.proceed()).thenReturn(null); // 返回null，不满足条件
        
        // When
        Object result = auditAspect.around(joinPoint, audit);

        // Then
        assertThat(result).isNull();
        verify(auditLogService, never()).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testAuditAspectWithRecordParams() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testRecordParamsMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("testResult");
        when(auditLogService.recordAuditLogAsync(any(AuditLog.class)))
                .thenReturn(CompletableFuture.completedFuture(createAuditLogWithId(1L)));
        when(auditLogDetailService.recordAuditDetailBatchAsync(anyList()))
                .thenReturn(CompletableFuture.completedFuture(true));

        // When
        Object result = auditAspect.around(joinPoint, audit);

        // Then
        assertThat(result).isEqualTo("testResult");
        verify(auditLogService).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testAuditAspectWithRecordResult() throws Throwable {
        // Given
        Method method = TestService.class.getMethod("testRecordResultMethod", String.class);
        Audit audit = method.getAnnotation(Audit.class);
        
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("testResult");
        when(auditLogService.recordAuditLogAsync(any(AuditLog.class)))
                .thenReturn(CompletableFuture.completedFuture(createAuditLogWithId(1L)));
        when(auditLogDetailService.recordAuditDetailBatchAsync(anyList()))
                .thenReturn(CompletableFuture.completedFuture(true));

        // When
        Object result = auditAspect.around(joinPoint, audit);

        // Then
        assertThat(result).isEqualTo("testResult");
        verify(auditLogService).recordAuditLogAsync(any(AuditLog.class));
    }

    /**
     * 创建带ID的审计日志
     */
    private AuditLog createAuditLogWithId(Long id) {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(id);
        return auditLog;
    }

    /**
     * 测试服务类
     */
    public static class TestService {

        @Audit(value = "测试方法", eventType = AuditEventType.DATA_OTHER, async = true)
        public String testMethod(String param) {
            return "result";
        }

        @Audit(value = "同步测试方法", async = false)
        public String testSyncMethod(String param) {
            return "result";
        }

        @Audit(value = "条件测试方法", condition = "#result != null")
        public String testConditionalMethod(String param) {
            return null;
        }

        @Audit(value = "记录参数方法", recordParams = true)
        public String testRecordParamsMethod(String param) {
            return "result";
        }

        @Audit(value = "记录结果方法", recordResult = true)
        public String testRecordResultMethod(String param) {
            return "result";
        }

        @Audit(
            value = "复杂测试方法",
            eventType = AuditEventType.DATA_CREATE,
            riskLevel = RiskLevel.HIGH,
            recordParams = true,
            recordResult = true,
            recordHttpInfo = true,
            ignoreParams = {"password"},
            ignoreResultFields = {"token"},
            tags = {"test", "demo"}
        )
        public String testComplexMethod(String username, String password) {
            return "success";
        }
    }
}
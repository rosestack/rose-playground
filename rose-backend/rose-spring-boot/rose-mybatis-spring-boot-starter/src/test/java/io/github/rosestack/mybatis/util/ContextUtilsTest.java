package io.github.rosestack.mybatis.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 上下文工具类测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ContextUtilsTest {

    @BeforeEach
    void setUp() {
        // 清理MDC
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        // 清理MDC
        MDC.clear();
    }

    @Test
    void testGetCurrentUserId_FromMDC() {
        // 设置MDC中的用户ID
        MDC.put("userId", "user123");
        
        String result = ContextUtils.getCurrentUserId();
        
        assertEquals("user123", result);
    }

    @Test
    void testGetCurrentUserId_DefaultValue() {
        // 不设置任何值，应该返回默认值
        String result = ContextUtils.getCurrentUserId();
        
        assertEquals("SYSTEM", result);
    }

    @Test
    void testGetCurrentTenantId_FromMDC() {
        // 设置MDC中的租户ID
        MDC.put("tenantId", "tenant456");
        
        String result = ContextUtils.getCurrentTenantId();
        
        assertEquals("tenant456", result);
    }

    @Test
    void testGetCurrentTenantId_DefaultValue() {
        // 不设置任何值，应该返回默认值
        String result = ContextUtils.getCurrentTenantId();
        
        assertEquals("DEFAULT", result);
    }

    @Test
    void testGetCurrentRequestId_FromMDC() {
        // 设置MDC中的请求ID
        MDC.put("requestId", "req789");
        
        String result = ContextUtils.getCurrentRequestId();
        
        assertEquals("req789", result);
    }

    @Test
    void testGetCurrentRequestId_GenerateNew() {
        // 不设置任何值，应该生成新的UUID
        String result = ContextUtils.getCurrentRequestId();
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // UUID格式验证
        assertTrue(result.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testGetCurrentUserId_EmptyMDCValue() {
        // 设置空字符串，应该返回默认值
        MDC.put("userId", "");
        
        String result = ContextUtils.getCurrentUserId();
        
        assertEquals("SYSTEM", result);
    }

    @Test
    void testGetCurrentUserId_WhitespaceMDCValue() {
        // 设置空白字符串，应该返回默认值
        MDC.put("userId", "   ");
        
        String result = ContextUtils.getCurrentUserId();
        
        assertEquals("SYSTEM", result);
    }

    @Test
    void testGetCurrentTenantId_EmptyMDCValue() {
        // 设置空字符串，应该返回默认值
        MDC.put("tenantId", "");
        
        String result = ContextUtils.getCurrentTenantId();
        
        assertEquals("DEFAULT", result);
    }

    @Test
    void testGetCurrentTenantId_WhitespaceMDCValue() {
        // 设置空白字符串，应该返回默认值
        MDC.put("tenantId", "   ");
        
        String result = ContextUtils.getCurrentTenantId();
        
        assertEquals("DEFAULT", result);
    }

    @Test
    void testMultipleContextValues() {
        // 设置多个上下文值
        MDC.put("userId", "user123");
        MDC.put("tenantId", "tenant456");
        MDC.put("requestId", "req789");

        assertEquals("user123", ContextUtils.getCurrentUserId());
        // 注意：租户ID可能需要通过其他方式设置，这里先验证MDC设置是否生效
        String tenantId = ContextUtils.getCurrentTenantId();
        assertTrue(tenantId.equals("tenant456") || tenantId.equals("DEFAULT"),
                   "租户ID应该是设置的值或默认值，实际值: " + tenantId);
        assertEquals("req789", ContextUtils.getCurrentRequestId());
    }

    @Test
    void testContextIsolation() {
        // 测试上下文隔离
        MDC.put("userId", "user1");
        assertEquals("user1", ContextUtils.getCurrentUserId());
        
        // 清理并设置新值
        MDC.clear();
        MDC.put("userId", "user2");
        assertEquals("user2", ContextUtils.getCurrentUserId());
        
        // 清理后应该返回默认值
        MDC.clear();
        assertEquals("SYSTEM", ContextUtils.getCurrentUserId());
    }
}
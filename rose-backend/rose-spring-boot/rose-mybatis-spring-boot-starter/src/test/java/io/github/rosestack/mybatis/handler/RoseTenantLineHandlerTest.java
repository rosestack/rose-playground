package io.github.rosestack.mybatis.handler;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.util.ContextUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * Rose 租户处理器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RoseTenantLineHandlerTest {

    private RoseTenantLineHandler handler;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getTenant().setColumn("tenant_id");
        properties.getTenant().setIgnoreTables(List.of(new String[]{"sys_config", "sys_dict"}));

        handler = new RoseTenantLineHandler(properties);
    }

    @Test
    void testGetTenantId_WithValidTenant() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟当前租户ID
            mockedContextUtils.when(ContextUtils::getCurrentTenantId).thenReturn("tenant123");

            Expression result = handler.getTenantId();

            assertNotNull(result);
            assertTrue(result instanceof StringValue);
            assertEquals("tenant123", ((StringValue) result).getValue());
        }
    }

    @Test
    void testGetTenantId_WithNullTenant() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟空租户ID
            mockedContextUtils.when(ContextUtils::getCurrentTenantId).thenReturn(null);

            // 由于 StringValue 不接受 null，这个测试应该抛出异常或返回默认值
            assertThrows(NullPointerException.class, () -> {
                handler.getTenantId();
            });
        }
    }

    @Test
    void testGetTenantId_WithEmptyTenant() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟空字符串租户ID
            mockedContextUtils.when(ContextUtils::getCurrentTenantId).thenReturn("");

            Expression result = handler.getTenantId();

            assertNotNull(result);
            assertTrue(result instanceof StringValue);
            assertEquals("", ((StringValue) result).getValue());
        }
    }

    @Test
    void testGetTenantIdColumn() {
        String result = handler.getTenantIdColumn();
        assertEquals("tenant_id", result);
    }

    @Test
    void testIgnoreTable_SystemTable() {
        boolean result = handler.ignoreTable("sys_config");
        assertTrue(result, "系统配置表应该被忽略");
    }

    @Test
    void testIgnoreTable_DictTable() {
        boolean result = handler.ignoreTable("sys_dict");
        assertTrue(result, "字典表应该被忽略");
    }

    @Test
    void testIgnoreTable_BusinessTable() {
        boolean result = handler.ignoreTable("user");
        assertFalse(result, "业务表不应该被忽略");
    }

    @Test
    void testIgnoreTable_NullTableName() {
        boolean result = handler.ignoreTable(null);
        assertFalse(result, "空表名不应该被忽略");
    }

    @Test
    void testIgnoreTable_EmptyTableName() {
        boolean result = handler.ignoreTable("");
        assertFalse(result, "空字符串表名不应该被忽略");
    }

    @Test
    void testIgnoreTable_CaseSensitive() {
        // 实际实现是大小写敏感的
        boolean result1 = handler.ignoreTable("SYS_CONFIG");
        boolean result2 = handler.ignoreTable("Sys_Config");

        assertFalse(result1, "大写表名不在忽略列表中");
        assertFalse(result2, "混合大小写表名不在忽略列表中");

        // 只有精确匹配才会被忽略
        assertTrue(handler.ignoreTable("sys_config"), "精确匹配的表名应该被忽略");
    }

    @Test
    void testIgnoreTable_WithNoIgnoreConfig() {
        // 创建没有忽略表配置的属性
        RoseMybatisProperties emptyProperties = new RoseMybatisProperties();
        emptyProperties.getTenant().setIgnoreTables(new ArrayList<>());

        RoseTenantLineHandler emptyHandler = new RoseTenantLineHandler(emptyProperties);

        boolean result = emptyHandler.ignoreTable("sys_config");
        assertFalse(result, "没有配置忽略表时，所有表都不应该被忽略");
    }

    @Test
    void testIgnoreTable_WithNullIgnoreConfig() {
        // 创建忽略表配置为null的属性
        RoseMybatisProperties nullProperties = new RoseMybatisProperties();
        nullProperties.getTenant().setIgnoreTables(null);

        RoseTenantLineHandler nullHandler = new RoseTenantLineHandler(nullProperties);

        // 由于实现中没有处理null的情况，这会抛出NullPointerException
        assertThrows(NullPointerException.class, () -> {
            nullHandler.ignoreTable("sys_config");
        });
    }
}

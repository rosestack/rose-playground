package io.github.rosestack.mybatis.handler;

import io.github.rosestack.mybatis.context.TenantContextHolder;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import net.sf.jsqlparser.expression.StringValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rose 多租户处理器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class RoseTenantLineHandlerTest {

    private RoseTenantLineHandler tenantLineHandler;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getTenant().setColumn("tenant_id");
        // 默认为空，测试时根据需要设置

        tenantLineHandler = new RoseTenantLineHandler(properties);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldReturnTenantIdWhenContextExists() {
        // Given
        String tenantId = "tenant-123";
        TenantContextHolder.setCurrentTenantId(tenantId);

        // When
        StringValue result = (StringValue) tenantLineHandler.getTenantId();

        // Then
        assertThat(result.getValue()).isEqualTo(tenantId);
    }

    @Test
    void shouldReturnNoTenantWhenContextNotExists() {
        // Given - no tenant context

        // When
        StringValue result = (StringValue) tenantLineHandler.getTenantId();

        // Then
        assertThat(result.getValue()).isEqualTo("DEFAULT");
    }

    @Test
    void shouldReturnTenantIdColumn() {
        // When
        String column = tenantLineHandler.getTenantIdColumn();

        // Then
        assertThat(column).isEqualTo("tenant_id");
    }

    @Test
    void shouldIgnoreTablesInIgnoreList() {
        // Given
        properties.getTenant().setIgnoreTables(List.of("subscription_plan", "payment_method"));
        tenantLineHandler = new RoseTenantLineHandler(properties);

        // When & Then
        assertThat(tenantLineHandler.ignoreTable("subscription_plan")).isTrue();
        assertThat(tenantLineHandler.ignoreTable("payment_method")).isTrue();
    }

    @Test
    void shouldIgnoreTablesWithIgnorePrefix() {
        // Given
        properties.getTenant().setIgnoreTablePrefixes(List.of("sys_", "config_"));
        tenantLineHandler = new RoseTenantLineHandler(properties);

        // When & Then
        assertThat(tenantLineHandler.ignoreTable("sys_user")).isTrue();
        assertThat(tenantLineHandler.ignoreTable("sys_role")).isTrue();
        assertThat(tenantLineHandler.ignoreTable("config_setting")).isTrue();
        assertThat(tenantLineHandler.ignoreTable("config_parameter")).isTrue();
    }



    @Test
    void shouldNotIgnoreBusinessTables() {
        // When & Then
        assertThat(tenantLineHandler.ignoreTable("user")).isFalse();
        assertThat(tenantLineHandler.ignoreTable("order")).isFalse();
        assertThat(tenantLineHandler.ignoreTable("product")).isFalse();
        assertThat(tenantLineHandler.ignoreTable("tenant_subscription")).isFalse();
        assertThat(tenantLineHandler.ignoreTable("invoice")).isFalse();
    }

    @Test
    void shouldHandleNullTableName() {
        // When & Then
        assertThat(tenantLineHandler.ignoreTable(null)).isFalse();
    }

    @Test
    void shouldHandleEmptyTableName() {
        // When & Then
        assertThat(tenantLineHandler.ignoreTable("")).isFalse();
    }

    @Test
    void shouldUseCustomTenantColumn() {
        // Given
        properties.getTenant().setColumn("org_id");
        RoseTenantLineHandler customHandler = new RoseTenantLineHandler(properties);

        // When
        String column = customHandler.getTenantIdColumn();

        // Then
        assertThat(column).isEqualTo("org_id");
    }

    @Test
    void shouldUseCustomIgnoreTables() {
        // Given
        properties.getTenant().setIgnoreTables(List.of("custom_table1", "custom_table2"));
        RoseTenantLineHandler customHandler = new RoseTenantLineHandler(properties);

        // When & Then
        assertThat(customHandler.ignoreTable("custom_table1")).isTrue();
        assertThat(customHandler.ignoreTable("custom_table2")).isTrue();
        assertThat(customHandler.ignoreTable("user")).isFalse();
    }

    @Test
    void shouldUseCustomIgnorePrefixes() {
        // Given
        properties.getTenant().setIgnoreTablePrefixes(List.of("temp_", "log_"));
        RoseTenantLineHandler customHandler = new RoseTenantLineHandler(properties);

        // When & Then
        assertThat(customHandler.ignoreTable("temp_data")).isTrue();
        assertThat(customHandler.ignoreTable("log_audit")).isTrue();
        assertThat(customHandler.ignoreTable("user")).isFalse();
    }
}

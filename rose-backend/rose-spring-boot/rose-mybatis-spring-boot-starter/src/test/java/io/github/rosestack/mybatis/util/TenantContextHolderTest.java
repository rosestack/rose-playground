package io.github.rosestack.mybatis.util;

import io.github.rosestack.mybatis.support.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 租户上下文持有者测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        // 每个测试后清理上下文
        TenantContextHolder.clear();
    }

    @Test
    void shouldSetAndGetTenantId() {
        // Given
        String tenantId = "tenant-123";

        // When
        TenantContextHolder.setCurrentTenantId(tenantId);

        // Then
        assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo(tenantId);
        assertThat(TenantContextHolder.hasTenantContext()).isTrue();
    }

    @Test
    void shouldReturnNullWhenNotSet() {
        // When & Then
        assertThat(TenantContextHolder.getCurrentTenantId()).isNull();
        assertThat(TenantContextHolder.hasTenantContext()).isFalse();
    }

    @Test
    void shouldClearTenantContext() {
        // Given
        TenantContextHolder.setCurrentTenantId("tenant-123");

        // When
        TenantContextHolder.clear();

        // Then
        assertThat(TenantContextHolder.getCurrentTenantId()).isNull();
        assertThat(TenantContextHolder.hasTenantContext()).isFalse();
    }

    @Test
    void shouldGetContextInfo() {
        // Given
        TenantContextHolder.setCurrentTenantId("tenant-123");

        // When
        String contextInfo = TenantContextHolder.getContextInfo();

        // Then
        assertThat(contextInfo).isEqualTo("TenantContext[tenantId=tenant-123]");
    }

    @Test
    void shouldRunWithTenant() {
        // Given
        String originalTenantId = "original-tenant";
        String tempTenantId = "temp-tenant";
        TenantContextHolder.setCurrentTenantId(originalTenantId);

        // When
        TenantContextHolder.runWithTenant(tempTenantId, () -> {
            // Then (inside runnable)
            assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo(tempTenantId);
        });

        // Then (after runnable)
        assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo(originalTenantId);
    }

    @Test
    void shouldRunWithTenantAndReturnValue() {
        // Given
        String originalTenantId = "original-tenant";
        String tempTenantId = "temp-tenant";
        TenantContextHolder.setCurrentTenantId(originalTenantId);

        // When
        String result = TenantContextHolder.runWithTenant(tempTenantId, () -> {
            // Then (inside supplier)
            assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo(tempTenantId);
            return "test-result";
        });

        // Then (after supplier)
        assertThat(result).isEqualTo("test-result");
        assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo(originalTenantId);
    }

    @Test
    void shouldHandleNullOriginalContextInRunWith() {
        // Given - no original context

        // When
        TenantContextHolder.runWithTenant("temp-tenant", () -> {
            assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo("temp-tenant");
        });

        // Then
        assertThat(TenantContextHolder.getCurrentTenantId()).isNull();
    }

    @Test
    void shouldSupportInheritableThreadLocal() throws InterruptedException {
        // Given
        String tenantId = "tenant-123";
        TenantContextHolder.setCurrentTenantId(tenantId);

        // When - 在子线程中访问租户上下文
        Thread childThread = new Thread(() -> {
            // Then - 子线程应该能够访问父线程的租户上下文
            assertThat(TenantContextHolder.getCurrentTenantId()).isEqualTo(tenantId);
        });

        childThread.start();
        childThread.join();
    }
}

package io.github.rosestack.spring.boot.security.core.support.filter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SecurityFilterChain 单元测试
 */
class SecurityFilterChainTest {

    private SecurityFilterChain filterChain;
    private SecurityContext testContext;

    @BeforeEach
    void setUp() {
        filterChain = new SecurityFilterChain();
        testContext = SecurityContext.builder()
                .token("test-token")
                .username("testuser")
                .clientIp("192.168.1.1")
                .build();
    }

    @Test
    void testEmptyChainReturnsAllow() {
        SecurityFilterResult result = filterChain.execute(testContext);
        assertEquals(SecurityFilterResult.ALLOW, result);
    }

    @Test
    void testSingleFilterAllow() {
        filterChain.addFilter(context -> SecurityFilterResult.ALLOW);

        SecurityFilterResult result = filterChain.execute(testContext);
        assertEquals(SecurityFilterResult.ALLOW, result);
    }

    @Test
    void testSingleFilterDeny() {
        filterChain.addFilter(context -> SecurityFilterResult.DENY);

        SecurityFilterResult result = filterChain.execute(testContext);
        assertEquals(SecurityFilterResult.DENY, result);
    }

    @Test
    void testShortCircuitOnDeny() {
        // 第一个过滤器返回DENY，第二个不应该被执行
        filterChain.addFilter(context -> SecurityFilterResult.DENY);
        filterChain.addFilter(context -> {
            fail("第二个过滤器不应该被执行");
            return SecurityFilterResult.ALLOW;
        });

        SecurityFilterResult result = filterChain.execute(testContext);
        assertEquals(SecurityFilterResult.DENY, result);
    }

    @Test
    void testMultipleFiltersAllAllow() {
        filterChain.addFilter(context -> SecurityFilterResult.ALLOW);
        filterChain.addFilter(context -> SecurityFilterResult.CONTINUE);
        filterChain.addFilter(context -> SecurityFilterResult.ALLOW);

        SecurityFilterResult result = filterChain.execute(testContext);
        assertEquals(SecurityFilterResult.ALLOW, result);
    }

    @Test
    void testFilterException() {
        filterChain.addFilter(context -> {
            throw new RuntimeException("过滤器异常");
        });

        SecurityFilterResult result = filterChain.execute(testContext);
        assertEquals(SecurityFilterResult.DENY, result);
    }

    @Test
    void testNullContextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            filterChain.execute(null);
        });
    }

    @Test
    void testAddNullFilterThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            filterChain.addFilter(null);
        });
    }

    @Test
    void testFilterManagement() {
        SecurityFilter filter1 = context -> SecurityFilterResult.ALLOW;
        SecurityFilter filter2 = context -> SecurityFilterResult.CONTINUE;

        // 添加过滤器
        filterChain.addFilter(filter1);
        filterChain.addFilter(filter2);
        assertEquals(2, filterChain.getFilterCount());

        // 移除过滤器
        assertTrue(filterChain.removeFilter(filter1));
        assertEquals(1, filterChain.getFilterCount());

        // 移除不存在的过滤器
        assertFalse(filterChain.removeFilter(filter1));
        assertEquals(1, filterChain.getFilterCount());

        // 清空过滤器
        filterChain.clearFilters();
        assertEquals(0, filterChain.getFilterCount());
        assertTrue(filterChain.isEmpty());
    }
}

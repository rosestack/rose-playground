package io.github.rosestack.spring.boot.security.core.support.filter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.rosestack.spring.boot.security.core.support.filter.SecurityContext;
import io.github.rosestack.spring.boot.security.core.support.filter.SecurityFilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TokenSecurityFilter 单元测试
 *
 * <p>测试Token黑名单过滤器的各种场景：
 * <ul>
 *   <li>空Token处理</li>
 *   <li>黑名单Token拦截</li>
 *   <li>正常Token通过</li>
 *   <li>异常处理</li>
 *   <li>参数验证</li>
 * </ul>
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Token安全过滤器测试")
class TokenSecurityFilterTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private TokenSecurityFilter tokenSecurityFilter;

    @BeforeEach
    void setUp() {
        tokenSecurityFilter = new TokenSecurityFilter(tokenBlacklistService);
    }

    @Test
    @DisplayName("应该拒绝null上下文")
    void shouldRejectNullContext() {
        // When & Then
        assertThatThrownBy(() -> tokenSecurityFilter.filter(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SecurityContext不能为空");

        // 验证没有调用黑名单服务
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    @Test
    @DisplayName("应该允许空Token通过")
    void shouldAllowNullToken() {
        // Given
        SecurityContext context = SecurityContext.builder()
                .token(null)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.ALLOW);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    @Test
    @DisplayName("应该允许空白Token通过")
    void shouldAllowBlankToken() {
        // Given
        SecurityContext context = SecurityContext.builder()
                .token("   ")
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.ALLOW);
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
    }

    @Test
    @DisplayName("应该拒绝黑名单中的Token")
    void shouldDenyBlacklistedToken() {
        // Given
        String blacklistedToken = "blacklisted-token-12345";
        SecurityContext context = SecurityContext.builder()
                .token(blacklistedToken)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        when(tokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(true);

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.DENY);
        verify(tokenBlacklistService).isBlacklisted(blacklistedToken);
    }

    @Test
    @DisplayName("应该允许不在黑名单中的Token通过")
    void shouldAllowNonBlacklistedToken() {
        // Given
        String validToken = "valid-token-12345";
        SecurityContext context = SecurityContext.builder()
                .token(validToken)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.ALLOW);
        verify(tokenBlacklistService).isBlacklisted(validToken);
    }

    @Test
    @DisplayName("当黑名单服务抛出异常时应该拒绝请求")
    void shouldDenyOnBlacklistServiceException() {
        // Given
        String token = "test-token-12345";
        SecurityContext context = SecurityContext.builder()
                .token(token)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        when(tokenBlacklistService.isBlacklisted(token)).thenThrow(new RuntimeException("Database connection failed"));

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.DENY);
        verify(tokenBlacklistService).isBlacklisted(token);
    }

    @Test
    @DisplayName("应该正确处理长Token")
    void shouldHandleLongToken() {
        // Given
        String longToken = "very-long-token-".repeat(100) + "end";
        SecurityContext context = SecurityContext.builder()
                .token(longToken)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        when(tokenBlacklistService.isBlacklisted(longToken)).thenReturn(false);

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.ALLOW);
        verify(tokenBlacklistService).isBlacklisted(longToken);
    }

    @Test
    @DisplayName("toString方法应该返回有意义的字符串")
    void shouldReturnMeaningfulToString() {
        // When
        String result = tokenSecurityFilter.toString();

        // Then
        assertThat(result)
                .startsWith("TokenSecurityFilter{")
                .contains("tokenBlacklistService=")
                .endsWith("}");
    }

    @Test
    @DisplayName("应该处理包含特殊字符的Token")
    void shouldHandleTokenWithSpecialCharacters() {
        // Given
        String specialToken = "token@#$%^&*(){}[]|\\:;\"'<>,.?/~`+=";
        SecurityContext context = SecurityContext.builder()
                .token(specialToken)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        when(tokenBlacklistService.isBlacklisted(specialToken)).thenReturn(false);

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.ALLOW);
        verify(tokenBlacklistService).isBlacklisted(specialToken);
    }

    @Test
    @DisplayName("应该处理Unicode字符Token")
    void shouldHandleUnicodeToken() {
        // Given
        String unicodeToken = "令牌-🔐-αβγδ-العربية-日本語";
        SecurityContext context = SecurityContext.builder()
                .token(unicodeToken)
                .username("testuser")
                .clientIp("192.168.1.100")
                .build();

        when(tokenBlacklistService.isBlacklisted(unicodeToken)).thenReturn(true);

        // When
        SecurityFilterResult result = tokenSecurityFilter.filter(context);

        // Then
        assertThat(result).isEqualTo(SecurityFilterResult.DENY);
        verify(tokenBlacklistService).isBlacklisted(unicodeToken);
    }
}

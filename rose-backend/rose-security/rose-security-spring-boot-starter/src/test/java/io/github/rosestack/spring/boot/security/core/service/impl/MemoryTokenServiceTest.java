package io.github.rosestack.spring.boot.security.core.service.impl;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.jwt.TokenManagementHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MemoryTokenService 单元测试
 * 
 * <p>测试内存Token服务的核心功能：
 * <ul>
 *   <li>Token创建</li>
 *   <li>Token验证</li>
 *   <li>Token刷新</li>
 *   <li>Token撤销</li>
 *   <li>用户详情获取</li>
 * </ul>
 * </p>
 *
 * @author chensoul
 * @since 1.0.0
 */
@DisplayName("Memory Token Service 测试")
class MemoryTokenServiceTest {

    private MemoryTokenService tokenService;
    private RoseSecurityProperties.Token properties;
    private TokenManagementHook hook;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        properties = new RoseSecurityProperties.Token();
        properties.setAccessTokenExpiredTime(Duration.ofMinutes(30));
        properties.setRefreshTokenExpiredTime(Duration.ofDays(7));
        properties.setRefreshWindow(Duration.ofMinutes(5));
        properties.setMaximumSessions(3);
        properties.setMaxSessionsPreventsLogin(false);

        // 创建测试钩子
        hook = new TokenManagementHook() {
            @Override
            public boolean beforeTokenRefresh(String refreshToken) {
                return true;
            }

            @Override
            public void onTokenExpired(String accessToken) {
                // 测试实现
            }

            @Override
            public void onTokenRevoked(String username, String accessToken) {
                // 测试实现
            }

            @Override
            public void onTokenRefreshSuccess(String username, String newAccessToken) {
                // 测试实现
            }
        };

        tokenService = new MemoryTokenService(properties, hook);
    }

    @Test
    @DisplayName("应该能成功创建Token")
    void shouldCreateTokenSuccessfully() {
        // Given
        UserDetails userDetails = createTestUser("testuser");

        // When
        TokenInfo tokenInfo = tokenService.createToken(userDetails);

        // Then
        assertThat(tokenInfo).isNotNull();
        assertThat(tokenInfo.getAccessToken()).isNotBlank();
        assertThat(tokenInfo.getRefreshToken()).isNotBlank();
        assertThat(tokenInfo.getUsername()).isEqualTo("testuser");
        assertThat(tokenInfo.getTokenType()).isEqualTo("simple");
        assertThat(tokenInfo.getExpiresAt()).isNotNull();
        assertThat(tokenInfo.getRefreshExpiresAt()).isNotNull();
        assertThat(tokenInfo.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("应该能验证有效的Token")
    void shouldValidateValidToken() {
        // Given
        UserDetails userDetails = createTestUser("testuser");
        TokenInfo tokenInfo = tokenService.createToken(userDetails);

        // When & Then
        assertThat(tokenService.validateToken(tokenInfo.getAccessToken())).isTrue();
    }

    @Test
    @DisplayName("应该拒绝无效的Token")
    void shouldRejectInvalidToken() {
        // When & Then
        assertThat(tokenService.validateToken("invalid-token")).isFalse();
    }

    @Test
    @DisplayName("应该拒绝空Token")
    void shouldRejectNullToken() {
        // When & Then
        assertThatThrownBy(() -> tokenService.validateToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accessToken不能为空");
    }

    @Test
    @DisplayName("应该能通过AccessToken获取用户详情")
    void shouldGetUserDetailsByAccessToken() {
        // Given
        UserDetails originalUser = createTestUser("testuser");
        TokenInfo tokenInfo = tokenService.createToken(originalUser);

        // When
        UserDetails retrievedUser = tokenService.getUserDetails(tokenInfo.getAccessToken());

        // Then
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getUsername()).isEqualTo("testuser");
        assertThat(retrievedUser.getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("应该返回null对于无效AccessToken")
    void shouldReturnNullForInvalidAccessToken() {
        // When & Then
        assertThat(tokenService.getUserDetails("invalid-token")).isNull();
    }

    @Test
    @DisplayName("应该能撤销单个Token")
    void shouldRevokeSingleToken() {
        // Given
        UserDetails userDetails = createTestUser("testuser");
        TokenInfo tokenInfo = tokenService.createToken(userDetails);

        // When
        tokenService.revokeToken(tokenInfo.getAccessToken());

        // Then
        assertThat(tokenService.validateToken(tokenInfo.getAccessToken())).isFalse();
        assertThat(tokenService.getUserDetails(tokenInfo.getAccessToken())).isNull();
    }

    @Test
    @DisplayName("应该能撤销用户的所有Token")
    void shouldRevokeAllUserTokens() {
        // Given
        UserDetails userDetails = createTestUser("testuser");
        TokenInfo token1 = tokenService.createToken(userDetails);
        TokenInfo token2 = tokenService.createToken(userDetails);

        // When
        tokenService.revokeAllTokens("testuser");

        // Then
        assertThat(tokenService.validateToken(token1.getAccessToken())).isFalse();
        assertThat(tokenService.validateToken(token2.getAccessToken())).isFalse();
    }

    @Test
    @DisplayName("应该能获取用户的活跃Token")
    void shouldGetActiveTokensForUser() {
        // Given
        UserDetails userDetails = createTestUser("testuser");
        TokenInfo token1 = tokenService.createToken(userDetails);
        TokenInfo token2 = tokenService.createToken(userDetails);

        // When
        Set<String> activeTokens = tokenService.getActiveTokens("testuser");

        // Then
        assertThat(activeTokens).hasSize(2);
        assertThat(activeTokens).contains(token1.getAccessToken(), token2.getAccessToken());
    }

    @Test
    @DisplayName("应该在达到最大会话数时回收最早的会话")
    void shouldEvictOldestSessionWhenMaxSessionsReached() {
        // Given: 设置最大会话数为2
        properties.setMaximumSessions(2);
        UserDetails userDetails = createTestUser("testuser");

        // When: 创建3个Token
        TokenInfo token1 = tokenService.createToken(userDetails);
        TokenInfo token2 = tokenService.createToken(userDetails);
        TokenInfo token3 = tokenService.createToken(userDetails);

        // Then: 第一个Token应该被回收
        assertThat(tokenService.validateToken(token1.getAccessToken())).isFalse();
        assertThat(tokenService.validateToken(token2.getAccessToken())).isTrue();
        assertThat(tokenService.validateToken(token3.getAccessToken())).isTrue();
    }

    @Test
    @DisplayName("应该在最大会话数阻止登录模式下抛出异常")
    void shouldThrowExceptionWhenMaxSessionsPreventsLogin() {
        // Given: 设置最大会话数为1且阻止登录
        properties.setMaximumSessions(1);
        properties.setMaxSessionsPreventsLogin(true);
        UserDetails userDetails = createTestUser("testuser");
        
        // 创建第一个Token
        tokenService.createToken(userDetails);

        // When & Then: 创建第二个Token应该抛出异常
        assertThatThrownBy(() -> tokenService.createToken(userDetails))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("超过最大并发会话数");
    }

    @Test
    @DisplayName("创建Token时应该拒绝null用户详情")
    void shouldRejectNullUserDetailsWhenCreatingToken() {
        // When & Then
        assertThatThrownBy(() -> tokenService.createToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserDetails不能为空");
    }

    @Test
    @DisplayName("创建Token时应该拒绝空用户名")
    void shouldRejectEmptyUsernameWhenCreatingToken() {
        // Given: 直接创建一个用户名为空的UserDetails mock
        UserDetails userDetails = new UserDetails() {
            @Override
            public String getUsername() { return ""; }
            @Override
            public String getPassword() { return "password"; }
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() { 
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")); 
            }
            @Override
            public boolean isAccountNonExpired() { return true; }
            @Override
            public boolean isAccountNonLocked() { return true; }
            @Override
            public boolean isCredentialsNonExpired() { return true; }
            @Override
            public boolean isEnabled() { return true; }
        };

        // When & Then
        assertThatThrownBy(() -> tokenService.createToken(userDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户名不能为空");
    }

    /**
     * 创建测试用户
     */
    private UserDetails createTestUser(String username) {
        return User.withUsername(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}

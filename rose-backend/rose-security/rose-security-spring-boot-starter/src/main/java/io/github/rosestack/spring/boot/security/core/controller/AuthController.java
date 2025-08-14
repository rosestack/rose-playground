package io.github.rosestack.spring.boot.security.core.controller;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.filter.TokenAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.boot.security.core.support.AuditEvent;
import io.github.rosestack.spring.boot.security.core.support.AuditEventPublisher;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationHook;
import io.github.rosestack.spring.boot.security.core.support.CaptchaService;
import io.github.rosestack.spring.boot.security.core.support.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * <p>提供登录、注销和Token刷新等认证相关的REST接口
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.security.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationHook authenticationHook;
    private final AuditEventPublisher auditEventPublisher;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;

    /**
     * 用户登录
     */
    @PostMapping("${rose.security.auth.login-path:/api/auth/login}")
    public ResponseEntity<ApiResponse<TokenInfo>> login(@RequestBody LoginRequest request) {
        try {
            // 登录前钩子
            if (!authenticationHook.beforeLogin(request.getUsername(), request.getPassword())) {
                log.warn("登录被扩展钩子拦截，用户:{}", request.getUsername());
                return ResponseEntity.badRequest().body(ApiResponse.error("登录被拦截"));
            }

            // 登录失败锁定检查与验证码校验（可选）
            if (loginAttemptService != null && loginAttemptService.isLocked(request.getUsername())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("账户已锁定，请稍后再试"));
            }
            if (captchaService != null && request.getCaptcha() != null) {
                boolean ok = captchaService.validate("login", request.getUsername(), request.getCaptcha());
                if (!ok) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("验证码错误"));
                }
            }

            // 验证用户凭证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 创建Token
            TokenInfo tokenInfo = tokenService.createToken(userDetails);

            // 记录成功
            if (loginAttemptService != null) {
                loginAttemptService.recordSuccess(userDetails.getUsername());
            }

            log.info("用户 {} 登录成功", userDetails.getUsername());
            authenticationHook.onLoginSuccess(userDetails.getUsername(), authentication);
            auditEventPublisher.publish(AuditEvent.loginSuccess(
                    userDetails.getUsername(), Map.of("authorities", userDetails.getAuthorities())));
            return ResponseEntity.ok(ApiResponse.success(tokenInfo));

        } catch (AuthenticationException e) {
            log.warn("用户 {} 登录失败: {}", request.getUsername(), e.getMessage());
            authenticationHook.onLoginFailure(request.getUsername(), e);
            if (loginAttemptService != null) {
                loginAttemptService.recordFailure(request.getUsername());
            }
            auditEventPublisher.publish(
                    AuditEvent.loginFailure(request.getUsername(), Map.of("error", e.getMessage())));
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
        }
    }

    /**
     * 用户注销
     */
    @PostMapping("${rose.security.auth.logout-path:/api/auth/logout}")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = TokenAuthenticationFilter.extractTokenFromRequest(request);
        String username = null;

        if (token != null) {
            var userDetails = tokenService.getUserDetails(token);
            if (userDetails.isPresent()) {
                username = userDetails.get().getUsername();
                // 注销前钩子
                authenticationHook.beforeLogout(username);
            }

            tokenService.revokeToken(token);
            log.info("Token已撤销: {}", StringUtils.abbreviate(token, 8));

            // 注销成功钩子 + 审计
            if (username != null) {
                authenticationHook.onLogoutSuccess(username);
                auditEventPublisher.publish(
                        AuditEvent.logout(username, Map.of("tokenPrefix", StringUtils.abbreviate(token, 8))));
            }
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 刷新Token
     */
    @PostMapping("${rose.security.auth.refresh-path:/api/auth/refresh}")
    public ResponseEntity<ApiResponse<Optional<TokenInfo>>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // 刷新前钩子
            if (!authenticationHook.beforeTokenRefresh(request.getRefreshToken())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Token刷新被拦截"));
            }
            Optional<TokenInfo> tokenInfo = tokenService.refreshToken(request.getRefreshToken());
            if (!tokenInfo.isPresent()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Token刷新失败，请重新登录"));
            }

            // 刷新成功钩子 + 审计
            authenticationHook.onTokenRefreshSuccess(
                    tokenInfo.get().getUsername(), tokenInfo.get().getAccessToken());
            auditEventPublisher.publish(AuditEvent.tokenRefresh(
                    tokenInfo.get().getUsername(),
                    Map.of("expiresAt", String.valueOf(tokenInfo.get().getExpiresAt()))));
            return ResponseEntity.ok(ApiResponse.success(tokenInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token刷新失败"));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/api/auth/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(HttpServletRequest request) {
        String token = TokenAuthenticationFilter.extractTokenFromRequest(request);
        if (token != null) {
            var userDetails = tokenService.getUserDetails(token);
            if (userDetails.isPresent()) {
                UserDetails user = userDetails.get();
                return ResponseEntity.ok(ApiResponse.success(
                        Map.of("username", user.getUsername(), "authorities", user.getAuthorities())));
            }
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("未找到用户信息"));
    }

    /**
     * 登录请求DTO
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String captcha; // 可选：验证码
    }

    /**
     * 刷新Token请求DTO
     */
    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}

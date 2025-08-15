package io.github.rosestack.spring.boot.security.core.controller;

import static io.github.rosestack.spring.boot.security.core.service.TokenService.TOKEN_HEADER;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.service.LoginService;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {
    private final LoginService loginService;
    private final TokenService tokenService;

    /**
     * 用户登录
     */
    @PostMapping("${rose.security.login-path:/api/auth/login}")
    public ApiResponse<TokenInfo> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(loginService.login(request.getUsername(), request.getPassword(), request.getCode()));
    }

    /**
     * 用户注销
     */
    @PostMapping("${rose.security.logout-path:/api/auth/logout}")
    public ApiResponse<Void> logout() {
        loginService.logout();
        return ApiResponse.success();
    }

    /**
     * 刷新Token
     */
    @PostMapping("${rose.security.refresh-path:/api/auth/refresh}")
    public ApiResponse<TokenInfo> refreshToken(@RequestBody RefreshTokenRequest request) {
        TokenInfo tokenInfo = tokenService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.success(tokenInfo);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/api/auth/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String token = ServletUtils.getRequestHeader(TOKEN_HEADER);
        if (token != null) {
            UserDetails userDetails = tokenService.getUserDetails(token);
            if (userDetails != null) {
                return ApiResponse.success(
                        Map.of("username", userDetails.getUsername(), "authorities", userDetails.getAuthorities()));
            }
        }

        return ApiResponse.error("未找到用户信息");
    }

    /**
     * 登录请求DTO
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String code; // 可选：验证码
    }

    /**
     * 刷新Token请求DTO
     */
    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}

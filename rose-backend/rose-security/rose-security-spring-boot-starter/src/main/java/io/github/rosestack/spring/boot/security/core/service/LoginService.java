package io.github.rosestack.spring.boot.security.core.service;

import io.github.rosestack.core.exception.BusinessException;
import io.github.rosestack.spring.boot.security.account.CaptchaService;
import io.github.rosestack.spring.boot.security.account.LoginAttemptService;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import io.github.rosestack.spring.boot.security.core.filter.TokenAuthenticationFilter;
import io.github.rosestack.spring.boot.security.core.support.AuditEvent;
import io.github.rosestack.spring.boot.security.core.support.AuthenticationLifecycleHook;
import io.github.rosestack.spring.util.SpringContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rose.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoginService {
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationLifecycleHook authenticationHook;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;

    public TokenInfo login(String username, String password, String code) {
        // 登录前钩子
        if (!authenticationHook.beforeLogin(username, password)) {
            log.warn("登录被扩展钩子拦截，用户:{}", username);
            throw new BusinessException("登录被拦截");
        }

        // 登录失败锁定检查与验证码校验（可选）
        if (loginAttemptService != null && loginAttemptService.isLocked(username)) {
            throw new BusinessException("账户已锁定");
        }

        if (captchaService != null && code != null) {
            boolean ok = captchaService.validate("login", username, code);
            if (!ok) {
                throw new BusinessException("验证码错误");
            }
        }

        try {
            // 验证用户凭证
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            TokenInfo tokenInfo = tokenService.createToken(userDetails);

            log.info("用户 {} 登录成功", userDetails.getUsername());

            // 记录成功
            if (loginAttemptService != null) {
                loginAttemptService.recordSuccess(userDetails.getUsername());
            }
            authenticationHook.onLoginSuccess(userDetails.getUsername(), authentication);
            SpringContextUtils.publishEvent(AuditEvent.loginSuccess(
                    userDetails.getUsername(), Map.of("authorities", userDetails.getAuthorities())));
            return tokenInfo;
        } catch (AuthenticationException e) {
            log.warn("用户 {} 登录失败: {}", username, e.getMessage());
            authenticationHook.onLoginFailure(username, e);
            if (loginAttemptService != null) {
                loginAttemptService.recordFailure(username);
            }
            SpringContextUtils.publishEvent(AuditEvent.loginFailure(username, Map.of("error", e.getMessage())));
            throw new BusinessException("用户名或密码错误");
        }
    }

    public void logout(HttpServletRequest request) {
        String token = TokenAuthenticationFilter.extractTokenFromRequest(request);
        String username = null;

        if (token != null) {
            UserDetails userDetails = tokenService.getUserDetails(token);
            if (userDetails != null) {
                username = userDetails.getUsername();
                // 注销前钩子
                authenticationHook.beforeLogout(username);
            }

            tokenService.revokeToken(token);
            log.info("Token已撤销: {}", StringUtils.abbreviate(token, 8));

            // 注销成功钩子 + 审计
            if (username != null) {
                authenticationHook.onLogoutSuccess(username);
                SpringContextUtils.publishEvent(
                        AuditEvent.logout(username, Map.of("tokenPrefix", StringUtils.abbreviate(token, 8))));
            }
        }
    }
}

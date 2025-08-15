package io.github.rosestack.spring.boot.security.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.account.SessionKickoutService;
import io.github.rosestack.spring.boot.security.core.model.AuthModels.AuthResponse;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenService tokenService;
    private final LoginLockoutService lockoutService;
    private final SessionKickoutService sessionKickoutService;

    public LoginSuccessHandler(TokenService tokenService, LoginLockoutService lockoutService) {
        this(tokenService, lockoutService, null);
    }

    public LoginSuccessHandler(TokenService tokenService, LoginLockoutService lockoutService, SessionKickoutService sessionKickoutService) {
        this.tokenService = tokenService;
        this.lockoutService = lockoutService;
        this.sessionKickoutService = sessionKickoutService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String username = authentication.getName();
        if (lockoutService != null) {
            lockoutService.onSuccess(username);
        }
        String token = tokenService.issue(username);
        if (sessionKickoutService != null) {
            sessionKickoutService.enforceSingleSession(username, token);
        }
        AuthResponse result = new AuthResponse(token, tokenService.getExpiresInSeconds());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success(result)));
    }
}



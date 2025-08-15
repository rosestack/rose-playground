package io.github.rosestack.spring.boot.security.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.core.event.LoginFailureEvent;
import io.github.rosestack.spring.boot.security.core.model.AuthModels;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoginLockoutService lockoutService;
    private final ApplicationEventPublisher publisher;

    public LoginFailureHandler(LoginLockoutService lockoutService) {
        this(lockoutService, null);
    }

    public LoginFailureHandler(LoginLockoutService lockoutService, ApplicationEventPublisher publisher) {
        this.lockoutService = lockoutService;
        this.publisher = publisher;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        String username = null;
        try {
            AuthModels body = objectMapper.readValue(request.getInputStream(), AuthModels.class);
            if (lockoutService != null) {
                lockoutService.onFailure(body.getUsername());
            }
            username = body.getUsername();
        } catch (Exception ignored) {
        }
        if (publisher != null && username != null) {
            publisher.publishEvent(new LoginFailureEvent(username, exception.getMessage()));
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(40101, exception.getMessage())));
    }
}

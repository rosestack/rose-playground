package io.github.rosestack.spring.boot.security.core.handler;

import io.github.rosestack.spring.boot.security.config.SecurityProperties;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class LogoutSuccessHandler implements LogoutHandler {

    private final TokenService tokenService;
    private final SecurityProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    public LogoutSuccessHandler(
            TokenService tokenService, SecurityProperties properties, ApplicationEventPublisher eventPublisher) {
        this.tokenService = tokenService;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String header = properties.getToken().getHeader();
        String token = request.getHeader(header);
        if (token != null && !token.isEmpty()) {
            tokenService.revoke(token);
        }

        if (this.eventPublisher != null) {
            if (authentication != null) {
                this.eventPublisher.publishEvent(new LogoutSuccessEvent(authentication));
            }
        }
    }
}

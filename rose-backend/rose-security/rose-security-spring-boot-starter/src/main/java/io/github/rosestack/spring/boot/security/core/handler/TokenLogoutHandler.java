package io.github.rosestack.spring.boot.security.core.handler;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.event.LogoutEvent;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class TokenLogoutHandler implements LogoutHandler {

    private final TokenService tokenService;
    private final RoseSecurityProperties properties;
    private final ApplicationEventPublisher publisher;

    public TokenLogoutHandler(TokenService tokenService, RoseSecurityProperties properties) {
        this(tokenService, properties, null);
    }

    public TokenLogoutHandler(TokenService tokenService, RoseSecurityProperties properties, ApplicationEventPublisher publisher) {
        this.tokenService = tokenService;
        this.properties = properties;
        this.publisher = publisher;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String header = properties.getToken().getHeader();
        String token = request.getHeader(header);
        if (token != null && !token.isEmpty()) {
            Optional<String> user = tokenService.resolveUsername(token);
            tokenService.revoke(token);
            if (publisher != null && user.isPresent()) {
                publisher.publishEvent(new LogoutEvent(user.get()));
            }
        }
    }
}



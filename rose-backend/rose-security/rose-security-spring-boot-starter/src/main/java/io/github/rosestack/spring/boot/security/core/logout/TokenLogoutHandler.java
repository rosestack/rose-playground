package io.github.rosestack.spring.boot.security.core.logout;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class TokenLogoutHandler implements LogoutHandler {

    private final TokenService tokenService;
    private final RoseSecurityProperties properties;

    public TokenLogoutHandler(TokenService tokenService, RoseSecurityProperties properties) {
        this.tokenService = tokenService;
        this.properties = properties;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String header = properties.getToken().getHeader();
        String token = request.getHeader(header);
        if (token != null && !token.isEmpty()) {
            tokenService.revoke(token);
        }
    }
}



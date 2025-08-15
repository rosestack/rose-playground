package io.github.rosestack.spring.boot.security.account;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.token.TokenService;

public class SessionKickoutService {

    private final TokenService tokenService;
    private final RoseSecurityProperties properties;

    public SessionKickoutService(TokenService tokenService, RoseSecurityProperties properties) {
        this.tokenService = tokenService;
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.getAccount().getKickout().isEnabled();
    }

    /** 主动下线：根据 token 撤销（需开启开关） */
    public boolean kickByToken(String token) {
        if (!isEnabled()) return false;
        return tokenService.revoke(token);
    }
}



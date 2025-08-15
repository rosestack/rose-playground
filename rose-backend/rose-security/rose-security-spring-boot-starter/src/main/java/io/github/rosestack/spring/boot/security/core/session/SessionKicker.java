package io.github.rosestack.spring.boot.security.core.session;

import io.github.rosestack.spring.boot.security.core.token.TokenService;

public class SessionKicker {

    private final TokenService tokenService;

    public SessionKicker(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 主动下线：根据 token 撤销。
     * @return 是否撤销成功
     */
    public boolean kickByToken(String token) {
        return tokenService.revoke(token);
    }
}



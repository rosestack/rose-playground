package io.github.rosestack.spring.boot.security.core.service.impl;

import io.github.rosestack.spring.boot.security.core.domain.UserTokenInfo;
import io.github.rosestack.spring.boot.security.core.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Redis Token 服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class RedisTokenService implements TokenService {

    @Override
    public UserTokenInfo createToken(UserDetails userDetails) {
        return null;
    }

    @Override
    public boolean validateToken(String accessToken) {
        return false;
    }

    @Override
    public UserDetails getUserDetails(String accessToken) {
        return null;
    }

    @Override
    public UserTokenInfo refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public void revokeToken(String accessToken) {}

    @Override
    public void revokeAllTokens(String username) {}

    @Override
    public int getActiveTokenCount(String username) {
        return 0;
    }
}

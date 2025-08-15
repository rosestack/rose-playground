package io.github.rosestack.spring.boot.security.account;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.token.TokenService;

import java.util.Comparator;
import java.util.Map;

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
        if (!isEnabled()) {
            return false;
        }
        return tokenService.revoke(token);
    }

    /** 主动下线：根据用户名撤销所有令牌（需开启开关） */
    public boolean kickByUsername(String username) {
        if (!isEnabled()) {
            return false;
        }
        tokenService.revokeAllForUser(username);
        return true;
    }

    /** 单会话策略：撤销该用户除当前 token 外的其他令牌（若 concurrentLimit<=1，则保留最新） */
    public int enforceSingleSession(String username, String currentToken) {
        if (!isEnabled()) {
            return 0;
        }
        RoseSecurityProperties.Account.Kickout k = properties.getAccount().getKickout();
        if (k.getConcurrentLimit() > 1) {
            return 0;
        }
        // 保留最新：根据 findUserTokens 中的签发时间选出最新，然后撤销其他
        Map<String, Long> tokens = tokenService.findUserTokens(username);
        if (tokens.isEmpty()) {
            return 0;
        }
        String newest = tokens.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(currentToken);
        // 若当前 token 不是最新，也保留当前，因为它刚刚签发
        String keep = currentToken != null ? currentToken : newest;
        return tokenService.revokeOthers(username, keep);
    }
}

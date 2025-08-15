package io.github.rosestack.spring.boot.security.core.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(TokenService.class)
@ConditionalOnProperty(prefix = "rose.security.token", name = "type", havingValue = "LOCAL", matchIfMissing = true)
public class OpaqueTokenService implements TokenService {

    private final Cache<String, String> tokenToUser;
    private final long ttlSeconds;

    public OpaqueTokenService(RoseSecurityProperties props) {
        Duration ttl = props.getToken().getTtl();
        this.ttlSeconds = ttl.getSeconds();
        this.tokenToUser = Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String issue(String username) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, username);
        return token;
    }

    @Override
    public Optional<String> resolveUsername(String token) {
        return Optional.ofNullable(tokenToUser.getIfPresent(token));
    }

    @Override
    public boolean revoke(String token) {
        String existed = tokenToUser.getIfPresent(token);
        if (existed != null) {
            tokenToUser.invalidate(token);
            return true;
        }
        return false;
    }

    @Override
    public long getExpiresInSeconds() {
        return ttlSeconds;
    }
}



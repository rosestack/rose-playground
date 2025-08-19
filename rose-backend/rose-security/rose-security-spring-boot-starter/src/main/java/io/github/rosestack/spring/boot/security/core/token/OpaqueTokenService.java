package io.github.rosestack.spring.boot.security.core.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OpaqueTokenService implements TokenService {

	private final Cache<String, String> tokenToUser;
	private final long ttlSeconds;
	private final Cache<String, java.util.Map<String, Long>> userToTokens;

	public OpaqueTokenService(RoseSecurityProperties props) {
		Duration ttl = props.getToken().getTtl();
		this.ttlSeconds = ttl.getSeconds();
		this.tokenToUser = Caffeine.newBuilder()
			.expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
			.build();
		this.userToTokens = Caffeine.newBuilder()
			.expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
			.build();
	}

	@Override
	public String issue(String username) {
		String token = UUID.randomUUID().toString();
		tokenToUser.put(token, username);
		userToTokens.asMap().compute(username, (u, map) -> {
			java.util.Map<String, Long> m = (map == null ? new java.util.concurrent.ConcurrentHashMap<>() : map);
			m.put(token, System.currentTimeMillis());
			return m;
		});
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
			java.util.Map<String, Long> m = userToTokens.getIfPresent(existed);
			if (m != null) {
				m.remove(token);
				if (m.isEmpty()) userToTokens.invalidate(existed);
			}
			return true;
		}
		return false;
	}

	@Override
	public long getExpiresInSeconds() {
		return ttlSeconds;
	}

	@Override
	public void revokeAllForUser(String username) {
		// naive scan; acceptable for in-memory minimal impl
		java.util.Map<String, Long> m = userToTokens.getIfPresent(username);
		if (m != null) {
			for (String token : m.keySet()) {
				tokenToUser.invalidate(token);
			}
			userToTokens.invalidate(username);
		}
	}

	@Override
	public int revokeOthers(String username, String exceptToken) {
		final int[] count = {0};
		java.util.Map<String, Long> m = userToTokens.getIfPresent(username);
		if (m != null) {
			java.util.Iterator<String> it = m.keySet().iterator();
			while (it.hasNext()) {
				String t = it.next();
				if (!t.equals(exceptToken)) {
					tokenToUser.invalidate(t);
					it.remove();
					count[0]++;
				}
			}
			if (m.isEmpty()) userToTokens.invalidate(username);
		}
		return count[0];
	}

	@Override
	public java.util.Map<String, Long> findUserTokens(String username) {
		java.util.Map<String, Long> m = userToTokens.getIfPresent(username);
		return m == null ? java.util.Collections.emptyMap() : java.util.Collections.unmodifiableMap(m);
	}
}

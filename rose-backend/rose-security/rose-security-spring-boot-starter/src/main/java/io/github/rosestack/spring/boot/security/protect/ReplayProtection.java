package io.github.rosestack.spring.boot.security.protect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

public class ReplayProtection {

	private final RoseSecurityProperties.Protect.Replay props;
	private final Cache<String, Boolean> nonceCache;

	public ReplayProtection(RoseSecurityProperties properties) {
		this.props = properties.getProtect().getReplay();
		Duration window = props.getWindow();
		this.nonceCache = Caffeine.newBuilder().expireAfterWrite(window).build();
	}

	public boolean isEnabled() {
		return props.isEnabled();
	}

	public boolean check(HttpServletRequest request) {
		if (!isEnabled()) {
			return true;
		}
		String nonce = request.getHeader(props.getNonceHeader());
		String ts = request.getHeader(props.getTimestampHeader());
		if (nonce == null || ts == null) {
			return false;
		}
		if (nonceCache.getIfPresent(nonce) != null) {
			return false; // seen
		}
		nonceCache.put(nonce, Boolean.TRUE);
		return true;
	}
}

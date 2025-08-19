package io.github.rosestack.spring.boot.security.protect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {
	private final RoseSecurityProperties.Protect.RateLimit props;
	private final Cache<String, AtomicInteger> counters;

	public RateLimiter(RoseSecurityProperties properties) {
		this.props = properties.getProtect().getRateLimit();
		Duration window = props.getWindow();
		this.counters = Caffeine.newBuilder().expireAfterWrite(window).build();
	}

	public boolean isEnabled() {
		return props.isEnabled();
	}

	public boolean allow(HttpServletRequest request, String username) {
		if (!isEnabled()) {
			return true;
		}
		String key = (username != null ? username : ServletUtils.getClientIp(request));
		AtomicInteger counter = counters.get(key, k -> new AtomicInteger(0));
		int v = counter.incrementAndGet();
		return v <= props.getLimit();
	}
}

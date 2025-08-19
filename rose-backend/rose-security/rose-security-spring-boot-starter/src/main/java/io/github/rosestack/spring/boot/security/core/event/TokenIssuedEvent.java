package io.github.rosestack.spring.boot.security.core.event;

import lombok.Getter;
import org.springframework.security.core.Authentication;

import java.time.Instant;

@Getter
public class TokenIssuedEvent {
	private final Authentication authentication;
	private final String token;
	private final Instant issuedAt;

	public TokenIssuedEvent(Authentication authentication, String token) {
		this.authentication = authentication;
		this.token = token;
		this.issuedAt = Instant.now();
	}
}

package io.github.rosestack.spring.boot.security.core.event;

import lombok.Getter;

@Getter
public class TokenRevokedEvent {
	private final String username;
	private final String token;
	private final boolean manual;

	public TokenRevokedEvent(String username, String token, boolean manual) {
		this.username = username;
		this.token = token;
		this.manual = manual;
	}
}

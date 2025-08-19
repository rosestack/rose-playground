package io.github.rosestack.spring.boot.security.account;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.event.TokenRevokedEvent;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Comparator;
import java.util.Map;

public class TokenKickoutService {

	private final TokenService tokenService;
	private final RoseSecurityProperties properties;
	private final ApplicationEventPublisher publisher;

	public TokenKickoutService(TokenService tokenService, RoseSecurityProperties properties) {
		this(tokenService, properties, null);
	}

	public TokenKickoutService(
		TokenService tokenService, RoseSecurityProperties properties, ApplicationEventPublisher publisher) {
		this.tokenService = tokenService;
		this.properties = properties;
		this.publisher = publisher;
	}

	public boolean isEnabled() {
		return properties.getAccount().getKickout().isEnabled();
	}

	/**
	 * 主动下线：根据 token 撤销（需开启开关）
	 */
	public boolean kickByToken(String token) {
		if (!isEnabled()) {
			return false;
		}
		boolean ok = tokenService.revoke(token);
		if (ok && publisher != null) {
			tokenService
				.resolveUsername(token)
				.ifPresent(user -> publisher.publishEvent(new TokenRevokedEvent(user, token, true)));
		}
		return ok;
	}

	/**
	 * 主动下线：根据用户名撤销所有令牌（需开启开关）
	 */
	public boolean kickByUsername(String username) {
		if (!isEnabled()) {
			return false;
		}
		tokenService.findUserTokens(username).keySet().forEach(t -> {
			tokenService.revoke(t);
			if (publisher != null) publisher.publishEvent(new TokenRevokedEvent(username, t, true));
		});
		return true;
	}

	/**
	 * 单会话策略：撤销该用户除当前 token 外的其他令牌（若 concurrentLimit<=1，则保留最新）
	 */
	public int enforceSingleSession(String username, String currentToken) {
		if (!isEnabled()) {
			return 0;
		}
		RoseSecurityProperties.Account.Kickout k = properties.getAccount().getKickout();
		if (k.getConcurrentLimit() > 1) {
			return 0;
		}
		Map<String, Long> tokens = tokenService.findUserTokens(username);
		if (tokens.isEmpty()) {
			return 0;
		}
		String newest = tokens.entrySet().stream()
			.max(Comparator.comparingLong(Map.Entry::getValue))
			.map(Map.Entry::getKey)
			.orElse(currentToken);
		String keep = currentToken != null ? currentToken : newest;
		int before = tokens.size();
		// revoke others and publish
		tokens.keySet().stream().filter(t -> !t.equals(keep)).forEach(t -> {
			tokenService.revoke(t);
			if (publisher != null) publisher.publishEvent(new TokenRevokedEvent(username, t, false));
		});
		return Math.max(0, before - 1);
	}
}

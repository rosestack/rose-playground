package io.github.rosestack.spring.boot.security.core.token;

import java.util.Optional;

public interface TokenService {
	String issue(String username);

	Optional<String> resolveUsername(String token);

	boolean revoke(String token);

	long getExpiresInSeconds();

	/**
	 * 撤销指定用户的所有令牌
	 */
	void revokeAllForUser(String username);

	/**
	 * 撤销指定用户除某个令牌外的其他令牌，返回撤销数量
	 */
	int revokeOthers(String username, String exceptToken);

	/**
	 * 返回该用户的所有令牌及其签发时间（epoch millis）
	 */
	java.util.Map<String, Long> findUserTokens(String username);
}

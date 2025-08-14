package io.github.rosestack.spring.boot.security.core.service;

import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import java.util.Optional;

import io.github.rosestack.spring.boot.security.core.domain.UserTokenInfo;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Token 服务接口
 *
 * <p>提供 Token 的创建、验证、刷新和销毁等核心功能
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface TokenService {
    String TOKEN_HEADER = "X-Token";
    String TOKEN_TYPE_SIMPLE = "simple";

    /**
     * 创建 Token
     *
     * @param userDetails 用户信息
     * @return Token 信息
     */
    UserTokenInfo createToken(UserDetails userDetails);

    /**
     * 验证 accessToken
     *
     * @param accessToken 令牌
     * @return 验证是否通过
     */
    boolean validateToken(String accessToken);

    /**
     * 从 accessToken 获取用户信息
     *
     * @param accessToken 令牌
     * @return 用户信息（可能为空）
     */
    UserDetails getUserDetails(String accessToken);

    /**
     * 刷新 accessToken
     *
     * @param refreshToken 原令牌
     * @return 新的 Token 信息
     */
    UserTokenInfo refreshAccessToken(String refreshToken);

    /**
     * 撤销 accessToken
     *
     * @param accessToken 令牌
     */
    void revokeToken(String accessToken);

    /**
     * 撤销用户的所有 Token
     *
     * @param username 用户名
     */
    void revokeAllTokens(String username);

    /**
     * 获取用户当前活跃的 Token 数量
     *
     * @param username 用户名
     * @return Token 数量
     */
    int getActiveTokenCount(String username);
}

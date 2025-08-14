package io.github.rosestack.spring.boot.security.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Token 信息
 *
 * <p>封装认证 Token 的相关信息
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Builder
public class TokenInfo {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 用户名
     */
    @JsonIgnore
    private String username;

    /**
     * 创建时间
     */
    @JsonIgnore
    private LocalDateTime createdAt;

    /**
     * 是否已过期
     *
     * @return 是否过期
     */
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

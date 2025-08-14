package io.github.rosestack.spring.boot.security.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

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
     * 是否已过期
     *
     * @return 是否过期
     */
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

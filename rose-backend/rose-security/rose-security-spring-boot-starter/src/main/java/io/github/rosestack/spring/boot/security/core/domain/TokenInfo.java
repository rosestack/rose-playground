package io.github.rosestack.spring.boot.security.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfo implements Comparable<TokenInfo> {

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
     * 用户名
     */
    private String username;

    /**
     * 访问令牌过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 刷新令牌过期时间
     */
    private LocalDateTime refreshExpiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 访问令牌是否已过期
     *
     * @return 是否过期
     */
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 刷新令牌是否已过期
     *
     * @return 是否过期
     */
    @JsonIgnore
    public boolean isRefreshExpired() {
        return refreshExpiresAt != null && LocalDateTime.now().isAfter(refreshExpiresAt);
    }

    /**
     * 实现Comparable接口，支持按创建时间排序
     * 用于会话管理中的自动排序
     *
     * @param other 其他TokenInfo对象
     * @return 比较结果
     */
    @Override
    public int compareTo(TokenInfo other) {
        if (other == null) return 1;

        // 首先按创建时间排序，早创建的排在前面
        int timeCompare = this.createdAt.compareTo(other.createdAt);
        if (timeCompare != 0) return timeCompare;

        // 如果创建时间相同，按refreshToken排序保证唯一性
        return this.refreshToken.compareTo(other.refreshToken);
    }

    /**
     * 重写equals方法，基于refreshToken判断相等性
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenInfo tokenInfo = (TokenInfo) obj;
        return Objects.equals(refreshToken, tokenInfo.refreshToken);
    }

    /**
     * 重写hashCode方法，基于refreshToken计算哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(refreshToken);
    }
}

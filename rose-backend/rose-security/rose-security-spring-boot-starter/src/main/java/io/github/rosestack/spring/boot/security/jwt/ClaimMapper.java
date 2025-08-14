package io.github.rosestack.spring.boot.security.jwt;

import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 自定义 Claim 映射 SPI
 */
public interface ClaimMapper {
    /**
     * 根据用户信息生成自定义 Claims
     */
    Map<String, Object> toClaims(UserDetails userDetails);

    /**
     * 从 Claims 还原 UserDetails（如需自定义权限等）
     */
    UserDetails fromClaims(Map<String, Object> claims);
}

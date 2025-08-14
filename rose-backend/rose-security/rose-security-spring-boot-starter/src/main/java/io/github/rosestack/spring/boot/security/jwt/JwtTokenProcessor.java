package io.github.rosestack.spring.boot.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import io.github.rosestack.spring.boot.security.core.domain.TokenInfo;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT Token 处理器接口
 *
 * <p>负责JWT的创建、解析、验证等核心功能，消除重复代码，
 * 提供统一的JWT处理能力。</p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface JwtTokenProcessor {

    /**
     * 创建JWT Token（使用UserDetails）
     *
     * @param userDetails 用户详情
     * @return Token信息
     */
    TokenInfo createToken(UserDetails userDetails);

    UserDetails parseToken(String accessToken);

    /**
     * 解析并验证JWT Token
     *
     * @param accessToken JWT字符串
     * @return JWT声明集合
     * @throws JwtTokenExpiredException 当Token过期时
     * @throws JwtValidationException   当Token验证失败时
     */
    JWTClaimsSet parseAndValidate(String accessToken);
}

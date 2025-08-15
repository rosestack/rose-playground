package io.github.rosestack.spring.boot.security.core.support;

import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.github.rosestack.spring.boot.security.core.service.TokenService.TOKEN_HEADER;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Data
@Builder
public class RoseWebAuthenticationDetails implements Serializable {
    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理字符串
     */
    private String userAgent;

    /**
     * HTTP引用页面
     */
    private String referer;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * HTTP方法
     */
    private String httpMethod;

    // ========== 会话信息 ==========

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * Token信息
     */
    private String token;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;

    /**
     * 地理位置信息
     */
    private String location;

    /**
     * 是否为可信设备
     */
    @Builder.Default
    private Boolean trustedDevice = false;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 认证级别
     */
    private String authLevel;

    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }

    /**
     * 复制当前上下文
     */
    public RoseWebAuthenticationDetails copy() {
        return RoseWebAuthenticationDetails.builder()
                .clientIp(this.clientIp)
                .userAgent(this.userAgent)
                .referer(this.referer)
                .requestId(this.requestId)
                .requestUri(this.requestUri)
                .httpMethod(this.httpMethod)
                .sessionId(this.sessionId)
                .token(this.token)
                .deviceFingerprint(this.deviceFingerprint)
                .location(this.location)
                .trustedDevice(this.trustedDevice)
                .userId(this.userId)
                .authLevel(this.authLevel)
                .attributes(this.attributes != null ? new HashMap<>(this.attributes) : new HashMap<>())
                .build();
    }

    private static String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprint = new StringBuilder();

        // User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            fingerprint.append(userAgent.hashCode());
        }

        // Accept-Language
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            fingerprint.append("-").append(acceptLanguage.hashCode());
        }

        // Accept-Encoding
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (acceptEncoding != null) {
            fingerprint.append("-").append(acceptEncoding.hashCode());
        }

        return "FP-" + Math.abs(fingerprint.toString().hashCode());
    }

    public static RoseWebAuthenticationDetails fromRequest(HttpServletRequest request) {
        if (request == null) {
            return RoseWebAuthenticationDetails.builder().build();
        }

        return RoseWebAuthenticationDetails.builder()
                .clientIp(ServletUtils.getClientIp())
                .userAgent(ServletUtils.getUserAgent())
                .referer(ServletUtils.getReferer())
                .requestUri(request.getRequestURI())
                .httpMethod(request.getMethod())
                .requestId(ServletUtils.getRequestId())
                .sessionId(ServletUtils.getSessionId())
                .token(ServletUtils.getRequestHeader(TOKEN_HEADER))
                .deviceFingerprint(generateDeviceFingerprint(request))
                .build();
    }
}

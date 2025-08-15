package io.github.rosestack.spring.boot.security.core.support;

import io.github.rosestack.spring.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 安全上下文
 *
 * <p>统一收集和传递安全相关的上下文信息，包括：
 * <ul>
 *   <li>请求信息：IP地址、User-Agent、Referer等</li>
 *   <li>会话信息：会话ID、Token信息、设备指纹</li>
 *   <li>用户信息：用户名、用户ID、权限等</li>
 *   <li>操作信息：操作类型、结果、时间戳</li>
 * </ul>
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 从请求创建安全上下文
 * SecurityContext context = SecurityContext.fromRequest(request);
 *
 * // 设置用户信息
 * context.setUsername("user123");
 * context.setUserId("12345");
 *
 * // 设置操作信息
 * context.setOperation("LOGIN");
 * context.setSuccess(true);
 * }</pre>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@Builder
public class SecurityContext {

    // ========== 请求信息 ==========

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
    @Builder.Default
    private String requestId = ServletUtils.getRequestId();

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
     * 用户名
     */
    private String username;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 认证级别
     */
    private String authLevel;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 错误代码（失败时）
     */
    private String errorCode;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;

    /**
     * 风险评分（0-100）
     */
    private Integer riskScore;

    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 从HttpServletRequest创建安全上下文
     *
     * @param request HTTP请求对象
     * @return 安全上下文
     */
    public static SecurityContext fromRequest(HttpServletRequest request) {
        if (request == null) {
            return SecurityContext.builder().build();
        }

        return SecurityContext.builder()
                .clientIp(ServletUtils.getClientIp())
                .userAgent(ServletUtils.getUserAgent())
                .referer(ServletUtils.getReferer())
                .requestUri(request.getRequestURI())
                .httpMethod(request.getMethod())
                .sessionId(ServletUtils.getSessionId())
                .build();
    }

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
     * 标记操作成功
     */
    public SecurityContext markSuccess() {
        this.success = true;
        this.errorCode = null;
        this.errorMessage = null;
        return this;
    }

    /**
     * 标记操作失败
     */
    public SecurityContext markFailure(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * 设置风险评分
     */
    public SecurityContext withRiskScore(int score) {
        this.riskScore = Math.max(0, Math.min(100, score));
        return this;
    }

    /**
     * 复制当前上下文
     */
    public SecurityContext copy() {
        return SecurityContext.builder()
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
                .username(this.username)
                .userId(this.userId)
                .userRole(this.userRole)
                .authLevel(this.authLevel)
                .operation(this.operation)
                .success(this.success)
                .errorCode(this.errorCode)
                .errorMessage(this.errorMessage)
                .riskScore(this.riskScore)
                .attributes(this.attributes != null ? new HashMap<>(this.attributes) : new HashMap<>())
                .build();
    }
}

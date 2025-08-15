package io.github.rosestack.spring.boot.security.core.support;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 审计事件
 *
 * <p>记录系统中的安全相关事件，用于安全审计和合规性检查。
 * 支持从SecurityContext构建，包含丰富的上下文信息。</p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 基本用法
 * AuditEvent event = AuditEvent.loginSuccess("user123", Map.of("role", "admin"));
 *
 * // 从SecurityContext构建
 * SecurityContext context = SecurityContext.fromRequest(request);
 * context.setUsername("user123").markSuccess();
 * AuditEvent event = AuditEvent.fromSecurityContext(AuditEventType.LOGIN_SUCCESS, context);
 * }</pre>
 *
 * @author chensoul
 * @since 1.0.0
 */
@Data
@Builder
public class AuditEvent {

    /**
     * 事件类型
     */
    private AuditEventType eventType;

    /**
     * 事件时间戳
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * 事件ID（用于追踪）
     */
    private String eventId;

    /**
     * 安全上下文，包含请求、用户、会话等完整信息
     */
    private SecurityContext context;

    /**
     * 扩展详情
     */
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    /**
     * 从SecurityContext创建审计事件
     *
     * @param eventType 事件类型
     * @param context 安全上下文
     * @return 审计事件
     */
    public static AuditEvent fromSecurityContext(
            AuditEventType eventType, SecurityContext context, Map<String, Object> details) {
        return AuditEvent.builder()
                .eventType(eventType)
                .timestamp(Instant.now())
                .eventId(context != null ? context.getRequestId() : null)
                .context(context) // 直接设置整个上下文
                .details(details)
                .build();
    }

    public static AuditEvent fromSecurityContext(AuditEventType eventType, Map<String, Object> details) {
        return fromSecurityContext(eventType, null, details);
    }
    /**
     * 添加详情信息
     */
    public AuditEvent addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    /**
     * 获取事件分类
     */
    public String getCategory() {
        return eventType != null ? eventType.getCategory() : "未知";
    }

    /**
     * 获取事件描述
     */
    public String getDescription() {
        return eventType != null ? eventType.getDescription() : "";
    }

    /**
     * 判断是否为安全事件
     */
    public boolean isSecurityEvent() {
        return eventType != null && eventType.isSecurityEvent();
    }

    // ========== 便捷访问器方法（向后兼容） ==========

    /**
     * 获取用户名
     */
    public String getUsername() {
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取用户ID
     */
    public String getUserId() {
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取用户角色
     */
    public String getUserRole() {
        return context != null ? context.getUserRole() : null;
    }

    /**
     * 获取客户端IP地址
     */
    public String getIpAddress() {
        return context != null ? context.getClientIp() : null;
    }

    /**
     * 获取用户代理字符串
     */
    public String getUserAgent() {
        return context != null ? context.getUserAgent() : null;
    }

    /**
     * 获取请求ID
     */
    public String getRequestId() {
        return context != null ? context.getRequestId() : null;
    }

    /**
     * 获取会话ID
     */
    public String getSessionId() {
        return context != null ? context.getSessionId() : null;
    }

    /**
     * 获取设备指纹
     */
    public String getDeviceFingerprint() {
        return context != null ? context.getDeviceFingerprint() : null;
    }

    /**
     * 获取地理位置
     */
    public String getLocation() {
        return context != null ? context.getLocation() : null;
    }

    /**
     * 获取操作是否成功
     */
    public Boolean getSuccess() {
        return context != null ? context.getSuccess() : null;
    }

    /**
     * 获取错误代码
     */
    public String getErrorCode() {
        return context != null ? context.getErrorCode() : null;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        return context != null ? context.getErrorMessage() : null;
    }

    /**
     * 获取风险评分
     */
    public Integer getRiskScore() {
        return context != null ? context.getRiskScore() : null;
    }

    /**
     * 安全地获取客户端IP，避免在测试环境中抛出异常
     */
    private static String getClientIpSafe() {
        // 在测试环境或没有请求上下文时返回默认值
        // 在实际应用中，这个值会在从SecurityContext创建AuditEvent时被正确设置
        return "127.0.0.1";
    }
}

package io.github.rosestack.spring.boot.security.core.support;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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
    private RoseWebAuthenticationDetails context;

    /**
     * 扩展详情
     */
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    public static AuditEvent create(
            AuditEventType eventType, RoseWebAuthenticationDetails context, Map<String, Object> details) {
        return AuditEvent.builder()
                .eventType(eventType)
                .timestamp(Instant.now())
                .eventId(context != null ? context.getRequestId() : null)
                .context(context) // 直接设置整个上下文
                .details(details == null ? new HashMap<>() : details)
                .build();
    }

    public static AuditEvent create(AuditEventType eventType, Map<String, Object> details) {
        return create(
                eventType,
                (RoseWebAuthenticationDetails)
                        SecurityContextHolder.getContext().getAuthentication().getDetails(),
                details);
    }

    public static AuditEvent create(AuditEventType eventType) {
        return create(eventType, new HashMap<>());
    }

    public AuditEvent addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
}

package io.github.rosestack.spring.boot.security.extension;

import io.github.rosestack.spring.util.ServletUtils;
import java.time.Instant;
import java.util.Map;
import lombok.Data;

/**
 * 审计事件
 */
@Data
public class AuditEvent {
    private String type; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_REFRESH
    private String username;
    private String ip;
    private Instant timestamp;
    private Map<String, Object> details;

    private AuditEvent(String type, String username, Map<String, Object> details) {
        this.details = details;
        this.ip = ServletUtils.getClientIpAddress();
        this.timestamp = Instant.now();
        this.type = type;
        this.username = username;
    }

    public static AuditEvent loginSuccess(String username, Map<String, Object> details) {
        return new AuditEvent("LOGIN_SUCCESS", username, details);
    }

    public static AuditEvent loginFailure(String username, Map<String, Object> details) {
        return new AuditEvent("LOGIN_FAILURE", username, details);
    }

    public static AuditEvent logout(String username, Map<String, Object> details) {
        return new AuditEvent("LOGOUT", username, details);
    }

    public static AuditEvent tokenRefresh(String username, Map<String, Object> details) {
        return new AuditEvent("TOKEN_REFRESH", username, details);
    }
}

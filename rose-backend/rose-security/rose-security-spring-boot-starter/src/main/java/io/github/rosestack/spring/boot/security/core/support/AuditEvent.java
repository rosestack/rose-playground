package io.github.rosestack.spring.boot.security.core.support;

import io.github.rosestack.spring.util.ServletUtils;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 审计事件
 */
@Data
public class AuditEvent {
    private String type; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, LOCKOUT, KICKOUT
    private String username;
    private String ipAddress;
    private Instant timestamp;
    private Map<String, Object> details;

    private AuditEvent(String type, String username, Map<String, Object> details) {
        this.details = details;
        this.ipAddress = ServletUtils.getClientIpAddress();
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
}

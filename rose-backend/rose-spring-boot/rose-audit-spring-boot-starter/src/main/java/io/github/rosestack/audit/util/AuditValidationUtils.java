package io.github.rosestack.audit.util;

import io.github.rosestack.audit.entity.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审计验证工具类
 * <p>
 * 简化的验证工具类，使用 Spring Validation 和 Bean Validation 进行数据验证。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public final class AuditValidationUtils {

    private AuditValidationUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 验证审计日志主表数据
     *
     * @param auditLog 审计日志对象
     * @return 验证结果
     */
    public static ValidationResult validateAuditLog(AuditLog auditLog) {
        ValidationResult result = new ValidationResult();

        if (auditLog == null) {
            result.addError("审计日志对象不能为空");
            return result;
        }

        // 验证必填字段
        if (auditLog.getEventTime() == null) {
            result.addError("事件时间不能为空");
        }
        if (!StringUtils.hasText(auditLog.getEventType())) {
            result.addError("事件类型不能为空");
        }
        if (!StringUtils.hasText(auditLog.getStatus())) {
            result.addError("操作状态不能为空");
        }

        // 验证事件时间不能是未来时间
        if (auditLog.getEventTime() != null && auditLog.getEventTime().isAfter(LocalDateTime.now().plusMinutes(1))) {
            result.addWarning("事件时间不应该是未来时间");
        }

        return result;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }

        public String getFirstWarning() {
            return warnings.isEmpty() ? null : warnings.get(0);
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            if (!errors.isEmpty()) {
                sb.append("错误: ").append(String.join("; ", errors));
            }
            if (!warnings.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("警告: ").append(String.join("; ", warnings));
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return getSummary();
        }
    }
}
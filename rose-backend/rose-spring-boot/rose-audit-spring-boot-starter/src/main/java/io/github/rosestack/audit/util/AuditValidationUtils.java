package io.github.rosestack.audit.util;

import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.AuditRiskLevel;
import io.github.rosestack.audit.enums.AuditStatus;
import io.github.rosestack.core.jackson.JsonUtils;
import io.github.rosestack.mybatis.support.encryption.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 审计验证工具类
 * <p>
 * 提供审计日志数据的完整性验证功能，包括数据格式验证、业务规则验证、
 * 数据一致性验证等。确保审计数据的准确性和可靠性。
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
     * 常用的正则表达式模式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern URI_PATTERN = Pattern.compile("^/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*$");
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_]{10,50}$");

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
        validateRequiredFields(auditLog, result);

        // 验证字段格式
        validateFieldFormats(auditLog, result);

        // 验证业务规则
        validateBusinessRules(auditLog, result);

        // 验证数据一致性
        validateDataConsistency(auditLog, result);

        return result;
    }

    /**
     * 验证审计日志详情数据
     *
     * @param auditLogDetail 审计日志详情对象
     * @return 验证结果
     */
    public static ValidationResult validateAuditLogDetail(AuditLogDetail auditLogDetail) {
        ValidationResult result = new ValidationResult();

        if (auditLogDetail == null) {
            result.addError("审计日志详情对象不能为空");
            return result;
        }

        // 验证必填字段
        if (auditLogDetail.getAuditLogId() == null) {
            result.addError("审计日志ID不能为空");
        }

        if (!StringUtils.hasText(auditLogDetail.getDetailType())) {
            result.addError("详情类型不能为空");
        }

        if (!StringUtils.hasText(auditLogDetail.getDetailKey())) {
            result.addError("详情键不能为空");
        }

        // 验证详情键的有效性
        if (StringUtils.hasText(auditLogDetail.getDetailKey())) {
            AuditDetailKey detailKey = AuditDetailKey.fromCode(auditLogDetail.getDetailKey());
            if (detailKey == null) {
                result.addError("无效的详情键: " + auditLogDetail.getDetailKey());
            } else {
                // 验证敏感数据标记的一致性
                if (auditLogDetail.getIsSensitive() != null && 
                    auditLogDetail.getIsSensitive() != detailKey.isSensitive()) {
                    result.addWarning("敏感数据标记与详情键定义不一致");
                }
            }
        }

        // 验证JSON格式
        if (StringUtils.hasText(auditLogDetail.getDetailValue())) {
            if (!JsonUtils.isValidJson(auditLogDetail.getDetailValue())) {
                result.addWarning("详情值不是有效的JSON格式");
            }
        }

        return result;
    }

    /**
     * 验证数据完整性
     *
     * @param data         原始数据
     * @param expectedHash 期望的哈希值
     * @return 验证结果
     */
    public static ValidationResult validateDataIntegrity(String data, String expectedHash) {
        ValidationResult result = new ValidationResult();

        if (!StringUtils.hasText(data)) {
            result.addError("数据不能为空");
            return result;
        }

        if (!StringUtils.hasText(expectedHash)) {
            result.addError("期望的哈希值不能为空");
            return result;
        }

        try {
            boolean isValid = EncryptionUtils.verifyHash(data, expectedHash);
            if (!isValid) {
                result.addError("数据完整性验证失败，哈希值不匹配");
            }
        } catch (Exception e) {
            result.addError("数据完整性验证异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 验证审计日志链的完整性
     *
     * @param currentLog  当前日志
     * @param previousLog 前一条日志
     * @return 验证结果
     */
    public static ValidationResult validateAuditChain(AuditLog currentLog, AuditLog previousLog) {
        ValidationResult result = new ValidationResult();

        if (currentLog == null) {
            result.addError("当前审计日志不能为空");
            return result;
        }

        if (previousLog == null) {
            // 第一条日志，不需要验证链
            return result;
        }

        // 验证时间顺序
        if (currentLog.getEventTime() != null && previousLog.getEventTime() != null) {
            if (currentLog.getEventTime().isBefore(previousLog.getEventTime())) {
                result.addError("当前日志时间不能早于前一条日志时间");
            }
        }

        // 验证哈希链
        if (StringUtils.hasText(currentLog.getPrevHash()) && StringUtils.hasText(previousLog.getHashValue())) {
            if (!currentLog.getPrevHash().equals(previousLog.getHashValue())) {
                result.addError("审计日志链完整性验证失败，前一条记录哈希值不匹配");
            }
        }

        return result;
    }

    /**
     * 验证批量审计日志
     *
     * @param auditLogs 审计日志列表
     * @return 验证结果
     */
    public static ValidationResult validateAuditLogBatch(List<AuditLog> auditLogs) {
        ValidationResult result = new ValidationResult();

        if (auditLogs == null || auditLogs.isEmpty()) {
            result.addError("审计日志列表不能为空");
            return result;
        }

        for (int i = 0; i < auditLogs.size(); i++) {
            AuditLog auditLog = auditLogs.get(i);
            ValidationResult singleResult = validateAuditLog(auditLog);
            
            if (!singleResult.isValid()) {
                result.addError("第" + (i + 1) + "条日志验证失败: " + singleResult.getFirstError());
            }
        }

        return result;
    }

    /**
     * 验证必填字段
     */
    private static void validateRequiredFields(AuditLog auditLog, ValidationResult result) {
        if (auditLog.getEventTime() == null) {
            result.addError("事件时间不能为空");
        }

        if (!StringUtils.hasText(auditLog.getEventType())) {
            result.addError("事件类型不能为空");
        }

        if (!StringUtils.hasText(auditLog.getStatus())) {
            result.addError("操作状态不能为空");
        }

        if (!StringUtils.hasText(auditLog.getRiskLevel())) {
            result.addError("风险等级不能为空");
        }
    }

    /**
     * 验证字段格式
     */
    private static void validateFieldFormats(AuditLog auditLog, ValidationResult result) {
        // 验证IP地址格式
        if (StringUtils.hasText(auditLog.getClientIp()) && 
            !IP_PATTERN.matcher(auditLog.getClientIp()).matches()) {
            result.addWarning("客户端IP地址格式不正确: " + auditLog.getClientIp());
        }

        if (StringUtils.hasText(auditLog.getServerIp()) && 
            !IP_PATTERN.matcher(auditLog.getServerIp()).matches()) {
            result.addWarning("服务器IP地址格式不正确: " + auditLog.getServerIp());
        }

        // 验证URI格式
        if (StringUtils.hasText(auditLog.getRequestUri()) && 
            !URI_PATTERN.matcher(auditLog.getRequestUri()).matches()) {
            result.addWarning("请求URI格式不正确: " + auditLog.getRequestUri());
        }

        // 验证追踪ID格式
        if (StringUtils.hasText(auditLog.getTraceId()) && 
            !TRACE_ID_PATTERN.matcher(auditLog.getTraceId()).matches()) {
            result.addWarning("追踪ID格式不正确: " + auditLog.getTraceId());
        }

        // 验证HTTP状态码
        if (auditLog.getHttpStatus() != null) {
            int status = auditLog.getHttpStatus();
            if (status < 100 || status > 599) {
                result.addWarning("HTTP状态码超出有效范围: " + status);
            }
        }

        // 验证执行时间
        if (auditLog.getExecutionTime() != null && auditLog.getExecutionTime() < 0) {
            result.addWarning("执行时间不能为负数: " + auditLog.getExecutionTime());
        }
    }

    /**
     * 验证业务规则
     */
    private static void validateBusinessRules(AuditLog auditLog, ValidationResult result) {
        // 验证事件类型的有效性
        if (StringUtils.hasText(auditLog.getEventType()) && StringUtils.hasText(auditLog.getEventSubtype())) {
            AuditEventType eventType = auditLog.getEventTypeEnum();
            if (eventType == null) {
                result.addWarning("无效的事件类型组合: " + auditLog.getEventType() + " - " + auditLog.getEventSubtype());
            }
        }

        // 验证状态的有效性
        if (StringUtils.hasText(auditLog.getStatus())) {
            AuditStatus status = AuditStatus.fromCode(auditLog.getStatus());
            if (status == null) {
                result.addError("无效的操作状态: " + auditLog.getStatus());
            }
        }

        // 验证风险等级的有效性
        if (StringUtils.hasText(auditLog.getRiskLevel())) {
            AuditRiskLevel auditRiskLevel = AuditRiskLevel.fromCode(auditLog.getRiskLevel());
            if (auditRiskLevel == null) {
                result.addError("无效的风险等级: " + auditLog.getRiskLevel());
            }
        }

        // 验证时间逻辑
        if (auditLog.getEventTime() != null && auditLog.getCreatedAt() != null) {
            if (auditLog.getEventTime().isAfter(auditLog.getCreatedAt().plusMinutes(5))) {
                result.addWarning("事件时间不应该显著晚于创建时间");
            }
        }

        // 验证事件时间不能是未来时间
        if (auditLog.getEventTime() != null && auditLog.getEventTime().isAfter(LocalDateTime.now().plusMinutes(1))) {
            result.addWarning("事件时间不应该是未来时间");
        }
    }

    /**
     * 验证数据一致性
     */
    private static void validateDataConsistency(AuditLog auditLog, ValidationResult result) {
        // 验证HTTP状态码与操作状态的一致性
        if (auditLog.getHttpStatus() != null && StringUtils.hasText(auditLog.getStatus())) {
            int httpStatus = auditLog.getHttpStatus();
            String operationStatus = auditLog.getStatus();

            if (httpStatus >= 200 && httpStatus < 300 && "FAILURE".equals(operationStatus)) {
                result.addWarning("HTTP状态码表示成功，但操作状态为失败");
            } else if (httpStatus >= 400 && "SUCCESS".equals(operationStatus)) {
                result.addWarning("HTTP状态码表示失败，但操作状态为成功");
            }
        }

        // 验证风险等级与事件类型的一致性
        AuditEventType eventType = auditLog.getEventTypeEnum();
        AuditRiskLevel auditRiskLevel = auditLog.getRiskLevelEnum();
        
        if (eventType != null && auditRiskLevel != null) {
            AuditRiskLevel expectedAuditRiskLevel = AuditRiskLevel.fromEventType(eventType);
            if (auditRiskLevel.compareTo(expectedAuditRiskLevel) < 0) {
                result.addWarning("风险等级可能过低，建议使用: " + expectedAuditRiskLevel.getCode());
            }
        }
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
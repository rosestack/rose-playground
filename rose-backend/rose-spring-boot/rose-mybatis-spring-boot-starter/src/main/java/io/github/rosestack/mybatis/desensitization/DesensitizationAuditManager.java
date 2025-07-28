package io.github.rosestack.mybatis.desensitization;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 脱敏审计管理器
 * <p>
 * 记录和管理数据脱敏操作的审计日志，支持：
 * 1. 脱敏操作记录
 * 2. 访问用户追踪
 * 3. 脱敏规则变更记录
 * 4. 审计报告生成
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class DesensitizationAuditManager {

    /**
     * 脱敏审计日志存储
     */
    private final List<DesensitizationAuditLog> auditLogs = new CopyOnWriteArrayList<>();
    
    /**
     * 用户脱敏统计
     */
    private final Map<String, UserDesensitizationStats> userStats = new ConcurrentHashMap<>();
    
    /**
     * 字段脱敏统计
     */
    private final Map<String, FieldDesensitizationStats> fieldStats = new ConcurrentHashMap<>();

    /**
     * 记录脱敏操作
     *
     * @param userId       用户ID
     * @param userRole     用户角色
     * @param tableName    表名
     * @param fieldName    字段名
     * @param originalValue 原始值（仅记录长度和类型，不记录实际值）
     * @param desensitizedValue 脱敏后的值
     * @param rule         脱敏规则
     * @param ipAddress    访问IP
     */
    public void recordDesensitization(String userId, String userRole, String tableName, 
                                    String fieldName, String originalValue, String desensitizedValue, 
                                    String rule, String ipAddress) {
        
        DesensitizationAuditLog auditLog = new DesensitizationAuditLog();
        auditLog.setUserId(userId);
        auditLog.setUserRole(userRole);
        auditLog.setTableName(tableName);
        auditLog.setFieldName(fieldName);
        auditLog.setOriginalValueLength(originalValue != null ? originalValue.length() : 0);
        auditLog.setDesensitizedValue(desensitizedValue);
        auditLog.setRule(rule);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setSuccess(true);
        
        // 添加到审计日志
        auditLogs.add(auditLog);
        
        // 更新用户统计
        updateUserStats(userId, userRole, true);
        
        // 更新字段统计
        updateFieldStats(tableName, fieldName, rule, true);
        
        log.debug("记录脱敏操作: 用户={}, 表={}, 字段={}, 规则={}", userId, tableName, fieldName, rule);
    }

    /**
     * 记录脱敏失败
     */
    public void recordDesensitizationFailure(String userId, String userRole, String tableName, 
                                           String fieldName, String rule, String errorMessage, String ipAddress) {
        
        DesensitizationAuditLog auditLog = new DesensitizationAuditLog();
        auditLog.setUserId(userId);
        auditLog.setUserRole(userRole);
        auditLog.setTableName(tableName);
        auditLog.setFieldName(fieldName);
        auditLog.setRule(rule);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setSuccess(false);
        auditLog.setErrorMessage(errorMessage);
        
        auditLogs.add(auditLog);
        updateUserStats(userId, userRole, false);
        updateFieldStats(tableName, fieldName, rule, false);
        
        log.warn("脱敏操作失败: 用户={}, 表={}, 字段={}, 错误={}", userId, tableName, fieldName, errorMessage);
    }

    /**
     * 获取用户脱敏审计日志
     */
    public List<DesensitizationAuditLog> getUserAuditLogs(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogs.stream()
                .filter(log -> userId.equals(log.getUserId()))
                .filter(log -> log.getTimestamp().isAfter(startTime) && log.getTimestamp().isBefore(endTime))
                .collect(Collectors.toList());
    }

    /**
     * 获取字段脱敏审计日志
     */
    public List<DesensitizationAuditLog> getFieldAuditLogs(String tableName, String fieldName, 
                                                          LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogs.stream()
                .filter(log -> tableName.equals(log.getTableName()) && fieldName.equals(log.getFieldName()))
                .filter(log -> log.getTimestamp().isAfter(startTime) && log.getTimestamp().isBefore(endTime))
                .collect(Collectors.toList());
    }

    /**
     * 生成脱敏审计报告
     */
    public DesensitizationAuditReport generateAuditReport(LocalDateTime startTime, LocalDateTime endTime) {
        List<DesensitizationAuditLog> periodLogs = auditLogs.stream()
                .filter(log -> log.getTimestamp().isAfter(startTime) && log.getTimestamp().isBefore(endTime))
                .collect(Collectors.toList());
        
        DesensitizationAuditReport report = new DesensitizationAuditReport();
        report.setStartTime(startTime);
        report.setEndTime(endTime);
        report.setTotalOperations(periodLogs.size());
        report.setSuccessfulOperations((int) periodLogs.stream().filter(DesensitizationAuditLog::isSuccess).count());
        report.setFailedOperations(report.getTotalOperations() - report.getSuccessfulOperations());
        
        // 按用户统计
        Map<String, Long> userOperations = periodLogs.stream()
                .collect(Collectors.groupingBy(DesensitizationAuditLog::getUserId, Collectors.counting()));
        report.setUserOperations(userOperations);
        
        // 按字段统计
        Map<String, Long> fieldOperations = periodLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getTableName() + "." + log.getFieldName(), Collectors.counting()));
        report.setFieldOperations(fieldOperations);
        
        // 按规则统计
        Map<String, Long> ruleOperations = periodLogs.stream()
                .collect(Collectors.groupingBy(DesensitizationAuditLog::getRule, Collectors.counting()));
        report.setRuleOperations(ruleOperations);
        
        // 按IP统计
        Map<String, Long> ipOperations = periodLogs.stream()
                .collect(Collectors.groupingBy(DesensitizationAuditLog::getIpAddress, Collectors.counting()));
        report.setIpOperations(ipOperations);
        
        return report;
    }

    /**
     * 检测异常脱敏行为
     */
    public List<AnomalousDesensitizationAlert> detectAnomalousActivity() {
        List<AnomalousDesensitizationAlert> alerts = new CopyOnWriteArrayList<>();
        
        // 检测频繁访问
        userStats.forEach((userId, stats) -> {
            if (stats.getRecentOperations() > 1000) { // 阈值可配置
                AnomalousDesensitizationAlert alert = new AnomalousDesensitizationAlert();
                alert.setType("FREQUENT_ACCESS");
                alert.setUserId(userId);
                alert.setDescription("用户频繁访问敏感数据: " + stats.getRecentOperations() + " 次");
                alert.setTimestamp(LocalDateTime.now());
                alerts.add(alert);
            }
        });
        
        // 检测失败率过高
        userStats.forEach((userId, stats) -> {
            if (stats.getTotalOperations() > 10 && stats.getFailureRate() > 0.5) {
                AnomalousDesensitizationAlert alert = new AnomalousDesensitizationAlert();
                alert.setType("HIGH_FAILURE_RATE");
                alert.setUserId(userId);
                alert.setDescription("用户脱敏失败率过高: " + String.format("%.2f%%", stats.getFailureRate() * 100));
                alert.setTimestamp(LocalDateTime.now());
                alerts.add(alert);
            }
        });
        
        return alerts;
    }

    /**
     * 更新用户统计
     */
    private void updateUserStats(String userId, String userRole, boolean success) {
        UserDesensitizationStats stats = userStats.computeIfAbsent(userId, k -> {
            UserDesensitizationStats newStats = new UserDesensitizationStats();
            newStats.setUserId(userId);
            newStats.setUserRole(userRole);
            return newStats;
        });
        
        stats.incrementTotal();
        if (!success) {
            stats.incrementFailures();
        }
        stats.updateRecentActivity();
    }

    /**
     * 更新字段统计
     */
    private void updateFieldStats(String tableName, String fieldName, String rule, boolean success) {
        String key = tableName + "." + fieldName;
        FieldDesensitizationStats stats = fieldStats.computeIfAbsent(key, k -> {
            FieldDesensitizationStats newStats = new FieldDesensitizationStats();
            newStats.setTableName(tableName);
            newStats.setFieldName(fieldName);
            return newStats;
        });
        
        stats.incrementTotal();
        if (!success) {
            stats.incrementFailures();
        }
        stats.addRule(rule);
    }

    /**
     * 清理过期审计日志
     */
    public void cleanupExpiredLogs(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        auditLogs.removeIf(log -> log.getTimestamp().isBefore(cutoffTime));
        log.info("清理了 {} 天前的脱敏审计日志", retentionDays);
    }

    /**
     * 脱敏审计日志
     */
    @Data
    public static class DesensitizationAuditLog {
        private String userId;
        private String userRole;
        private String tableName;
        private String fieldName;
        private int originalValueLength;
        private String desensitizedValue;
        private String rule;
        private String ipAddress;
        private LocalDateTime timestamp;
        private boolean success;
        private String errorMessage;
    }

    /**
     * 用户脱敏统计
     */
    @Data
    public static class UserDesensitizationStats {
        private String userId;
        private String userRole;
        private long totalOperations;
        private long failedOperations;
        private long recentOperations; // 最近1小时的操作数
        private LocalDateTime lastActivity;
        
        public void incrementTotal() { totalOperations++; }
        public void incrementFailures() { failedOperations++; }
        public double getFailureRate() { return totalOperations > 0 ? (double) failedOperations / totalOperations : 0; }
        public void updateRecentActivity() { 
            lastActivity = LocalDateTime.now();
            // 这里应该实现滑动窗口计算最近操作数
            recentOperations++;
        }
    }

    /**
     * 字段脱敏统计
     */
    @Data
    public static class FieldDesensitizationStats {
        private String tableName;
        private String fieldName;
        private long totalOperations;
        private long failedOperations;
        private Map<String, Long> ruleUsage = new ConcurrentHashMap<>();
        
        public void incrementTotal() { totalOperations++; }
        public void incrementFailures() { failedOperations++; }
        public void addRule(String rule) { ruleUsage.merge(rule, 1L, Long::sum); }
        public double getFailureRate() { return totalOperations > 0 ? (double) failedOperations / totalOperations : 0; }
    }

    /**
     * 脱敏审计报告
     */
    @Data
    public static class DesensitizationAuditReport {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int totalOperations;
        private int successfulOperations;
        private int failedOperations;
        private Map<String, Long> userOperations;
        private Map<String, Long> fieldOperations;
        private Map<String, Long> ruleOperations;
        private Map<String, Long> ipOperations;
    }

    /**
     * 异常脱敏行为告警
     */
    @Data
    public static class AnomalousDesensitizationAlert {
        private String type;
        private String userId;
        private String description;
        private LocalDateTime timestamp;
    }
}

package io.github.rosestack.audit.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rosestack.audit.config.AuditProperties;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.enums.AuditEventType;
import io.github.rosestack.audit.enums.AuditRiskLevel;
import io.github.rosestack.audit.mapper.AuditLogMapper;
import io.github.rosestack.audit.service.AuditLogService;
import io.github.rosestack.audit.util.AuditValidationUtils;
import io.github.rosestack.core.util.ServletUtils;
import io.github.rosestack.mybatis.support.encryption.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现类
 * <p>
 * 提供审计日志的完整业务功能实现，包括记录、查询、统计、分析等。
 * 支持同步和异步处理，确保高性能和可靠性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final AuditProperties auditProperties;

    @Override
    public AuditLog recordAuditLog(AuditLog auditLog) {
        log.debug("开始记录审计日志: {}", auditLog.getOperationName());

        try {
            // 数据验证
            validateAuditLog(auditLog);

            // 补充上下文信息
            enrichAuditLogContext(auditLog);

            // 生成哈希值
            generateHashValues(auditLog);

            // 保存到数据库
            boolean success = save(auditLog);
            if (!success) {
                throw new RuntimeException("保存审计日志失败");
            }

            log.debug("审计日志记录成功，ID: {}", auditLog.getId());
            return auditLog;

        } catch (Exception e) {
            log.error("记录审计日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("记录审计日志失败", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<AuditLog> recordAuditLogAsync(AuditLog auditLog) {
        return CompletableFuture.supplyAsync(() -> recordAuditLog(auditLog));
    }

    @Override
    public boolean recordAuditLogBatch(List<AuditLog> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            log.warn("批量记录审计日志：日志列表为空");
            return true;
        }

        log.debug("开始批量记录审计日志，数量: {}", auditLogs.size());

        try {
            // 批量验证和处理
            for (AuditLog auditLog : auditLogs) {
                validateAuditLog(auditLog);
                enrichAuditLogContext(auditLog);
                generateHashValues(auditLog);
            }

            // 批量保存
            int batchSize = auditProperties.getStorage().getBatchSize();
            boolean success = saveBatch(auditLogs, batchSize);

            if (success) {
                log.debug("批量记录审计日志成功，数量: {}", auditLogs.size());
            } else {
                log.error("批量记录审计日志失败");
            }

            return success;

        } catch (Exception e) {
            log.error("批量记录审计日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量记录审计日志失败", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<Boolean> recordAuditLogBatchAsync(List<AuditLog> auditLogs) {
        return CompletableFuture.supplyAsync(() -> recordAuditLogBatch(auditLogs));
    }

    @Override
    public Long recordSimpleAuditLog(AuditEventType eventType, String operationName,
                                     String userId, String requestUri, String status) {
        AuditLog auditLog = AuditLog.builder()
                .eventTime(LocalDateTime.now())
                .operationName(operationName)
                .userId(userId)
                .requestUri(requestUri)
                .status(status)
                .build();

        // 设置事件类型
        auditLog.setEventType(eventType);

        // 设置风险等级
        AuditRiskLevel auditRiskLevel = AuditRiskLevel.fromEventType(eventType);
        auditLog.setRiskLevel(auditRiskLevel);

        // 记录日志
        AuditLog savedLog = recordAuditLog(auditLog);
        return savedLog.getId();
    }

    // ==================== 查询审计日志 ====================

    @Override
    @Cacheable(value = "auditLog", key = "'user:' + #userId")
    public List<AuditLog> findByUserId(String userId) {
        return auditLogMapper.selectByUserId(userId);
    }

    @Override
    public IPage<AuditLog> findByUserIdWithPage(String userId, Page<AuditLog> page) {
        return auditLogMapper.selectPageByTenantId(page, userId);
    }

    @Override
    @Cacheable(value = "auditLog", key = "'eventType:' + #eventType")
    public List<AuditLog> findByEventType(String eventType) {
        return auditLogMapper.selectByEventType(eventType);
    }

    @Override
    public List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public IPage<AuditLog> findByTimeRangeWithPage(LocalDateTime startTime, LocalDateTime endTime, Page<AuditLog> page) {
        return auditLogMapper.selectPageByTimeRange(page, startTime, endTime);
    }

    @Override
    public IPage<AuditLog> findByConditions(String userId, String eventType, String riskLevel,
                                            String status, LocalDateTime startTime, LocalDateTime endTime,
                                            Page<AuditLog> page) {
        return auditLogMapper.selectPageByConditions(page, userId, eventType, riskLevel, status, startTime, endTime);
    }

    @Override
    public List<AuditLog> findHighRiskLogs(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectHighRiskLogs(startTime, endTime);
    }

    @Override
    public List<AuditLog> findFailedOperations(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectFailedOperations(startTime, endTime);
    }

    @Override
    public List<AuditLog> findSecurityEvents(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectSecurityEvents(startTime, endTime);
    }

    // ==================== 统计分析 ====================

    @Override
    public Long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByTimeRange(startTime, endTime);
    }

    @Override
    public Long countByUserId(String userId) {
        return auditLogMapper.countByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> countByEventType(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByEventType(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByRiskLevel(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByRiskLevel(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByStatus(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByStatus(startTime, endTime);
    }

    @Override
    public Map<String, Object> getUserBehaviorAnalysis(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> analysis = new HashMap<>();

        // 基础统计
        Long totalLogs = auditLogMapper.countByUserId(userId);
        List<AuditLog> userLogs = auditLogMapper.selectByUserAndTimeRange(userId, startTime, endTime);

        analysis.put("totalLogs", totalLogs);
        analysis.put("periodLogs", userLogs.size());

        // 活动时间分析
        Map<Integer, Long> hourlyActivity = userLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getEventTime().getHour(),
                        Collectors.counting()
                ));
        analysis.put("hourlyActivity", hourlyActivity);

        // 操作类型分析
        Map<String, Long> operationTypes = userLogs.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getEventType,
                        Collectors.counting()
                ));
        analysis.put("operationTypes", operationTypes);

        // 风险等级分析
        Map<String, Long> riskLevels = userLogs.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getRiskLevel,
                        Collectors.counting()
                ));
        analysis.put("riskLevels", riskLevels);

        // 成功率分析
        long successCount = userLogs.stream()
                .mapToLong(log -> log.isSuccess() ? 1 : 0)
                .sum();
        double successRate = userLogs.isEmpty() ? 0.0 : (double) successCount / userLogs.size();
        analysis.put("successRate", successRate);

        return analysis;
    }

    @Override
    public Map<String, Object> getSecurityRiskAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> analysis = new HashMap<>();

        // 高风险事件统计
        List<AuditLog> highRiskLogs = findHighRiskLogs(startTime, endTime);
        analysis.put("highRiskEventCount", highRiskLogs.size());

        // 安全事件统计
        List<AuditLog> securityEvents = findSecurityEvents(startTime, endTime);
        analysis.put("securityEventCount", securityEvents.size());

        // 失败操作统计
        List<AuditLog> failedOperations = findFailedOperations(startTime, endTime);
        analysis.put("failedOperationCount", failedOperations.size());

        // 风险等级分布
        List<Map<String, Object>> riskDistribution = countByRiskLevel(startTime, endTime);
        analysis.put("riskDistribution", riskDistribution);

        // 异常IP分析
        Map<String, Long> ipActivity = highRiskLogs.stream()
                .filter(log -> StringUtils.hasText(log.getClientIp()))
                .collect(Collectors.groupingBy(
                        AuditLog::getClientIp,
                        Collectors.counting()
                ));
        analysis.put("suspiciousIps", ipActivity);

        // 异常用户分析
        Map<String, Long> userActivity = highRiskLogs.stream()
                .filter(log -> StringUtils.hasText(log.getUserId()))
                .collect(Collectors.groupingBy(
                        AuditLog::getUserId,
                        Collectors.counting()
                ));
        analysis.put("suspiciousUsers", userActivity);

        return analysis;
    }

    @Override
    public List<Map<String, Object>> getActivityTrend(LocalDateTime startTime, LocalDateTime endTime, String interval) {
        List<AuditLog> logs = findByTimeRange(startTime, endTime);

        ChronoUnit unit;
        switch (interval.toUpperCase()) {
            case "HOUR":
                unit = ChronoUnit.HOURS;
                break;
            case "DAY":
                unit = ChronoUnit.DAYS;
                break;
            case "WEEK":
                unit = ChronoUnit.WEEKS;
                break;
            case "MONTH":
                unit = ChronoUnit.MONTHS;
                break;
            default:
                unit = ChronoUnit.DAYS;
        }

        // 按时间间隔分组统计
        Map<LocalDateTime, Long> trendData = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getEventTime().truncatedTo(unit),
                        Collectors.counting()
                ));

        // 转换为列表格式
        return trendData.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("time", entry.getKey());
                    point.put("count", entry.getValue());
                    return point;
                })
                .sorted((a, b) -> ((LocalDateTime) a.get("time")).compareTo((LocalDateTime) b.get("time")))
                .collect(Collectors.toList());
    }

    // ==================== 数据管理 ====================

    @Override
    public int cleanupExpiredLogs(LocalDateTime expireTime, int batchSize) {
        log.info("开始清理过期审计日志，过期时间: {}, 批量大小: {}", expireTime, batchSize);

        int totalDeleted = 0;
        List<Long> expiredIds;

        do {
            expiredIds = auditLogMapper.selectExpiredLogIds(expireTime, batchSize);
            if (!expiredIds.isEmpty()) {
                int deleted = auditLogMapper.deleteBatchIds(expiredIds);
                totalDeleted += deleted;
                log.debug("删除过期审计日志 {} 条", deleted);
            }
        } while (expiredIds.size() == batchSize);

        log.info("清理过期审计日志完成，共删除 {} 条记录", totalDeleted);
        return totalDeleted;
    }

    @Override
    public int archiveAuditLogs(LocalDateTime archiveTime, int batchSize) {
        // 这里可以实现归档逻辑，比如移动到归档表或导出到文件
        log.info("归档审计日志功能待实现，归档时间: {}, 批量大小: {}", archiveTime, batchSize);
        return 0;
    }

    @Override
    public Map<String, Object> validateAuditChain(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        List<AuditLog> logs = findByTimeRange(startTime, endTime);

        int totalLogs = logs.size();
        int validChains = 0;
        int brokenChains = 0;

        for (int i = 1; i < logs.size(); i++) {
            AuditLog currentLog = logs.get(i);
            AuditLog previousLog = logs.get(i - 1);

            AuditValidationUtils.ValidationResult chainResult =
                    AuditValidationUtils.validateAuditChain(currentLog, previousLog);

            if (chainResult.isValid()) {
                validChains++;
            } else {
                brokenChains++;
                log.warn("发现断裂的审计链: 当前日志ID {}, 前一条日志ID {}, 错误: {}",
                        currentLog.getId(), previousLog.getId(), chainResult.getFirstError());
            }
        }

        result.put("totalLogs", totalLogs);
        result.put("validChains", validChains);
        result.put("brokenChains", brokenChains);
        result.put("chainIntegrity", totalLogs > 0 ? (double) validChains / (totalLogs - 1) : 1.0);

        return result;
    }

    @Override
    public Map<String, Object> rebuildAuditChain(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("开始重建审计日志链，时间范围: {} - {}", startTime, endTime);

        List<AuditLog> logs = findByTimeRange(startTime, endTime);
        int rebuiltCount = 0;

        for (int i = 0; i < logs.size(); i++) {
            AuditLog currentLog = logs.get(i);

            // 重新生成哈希值
            generateHashValues(currentLog);

            // 设置前一条记录的哈希值
            if (i > 0) {
                AuditLog previousLog = logs.get(i - 1);
                currentLog.setPrevHash(previousLog.getHashValue());
            }

            // 更新记录
            updateById(currentLog);
            rebuiltCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rebuiltCount", rebuiltCount);
        result.put("totalLogs", logs.size());

        log.info("重建审计日志链完成，重建 {} 条记录", rebuiltCount);
        return result;
    }

    // ==================== 租户相关 ====================

    @Override
    public List<AuditLog> findByTenantId(String tenantId) {
        return auditLogMapper.selectByTenantId(tenantId);
    }

    @Override
    public IPage<AuditLog> findByTenantIdWithPage(String tenantId, Page<AuditLog> page) {
        return auditLogMapper.selectPageByTenantId(page, tenantId);
    }

    @Override
    public Long countByTenantId(String tenantId) {
        return auditLogMapper.countByTenantId(tenantId);
    }

    @Override
    public Map<String, Object> getTenantAuditReport(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();

        // 基础统计
        Long totalLogs = countByTenantId(tenantId);
        report.put("totalLogs", totalLogs);

        // 时间段内的日志
        List<AuditLog> periodLogs = auditLogMapper.selectByTimeRange(startTime, endTime)
                .stream()
                .filter(log -> tenantId.equals(log.getTenantId()))
                .collect(Collectors.toList());
        report.put("periodLogs", periodLogs.size());

        // 事件类型分布
        Map<String, Long> eventTypes = periodLogs.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getEventType,
                        Collectors.counting()
                ));
        report.put("eventTypes", eventTypes);

        // 风险等级分布
        Map<String, Long> riskLevels = periodLogs.stream()
                .collect(Collectors.groupingBy(
                        AuditLog::getRiskLevel,
                        Collectors.counting()
                ));
        report.put("riskLevels", riskLevels);

        return report;
    }

    // ==================== 缓存管理 ====================

    @Override
    @CacheEvict(value = "auditLog", allEntries = true)
    public void clearCache() {
        log.info("清空审计日志缓存");
    }

    @Override
    public void warmupCache(String tenantId) {
        log.info("预热审计日志缓存，租户: {}", tenantId);
        if (StringUtils.hasText(tenantId)) {
            findByTenantId(tenantId);
        }
    }

    @Override
    public Map<String, Object> getCacheStats() {
        // 这里可以集成缓存统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheProvider", "Spring Cache");
        stats.put("cacheName", "auditLog");
        return stats;
    }

    /**
     * 验证审计日志数据
     */
    private void validateAuditLog(AuditLog auditLog) {
        AuditValidationUtils.ValidationResult result = AuditValidationUtils.validateAuditLog(auditLog);
        if (!result.isValid()) {
            throw new IllegalArgumentException("审计日志数据验证失败: " + result.getFirstError());
        }
        if (result.hasWarnings()) {
            log.warn("审计日志数据验证警告: {}", result.getFirstWarning());
        }
    }

    /**
     * 补充审计日志上下文信息
     */
    private void enrichAuditLogContext(AuditLog auditLog) {
        // 设置事件时间
        if (auditLog.getEventTime() == null) {
            auditLog.setEventTime(LocalDateTime.now());
        }

        // 设置创建时间
        if (auditLog.getCreatedTime() == null) {
            auditLog.setCreatedTime(LocalDateTime.now());
        }

        // 补充上下文信息
        if (!StringUtils.hasText(auditLog.getUserId())) {
            auditLog.setUserId(ServletUtils.getCurrentUserId());
        }
        if (!StringUtils.hasText(auditLog.getUserName())) {
            auditLog.setUserName(ServletUtils.getCurrentUsername());
        }
        if (!StringUtils.hasText(auditLog.getTenantId())) {
            auditLog.setTenantId(ServletUtils.getCurrentTenantId());
        }
        if (!StringUtils.hasText(auditLog.getClientIp())) {
            auditLog.setClientIp(ServletUtils.getClientIpAddress());
        }
        if (!StringUtils.hasText(auditLog.getServerIp())) {
            auditLog.setServerIp(ServletUtils.getCurrentRequest().getServerName());
        }
        if (!StringUtils.hasText(auditLog.getTraceId())) {
            auditLog.setTraceId(ServletUtils.getTraceId());
        }
        if (!StringUtils.hasText(auditLog.getRequestUri())) {
            auditLog.setRequestUri(ServletUtils.getCurrentRequest().getRequestURI());
        }
        if (!StringUtils.hasText(auditLog.getHttpMethod())) {
            auditLog.setHttpMethod(ServletUtils.getCurrentRequest().getMethod());
        }
        if (!StringUtils.hasText(auditLog.getSessionId())) {
            auditLog.setSessionId(ServletUtils.getSessionId());
        }
        if (!StringUtils.hasText(auditLog.getUserAgent())) {
            auditLog.setUserAgent(ServletUtils.getUserAgent());
        }
    }

    /**
     * 生成哈希值
     */
    private void generateHashValues(AuditLog auditLog) {
        // 生成当前记录的哈希值
        String dataForHash = buildHashData(auditLog);
        String hashValue = EncryptionUtils.generateHash(dataForHash);
        auditLog.setHashValue(hashValue);

        // 设置数字签名（如果需要）
        auditLog.setDigitalSignature(hashValue);
    }

    /**
     * 构建用于哈希的数据
     */
    private String buildHashData(AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();
        sb.append(auditLog.getEventTime());
        sb.append(auditLog.getEventType());
        sb.append(auditLog.getEventSubtype());
        sb.append(auditLog.getOperationName());
        sb.append(auditLog.getUserId());
        sb.append(auditLog.getRequestUri());
        sb.append(auditLog.getStatus());
        return sb.toString();
    }
}
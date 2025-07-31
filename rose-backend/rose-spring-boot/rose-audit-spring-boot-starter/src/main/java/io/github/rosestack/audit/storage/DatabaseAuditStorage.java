package io.github.rosestack.audit.storage;

import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.properties.AuditProperties;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据库审计存储实现
 * <p>
 * 使用数据库作为审计日志的存储介质，提供高可靠性和查询能力。
 * 支持同步和异步存储，批量处理，统计监控等功能。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseAuditStorage implements AuditStorage {

    private final AuditLogService auditLogService;
    private final AuditLogDetailService auditLogDetailService;
    private final AuditProperties auditProperties;

    // 统计信息
    private final AtomicLong totalStored = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private volatile String lastError;
    private volatile long lastResponseTime;

    @Override
    public AuditLog store(AuditLog auditLog) {
        long startTime = System.currentTimeMillis();
        try {
            AuditLog result = auditLogService.recordAuditLog(auditLog);
            totalStored.incrementAndGet();
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.debug("数据库存储审计日志成功，ID: {}, 耗时: {}ms", result.getId(), lastResponseTime);
            return result;
        } catch (Exception e) {
            totalFailed.incrementAndGet();
            lastError = e.getMessage();
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.error("数据库存储审计日志失败，耗时: {}ms, 错误: {}", lastResponseTime, e.getMessage(), e);
            throw new RuntimeException("数据库存储审计日志失败", e);
        }
    }

    @Override
    public CompletableFuture<AuditLog> storeAsync(AuditLog auditLog) {
        return auditLogService.recordAuditLogAsync(auditLog)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        totalStored.incrementAndGet();
                        log.debug("数据库异步存储审计日志成功，ID: {}", result.getId());
                    } else {
                        totalFailed.incrementAndGet();
                        lastError = throwable.getMessage();
                        log.error("数据库异步存储审计日志失败: {}", throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    public boolean storeBatch(List<AuditLog> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return true;
        }

        long startTime = System.currentTimeMillis();
        try {
            boolean result = auditLogService.recordAuditLogBatch(auditLogs);
            if (result) {
                totalStored.addAndGet(auditLogs.size());
            } else {
                totalFailed.addAndGet(auditLogs.size());
            }
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.debug("数据库批量存储审计日志完成，数量: {}, 成功: {}, 耗时: {}ms", 
                    auditLogs.size(), result, lastResponseTime);
            return result;
        } catch (Exception e) {
            totalFailed.addAndGet(auditLogs.size());
            lastError = e.getMessage();
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.error("数据库批量存储审计日志失败，数量: {}, 耗时: {}ms, 错误: {}", 
                    auditLogs.size(), lastResponseTime, e.getMessage(), e);
            throw new RuntimeException("数据库批量存储审计日志失败", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> storeBatchAsync(List<AuditLog> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }

        return auditLogService.recordAuditLogBatchAsync(auditLogs)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        if (result) {
                            totalStored.addAndGet(auditLogs.size());
                        } else {
                            totalFailed.addAndGet(auditLogs.size());
                        }
                        log.debug("数据库异步批量存储审计日志完成，数量: {}, 成功: {}", auditLogs.size(), result);
                    } else {
                        totalFailed.addAndGet(auditLogs.size());
                        lastError = throwable.getMessage();
                        log.error("数据库异步批量存储审计日志失败，数量: {}, 错误: {}", 
                                auditLogs.size(), throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    public AuditLogDetail storeDetail(AuditLogDetail auditLogDetail) {
        long startTime = System.currentTimeMillis();
        try {
            AuditLogDetail result = auditLogDetailService.recordAuditDetail(auditLogDetail);
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.debug("数据库存储审计详情成功，ID: {}, 耗时: {}ms", result.getId(), lastResponseTime);
            return result;
        } catch (Exception e) {
            lastError = e.getMessage();
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.error("数据库存储审计详情失败，耗时: {}ms, 错误: {}", lastResponseTime, e.getMessage(), e);
            throw new RuntimeException("数据库存储审计详情失败", e);
        }
    }

    @Override
    public CompletableFuture<AuditLogDetail> storeDetailAsync(AuditLogDetail auditLogDetail) {
        return auditLogDetailService.recordAuditDetailAsync(auditLogDetail)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.debug("数据库异步存储审计详情成功，ID: {}", result.getId());
                    } else {
                        lastError = throwable.getMessage();
                        log.error("数据库异步存储审计详情失败: {}", throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    public boolean storeDetailBatch(List<AuditLogDetail> auditLogDetails) {
        if (auditLogDetails == null || auditLogDetails.isEmpty()) {
            return true;
        }

        long startTime = System.currentTimeMillis();
        try {
            boolean result = auditLogDetailService.recordAuditDetailBatch(auditLogDetails);
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.debug("数据库批量存储审计详情完成，数量: {}, 成功: {}, 耗时: {}ms", 
                    auditLogDetails.size(), result, lastResponseTime);
            return result;
        } catch (Exception e) {
            lastError = e.getMessage();
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.error("数据库批量存储审计详情失败，数量: {}, 耗时: {}ms, 错误: {}", 
                    auditLogDetails.size(), lastResponseTime, e.getMessage(), e);
            throw new RuntimeException("数据库批量存储审计详情失败", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> storeDetailBatchAsync(List<AuditLogDetail> auditLogDetails) {
        if (auditLogDetails == null || auditLogDetails.isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }

        return auditLogDetailService.recordAuditDetailBatchAsync(auditLogDetails)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.debug("数据库异步批量存储审计详情完成，数量: {}, 成功: {}", auditLogDetails.size(), result);
                    } else {
                        lastError = throwable.getMessage();
                        log.error("数据库异步批量存储审计详情失败，数量: {}, 错误: {}", 
                                auditLogDetails.size(), throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    public String getStorageType() {
        return "database";
    }

    @Override
    public boolean isHealthy() {
        try {
            // 简单的健康检查：尝试查询审计日志数量
            auditLogService.count();
            return true;
        } catch (Exception e) {
            log.warn("数据库审计存储健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public StorageStats getStats() {
        StorageStats stats = new StorageStats();
        stats.setTotalStored(totalStored.get());
        stats.setTotalFailed(totalFailed.get());
        stats.setAvgResponseTime(lastResponseTime);
        stats.setLastError(lastError);
        return stats;
    }
}
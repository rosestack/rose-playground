package io.github.rosestack.audit.storage;

import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 审计存储接口
 * <p>
 * 定义审计日志的存储操作，支持多种存储实现：数据库、文件、消息队列等。
 * 提供同步和异步两种存储方式，确保高性能和可靠性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface AuditStorage {

    /**
     * 存储审计日志（同步）
     *
     * @param auditLog 审计日志
     * @return 存储后的审计日志
     */
    AuditLog store(AuditLog auditLog);


    /**
     * 批量存储审计日志
     *
     * @param auditLogs 审计日志列表
     * @return 是否成功
     */
    boolean storeBatch(List<AuditLog> auditLogs);

    /**
     * 存储审计详情
     *
     * @param auditLogDetail 审计详情
     * @return 存储后的审计详情
     */
    AuditLogDetail storeDetail(AuditLogDetail auditLogDetail);

    /**
     * 批量存储审计详情
     *
     * @param auditLogDetails 审计详情列表
     * @return 是否成功
     */
    boolean storeDetailBatch(List<AuditLogDetail> auditLogDetails);

    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    String getStorageType();

    /**
     * 检查存储健康状态
     *
     * @return 是否健康
     */
    boolean isHealthy();

    /**
     * 获取存储统计信息
     *
     * @return 统计信息
     */
    StorageStats getStats();

    /**
     * 存储统计信息
     */
    class StorageStats {
        private long totalStored;
        private long totalFailed;
        private long avgResponseTime;
        private String lastError;

        // Getters and Setters
        public long getTotalStored() { return totalStored; }
        public void setTotalStored(long totalStored) { this.totalStored = totalStored; }

        public long getTotalFailed() { return totalFailed; }
        public void setTotalFailed(long totalFailed) { this.totalFailed = totalFailed; }

        public long getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(long avgResponseTime) { this.avgResponseTime = avgResponseTime; }

        public String getLastError() { return lastError; }
        public void setLastError(String lastError) { this.lastError = lastError; }
    }
}
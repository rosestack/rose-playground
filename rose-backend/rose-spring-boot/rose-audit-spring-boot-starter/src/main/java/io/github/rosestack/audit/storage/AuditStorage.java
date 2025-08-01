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
     * 批量存储审计详情
     *
     * @param auditLogDetails 审计详情列表
     * @return 是否成功
     */
    boolean storeDetailBatch(List<AuditLogDetail> auditLogDetails);
}
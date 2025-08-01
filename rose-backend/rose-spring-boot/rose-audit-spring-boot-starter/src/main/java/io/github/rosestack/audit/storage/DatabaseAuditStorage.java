package io.github.rosestack.audit.storage;

import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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

    // 统计信息
    private final AtomicLong totalStored = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
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
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.error("数据库存储审计日志失败，耗时: {}ms, 错误: {}", lastResponseTime, e.getMessage(), e);
            throw new RuntimeException("数据库存储审计日志失败", e);
        }
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
            lastResponseTime = System.currentTimeMillis() - startTime;
            log.error("数据库批量存储审计详情失败，数量: {}, 耗时: {}ms, 错误: {}",
                    auditLogDetails.size(), lastResponseTime, e.getMessage(), e);
            throw new RuntimeException("数据库批量存储审计详情失败", e);
        }
    }
}
package io.github.rosestack.audit.listener;

import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.event.AuditEvent;
import io.github.rosestack.audit.config.AuditProperties;
import io.github.rosestack.audit.storage.AuditStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 审计事件监听器
 * <p>
 * 监听审计事件并根据配置选择合适的存储方式进行处理。
 * 支持同步和异步两种处理模式。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {
    private final AuditStorage auditStorage;
    private final AuditProperties auditProperties;

    /**
     * 处理审计事件
     *
     * @param auditEvent 审计事件
     */
    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent auditEvent) {
        if (!auditProperties.isEnabled()) {
            log.debug("审计功能已禁用，跳过事件处理");
            return;
        }

        try {
            handleSaveEventSync(auditEvent);
        } catch (Exception e) {
            log.error("处理审计事件失败: {}", e.getMessage(), e);
            // 根据配置决定是否重试或记录到备用存储
            handleEventProcessingFailure(auditEvent, e);
        }
    }

    /**
     * 处理保存事件（同步）
     */
    private void handleSaveEventSync(AuditEvent auditEvent) {
        AuditLog auditLog = auditEvent.getAuditLog();
        List<AuditLogDetail> auditLogDetails = auditEvent.getAuditLogDetails();

        // 保存主记录
        AuditLog savedAuditLog = auditStorage.store(auditLog);
        log.debug("保存审计日志成功: {}", savedAuditLog.getId());

        // 保存详细记录
        if (auditLogDetails != null && !auditLogDetails.isEmpty()) {
            // 更新详细记录的审计日志ID
            auditLogDetails.forEach(detail -> {
                detail.setAuditLogId(savedAuditLog.getId());
                detail.setIsEncrypted(auditLog.isHighRisk());
            });
            boolean detailsSaved = auditStorage.storeDetailBatch(auditLogDetails);
            log.debug("保存审计详情成功: {} 条记录", auditLogDetails.size());
        }
    }

    /**
     * 处理事件处理失败的情况
     */
    private void handleEventProcessingFailure(AuditEvent auditEvent, Exception e) {
        // 可以在这里实现重试机制、备用存储等容错策略
        // 例如：将失败的事件写入文件或发送到死信队列

        // 记录失败统计
        recordFailureStats(auditEvent, e);
    }

    /**
     * 记录失败统计
     */
    private void recordFailureStats(AuditEvent auditEvent, Exception e) {
        // 实现失败统计逻辑
        log.warn("记录审计事件处理失败统计: 错误类型={}", e.getClass().getSimpleName());
    }
}

package io.github.rosestack.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rosestack.audit.entity.AuditLog;

/**
 * 审计日志服务接口
 * <p>
 * 提供审计日志的完整业务功能，包括记录、查询、统计、分析等。
 * 支持同步和异步处理，确保高性能和可靠性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface AuditLogService extends IService<AuditLog> {

    /**
     * 记录审计日志（同步）
     *
     * @param auditLog 审计日志对象
     * @return 保存后的审计日志
     */
    AuditLog recordAuditLog(AuditLog auditLog);
}
package io.github.rosestack.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rosestack.audit.entity.AuditLogDetail;


import java.util.List;

/**
 * 审计日志详情服务接口
 * <p>
 * 提供审计日志详情的完整业务功能，包括记录、查询、统计、加密脱敏等。
 * 支持同步和异步处理，确保高性能和数据安全。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface AuditLogDetailService extends IService<AuditLogDetail> {

    /**
     * 批量记录审计详情
     *
     * @param auditLogDetails 审计详情列表
     * @return 是否成功
     */
    boolean recordAuditDetailBatch(List<AuditLogDetail> auditLogDetails);

}
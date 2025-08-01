package io.github.rosestack.audit.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.mapper.AuditLogDetailMapper;
import io.github.rosestack.audit.service.AuditLogDetailService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志详情服务实现类
 * <p>
 * 提供审计日志详情的完整业务功能实现，包括记录、查询、统计、加密脱敏等。
 * 支持同步和异步处理，确保高性能和数据安全。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AuditLogDetailServiceImpl extends ServiceImpl<AuditLogDetailMapper, AuditLogDetail> implements AuditLogDetailService {

    @Override
    public boolean recordAuditDetailBatch(List<AuditLogDetail> auditLogDetails) {
        if (auditLogDetails == null || auditLogDetails.isEmpty()) {
            log.warn("批量记录审计详情：详情列表为空");
            return true;
        }

        log.debug("开始批量记录审计详情，数量: {}", auditLogDetails.size());

        try {
            // 批量处理
            for (AuditLogDetail detail : auditLogDetails) {
                enrichAuditDetailContext(detail);
            }

            // 批量保存
            boolean success = saveBatch(auditLogDetails, 1000); // 默认批次大小

            if (success) {
                log.debug("批量记录审计详情成功，数量: {}", auditLogDetails.size());
            } else {
                log.error("批量记录审计详情失败");
            }

            return success;

        } catch (Exception e) {
            log.error("批量记录审计详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量记录审计详情失败", e);
        }
    }

    /**
     * 补充审计详情上下文信息
     */
    private void enrichAuditDetailContext(AuditLogDetail auditLogDetail) {
        // 设置创建时间
        if (auditLogDetail.getCreatedTime() == null) {
            auditLogDetail.setCreatedTime(LocalDateTime.now());
        }

        // 设置租户ID
        if (!StringUtils.hasText(auditLogDetail.getTenantId())) {
            auditLogDetail.setTenantId("default"); // 默认租户
        }

        // 根据详情键设置敏感数据标记
        if (StringUtils.hasText(auditLogDetail.getDetailKey())) {
            AuditDetailKey detailKey = AuditDetailKey.fromCode(auditLogDetail.getDetailKey());
            if (auditLogDetail.getIsSensitive() == null) {
                if (detailKey != null) {
                    auditLogDetail.setIsSensitive(detailKey.isSensitive());
                }
            }
        }

        // 设置详情类型
        if (!StringUtils.hasText(auditLogDetail.getDetailType()) && StringUtils.hasText(auditLogDetail.getDetailKey())) {
            AuditDetailKey detailKey = AuditDetailKey.fromCode(auditLogDetail.getDetailKey());
            if (detailKey != null) {
                auditLogDetail.setDetailType(detailKey.getDetailType());
            }
        }
    }

}
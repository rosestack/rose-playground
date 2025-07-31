package io.github.rosestack.audit.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditDetailType;
import io.github.rosestack.audit.mapper.AuditLogDetailMapper;
import io.github.rosestack.audit.properties.AuditProperties;
import io.github.rosestack.audit.service.AuditLogDetailService;
import io.github.rosestack.audit.util.AuditEncryptionUtils;
import io.github.rosestack.audit.util.AuditJsonUtils;
import io.github.rosestack.audit.util.AuditMaskingUtils;
import io.github.rosestack.core.jackson.JsonUtils;
import io.github.rosestack.core.util.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuditLogDetailServiceImpl extends ServiceImpl<AuditLogDetailMapper, AuditLogDetail> implements AuditLogDetailService {

    private final AuditLogDetailMapper auditLogDetailMapper;
    private final AuditProperties auditProperties;

    // ==================== 记录审计详情 ====================

    @Override
    public AuditLogDetail recordAuditDetail(AuditLogDetail auditLogDetail) {
        log.debug("开始记录审计详情: {}", auditLogDetail.getDetailKey());

        try {
            // 补充上下文信息
            enrichAuditDetailContext(auditLogDetail);

            // 保存到数据库
            boolean success = save(auditLogDetail);
            if (!success) {
                throw new RuntimeException("保存审计详情失败");
            }

            log.debug("审计详情记录成功，ID: {}", auditLogDetail.getId());
            return auditLogDetail;

        } catch (Exception e) {
            log.error("记录审计详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("记录审计详情失败", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<AuditLogDetail> recordAuditDetailAsync(AuditLogDetail auditLogDetail) {
        return CompletableFuture.supplyAsync(() -> recordAuditDetail(auditLogDetail));
    }

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
            int batchSize = auditProperties.getStorage().getBatchSize();
            boolean success = saveBatch(auditLogDetails, batchSize);

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

    @Override
    @Async
    public CompletableFuture<Boolean> recordAuditDetailBatchAsync(List<AuditLogDetail> auditLogDetails) {
        return CompletableFuture.supplyAsync(() -> recordAuditDetailBatch(auditLogDetails));
    }

    @Override
    public Long recordSimpleAuditDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue) {
        AuditLogDetail detail = AuditLogDetail.builder()
                .auditLogId(auditLogId)
                .detailKey(detailKey.getCode())
                .detailType(detailKey.getDetailType().getCode())
                .detailValue(detailValue)
                .isSensitive(detailKey.isSensitive())
                .build();

        AuditLogDetail savedDetail = recordAuditDetail(detail);
        return savedDetail.getId();
    }

    @Override
    public List<Long> recordHttpDetails(Long auditLogId, Map<String, Object> requestParams,
                                        String requestBody, String responseBody) {
        List<Long> detailIds = new ArrayList<>();

        // 记录请求参数
        if (requestParams != null && !requestParams.isEmpty()) {
            String paramsJson = AuditJsonUtils.toJsonString(requestParams);
            Long id = recordSimpleAuditDetail(auditLogId, AuditDetailKey.REQUEST_PARAMS, paramsJson);
            detailIds.add(id);
        }

        // 记录请求体
        if (StringUtils.hasText(requestBody)) {
            Long id = recordSimpleAuditDetail(auditLogId, AuditDetailKey.REQUEST_BODY, requestBody);
            detailIds.add(id);
        }

        // 记录响应体
        if (StringUtils.hasText(responseBody)) {
            Long id = recordSimpleAuditDetail(auditLogId, AuditDetailKey.RESPONSE_RESULT, responseBody);
            detailIds.add(id);
        }

        // 记录请求头
        Map<String, String> headers = ServletUtils.getRequestHeaders();
        if (!headers.isEmpty()) {
            String headersJson = AuditJsonUtils.toJsonString(headers);
            Long id = recordSimpleAuditDetail(auditLogId, AuditDetailKey.REQUEST_HEADERS, headersJson);
            detailIds.add(id);
        }

        return detailIds;
    }

    @Override
    public List<Long> recordDataChangeDetails(Long auditLogId, Object beforeData, Object afterData) {
        List<Long> detailIds = new ArrayList<>();

        // 记录变更前数据
        if (beforeData != null) {
            String beforeJson = AuditJsonUtils.toJsonString(beforeData);
            Long id = recordSimpleAuditDetail(auditLogId, AuditDetailKey.DATA_CHANGE_BEFORE, beforeJson);
            detailIds.add(id);
        }

        // 记录变更后数据
        if (afterData != null) {
            String afterJson = AuditJsonUtils.toJsonString(afterData);
            Long id = recordSimpleAuditDetail(auditLogId, AuditDetailKey.DATA_CHANGE_AFTER, afterJson);
            detailIds.add(id);
        }

        return detailIds;
    }

    // ==================== 查询审计详情 ====================

    @Override
    @Cacheable(value = "auditDetail", key = "'auditLog:' + #auditLogId")
    public List<AuditLogDetail> findByAuditLogId(Long auditLogId) {
        return auditLogDetailMapper.selectByAuditLogId(auditLogId);
    }

    @Override
    public IPage<AuditLogDetail> findByAuditLogIdWithPage(Long auditLogId, Page<AuditLogDetail> page) {
        return auditLogDetailMapper.selectPageByAuditLogId(page, auditLogId);
    }

    @Override
    public List<AuditLogDetail> findByDetailType(AuditDetailType detailType) {
        return auditLogDetailMapper.selectByDetailType(detailType.getCode());
    }

    @Override
    public List<AuditLogDetail> findByDetailKey(AuditDetailKey detailKey) {
        return auditLogDetailMapper.selectByDetailKey(detailKey.getCode());
    }

    @Override
    public List<AuditLogDetail> findByAuditLogIdAndDetailType(Long auditLogId, AuditDetailType detailType) {
        return auditLogDetailMapper.selectByAuditLogIdAndDetailType(auditLogId, detailType.getCode());
    }

    @Override
    public AuditLogDetail findByAuditLogIdAndDetailKey(Long auditLogId, AuditDetailKey detailKey) {
        return auditLogDetailMapper.selectByAuditLogIdAndDetailKey(auditLogId, detailKey.getCode());
    }

    @Override
    public List<AuditLogDetail> findByAuditLogIds(List<Long> auditLogIds) {
        return auditLogDetailMapper.selectByAuditLogIds(auditLogIds);
    }

    @Override
    public List<AuditLogDetail> findSensitiveDetails() {
        return auditLogDetailMapper.selectSensitiveDetails();
    }

    @Override
    public List<AuditLogDetail> findSensitiveButNotEncrypted() {
        return auditLogDetailMapper.selectSensitiveButNotEncrypted();
    }

    // ==================== 数据处理 ====================

    @Override
    public boolean encryptSensitiveDetail(Long detailId) {
        AuditLogDetail detail = getById(detailId);
        if (detail == null) {
            log.warn("未找到详情记录，ID: {}", detailId);
            return false;
        }

        if (!Boolean.TRUE.equals(detail.getIsSensitive())) {
            log.debug("详情记录不是敏感数据，无需加密，ID: {}", detailId);
            return true;
        }

        try {
            String encryptedValue = AuditEncryptionUtils.encryptSensitiveData(detail.getDetailValue(), auditProperties);
            detail.setDetailValue(encryptedValue);

            boolean success = updateById(detail);
            if (success) {
                log.debug("敏感详情加密成功，ID: {}", detailId);
            }
            return success;

        } catch (Exception e) {
            log.error("敏感详情加密失败，ID: {}, 错误: {}", detailId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int encryptSensitiveDetailBatch(List<Long> detailIds) {
        if (detailIds == null || detailIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long detailId : detailIds) {
            if (encryptSensitiveDetail(detailId)) {
                successCount++;
            }
        }

        log.info("批量加密敏感详情完成，成功: {}, 总数: {}", successCount, detailIds.size());
        return successCount;
    }

    @Override
    public AuditLogDetail decryptDetail(Long detailId) {
        AuditLogDetail detail = getById(detailId);
        if (detail == null) {
            return null;
        }

        try {
            String decryptedValue = AuditEncryptionUtils.decryptSensitiveData(detail.getDetailValue(), auditProperties);

            // 创建新对象，不修改原对象
            AuditLogDetail decryptedDetail = new AuditLogDetail();
            decryptedDetail.setId(detail.getId());
            decryptedDetail.setAuditLogId(detail.getAuditLogId());
            decryptedDetail.setDetailType(AuditDetailType.valueOf(detail.getDetailType()));
            decryptedDetail.setDetailKey(AuditDetailKey.valueOf(detail.getDetailKey()));
            decryptedDetail.setDetailValue(decryptedValue);
            decryptedDetail.setIsSensitive(detail.getIsSensitive());
            decryptedDetail.setTenantId(detail.getTenantId());
            decryptedDetail.setCreatedAt(detail.getCreatedAt());

            return decryptedDetail;

        } catch (Exception e) {
            log.error("详情解密失败，ID: {}, 错误: {}", detailId, e.getMessage(), e);
            return detail; // 返回原始数据
        }
    }

    @Override
    public AuditLogDetail maskSensitiveDetail(AuditLogDetail auditLogDetail) {
        if (auditLogDetail == null || !Boolean.TRUE.equals(auditLogDetail.getIsSensitive())) {
            return auditLogDetail;
        }

        try {
            String maskedValue = AuditMaskingUtils.maskByFieldName(
                    auditLogDetail.getDetailKey(),
                    auditLogDetail.getDetailValue());

            // 创建新对象，不修改原对象
            AuditLogDetail maskedDetail = new AuditLogDetail();
            maskedDetail.setId(auditLogDetail.getId());
            maskedDetail.setAuditLogId(auditLogDetail.getAuditLogId());
            maskedDetail.setDetailType(AuditDetailType.valueOf(auditLogDetail.getDetailType()));
            maskedDetail.setDetailKey(AuditDetailKey.valueOf(auditLogDetail.getDetailKey()));
            maskedDetail.setDetailValue(maskedValue);
            maskedDetail.setIsSensitive(auditLogDetail.getIsSensitive());
            maskedDetail.setTenantId(auditLogDetail.getTenantId());
            maskedDetail.setCreatedAt(auditLogDetail.getCreatedAt());

            return maskedDetail;

        } catch (Exception e) {
            log.error("详情脱敏失败，ID: {}, 错误: {}", auditLogDetail.getId(), e.getMessage(), e);
            return auditLogDetail; // 返回原始数据
        }
    }

    @Override
    public List<AuditLogDetail> maskSensitiveDetailBatch(List<AuditLogDetail> auditLogDetails) {
        if (auditLogDetails == null || auditLogDetails.isEmpty()) {
            return auditLogDetails;
        }

        return auditLogDetails.stream()
                .map(this::maskSensitiveDetail)
                .collect(Collectors.toList());
    }

    // ==================== 统计分析 ====================

    @Override
    public Long countByAuditLogId(Long auditLogId) {
        return auditLogDetailMapper.countByAuditLogId(auditLogId);
    }

    @Override
    public List<Map<String, Object>> countByDetailType() {
        return auditLogDetailMapper.countByDetailType();
    }

    @Override
    public List<Map<String, Object>> countByDetailKey() {
        return auditLogDetailMapper.countByDetailKey();
    }

    @Override
    public Long countSensitiveDetails() {
        return auditLogDetailMapper.countSensitiveDetails();
    }

    @Override
    public Long countEncryptedDetails() {
        return auditLogDetailMapper.countEncryptedDetails();
    }

    @Override
    public Map<String, Object> getDetailStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();

        List<AuditLogDetail> details = findByTimeRange(startTime, endTime);

        // 基础统计
        statistics.put("totalDetails", details.size());
        statistics.put("sensitiveDetails", details.stream().mapToLong(d -> Boolean.TRUE.equals(d.getIsSensitive()) ? 1 : 0).sum());

        // 详情类型分布
        Map<String, Long> typeDistribution = details.stream()
                .collect(Collectors.groupingBy(
                        AuditLogDetail::getDetailType,
                        Collectors.counting()
                ));
        statistics.put("typeDistribution", typeDistribution);

        // 详情键分布
        Map<String, Long> keyDistribution = details.stream()
                .collect(Collectors.groupingBy(
                        AuditLogDetail::getDetailKey,
                        Collectors.counting()
                ));
        statistics.put("keyDistribution", keyDistribution);

        // 数据大小统计
        long totalSize = details.stream()
                .mapToLong(d -> d.getDetailValue() != null ? d.getDetailValue().length() : 0)
                .sum();
        statistics.put("totalDataSize", totalSize);
        statistics.put("averageDataSize", details.isEmpty() ? 0 : totalSize / details.size());

        return statistics;
    }

    @Override
    public Map<String, Object> getSensitiveDataSecurityReport() {
        Map<String, Object> report = new HashMap<>();

        // 敏感数据统计
        Long totalSensitive = countSensitiveDetails();
        Long totalEncrypted = countEncryptedDetails();
        List<AuditLogDetail> unencryptedSensitive = findSensitiveButNotEncrypted();

        report.put("totalSensitiveDetails", totalSensitive);
        report.put("totalEncryptedDetails", totalEncrypted);
        report.put("unencryptedSensitiveDetails", unencryptedSensitive.size());

        // 加密率
        double encryptionRate = totalSensitive > 0 ? (double) totalEncrypted / totalSensitive : 1.0;
        report.put("encryptionRate", encryptionRate);

        // 安全风险评估
        String riskLevel;
        if (encryptionRate >= 0.95) {
            riskLevel = "LOW";
        } else if (encryptionRate >= 0.8) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "HIGH";
        }
        report.put("securityRiskLevel", riskLevel);

        // 未加密敏感数据详情
        Map<String, Long> unencryptedByType = unencryptedSensitive.stream()
                .collect(Collectors.groupingBy(
                        AuditLogDetail::getDetailType,
                        Collectors.counting()
                ));
        report.put("unencryptedByType", unencryptedByType);

        return report;
    }

    // ==================== 数据管理 ====================

    @Override
    public int deleteByAuditLogIds(List<Long> auditLogIds) {
        if (auditLogIds == null || auditLogIds.isEmpty()) {
            return 0;
        }
        return auditLogDetailMapper.deleteByAuditLogIds(auditLogIds);
    }

    @Override
    public int cleanupExpiredDetails(LocalDateTime expireTime, int batchSize) {
        log.info("开始清理过期审计详情，过期时间: {}, 批量大小: {}", expireTime, batchSize);

        int totalDeleted = 0;
        List<Long> expiredIds;

        do {
            expiredIds = auditLogDetailMapper.selectExpiredDetailIds(expireTime, batchSize);
            if (!expiredIds.isEmpty()) {
                int deleted = auditLogDetailMapper.deleteBatchIds(expiredIds);
                totalDeleted += deleted;
                log.debug("删除过期审计详情 {} 条", deleted);
            }
        } while (expiredIds.size() == batchSize);

        log.info("清理过期审计详情完成，共删除 {} 条记录", totalDeleted);
        return totalDeleted;
    }

    @Override
    public long compressDetailData(Long auditLogId) {
        List<AuditLogDetail> details = findByAuditLogId(auditLogId);
        if (details.isEmpty()) {
            return 0;
        }

        long originalSize = 0;
        long compressedSize = 0;

        for (AuditLogDetail detail : details) {
            if (StringUtils.hasText(detail.getDetailValue())) {
                originalSize += detail.getDetailValue().length();

                // 压缩JSON数据
                String compressedValue = JsonUtils.toString(detail.getDetailValue());
                detail.setDetailValue(compressedValue);
                compressedSize += compressedValue.length();

                updateById(detail);
            }
        }

        log.info("压缩审计详情数据完成，审计日志ID: {}, 原始大小: {}, 压缩后大小: {}, 压缩率: {}%",
                auditLogId, originalSize, compressedSize,
                originalSize > 0 ? (double) (originalSize - compressedSize) / originalSize * 100 : 0);

        return compressedSize;
    }

    @Override
    public Map<String, Object> validateDetailIntegrity(Long auditLogId) {
        Map<String, Object> result = new HashMap<>();
        List<AuditLogDetail> details = findByAuditLogId(auditLogId);

        int totalDetails = details.size();
        int validDetails = 0;
        int invalidDetails = 0;
        List<String> errors = new ArrayList<>();

        for (AuditLogDetail detail : details) {
            try {
                // 验证JSON格式
                if (StringUtils.hasText(detail.getDetailValue()) &&
                        !JsonUtils.isValidJson(detail.getDetailValue())) {
                    errors.add("详情ID " + detail.getId() + " JSON格式无效");
                    invalidDetails++;
                    continue;
                }

                // 验证敏感数据标记一致性
                AuditDetailKey detailKey = AuditDetailKey.fromCode(detail.getDetailKey());
                if (detailKey != null && detail.getIsSensitive() != null &&
                        detail.getIsSensitive() != detailKey.isSensitive()) {
                    errors.add("详情ID " + detail.getId() + " 敏感数据标记不一致");
                    invalidDetails++;
                    continue;
                }

                validDetails++;

            } catch (Exception e) {
                errors.add("详情ID " + detail.getId() + " 验证异常: " + e.getMessage());
                invalidDetails++;
            }
        }

        result.put("auditLogId", auditLogId);
        result.put("totalDetails", totalDetails);
        result.put("validDetails", validDetails);
        result.put("invalidDetails", invalidDetails);
        result.put("integrityRate", totalDetails > 0 ? (double) validDetails / totalDetails : 1.0);
        result.put("errors", errors);

        return result;
    }

    // ==================== 租户相关 ====================

    @Override
    public List<AuditLogDetail> findByTenantId(String tenantId) {
        return auditLogDetailMapper.selectByTenantId(tenantId);
    }

    @Override
    public IPage<AuditLogDetail> findByTenantIdWithPage(String tenantId, Page<AuditLogDetail> page) {
        return auditLogDetailMapper.selectPageByTenantId(page, tenantId);
    }

    @Override
    public Long countByTenantId(String tenantId) {
        return auditLogDetailMapper.countByTenantId(tenantId);
    }

    // ==================== 高级查询 ====================

    @Override
    public List<AuditLogDetail> findByConditions(Long auditLogId, AuditDetailType detailType,
                                                 AuditDetailKey detailKey, Boolean isSensitive) {
        return auditLogDetailMapper.selectByConditions(
                auditLogId,
                detailType != null ? detailType.getCode() : null,
                detailKey != null ? detailKey.getCode() : null,
                isSensitive
        );
    }

    @Override
    public List<AuditLogDetail> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogDetailMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public IPage<AuditLogDetail> findByTimeRangeWithPage(LocalDateTime startTime, LocalDateTime endTime, Page<AuditLogDetail> page) {
        return auditLogDetailMapper.selectPageByTimeRange(page, startTime, endTime);
    }

    @Override
    public IPage<AuditLogDetail> searchDetailContent(String keyword, Page<AuditLogDetail> page) {
        // 这里可以实现全文搜索功能
        // 暂时使用简单的LIKE查询
        log.info("搜索详情内容功能待完善，关键词: {}", keyword);
        return new Page<>(page.getCurrent(), page.getSize());
    }

    // ==================== 缓存管理 ====================

    @Override
    @CacheEvict(value = "auditDetail", allEntries = true)
    public void clearCache() {
        log.info("清空审计详情缓存");
    }

    @Override
    public void warmupCache(Long auditLogId) {
        log.info("预热审计详情缓存，审计日志ID: {}", auditLogId);
        if (auditLogId != null) {
            findByAuditLogId(auditLogId);
        }
    }

    @Override
    public Map<String, Object> getCacheStats() {
        // 这里可以集成缓存统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheProvider", "Spring Cache");
        stats.put("cacheName", "auditDetail");
        return stats;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 补充审计详情上下文信息
     */
    private void enrichAuditDetailContext(AuditLogDetail auditLogDetail) {
        // 设置创建时间
        if (auditLogDetail.getCreatedAt() == null) {
            auditLogDetail.setCreatedAt(LocalDateTime.now());
        }

        // 设置租户ID
        if (!StringUtils.hasText(auditLogDetail.getTenantId())) {
            auditLogDetail.setTenantId(ServletUtils.getCurrentTenantId());
        }

        // 根据详情键设置敏感数据标记
        if (StringUtils.hasText(auditLogDetail.getDetailKey()) && auditLogDetail.getIsSensitive() == null) {
            AuditDetailKey detailKey = AuditDetailKey.fromCode(auditLogDetail.getDetailKey());
            if (detailKey != null) {
                auditLogDetail.setIsSensitive(detailKey.isSensitive());
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

    /**
     * 处理敏感数据
     */
    private void processSensitiveData(AuditLogDetail auditLogDetail) {
        if (!Boolean.TRUE.equals(auditLogDetail.getIsSensitive())) {
            return;
        }


    }
}
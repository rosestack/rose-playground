package io.github.rosestack.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rosestack.audit.entity.AuditLogDetail;
import io.github.rosestack.audit.enums.AuditDetailKey;
import io.github.rosestack.audit.enums.AuditDetailType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    // ==================== 记录审计详情 ====================

    /**
     * 记录审计详情（同步）
     *
     * @param auditLogDetail 审计详情对象
     * @return 保存后的审计详情
     */
    AuditLogDetail recordAuditDetail(AuditLogDetail auditLogDetail);

    /**
     * 记录审计详情（异步）
     *
     * @param auditLogDetail 审计详情对象
     * @return 异步结果
     */
    CompletableFuture<AuditLogDetail> recordAuditDetailAsync(AuditLogDetail auditLogDetail);

    /**
     * 批量记录审计详情
     *
     * @param auditLogDetails 审计详情列表
     * @return 是否成功
     */
    boolean recordAuditDetailBatch(List<AuditLogDetail> auditLogDetails);

    /**
     * 批量记录审计详情（异步）
     *
     * @param auditLogDetails 审计详情列表
     * @return 异步结果
     */
    CompletableFuture<Boolean> recordAuditDetailBatchAsync(List<AuditLogDetail> auditLogDetails);

    /**
     * 快速记录审计详情
     *
     * @param auditLogId  审计日志ID
     * @param detailKey   详情键
     * @param detailValue 详情值
     * @return 详情ID
     */
    Long recordSimpleAuditDetail(Long auditLogId, AuditDetailKey detailKey, String detailValue);

    /**
     * 记录HTTP请求详情
     *
     * @param auditLogId    审计日志ID
     * @param requestParams 请求参数
     * @param requestBody   请求体
     * @param responseBody  响应体
     * @return 详情ID列表
     */
    List<Long> recordHttpDetails(Long auditLogId, Map<String, Object> requestParams, 
                                String requestBody, String responseBody);

    /**
     * 记录数据变更详情
     *
     * @param auditLogId 审计日志ID
     * @param beforeData 变更前数据
     * @param afterData  变更后数据
     * @return 详情ID列表
     */
    List<Long> recordDataChangeDetails(Long auditLogId, Object beforeData, Object afterData);

    // ==================== 查询审计详情 ====================

    /**
     * 根据审计日志ID查询所有详情
     *
     * @param auditLogId 审计日志ID
     * @return 详情列表
     */
    List<AuditLogDetail> findByAuditLogId(Long auditLogId);

    /**
     * 根据审计日志ID分页查询详情
     *
     * @param auditLogId 审计日志ID
     * @param page       分页参数
     * @return 分页结果
     */
    IPage<AuditLogDetail> findByAuditLogIdWithPage(Long auditLogId, Page<AuditLogDetail> page);

    /**
     * 根据详情类型查询详情
     *
     * @param detailType 详情类型
     * @return 详情列表
     */
    List<AuditLogDetail> findByDetailType(AuditDetailType detailType);

    /**
     * 根据详情键查询详情
     *
     * @param detailKey 详情键
     * @return 详情列表
     */
    List<AuditLogDetail> findByDetailKey(AuditDetailKey detailKey);

    /**
     * 根据审计日志ID和详情类型查询详情
     *
     * @param auditLogId 审计日志ID
     * @param detailType 详情类型
     * @return 详情列表
     */
    List<AuditLogDetail> findByAuditLogIdAndDetailType(Long auditLogId, AuditDetailType detailType);

    /**
     * 根据审计日志ID和详情键查询单个详情
     *
     * @param auditLogId 审计日志ID
     * @param detailKey  详情键
     * @return 详情对象
     */
    AuditLogDetail findByAuditLogIdAndDetailKey(Long auditLogId, AuditDetailKey detailKey);

    /**
     * 批量查询多个审计日志的详情
     *
     * @param auditLogIds 审计日志ID列表
     * @return 详情列表
     */
    List<AuditLogDetail> findByAuditLogIds(List<Long> auditLogIds);

    /**
     * 查询敏感数据详情
     *
     * @return 敏感详情列表
     */
    List<AuditLogDetail> findSensitiveDetails();

    /**
     * 查询已加密的详情
     *
     * @return 已加密详情列表
     */
    List<AuditLogDetail> findEncryptedDetails();

    /**
     * 查询敏感但未加密的详情（用于数据安全检查）
     *
     * @return 敏感但未加密的详情列表
     */
    List<AuditLogDetail> findSensitiveButNotEncrypted();

    // ==================== 数据处理 ====================

    /**
     * 加密敏感详情数据
     *
     * @param detailId 详情ID
     * @return 是否成功
     */
    boolean encryptSensitiveDetail(Long detailId);

    /**
     * 批量加密敏感详情数据
     *
     * @param detailIds 详情ID列表
     * @return 成功加密的数量
     */
    int encryptSensitiveDetailBatch(List<Long> detailIds);

    /**
     * 解密详情数据
     *
     * @param detailId 详情ID
     * @return 解密后的详情
     */
    AuditLogDetail decryptDetail(Long detailId);

    /**
     * 脱敏详情数据（用于展示）
     *
     * @param auditLogDetail 原始详情
     * @return 脱敏后的详情
     */
    AuditLogDetail maskSensitiveDetail(AuditLogDetail auditLogDetail);

    /**
     * 批量脱敏详情数据
     *
     * @param auditLogDetails 原始详情列表
     * @return 脱敏后的详情列表
     */
    List<AuditLogDetail> maskSensitiveDetailBatch(List<AuditLogDetail> auditLogDetails);

    // ==================== 统计分析 ====================

    /**
     * 统计指定审计日志的详情数量
     *
     * @param auditLogId 审计日志ID
     * @return 详情数量
     */
    Long countByAuditLogId(Long auditLogId);

    /**
     * 统计各详情类型的数量
     *
     * @return 统计结果 Map<详情类型, 数量>
     */
    List<Map<String, Object>> countByDetailType();

    /**
     * 统计各详情键的数量
     *
     * @return 统计结果 Map<详情键, 数量>
     */
    List<Map<String, Object>> countByDetailKey();

    /**
     * 统计敏感数据的数量
     *
     * @return 敏感数据数量
     */
    Long countSensitiveDetails();

    /**
     * 统计已加密数据的数量
     *
     * @return 已加密数据数量
     */
    Long countEncryptedDetails();

    /**
     * 获取详情数据统计报告
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计报告
     */
    Map<String, Object> getDetailStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取敏感数据安全报告
     *
     * @return 安全报告
     */
    Map<String, Object> getSensitiveDataSecurityReport();

    // ==================== 数据管理 ====================

    /**
     * 根据审计日志ID列表批量删除详情
     *
     * @param auditLogIds 审计日志ID列表
     * @return 删除的记录数
     */
    int deleteByAuditLogIds(List<Long> auditLogIds);

    /**
     * 清理过期的详情数据
     *
     * @param expireTime 过期时间
     * @param batchSize  批量大小
     * @return 清理的记录数
     */
    int cleanupExpiredDetails(LocalDateTime expireTime, int batchSize);

    /**
     * 压缩详情数据
     *
     * @param auditLogId 审计日志ID
     * @return 压缩后的数据大小
     */
    long compressDetailData(Long auditLogId);

    /**
     * 验证详情数据完整性
     *
     * @param auditLogId 审计日志ID
     * @return 验证结果
     */
    Map<String, Object> validateDetailIntegrity(Long auditLogId);

    // ==================== 租户相关 ====================

    /**
     * 根据租户ID查询详情
     *
     * @param tenantId 租户ID
     * @return 详情列表
     */
    List<AuditLogDetail> findByTenantId(String tenantId);

    /**
     * 根据租户ID分页查询详情
     *
     * @param tenantId 租户ID
     * @param page     分页参数
     * @return 分页结果
     */
    IPage<AuditLogDetail> findByTenantIdWithPage(String tenantId, Page<AuditLogDetail> page);

    /**
     * 统计租户的详情数量
     *
     * @param tenantId 租户ID
     * @return 详情数量
     */
    Long countByTenantId(String tenantId);

    // ==================== 高级查询 ====================

    /**
     * 根据多个条件查询详情
     *
     * @param auditLogId  审计日志ID（可选）
     * @param detailType  详情类型（可选）
     * @param detailKey   详情键（可选）
     * @param isSensitive 是否敏感（可选）
     * @param isEncrypted 是否加密（可选）
     * @return 详情列表
     */
    List<AuditLogDetail> findByConditions(Long auditLogId, AuditDetailType detailType, 
                                          AuditDetailKey detailKey, Boolean isSensitive, Boolean isEncrypted);

    /**
     * 根据时间范围查询详情
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 详情列表
     */
    List<AuditLogDetail> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围分页查询详情
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param page      分页参数
     * @return 分页结果
     */
    IPage<AuditLogDetail> findByTimeRangeWithPage(LocalDateTime startTime, LocalDateTime endTime, Page<AuditLogDetail> page);

    /**
     * 搜索详情内容
     *
     * @param keyword 关键词
     * @param page    分页参数
     * @return 搜索结果
     */
    IPage<AuditLogDetail> searchDetailContent(String keyword, Page<AuditLogDetail> page);

    // ==================== 缓存管理 ====================

    /**
     * 清空详情缓存
     */
    void clearCache();

    /**
     * 预热详情缓存
     *
     * @param auditLogId 审计日志ID（可选）
     */
    void warmupCache(Long auditLogId);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    Map<String, Object> getCacheStats();
}
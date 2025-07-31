package io.github.rosestack.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.rosestack.audit.entity.AuditLog;
import io.github.rosestack.audit.enums.AuditEventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    /**
     * 记录审计日志（异步）
     *
     * @param auditLog 审计日志对象
     * @return 异步结果
     */
    CompletableFuture<AuditLog> recordAuditLogAsync(AuditLog auditLog);

    /**
     * 批量记录审计日志
     *
     * @param auditLogs 审计日志列表
     * @return 是否成功
     */
    boolean recordAuditLogBatch(List<AuditLog> auditLogs);

    /**
     * 批量记录审计日志（异步）
     *
     * @param auditLogs 审计日志列表
     * @return 异步结果
     */
    CompletableFuture<Boolean> recordAuditLogBatchAsync(List<AuditLog> auditLogs);

    /**
     * 快速记录审计日志（简化版本）
     *
     * @param eventType     事件类型
     * @param operationName 操作名称
     * @param userId        用户ID
     * @param requestUri    请求URI
     * @param status        操作状态
     * @return 审计日志ID
     */
    Long recordSimpleAuditLog(AuditEventType eventType, String operationName,
                              String userId, String requestUri, String status);

    /**
     * 根据用户ID查询审计日志
     *
     * @param userId 用户ID
     * @return 审计日志列表
     */
    List<AuditLog> findByUserId(String userId);

    /**
     * 根据用户ID分页查询审计日志
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 分页结果
     */
    IPage<AuditLog> findByUserIdWithPage(String userId, Page<AuditLog> page);

    /**
     * 根据事件类型查询审计日志
     *
     * @param eventType 事件类型
     * @return 审计日志列表
     */
    List<AuditLog> findByEventType(String eventType);

    /**
     * 根据时间范围查询审计日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审计日志列表
     */
    List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围分页查询审计日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param page      分页参数
     * @return 分页结果
     */
    IPage<AuditLog> findByTimeRangeWithPage(LocalDateTime startTime, LocalDateTime endTime, Page<AuditLog> page);

    /**
     * 多条件查询审计日志
     *
     * @param userId    用户ID（可选）
     * @param eventType 事件类型（可选）
     * @param riskLevel 风险等级（可选）
     * @param status    操作状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param page      分页参数
     * @return 分页结果
     */
    IPage<AuditLog> findByConditions(String userId, String eventType, String riskLevel,
                                     String status, LocalDateTime startTime, LocalDateTime endTime,
                                     Page<AuditLog> page);

    /**
     * 查询高风险审计日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 高风险日志列表
     */
    List<AuditLog> findHighRiskLogs(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询失败的操作日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 失败操作日志列表
     */
    List<AuditLog> findFailedOperations(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询安全事件日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 安全事件日志列表
     */
    List<AuditLog> findSecurityEvents(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 统计分析 ====================

    /**
     * 统计指定时间范围内的审计日志数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志数量
     */
    Long countByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定用户的审计日志数量
     *
     * @param userId 用户ID
     * @return 日志数量
     */
    Long countByUserId(String userId);

    /**
     * 统计各事件类型的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果 Map<事件类型, 数量>
     */
    List<Map<String, Object>> countByEventType(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计各风险等级的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果 Map<风险等级, 数量>
     */
    List<Map<String, Object>> countByRiskLevel(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计各操作状态的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果 Map<操作状态, 数量>
     */
    List<Map<String, Object>> countByStatus(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户行为分析报告
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分析报告
     */
    Map<String, Object> getUserBehaviorAnalysis(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取安全风险分析报告
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 风险分析报告
     */
    Map<String, Object> getSecurityRiskAnalysis(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取系统活动趋势分析
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  时间间隔（HOUR, DAY, WEEK, MONTH）
     * @return 趋势分析数据
     */
    List<Map<String, Object>> getActivityTrend(LocalDateTime startTime, LocalDateTime endTime, String interval);

    /**
     * 清理过期的审计日志
     *
     * @param expireTime 过期时间
     * @param batchSize  批量大小
     * @return 清理的记录数
     */
    int cleanupExpiredLogs(LocalDateTime expireTime, int batchSize);

    /**
     * 归档审计日志
     *
     * @param archiveTime 归档时间
     * @param batchSize   批量大小
     * @return 归档的记录数
     */
    int archiveAuditLogs(LocalDateTime archiveTime, int batchSize);

    /**
     * 验证审计日志链的完整性
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 验证结果
     */
    Map<String, Object> validateAuditChain(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 重建审计日志链
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 重建结果
     */
    Map<String, Object> rebuildAuditChain(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据租户ID查询审计日志
     *
     * @param tenantId 租户ID
     * @return 审计日志列表
     */
    List<AuditLog> findByTenantId(String tenantId);

    /**
     * 根据租户ID分页查询审计日志
     *
     * @param tenantId 租户ID
     * @param page     分页参数
     * @return 分页结果
     */
    IPage<AuditLog> findByTenantIdWithPage(String tenantId, Page<AuditLog> page);

    /**
     * 统计租户的审计日志数量
     *
     * @param tenantId 租户ID
     * @return 日志数量
     */
    Long countByTenantId(String tenantId);

    /**
     * 获取租户的审计统计报告
     *
     * @param tenantId  租户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计报告
     */
    Map<String, Object> getTenantAuditReport(String tenantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清空审计日志缓存
     */
    void clearCache();

    /**
     * 预热审计日志缓存
     *
     * @param tenantId 租户ID（可选）
     */
    void warmupCache(String tenantId);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    Map<String, Object> getCacheStats();
}
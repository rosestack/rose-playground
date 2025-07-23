package io.github.rose.security.rbac.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 权限审计服务
 * <p>
 * 提供完整的权限审计功能，包括：
 * - 权限检查审计
 * - 权限变更审计
 * - 异常权限使用检测
 * - 审计日志查询和分析
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionAuditService {

    private final ApplicationEventPublisher eventPublisher;
    private final PermissionAuditRepository auditRepository;

    /**
     * 记录权限检查审计
     *
     * @param auditInfo 审计信息
     */
    @Async
    public void auditPermissionCheck(PermissionCheckAudit auditInfo) {
        try {
            // 保存审计记录
            auditRepository.save(auditInfo);
            
            // 发布审计事件
            eventPublisher.publishEvent(new PermissionCheckAuditEvent(auditInfo));
            
            // 异常检测
            if (auditInfo.isAccessDenied()) {
                detectAnomalousAccess(auditInfo);
            }
            
            log.debug("权限检查审计记录已保存: userId={}, permission={}, result={}", 
                    auditInfo.getUserId(), auditInfo.getPermission(), auditInfo.isGranted());
                    
        } catch (Exception e) {
            log.error("权限检查审计失败", e);
        }
    }

    /**
     * 记录权限变更审计
     *
     * @param auditInfo 审计信息
     */
    @Async
    public void auditPermissionChange(PermissionChangeAudit auditInfo) {
        try {
            // 保存审计记录
            auditRepository.save(auditInfo);
            
            // 发布审计事件
            eventPublisher.publishEvent(new PermissionChangeAuditEvent(auditInfo));
            
            log.info("权限变更审计记录已保存: type={}, entityId={}, operation={}", 
                    auditInfo.getChangeType(), auditInfo.getEntityId(), auditInfo.getOperation());
                    
        } catch (Exception e) {
            log.error("权限变更审计失败", e);
        }
    }

    /**
     * 查询权限审计日志
     *
     * @param query 查询条件
     * @return 审计日志列表
     */
    public List<PermissionAuditLog> queryAuditLogs(PermissionAuditQuery query) {
        try {
            return auditRepository.findByQuery(query);
        } catch (Exception e) {
            log.error("查询权限审计日志失败", e);
            return List.of();
        }
    }

    /**
     * 异步查询权限审计日志
     *
     * @param query 查询条件
     * @return 审计日志列表的Future
     */
    @Async
    public CompletableFuture<List<PermissionAuditLog>> queryAuditLogsAsync(PermissionAuditQuery query) {
        return CompletableFuture.supplyAsync(() -> queryAuditLogs(query));
    }

    /**
     * 生成权限使用报告
     *
     * @param tenantId  租户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 权限使用报告
     */
    public PermissionUsageReport generateUsageReport(String tenantId, 
                                                   LocalDateTime startTime, 
                                                   LocalDateTime endTime) {
        try {
            // 查询权限使用统计
            Map<String, Long> permissionUsageCount = auditRepository
                    .countPermissionUsage(tenantId, startTime, endTime);
            
            // 查询用户权限使用统计
            Map<Long, Long> userPermissionUsage = auditRepository
                    .countUserPermissionUsage(tenantId, startTime, endTime);
            
            // 查询权限拒绝统计
            Map<String, Long> permissionDeniedCount = auditRepository
                    .countPermissionDenied(tenantId, startTime, endTime);
            
            // 查询异常权限使用
            List<AnomalousPermissionUsage> anomalousUsage = auditRepository
                    .findAnomalousUsage(tenantId, startTime, endTime);
            
            return PermissionUsageReport.builder()
                    .tenantId(tenantId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .permissionUsageCount(permissionUsageCount)
                    .userPermissionUsage(userPermissionUsage)
                    .permissionDeniedCount(permissionDeniedCount)
                    .anomalousUsage(anomalousUsage)
                    .generatedTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("生成权限使用报告失败: tenantId={}", tenantId, e);
            return PermissionUsageReport.empty(tenantId, startTime, endTime);
        }
    }

    /**
     * 检测异常权限访问
     *
     * @param auditInfo 审计信息
     */
    private void detectAnomalousAccess(PermissionCheckAudit auditInfo) {
        try {
            // 检测频繁的权限拒绝
            if (isFrequentDenial(auditInfo)) {
                publishAnomalousEvent(AnomalousType.FREQUENT_DENIAL, auditInfo);
            }
            
            // 检测异常时间访问
            if (isAnomalousTimeAccess(auditInfo)) {
                publishAnomalousEvent(AnomalousType.ANOMALOUS_TIME, auditInfo);
            }
            
            // 检测异常IP访问
            if (isAnomalousIpAccess(auditInfo)) {
                publishAnomalousEvent(AnomalousType.ANOMALOUS_IP, auditInfo);
            }
            
        } catch (Exception e) {
            log.error("异常权限访问检测失败", e);
        }
    }

    /**
     * 检测是否为频繁拒绝
     */
    private boolean isFrequentDenial(PermissionCheckAudit auditInfo) {
        // 查询最近5分钟内同一用户的权限拒绝次数
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long denialCount = auditRepository.countDenialsByUserSince(
                auditInfo.getUserId(), auditInfo.getTenantId(), fiveMinutesAgo);
        
        return denialCount >= 10; // 5分钟内拒绝超过10次
    }

    /**
     * 检测是否为异常时间访问
     */
    private boolean isAnomalousTimeAccess(PermissionCheckAudit auditInfo) {
        // 检测是否在非工作时间访问敏感权限
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // 非工作时间（22:00-06:00）访问敏感权限
        boolean isNonWorkingHours = hour >= 22 || hour <= 6;
        boolean isSensitivePermission = auditInfo.getPermission().contains("admin") || 
                                      auditInfo.getPermission().contains("delete") ||
                                      auditInfo.getPermission().contains("system");
        
        return isNonWorkingHours && isSensitivePermission;
    }

    /**
     * 检测是否为异常IP访问
     */
    private boolean isAnomalousIpAccess(PermissionCheckAudit auditInfo) {
        // 检测是否来自异常IP地址
        String clientIp = auditInfo.getClientIp();
        if (clientIp == null) {
            return false;
        }
        
        // 查询用户最近30天的常用IP
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<String> commonIps = auditRepository.findCommonIpsByUser(
                auditInfo.getUserId(), auditInfo.getTenantId(), thirtyDaysAgo);
        
        // 如果当前IP不在常用IP列表中，则认为异常
        return !commonIps.contains(clientIp);
    }

    /**
     * 发布异常事件
     */
    private void publishAnomalousEvent(AnomalousType type, PermissionCheckAudit auditInfo) {
        AnomalousPermissionAccessEvent event = AnomalousPermissionAccessEvent.builder()
                .type(type)
                .userId(auditInfo.getUserId())
                .tenantId(auditInfo.getTenantId())
                .permission(auditInfo.getPermission())
                .clientIp(auditInfo.getClientIp())
                .userAgent(auditInfo.getUserAgent())
                .timestamp(auditInfo.getTimestamp())
                .build();
        
        eventPublisher.publishEvent(event);
        
        log.warn("检测到异常权限访问: type={}, userId={}, permission={}", 
                type, auditInfo.getUserId(), auditInfo.getPermission());
    }

    /**
     * 异常类型枚举
     */
    public enum AnomalousType {
        FREQUENT_DENIAL,    // 频繁拒绝
        ANOMALOUS_TIME,     // 异常时间
        ANOMALOUS_IP,       // 异常IP
        PRIVILEGE_ESCALATION // 权限提升
    }
}
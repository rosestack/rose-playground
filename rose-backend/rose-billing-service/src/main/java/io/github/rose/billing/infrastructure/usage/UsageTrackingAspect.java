package io.github.rose.billing.infrastructure.usage;

import io.github.rose.billing.domain.service.BillingService;
import io.github.rose.billing.domain.service.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

/**
 * 使用量自动监控切面
 * 通过AOP自动记录API调用、存储使用等
 *
 * @author rose
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UsageTrackingAspect {

    private final BillingService billingService;
    private final TenantContextHolder tenantContextHolder;

    /**
     * 监控API调用
     */
    @AfterReturning("@annotation(trackApiUsage)")
    public void trackApiCall(JoinPoint joinPoint, TrackApiUsage trackApiUsage) {
        try {
            String tenantId = tenantContextHolder.getCurrentTenantId();
            if (tenantId != null) {
                String apiPath = trackApiUsage.value();
                if (apiPath.isEmpty()) {
                    apiPath = joinPoint.getSignature().getName();
                }

                billingService.recordUsage(
                        tenantId,
                        "API_CALLS",
                        BigDecimal.ONE,
                        apiPath,
                        buildApiMetadata(joinPoint)
                );

                log.debug("记录API调用使用量：{} - {}", tenantId, apiPath);
            }
        } catch (Exception e) {
            log.error("记录API使用量失败", e);
        }
    }

    /**
     * 监控存储使用
     */
    @AfterReturning(value = "@annotation(trackStorageUsage)", returning = "result")
    public void trackStorageUsage(JoinPoint joinPoint, TrackStorageUsage trackStorageUsage, Object result) {
        try {
            String tenantId = tenantContextHolder.getCurrentTenantId();
            if (tenantId != null && result instanceof Number) {
                BigDecimal storageSize = new BigDecimal(result.toString());

                billingService.recordUsage(
                        tenantId,
                        "STORAGE",
                        storageSize,
                        trackStorageUsage.resourceType(),
                        buildStorageMetadata(joinPoint, storageSize)
                );

                log.debug("记录存储使用量：{} - {} bytes", tenantId, storageSize);
            }
        } catch (Exception e) {
            log.error("记录存储使用量失败", e);
        }
    }

    /**
     * 监控用户数变化
     */
    @AfterReturning("@annotation(trackUserChange)")
    public void trackUserChange(JoinPoint joinPoint, TrackUserChange trackUserChange) {
        try {
            String tenantId = tenantContextHolder.getCurrentTenantId();
            if (tenantId != null) {
                // 获取当前用户总数
                int currentUserCount = getUserCount(tenantId);

                billingService.recordUsage(
                        tenantId,
                        "USERS",
                        new BigDecimal(currentUserCount),
                        "USER_COUNT",
                        "{\"operation\":\"" + trackUserChange.operation() + "\"}"
                );

                log.debug("记录用户数变化：{} - {} users", tenantId, currentUserCount);
            }
        } catch (Exception e) {
            log.error("记录用户数变化失败", e);
        }
    }

    /**
     * 监控邮件发送
     */
    @AfterReturning("@annotation(trackEmailUsage)")
    public void trackEmailSent(JoinPoint joinPoint, TrackEmailUsage trackEmailUsage) {
        try {
            String tenantId = tenantContextHolder.getCurrentTenantId();
            if (tenantId != null) {
                billingService.recordUsage(
                        tenantId,
                        "EMAIL_SENT",
                        BigDecimal.ONE,
                        trackEmailUsage.emailType(),
                        buildEmailMetadata(joinPoint)
                );

                log.debug("记录邮件发送使用量：{} - {}", tenantId, trackEmailUsage.emailType());
            }
        } catch (Exception e) {
            log.error("记录邮件使用量失败", e);
        }
    }

    /**
     * 监控短信发送
     */
    @AfterReturning("@annotation(trackSmsUsage)")
    public void trackSmsSent(JoinPoint joinPoint, TrackSmsUsage trackSmsUsage) {
        try {
            String tenantId = tenantContextHolder.getCurrentTenantId();
            if (tenantId != null) {
                billingService.recordUsage(
                        tenantId,
                        "SMS_SENT",
                        BigDecimal.ONE,
                        trackSmsUsage.smsType(),
                        buildSmsMetadata(joinPoint)
                );

                log.debug("记录短信发送使用量：{} - {}", tenantId, trackSmsUsage.smsType());
            }
        } catch (Exception e) {
            log.error("记录短信使用量失败", e);
        }
    }

    private String buildApiMetadata(JoinPoint joinPoint) {
        return String.format("{\"method\":\"%s\",\"timestamp\":\"%s\",\"args\":%d}",
                joinPoint.getSignature().getName(),
                System.currentTimeMillis(),
                joinPoint.getArgs().length);
    }

    private String buildStorageMetadata(JoinPoint joinPoint, BigDecimal size) {
        return String.format("{\"operation\":\"%s\",\"size\":%s,\"timestamp\":\"%s\"}",
                joinPoint.getSignature().getName(), size, System.currentTimeMillis());
    }

    private String buildEmailMetadata(JoinPoint joinPoint) {
        return String.format("{\"method\":\"%s\",\"timestamp\":\"%s\"}",
                joinPoint.getSignature().getName(), System.currentTimeMillis());
    }

    private String buildSmsMetadata(JoinPoint joinPoint) {
        return String.format("{\"method\":\"%s\",\"timestamp\":\"%s\"}",
                joinPoint.getSignature().getName(), System.currentTimeMillis());
    }

    private int getUserCount(String tenantId) {
        // TODO: 实现获取租户用户数的逻辑
        // 可以通过调用UserService或直接查询数据库
        return 1; // 占位符
    }
}


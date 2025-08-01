package io.github.rosestack.audit.util;

import io.github.rosestack.mybatis.support.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * 审计服务工具类
 * <p>
 * 提供审计服务的公共功能，包括上下文信息补充、错误处理、缓存管理等。
 * 减少代码重复，提高代码质量和可维护性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class AuditServiceUtils {

    /**
     * 统一的错误处理
     *
     * @param operation 操作名称
     * @param e         异常
     * @param context   上下文信息
     * @throws RuntimeException 包装后的运行时异常
     */
    public static void handleError(String operation, Exception e, Object context) {
        String message = String.format("%s失败", operation);
        log.error("{}: {}, 上下文: {}", message, e.getMessage(), context, e);
        throw new RuntimeException(message, e);
    }


    /**
     * 补充租户信息
     *
     * @param tenantId 当前租户ID
     * @return 补充后的租户ID
     */
    public static String enrichTenantContext(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            tenantId = TenantContextHolder.getCurrentTenantId();
        }
        return tenantId;
    }

    /**
     * 执行带错误处理的操作
     *
     * @param operation 操作名称
     * @param task      要执行的任务
     * @param context   上下文信息
     * @param <T>       返回类型
     * @return 操作结果
     */
    public static <T> T executeWithErrorHandling(String operation, Supplier<T> task, Object context) {
        try {
            return task.get();
        } catch (Exception e) {
            handleError(operation, e, context);
            return null; // 不会执行到这里，因为 handleError 会抛出异常
        }
    }
}

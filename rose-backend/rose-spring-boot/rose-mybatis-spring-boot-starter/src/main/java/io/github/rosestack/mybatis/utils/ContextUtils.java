package io.github.rosestack.mybatis.utils;

import io.github.rosestack.mybatis.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 上下文工具类
 * <p>
 * 统一处理用户、租户、请求等上下文信息的获取。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class ContextUtils {

    /**
     * 获取当前用户ID
     * <p>
     * 优先级：MDC > SecurityContext > 默认值
     * </p>
     *
     * @return 当前用户ID
     */
    public static String getCurrentUserId() {
        try {
            // 1. 尝试从 MDC 获取
            String userId = MDC.get("userId");
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }

            // 2. 尝试从 Spring Security 获取
            userId = getFromSecurityContext();
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }

            // 3. 尝试从自定义用户上下文获取
            userId = getFromCustomUserContext();
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }

        } catch (Exception e) {
            log.debug("获取当前用户ID失败: {}", e.getMessage());
        }

        // 4. 返回默认值
        return "SYSTEM";
    }

    /**
     * 获取当前租户ID
     * <p>
     * 优先级：MDC > TenantContext > 默认值
     * </p>
     *
     * @return 当前租户ID
     */
    public static String getCurrentTenantId() {
        try {
            // 1. 尝试从 MDC 获取
            String tenantId = MDC.get("tenantId");
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                return tenantId;
            }

            // 2. 尝试从租户上下文获取
            tenantId = TenantContextHolder.getCurrentTenantId();
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                return tenantId;
            }

        } catch (Exception e) {
            log.debug("获取当前租户ID失败: {}", e.getMessage());
        }

        // 3. 返回默认值
        return "DEFAULT";
    }

    /**
     * 获取当前请求ID
     * <p>
     * 优先级：MDC > 生成新的UUID
     * </p>
     *
     * @return 当前请求ID
     */
    public static String getCurrentRequestId() {
        try {
            String requestId = MDC.get("requestId");
            if (requestId != null && !requestId.trim().isEmpty()) {
                return requestId;
            }
        } catch (Exception e) {
            log.debug("获取当前请求ID失败: {}", e.getMessage());
        }

        // 生成新的请求ID
        return UUID.randomUUID().toString();
    }

    /**
     * 获取当前环境
     * <p>
     * 优先级：系统属性 > 环境变量 > 默认值
     * </p>
     *
     * @return 当前环境
     */
    public static String getCurrentEnvironment() {
        try {
            // 1. 尝试从系统属性获取
            String env = System.getProperty("spring.profiles.active");
            if (env != null && !env.trim().isEmpty()) {
                return env.trim();
            }

            // 2. 尝试从环境变量获取
            env = System.getenv("SPRING_PROFILES_ACTIVE");
            if (env != null && !env.trim().isEmpty()) {
                return env.trim();
            }

        } catch (Exception e) {
            log.debug("获取当前环境失败: {}", e.getMessage());
        }

        // 3. 返回默认值
        return "dev";
    }

    /**
     * 设置用户上下文
     *
     * @param userId    用户ID
     * @param tenantId  租户ID
     * @param requestId 请求ID
     */
    public static void setContext(String userId, String tenantId, String requestId) {
        if (userId != null) {
            MDC.put("userId", userId);
        }
        if (tenantId != null) {
            MDC.put("tenantId", tenantId);
        }
        if (requestId != null) {
            MDC.put("requestId", requestId);
        }
    }

    /**
     * 清除上下文
     */
    public static void clearContext() {
        MDC.remove("userId");
        MDC.remove("tenantId");
        MDC.remove("requestId");
    }

    /**
     * 从 Spring Security 获取用户ID
     */
    private static String getFromSecurityContext() {
        try {
            // 使用反射避免强依赖 Spring Security
            Class<?> securityContextHolderClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = securityContextHolderClass.getMethod("getContext").invoke(null);

            if (context != null) {
                Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
                if (authentication != null) {
                    Object principal = authentication.getClass().getMethod("getPrincipal").invoke(authentication);
                    if (principal != null) {
                        // 如果是 UserDetails 类型
                        try {
                            return (String) principal.getClass().getMethod("getUsername").invoke(principal);
                        } catch (Exception e) {
                            // 如果是字符串类型
                            return principal.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("从 Spring Security 获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从自定义用户上下文获取用户ID
     */
    private static String getFromCustomUserContext() {
        try {
            // 尝试从自定义用户上下文获取
            Class<?> userContextClass = Class.forName("io.github.rosestack.mybatis.context.UserContextHolder");
            return (String) userContextClass.getMethod("getCurrentUserId").invoke(null);
        } catch (Exception e) {
            log.debug("从自定义用户上下文获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}

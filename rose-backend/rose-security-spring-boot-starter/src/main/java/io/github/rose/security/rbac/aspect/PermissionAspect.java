package io.github.rose.security.rbac.aspect;

import io.github.rose.security.auth.exception.AccessDeniedException;
import io.github.rose.security.model.SecurityUser;
import io.github.rose.security.rbac.annotation.HasAnyPermission;
import io.github.rose.security.rbac.annotation.HasPermission;
import io.github.rose.security.rbac.annotation.HasRole;
import io.github.rose.security.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限注解切面处理器
 * <p>
 * 处理权限相关注解的AOP切面，提供方法级别的权限控制。
 * 支持@HasPermission、@HasAnyPermission、@HasRole等注解。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    /**
     * 处理@HasPermission注解
     */
    @Around("@annotation(hasPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, HasPermission hasPermission) throws Throwable {
        SecurityUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("用户未登录");
        }

        String permission = hasPermission.value();
        String tenantId = currentUser.getTenantId();

        boolean hasPermissionResult = permissionService.hasPermission(currentUser.getId(), permission, tenantId);

        if (!hasPermissionResult) {
            if (hasPermission.required()) {
                String errorMessage = hasPermission.errorMessage();
                log.warn("权限检查失败: userId={}, permission={}, tenantId={}, method={}", 
                        currentUser.getId(), permission, tenantId, getMethodName(joinPoint));
                throw new AccessDeniedException(errorMessage);
            } else {
                log.info("权限检查失败但允许继续执行: userId={}, permission={}, tenantId={}, method={}", 
                        currentUser.getId(), permission, tenantId, getMethodName(joinPoint));
            }
        } else {
            log.debug("权限检查通过: userId={}, permission={}, tenantId={}, method={}", 
                    currentUser.getId(), permission, tenantId, getMethodName(joinPoint));
        }

        return joinPoint.proceed();
    }

    /**
     * 处理@HasAnyPermission注解
     */
    @Around("@annotation(hasAnyPermission)")
    public Object checkAnyPermission(ProceedingJoinPoint joinPoint, HasAnyPermission hasAnyPermission) throws Throwable {
        SecurityUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("用户未登录");
        }

        String[] permissions = hasAnyPermission.value();
        String tenantId = currentUser.getTenantId();
        Set<String> permissionSet = Arrays.stream(permissions).collect(Collectors.toSet());

        boolean hasAnyPermissionResult = permissionService.hasAnyPermission(currentUser.getId(), permissionSet, tenantId);

        if (!hasAnyPermissionResult) {
            if (hasAnyPermission.required()) {
                String errorMessage = hasAnyPermission.errorMessage();
                log.warn("任一权限检查失败: userId={}, permissions={}, tenantId={}, method={}", 
                        currentUser.getId(), permissionSet, tenantId, getMethodName(joinPoint));
                throw new AccessDeniedException(errorMessage);
            } else {
                log.info("任一权限检查失败但允许继续执行: userId={}, permissions={}, tenantId={}, method={}", 
                        currentUser.getId(), permissionSet, tenantId, getMethodName(joinPoint));
            }
        } else {
            log.debug("任一权限检查通过: userId={}, permissions={}, tenantId={}, method={}", 
                    currentUser.getId(), permissionSet, tenantId, getMethodName(joinPoint));
        }

        return joinPoint.proceed();
    }

    /**
     * 处理@HasRole注解
     */
    @Around("@annotation(hasRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, HasRole hasRole) throws Throwable {
        SecurityUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("用户未登录");
        }

        String roleName = hasRole.value();
        String tenantId = currentUser.getTenantId();

        // 检查用户是否拥有指定角色
        boolean hasRoleResult = permissionService.getUserRoles(currentUser.getId(), tenantId)
                .stream()
                .anyMatch(role -> role.getName().equals(roleName));

        if (!hasRoleResult) {
            if (hasRole.required()) {
                String errorMessage = hasRole.errorMessage();
                log.warn("角色检查失败: userId={}, role={}, tenantId={}, method={}", 
                        currentUser.getId(), roleName, tenantId, getMethodName(joinPoint));
                throw new AccessDeniedException(errorMessage);
            } else {
                log.info("角色检查失败但允许继续执行: userId={}, role={}, tenantId={}, method={}", 
                        currentUser.getId(), roleName, tenantId, getMethodName(joinPoint));
            }
        } else {
            log.debug("角色检查通过: userId={}, role={}, tenantId={}, method={}", 
                    currentUser.getId(), roleName, tenantId, getMethodName(joinPoint));
        }

        return joinPoint.proceed();
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前用户
     */
    private SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return (SecurityUser) principal;
        }

        return null;
    }

    /**
     * 获取方法名称
     *
     * @param joinPoint 连接点
     * @return 方法名称
     */
    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}
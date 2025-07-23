package io.github.rose.security.rbac.expression;

import io.github.rose.security.model.SecurityUser;
import io.github.rose.security.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限表达式处理器
 * <p>
 * 提供Spring Security表达式中使用的权限检查方法。
 * 可以在@PreAuthorize、@PostAuthorize等注解中使用。
 * </p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @PreAuthorize("@permissionExpressionHandler.hasPermission(authentication, 'user:create')")
 * public User createUser(UserCreateRequest request) {
 *     // 方法实现
 * }
 * 
 * @PreAuthorize("@permissionExpressionHandler.hasAnyPermission(authentication, 'user:create,user:update')")
 * public User saveUser(UserSaveRequest request) {
 *     // 方法实现
 * }
 * }</pre>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component("permissionExpressionHandler")
@RequiredArgsConstructor
public class PermissionExpressionHandler {

    private final PermissionService permissionService;

    /**
     * 检查用户是否拥有指定权限
     *
     * @param authentication 认证信息
     * @param permission     权限名称
     * @return 是否拥有权限
     */
    public boolean hasPermission(Authentication authentication, String permission) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            log.warn("无法获取用户信息进行权限检查: permission={}", permission);
            return false;
        }

        try {
            boolean result = permissionService.hasPermission(user.getId(), permission, user.getTenantId());
            log.debug("权限表达式检查结果: userId={}, permission={}, result={}", user.getId(), permission, result);
            return result;
        } catch (Exception e) {
            log.error("权限表达式检查异常: userId={}, permission={}", user.getId(), permission, e);
            return false;
        }
    }

    /**
     * 检查用户是否拥有任一权限
     *
     * @param authentication 认证信息
     * @param permissions    权限名称，多个权限用逗号分隔
     * @return 是否拥有任一权限
     */
    public boolean hasAnyPermission(Authentication authentication, String permissions) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            log.warn("无法获取用户信息进行权限检查: permissions={}", permissions);
            return false;
        }

        Set<String> permissionSet = Arrays.stream(permissions.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        try {
            boolean result = permissionService.hasAnyPermission(user.getId(), permissionSet, user.getTenantId());
            log.debug("任一权限表达式检查结果: userId={}, permissions={}, result={}", user.getId(), permissionSet, result);
            return result;
        } catch (Exception e) {
            log.error("任一权限表达式检查异常: userId={}, permissions={}", user.getId(), permissionSet, e);
            return false;
        }
    }

    /**
     * 检查用户是否拥有所有权限
     *
     * @param authentication 认证信息
     * @param permissions    权限名称，多个权限用逗号分隔
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(Authentication authentication, String permissions) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            log.warn("无法获取用户信息进行权限检查: permissions={}", permissions);
            return false;
        }

        Set<String> permissionSet = Arrays.stream(permissions.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        try {
            boolean result = permissionService.hasAllPermissions(user.getId(), permissionSet, user.getTenantId());
            log.debug("全部权限表达式检查结果: userId={}, permissions={}, result={}", user.getId(), permissionSet, result);
            return result;
        } catch (Exception e) {
            log.error("全部权限表达式检查异常: userId={}, permissions={}", user.getId(), permissionSet, e);
            return false;
        }
    }

    /**
     * 检查用户是否拥有指定角色
     *
     * @param authentication 认证信息
     * @param roleName       角色名称
     * @return 是否拥有角色
     */
    public boolean hasRole(Authentication authentication, String roleName) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            log.warn("无法获取用户信息进行角色检查: roleName={}", roleName);
            return false;
        }

        try {
            boolean result = permissionService.getUserRoles(user.getId(), user.getTenantId())
                    .stream()
                    .anyMatch(role -> role.getName().equals(roleName));
            log.debug("角色表达式检查结果: userId={}, roleName={}, result={}", user.getId(), roleName, result);
            return result;
        } catch (Exception e) {
            log.error("角色表达式检查异常: userId={}, roleName={}", user.getId(), roleName, e);
            return false;
        }
    }

    /**
     * 检查用户是否可以访问指定租户的资源
     *
     * @param authentication 认证信息
     * @param targetTenantId 目标租户ID
     * @return 是否可以访问
     */
    public boolean canAccessTenant(Authentication authentication, String targetTenantId) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            log.warn("无法获取用户信息进行租户访问检查: targetTenantId={}", targetTenantId);
            return false;
        }

        try {
            boolean result = user.canAccessTenant(targetTenantId);
            log.debug("租户访问检查结果: userId={}, targetTenantId={}, result={}", user.getId(), targetTenantId, result);
            return result;
        } catch (Exception e) {
            log.error("租户访问检查异常: userId={}, targetTenantId={}", user.getId(), targetTenantId, e);
            return false;
        }
    }

    /**
     * 检查用户是否可以访问指定客户的资源
     *
     * @param authentication   认证信息
     * @param targetCustomerId 目标客户ID
     * @return 是否可以访问
     */
    public boolean canAccessCustomer(Authentication authentication, String targetCustomerId) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            log.warn("无法获取用户信息进行客户访问检查: targetCustomerId={}", targetCustomerId);
            return false;
        }

        try {
            boolean result = user.canAccessCustomer(targetCustomerId);
            log.debug("客户访问检查结果: userId={}, targetCustomerId={}, result={}", user.getId(), targetCustomerId, result);
            return result;
        } catch (Exception e) {
            log.error("客户访问检查异常: userId={}, targetCustomerId={}", user.getId(), targetCustomerId, e);
            return false;
        }
    }

    /**
     * 检查是否为系统管理员
     *
     * @param authentication 认证信息
     * @return 是否为系统管理员
     */
    public boolean isSystemAdmin(Authentication authentication) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            return false;
        }

        try {
            boolean result = user.isSystemAdmin();
            log.debug("系统管理员检查结果: userId={}, result={}", user.getId(), result);
            return result;
        } catch (Exception e) {
            log.error("系统管理员检查异常: userId={}", user.getId(), e);
            return false;
        }
    }

    /**
     * 检查是否为租户管理员
     *
     * @param authentication 认证信息
     * @return 是否为租户管理员
     */
    public boolean isTenantAdmin(Authentication authentication) {
        SecurityUser user = extractSecurityUser(authentication);
        if (user == null) {
            return false;
        }

        try {
            boolean result = user.isTenantAdmin();
            log.debug("租户管理员检查结果: userId={}, result={}", user.getId(), result);
            return result;
        } catch (Exception e) {
            log.error("租户管理员检查异常: userId={}", user.getId(), e);
            return false;
        }
    }

    /**
     * 从认证信息中提取SecurityUser
     *
     * @param authentication 认证信息
     * @return SecurityUser对象
     */
    private SecurityUser extractSecurityUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return (SecurityUser) principal;
        }

        log.warn("认证主体不是SecurityUser类型: {}", principal.getClass().getName());
        return null;
    }
}
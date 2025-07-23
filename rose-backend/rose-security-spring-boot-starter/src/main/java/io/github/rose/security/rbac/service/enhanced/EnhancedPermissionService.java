package io.github.rose.security.rbac.service.enhanced;

import io.github.rose.security.rbac.domain.Permission;
import io.github.rose.security.rbac.domain.Role;
import io.github.rose.security.rbac.service.PermissionService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 增强权限服务接口
 * <p>
 * 在基础权限服务基础上，提供权限继承、动态权限、批量操作、
 * 权限审计等高级功能。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface EnhancedPermissionService extends PermissionService {

    // ==================== 权限继承 ====================

    /**
     * 获取用户的有效权限（包含继承权限）
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 有效权限集合
     */
    Set<String> getEffectivePermissions(Long userId, String tenantId);

    /**
     * 获取角色的有效权限（包含继承权限）
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 有效权限集合
     */
    Set<String> getEffectiveRolePermissions(Long roleId, String tenantId);

    /**
     * 设置角色继承关系
     *
     * @param childRoleId  子角色ID
     * @param parentRoleId 父角色ID
     * @param tenantId     租户ID
     * @return 是否设置成功
     */
    boolean setRoleInheritance(Long childRoleId, Long parentRoleId, String tenantId);

    /**
     * 移除角色继承关系
     *
     * @param childRoleId  子角色ID
     * @param parentRoleId 父角色ID
     * @param tenantId     租户ID
     * @return 是否移除成功
     */
    boolean removeRoleInheritance(Long childRoleId, Long parentRoleId, String tenantId);

    /**
     * 获取角色的继承层次结构
     *
     * @param roleId   角色ID
     * @param tenantId 租户ID
     * @return 继承层次结构
     */
    RoleHierarchy getRoleHierarchy(Long roleId, String tenantId);

    // ==================== 动态权限 ====================

    /**
     * 创建动态权限
     *
     * @param permission 权限对象
     * @return 创建的权限
     */
    Permission createDynamicPermission(Permission permission);

    /**
     * 更新动态权限
     *
     * @param permission 权限对象
     * @return 更新的权限
     */
    Permission updateDynamicPermission(Permission permission);

    /**
     * 删除动态权限
     *
     * @param permissionId 权限ID
     * @param tenantId     租户ID
     * @return 是否删除成功
     */
    boolean deleteDynamicPermission(Long permissionId, String tenantId);

    /**
     * 刷新动态权限
     *
     * @param tenantId 租户ID
     */
    void refreshDynamicPermissions(String tenantId);

    // ==================== 批量操作 ====================

    /**
     * 批量检查用户权限
     *
     * @param userIds     用户ID集合
     * @param permissions 权限集合
     * @param tenantId    租户ID
     * @return 权限检查结果矩阵
     */
    Map<Long, Map<String, Boolean>> batchCheckUserPermissions(
            Set<Long> userIds, Set<String> permissions, String tenantId);

    /**
     * 批量为用户分配角色
     *
     * @param userRoleAssignments 用户角色分配映射
     * @param tenantId            租户ID
     * @return 分配结果
     */
    Map<Long, Map<Long, Boolean>> batchAssignRoles(
            Map<Long, Set<Long>> userRoleAssignments, String tenantId);

    /**
     * 批量为角色分配权限
     *
     * @param rolePermissionAssignments 角色权限分配映射
     * @param tenantId                  租户ID
     * @return 分配结果
     */
    Map<Long, Map<Long, Boolean>> batchAssignPermissions(
            Map<Long, Set<Long>> rolePermissionAssignments, String tenantId);

    // ==================== 异步操作 ====================

    /**
     * 异步获取用户权限
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限集合的Future
     */
    CompletableFuture<Set<String>> getUserPermissionsAsync(Long userId, String tenantId);

    /**
     * 异步权限检查
     *
     * @param userId     用户ID
     * @param permission 权限名称
     * @param tenantId   租户ID
     * @return 权限检查结果的Future
     */
    CompletableFuture<Boolean> hasPermissionAsync(Long userId, String permission, String tenantId);

    /**
     * 异步预加载权限缓存
     *
     * @param tenantId 租户ID
     * @return 预加载结果的Future
     */
    CompletableFuture<Void> preloadPermissionCacheAsync(String tenantId);

    // ==================== 权限分析 ====================

    /**
     * 分析用户权限使用情况
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限使用分析结果
     */
    PermissionUsageAnalysis analyzeUserPermissionUsage(Long userId, String tenantId);

    /**
     * 分析角色权限分布
     *
     * @param tenantId 租户ID
     * @return 角色权限分布分析结果
     */
    RolePermissionDistribution analyzeRolePermissionDistribution(String tenantId);

    /**
     * 检测权限冗余
     *
     * @param tenantId 租户ID
     * @return 权限冗余检测结果
     */
    PermissionRedundancyReport detectPermissionRedundancy(String tenantId);

    // ==================== 权限模板 ====================

    /**
     * 创建权限模板
     *
     * @param template 权限模板
     * @return 创建的模板
     */
    PermissionTemplate createPermissionTemplate(PermissionTemplate template);

    /**
     * 应用权限模板
     *
     * @param templateId 模板ID
     * @param userId     用户ID
     * @param tenantId   租户ID
     * @return 是否应用成功
     */
    boolean applyPermissionTemplate(Long templateId, Long userId, String tenantId);

    /**
     * 获取可用的权限模板
     *
     * @param tenantId 租户ID
     * @return 权限模板列表
     */
    List<PermissionTemplate> getAvailableTemplates(String tenantId);

    // ==================== 权限验证 ====================

    /**
     * 验证权限配置的一致性
     *
     * @param tenantId 租户ID
     * @return 验证结果
     */
    PermissionValidationResult validatePermissionConfiguration(String tenantId);

    /**
     * 检测权限配置冲突
     *
     * @param tenantId 租户ID
     * @return 冲突检测结果
     */
    List<PermissionConflict> detectPermissionConflicts(String tenantId);

    /**
     * 修复权限配置问题
     *
     * @param tenantId 租户ID
     * @return 修复结果
     */
    PermissionRepairResult repairPermissionConfiguration(String tenantId);

    // ==================== 内部类定义 ====================

    /**
     * 角色层次结构
     */
    interface RoleHierarchy {
        Long getRoleId();
        String getRoleName();
        List<RoleHierarchy> getChildren();
        List<RoleHierarchy> getParents();
        int getDepth();
    }

    /**
     * 权限使用分析结果
     */
    interface PermissionUsageAnalysis {
        Long getUserId();
        Set<String> getUsedPermissions();
        Set<String> getUnusedPermissions();
        Map<String, Integer> getPermissionUsageCount();
        Map<String, Long> getPermissionLastUsedTime();
    }

    /**
     * 角色权限分布分析结果
     */
    interface RolePermissionDistribution {
        String getTenantId();
        Map<String, Integer> getRoleUserCount();
        Map<String, Set<String>> getRolePermissions();
        Map<String, Integer> getPermissionRoleCount();
        List<String> getOrphanedPermissions();
    }

    /**
     * 权限冗余检测结果
     */
    interface PermissionRedundancyReport {
        String getTenantId();
        List<RedundantPermission> getRedundantPermissions();
        List<UnusedRole> getUnusedRoles();
        List<OverlappingRole> getOverlappingRoles();
    }

    /**
     * 冗余权限
     */
    interface RedundantPermission {
        String getPermissionName();
        List<String> getRedundantInRoles();
        String getRecommendation();
    }

    /**
     * 未使用角色
     */
    interface UnusedRole {
        String getRoleName();
        int getDaysSinceLastUsed();
        String getRecommendation();
    }

    /**
     * 重叠角色
     */
    interface OverlappingRole {
        String getRole1();
        String getRole2();
        Set<String> getOverlappingPermissions();
        double getSimilarityScore();
    }

    /**
     * 权限模板
     */
    interface PermissionTemplate {
        Long getId();
        String getName();
        String getDescription();
        Set<String> getPermissions();
        Set<String> getRoles();
        String getTenantId();
    }

    /**
     * 权限验证结果
     */
    interface PermissionValidationResult {
        boolean isValid();
        List<String> getErrors();
        List<String> getWarnings();
        Map<String, Object> getDetails();
    }

    /**
     * 权限冲突
     */
    interface PermissionConflict {
        String getType();
        String getDescription();
        List<String> getInvolvedEntities();
        String getSeverity();
        String getRecommendation();
    }

    /**
     * 权限修复结果
     */
    interface PermissionRepairResult {
        boolean isSuccess();
        int getFixedIssues();
        List<String> getFixedItems();
        List<String> getUnfixedItems();
        String getSummary();
    }
}
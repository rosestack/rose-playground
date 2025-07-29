package io.github.rosestack.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import io.github.rosestack.mybatis.annotation.DataPermission;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.enums.DataScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.rosestack.core.util.ServletUtils.getCurrentUserId;

/**
 * MyBatis Plus 数据权限处理器适配器
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RoseDataPermissionHandler implements MultiDataPermissionHandler {

    // 注解缓存 - 缓存 mappedStatementId 对应的注解信息
    private final Map<String, DataPermission> annotationCache = new ConcurrentHashMap<>();

    // 权限值缓存 - 缓存用户权限值，避免重复查询
    private final Map<String, CacheEntry<List<String>>> permissionCache = new ConcurrentHashMap<>();

    private final RoseMybatisProperties properties;

    // 上次清理时间
    private volatile long lastCleanupTime = System.currentTimeMillis();

    /**
     * 缓存条目
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long expireTime;

        public CacheEntry(T value, long ttlMinutes) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ttlMinutes);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public T getValue() {
            return value;
        }
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        try {
            // 定期清理过期缓存
            cleanupExpiredCache();

            // 获取数据权限注解（带缓存）
            DataPermission dataPermission = getDataPermissionAnnotationCached(mappedStatementId);
            if (dataPermission == null) {
                log.debug("方法 {} 没有数据权限注解，跳过权限控制", mappedStatementId);
                return null;
            }

            // 检查是否需要权限控制
            if (!needPermissionControl(dataPermission)) {
                log.debug("方法 {} 不需要权限控制，跳过", mappedStatementId);
                return null;
            }

            // 获取权限值（带缓存）
            List<String> permissionValues = getPermissionValuesCached(dataPermission);
            if (CollectionUtils.isEmpty(permissionValues)) {
                log.debug("方法 {} 权限值为空，不限制数据访问", mappedStatementId);
                return null;
            }

            // 构建权限过滤条件
            Expression permissionExpression = buildPermissionExpression(table, dataPermission, permissionValues);
            if (permissionExpression != null) {
                log.debug("为表 {} 添加数据权限条件: {}", table.getName(), permissionExpression);
                return permissionExpression;
            }

            return null;
        } catch (Exception e) {
            log.error("处理数据权限时发生错误，mappedStatementId: {}, table: {}",
                    mappedStatementId, table.getName(), e);
            return null;
        }
    }

    boolean needPermissionControl(DataPermission dataPermission) {
        return dataPermission != null && dataPermission.scope() != DataScope.ALL;
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > TimeUnit.MINUTES.toMillis(properties.getDataPermission().getCache().getCleanupIntervalMinutes())) {
            // 清理权限值缓存
            permissionCache.entrySet().removeIf(entry -> entry.getValue().isExpired());

            lastCleanupTime = currentTime;
            log.debug("清理过期缓存完成，当前缓存大小 - 注解: {}, 权限值: {}",
                    annotationCache.size(), permissionCache.size());
        }
    }

    /**
     * 获取数据权限注解（带缓存）
     */
    private DataPermission getDataPermissionAnnotationCached(String mappedStatementId) {
        // 先从缓存获取
        DataPermission cached = annotationCache.get(mappedStatementId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询注解
        DataPermission annotation = getDataPermissionAnnotation(mappedStatementId);
        if (annotation == null) {
            return null;
        }
        // 缓存结果（包括 null 值，避免重复查询不存在的注解）
        annotationCache.put(mappedStatementId, annotation);

        return annotation;
    }

    /**
     * 获取权限值（带缓存）
     */
    private List<String> getPermissionValuesCached(DataPermission dataPermission) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return getPermissionValues(dataPermission); // 无用户信息时不缓存
        }

        // 构建缓存键
        String cacheKey = buildPermissionCacheKey(currentUserId, dataPermission);

        // 先从缓存获取
        CacheEntry<List<String>> cached = permissionCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("权限值缓存命中: {}", cacheKey);
            return cached.getValue();
        }

        // 缓存未命中或已过期，查询权限值
        List<String> permissionValues = getPermissionValues(dataPermission);

        // 缓存结果
        permissionCache.put(cacheKey, new CacheEntry<>(permissionValues, properties.getDataPermission().getCache().getExpireMinutes()));
        log.debug("权限值已缓存: {} -> {}", cacheKey, permissionValues);

        return permissionValues;
    }

    /**
     * 构建权限缓存键
     */
    private String buildPermissionCacheKey(String userId, DataPermission dataPermission) {
        return String.format("%s:%s:%s:%s",
                userId,
                dataPermission.type(),
                dataPermission.scope(),
                dataPermission.field());
    }

    /**
     * 构建权限过滤表达式
     */
    private Expression buildPermissionExpression(Table table, DataPermission dataPermission, List<String> permissionValues) {
        String fieldName = dataPermission.field();

        // 处理表别名
        Column column = new Column(table, fieldName);

        if (permissionValues.size() == 1) {
            // 单个值使用等号
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(new StringValue(permissionValues.get(0)));
            return equalsTo;
        } else {
            // 多个值使用 IN 条件
            InExpression inExpression = new InExpression();
            inExpression.setLeftExpression(column);

            ExpressionList expressionList = new ExpressionList();
            List<Expression> expressions = permissionValues.stream()
                    .map(StringValue::new)
                    .collect(Collectors.toList());
            expressionList.setExpressions(expressions);

            inExpression.setRightExpression(expressionList);
            return inExpression;
        }
    }

    /**
     * 获取数据权限注解
     */
    private DataPermission getDataPermissionAnnotation(String mappedStatementId) {
        try {
            // 解析 mappedStatementId 获取类名和方法名
            int lastDotIndex = mappedStatementId.lastIndexOf('.');
            if (lastDotIndex == -1) {
                return null;
            }

            String className = mappedStatementId.substring(0, lastDotIndex);
            String methodName = mappedStatementId.substring(lastDotIndex + 1);

            // 先检查类级别的注解
            Class<?> mapperClass = Class.forName(className);
            DataPermission classAnnotation = mapperClass.getAnnotation(DataPermission.class);

            // 再检查方法级别的注解
            Method[] methods = mapperClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    DataPermission methodAnnotation = method.getAnnotation(DataPermission.class);
                    if (methodAnnotation != null) {
                        return methodAnnotation; // 方法级别注解优先
                    }
                }
            }

            return classAnnotation; // 返回类级别注解
        } catch (ClassNotFoundException e) {
            log.warn("无法找到 Mapper 类: {}", mappedStatementId, e);
            return null;
        } catch (Exception e) {
            log.error("获取数据权限注解时发生错误: {}", mappedStatementId, e);
            return null;
        }
    }

    public List<String> getPermissionValues(DataPermission dataPermission) {
        switch (dataPermission.type()) {
            case USER:
                return getUserPermissionValues(dataPermission);
            case PARENT:
                return getParentPermissionValues(dataPermission);
            case PARENT_PARENT:
                return getParentParentPermissionValues(dataPermission);
            case ROLE:
                return getRolePermissionValues(dataPermission);
            case CUSTOM:
                return getCustomPermissionValues(dataPermission);
            default:
                log.warn("不支持的数据权限类型: {}", dataPermission.type());
                return Collections.emptyList();
        }
    }

    /**
     * 获取用户级权限值
     */
    private List<String> getUserPermissionValues(DataPermission dataPermission) {
        // 从用户上下文获取当前用户ID
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            switch (dataPermission.scope()) {
                case SELF:
                    return Arrays.asList(currentUserId);
                case ALL:
                    return Collections.emptyList(); // 空列表表示不限制
                default:
                    log.warn("用户级权限不支持的数据范围: {}", dataPermission.scope());
                    return Arrays.asList(currentUserId);
            }
        }

        log.warn("未找到当前用户信息，使用默认权限控制");
        return List.of("-1");
    }

    private List<String> getParentPermissionValues(DataPermission dataPermission) {
        String currentParentId = getCurrentParentId();
        if (currentParentId != null) {
            switch (dataPermission.scope()) {
                case PARENT_AND_CHILD:
                    return getParentAndChildIds(currentParentId);
                case ALL:
                    return Collections.emptyList();
                default:
                    return List.of(currentParentId);
            }
        }

        log.warn("未找到当前部门信息，使用默认权限控制");
        return List.of("UNKNOWN");
    }

    /**
     * 获取组织级权限值
     */
    private List<String> getParentParentPermissionValues(DataPermission dataPermission) {
        String currentParentParentId = getCurrentParentParentId();
        if (currentParentParentId != null) {
            switch (dataPermission.scope()) {
                case PARENT_PARENT_AND_CHILD:
                    return getParentParentAndChildIds(currentParentParentId);
                case ALL:
                    return Collections.emptyList();
                default:
                    return List.of(currentParentParentId);
            }
        }

        log.warn("未找到当前组织信息，使用默认权限控制");
        return List.of("UNKNOWN");
    }

    /**
     * 获取角色级权限值
     */
    private List<String> getRolePermissionValues(DataPermission dataPermission) {
        List<String> currentRoleIds = getCurrentRoleIds();
        if (!currentRoleIds.isEmpty()) {
            return currentRoleIds;
        }

        log.warn("未找到当前角色信息，使用默认权限控制");
        return List.of("UNKNOWN");
    }

    /**
     * 获取自定义权限值
     */
    private List<String> getCustomPermissionValues(DataPermission dataPermission) {
        // 自定义权限逻辑，可以通过扩展点实现
        log.info("使用自定义数据权限逻辑，字段: {}", dataPermission.field());
        return Collections.emptyList();
    }

    /**
     * 获取当前部门ID
     */
    private String getCurrentParentId() {
        // TODO: 集成实际的部门上下文
        return "DEFAULT_DEPT";
    }

    /**
     * 获取当前组织ID
     */
    private String getCurrentParentParentId() {
        // TODO: 集成实际的组织上下文
        return "DEFAULT_ORG";
    }

    /**
     * 获取当前用户的角色ID列表
     */
    private List<String> getCurrentRoleIds() {
        // TODO: 集成实际的角色上下文
        return Arrays.asList("DEFAULT_ROLE");
    }

    /**
     * 获取部门及其子部门ID列表
     */
    private List<String> getParentAndChildIds(String parentId) {
        // TODO: 集成实际的部门层级查询
        return Arrays.asList(parentId, parentId + "_CHILD1", parentId + "_CHILD2");
    }

    /**
     * 获取组织及其子组织ID列表
     */
    private List<String> getParentParentAndChildIds(String parentParentId) {
        // TODO: 集成实际的组织层级查询
        return Arrays.asList(parentParentId, parentParentId + "_CHILD1", parentParentId + "_CHILD2");
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        annotationCache.clear();
        permissionCache.clear();
        log.info("所有数据权限缓存已清空");
    }

    /**
     * 清空指定用户的权限缓存
     */
    public void clearUserPermissionCache(String userId) {
        if (userId == null) return;

        permissionCache.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        log.info("用户 {} 的权限缓存已清空", userId);
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("annotationCacheSize", annotationCache.size());
        stats.put("permissionCacheSize", permissionCache.size());

        // 统计过期的权限缓存数量
        long expiredCount = permissionCache.values().stream()
                .mapToLong(entry -> entry.isExpired() ? 1 : 0)
                .sum();
        stats.put("expiredPermissionCacheCount", expiredCount);

        stats.put("lastCleanupTime", LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastCleanupTime),
                java.time.ZoneId.systemDefault()));

        return stats;
    }
}

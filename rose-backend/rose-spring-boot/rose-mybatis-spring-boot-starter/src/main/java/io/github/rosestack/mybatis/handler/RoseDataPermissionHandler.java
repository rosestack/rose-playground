package io.github.rosestack.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import io.github.rosestack.core.spring.SpringBeanUtils;
import io.github.rosestack.mybatis.annotation.DataPermission;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.datapermission.DataPermissionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

            // 获取权限值（带缓存）
            List<String> permissionValues = getPermissionValuesCached(dataPermission);
            if (CollectionUtils.isEmpty(permissionValues)) {
                log.debug("方法 {} 权限值为空，不限制数据访问", mappedStatementId);
                return null;
            }

            // 构建权限过滤条件
            Expression permissionExpression = buildPermissionExpression(table, dataPermission, permissionValues);
            log.debug("为表 {} 添加数据权限条件: {}", table.getName(), permissionExpression);
            return permissionExpression;

        } catch (Exception e) {
            log.error("处理数据权限时发生错误，mappedStatementId: {}, table: {}",
                    mappedStatementId, table.getName(), e);
            return null;
        }
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
    protected String buildPermissionCacheKey(String userId, DataPermission dataPermission) {
        return String.format("%s:%s:%s", userId, dataPermission.name(), dataPermission.field());
    }

    /**
     * 构建权限过滤表达式
     */
    private Expression buildPermissionExpression(Table table, DataPermission dataPermission, List<String> permissionValues) {
        String fieldName = dataPermission.field();
        DataPermission.FieldType fieldType = dataPermission.fieldType();

        // 处理表别名
        Column column = new Column(table, fieldName);
        if (permissionValues.size() == 1) {
            // 单个值使用等号
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(createValueExpression(permissionValues.get(0), fieldType));
            return equalsTo;
        } else {
            // 多个值使用 IN 条件
            InExpression inExpression = new InExpression();
            inExpression.setLeftExpression(column);

            ExpressionList expressionList = new ExpressionList();
            List<Expression> expressions = permissionValues.stream()
                    .map(value -> createValueExpression(value, fieldType))
                    .collect(Collectors.toList());
            expressionList.setExpressions(expressions);

            inExpression.setRightExpression(expressionList);
            return inExpression;
        }
    }

    /**
     * 根据字段类型创建对应的值表达式
     */
    private Expression createValueExpression(String value, DataPermission.FieldType fieldType) {
        if (value == null) {
            return new NullValue();
        }

        switch (fieldType) {
            case NUMBER:
                try {
                    return new LongValue(Long.parseLong(value));
                } catch (NumberFormatException e) {
                    log.warn("无法将值 '{}' 转换为 Long 类型，使用字符串类型", value);
                    return new StringValue(value);
                }
            default:
                return new StringValue(value);
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
        DataPermissionProvider dataPermissionProvider = SpringBeanUtils.getSortedBeans(DataPermissionProvider.class)
                .stream().filter(provider -> provider.support(dataPermission.name()))
                .findFirst().orElse(null);
        if (dataPermissionProvider == null) {
            throw new RuntimeException("未找到数据权限提供者");
        }
        log.debug("使用 {} 处理数据权限", dataPermissionProvider.getClass().getName());
        return dataPermissionProvider.getPermissionValues();
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

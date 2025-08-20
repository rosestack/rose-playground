package io.github.rosestack.spring.boot.mybatis.permission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.rosestack.mybatis.annotation.DataPermission;
import io.github.rosestack.mybatis.permission.CurrentUserProvider;
import io.github.rosestack.spring.boot.mybatis.permission.provider.DataPermissionProviderManager;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * MyBatis Plus 数据权限处理器适配器
 */
@Slf4j
public class RoseDataPermissionHandler implements MultiDataPermissionHandler {
    // 注解缓存 - 缓存 mappedStatementId 对应的注解信息
    private final Map<String, DataPermission> annotationCache = new ConcurrentHashMap<>();
    private final DataPermissionProviderManager providerManager;
    private final Cache<String, List<String>> permissionCache;
    private final CurrentUserProvider currentUserProvider;
    private final DataPermissionMetrics metrics;

    public RoseDataPermissionHandler(
            DataPermissionProviderManager providerManager,
            @Autowired(required = false) Cache<String, List<String>> permissionCache,
            CurrentUserProvider currentUserProvider,
            @Autowired(required = false) MeterRegistry registry) {
        this.currentUserProvider = currentUserProvider;
        this.providerManager = providerManager;
        this.permissionCache = permissionCache;
        this.metrics = registry != null ? new DataPermissionMetrics(registry) : null;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        Timer.Sample sample = metrics != null ? metrics.start() : null;
        try {
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
            if (metrics != null) {
                metrics.incrementError();
            }
            log.error("处理数据权限时发生错误，mappedStatementId: {}, table: {}", mappedStatementId, table.getName(), e);
            return null;
        } finally {
            if (metrics != null) {
                metrics.record(sample);
            }
        }
    }

    /**
     * 获取数据权限注解（带缓存）
     */
    private DataPermission getDataPermissionAnnotationCached(String mappedStatementId) {
        DataPermission cached = annotationCache.get(mappedStatementId);
        if (cached != null) {
            return cached;
        }
        DataPermission annotation = getDataPermissionAnnotation(mappedStatementId);
        if (annotation == null) {
            return null;
        }
        annotationCache.put(mappedStatementId, annotation);
        return annotation;
    }

    /**
     * 获取权限值（带缓存）
     */
    private List<String> getPermissionValuesCached(DataPermission dataPermission) {
        String currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            return getPermissionValues(dataPermission); // 无用户信息时不缓存
        }

        if (permissionCache == null) {
            return getPermissionValues(dataPermission);
        }

        // 构建缓存键
        String cacheKey = buildPermissionCacheKey(currentUserId, dataPermission);

        // 先从缓存获取
        List<String> cached = permissionCache.getIfPresent(cacheKey);
        if (cached != null) {
            if (metrics != null) metrics.incrementCacheHit();
            log.debug("权限值缓存命中: {}", cacheKey);
            return cached;
        }

        // 缓存未命中或已过期，查询权限值
        List<String> permissionValues = getPermissionValues(dataPermission);

        // 缓存结果
        permissionCache.put(cacheKey, permissionValues);
        if (metrics != null) metrics.incrementCacheMiss();
        log.debug("权限值已缓存: {} -> {}", cacheKey, permissionValues);

        return permissionValues;
    }

    /**
     * 构建权限缓存键
     */
    protected String buildPermissionCacheKey(String userId, DataPermission dataPermission) {
        return String.format("%s:%s:%s", userId, dataPermission.field(), dataPermission.fieldType());
    }

    /**
     * 构建权限过滤表达式
     */
    private Expression buildPermissionExpression(
            Table table, DataPermission dataPermission, List<String> permissionValues) {
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
        return providerManager.getPermissionValues(dataPermission.field());
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        annotationCache.clear();
        if (permissionCache != null) {
            permissionCache.invalidateAll();
        }
        log.info("所有数据权限缓存已清空");
    }

    /**
     * 清空指定用户的权限缓存
     */
    public void clearUserPermissionCache(String userId) {
        if (userId == null) {
            return;
        }

        if (permissionCache != null) {
            permissionCache.asMap().keySet().removeIf(key -> key.startsWith(userId + ":"));
        }

        log.info("用户 {} 的权限缓存已清空", userId);
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("annotationCacheSize", annotationCache.size());
        if (permissionCache != null) {
            stats.put("permissionCacheSize", permissionCache.estimatedSize());
        }
        stats.put("lastCleanupTime", LocalDateTime.now());
        return stats;
    }
}

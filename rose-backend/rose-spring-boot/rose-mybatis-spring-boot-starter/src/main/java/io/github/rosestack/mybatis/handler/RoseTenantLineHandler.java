package io.github.rosestack.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * Rose 多租户处理器
 * <p>
 * 实现 MyBatis Plus 的多租户插件接口，自动为 SQL 语句添加租户条件。
 * 支持灵活的表名忽略配置，确保系统表和配置表不受多租户影响。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RoseTenantLineHandler implements TenantLineHandler {

    private final RoseMybatisProperties properties;

    /**
     * 获取租户ID表达式
     * <p>
     * 从当前线程上下文中获取租户ID，如果没有租户上下文，
     * 返回一个不存在的租户ID以确保数据隔离。
     * </p>
     *
     * @return 租户ID表达式
     */
    @Override
    public Expression getTenantId() {
        String tenantId = ContextUtils.getCurrentTenantId();
        log.debug("获取当前租户ID: {}", tenantId);
        return new StringValue(tenantId);
    }

    /**
     * 获取租户字段名
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return properties.getTenant().getColumn();
    }

    /**
     * 判断是否忽略指定表的多租户处理
     * <p>
     * 根据配置的忽略表名列表和表名前缀列表来判断是否需要忽略多租户处理。
     * </p>
     *
     * @param tableName 表名
     * @return 如果需要忽略则返回 true，否则返回 false
     */
    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null) {
            return false;
        }

        // 检查是否在忽略表名列表中
        if (properties.getTenant().getIgnoreTables().contains(tableName)) {
            log.debug("表 {} 在忽略列表中，跳过多租户处理", tableName);
            return true;
        }

        // 检查是否匹配忽略的表名前缀
        for (String prefix : properties.getTenant().getIgnoreTablePrefixes()) {
            if (tableName.startsWith(prefix)) {
                log.debug("表 {} 匹配忽略前缀 {}，跳过多租户处理", tableName, prefix);
                return true;
            }
        }

        log.debug("表 {} 需要多租户处理", tableName);
        return false;
    }

}

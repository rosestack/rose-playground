package io.github.rosestack.spring.boot.mybatis.tenant;

import io.github.rosestack.spring.boot.mybatis.config.RoseMybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rose 多租户处理器
 *
 * <p>实现 MyBatis Plus 的多租户插件接口，自动为 SQL 语句添加租户条件。 支持灵活的表名忽略配置，确保系统表和配置表不受多租户影响。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RoseTenantLineHandler extends AbstractTenantLineHandler {

    private final RoseMybatisProperties properties;

    @Override
    protected String getTenantIdColumnInternal() {
        return properties.getTenant().getColumn();
    }

    @Override
    protected boolean isIgnoreTableInternal(String tableName) {
        if (tableName == null) {
            return false;
        }
        if (properties.getTenant().getIgnoreTables().contains(tableName)) {
            return true;
        }
        for (String prefix : properties.getTenant().getIgnoreTablePrefixes()) {
            if (tableName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

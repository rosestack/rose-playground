package io.github.rosestack.infrastructure.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态表名处理器工厂，用于创建不同表的动态表名处理器
 */
@Slf4j
public class DynamicTableNameFactory {
    public static final String TENANT_ID = "ROOT";

    public static TableNameHandler tableNameHandler() {
        return new TableNameHandler() {
            @Override
            public String dynamicTableName(String sql, String tableName) {
                log.debug("动态表名替换: {} -> {}", tableName, tableName + "_" + TENANT_ID);
                if (tableName.equals("t_user")) {
                    return tableName + "_" + TENANT_ID;
                }

                if (tableName.equals("t_log")) {
                    String yearMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
                    log.debug("动态表名替换: {} -> {}", tableName, tableName + "_" + yearMonth);
                    return tableName + "_" + yearMonth;
                }

                return tableName;
            }
        };
    }
}
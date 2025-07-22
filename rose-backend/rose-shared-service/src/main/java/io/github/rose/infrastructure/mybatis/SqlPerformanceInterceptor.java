package io.github.rose.infrastructure.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * SQL性能分析拦截器
 * <p>
 * 用于分析SQL执行性能，记录慢SQL，便于优化
 */
@Slf4j
@Data
public class SqlPerformanceInterceptor implements InnerInterceptor {

    /**
     * 最大执行时间，单位毫秒
     */
    private long maxTime = 1000; // 默认超过1秒的SQL会被记录为慢SQL

    /**
     * 是否格式化SQL
     */
    private boolean format = false;

    /**
     * 是否输出实际参数值
     */
    private boolean showParams = true;

    /**
     * 是否记录所有SQL，false则只记录慢SQL
     */
    private boolean logAllSql = false;

    /**
     * 是否写入慢SQL日志文件
     */
    private boolean writeIntoFile = false;

    /**
     * 慢SQL日志文件路径
     */
    private String slowSqlLogFilePath = "logs/slow-sql.log";

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        MetaObject metaObject = MetaObject.forObject(sh, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String sqlId = mappedStatement.getId();
        BoundSql boundSql = sh.getBoundSql();
        String sql = boundSql.getSql();
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // 格式化SQL
        String formattedSql = sql;
        if (showParams) {
            formattedSql = formatSql(mappedStatement.getConfiguration(), sql, parameterObject, parameterMappings);
        }

        if (format) {
            formattedSql = beautifySql(formattedSql);
        }

        long start = System.currentTimeMillis();
        try {
            // 执行SQL
            InnerInterceptor.super.beforePrepare(sh, connection, transactionTimeout);
        } finally {
            long end = System.currentTimeMillis();
            long timing = end - start;

            // 记录慢SQL
            if (timing > maxTime) {
                String slowSqlMessage = String.format("慢SQL检测 - 执行耗时: %d ms, SQL ID: %s, SQL: %s", timing, sqlId, formattedSql);
                log.warn(slowSqlMessage);

                // 写入慢SQL日志文件
                if (writeIntoFile) {
                    writeSlowSqlLog(slowSqlMessage);
                }
            } else if (logAllSql) {
                // 记录所有SQL
                log.debug("SQL执行 - 耗时: {} ms, SQL ID: {}, SQL: {}", timing, sqlId, formattedSql);
            }
        }
    }

    /**
     * 写入慢SQL日志到文件
     */
    private void writeSlowSqlLog(String slowSqlMessage) {
        // 实现写入日志文件的逻辑
        // 可以使用java.nio.file.Files或者其他日志框架实现
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(slowSqlLogFilePath),
                    (java.time.LocalDateTime.now() + " - " + slowSqlMessage + "\n").getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            log.error("写入慢SQL日志文件失败", e);
        }
    }

    /**
     * 美化SQL
     */
    private String beautifySql(String sql) {
        // 简单的SQL美化，实际项目中可以使用更复杂的SQL格式化工具
        sql = sql.replaceAll("(?i)SELECT", "\nSELECT")
                .replaceAll("(?i)FROM", "\nFROM")
                .replaceAll("(?i)WHERE", "\nWHERE")
                .replaceAll("(?i)AND", "\n  AND")
                .replaceAll("(?i)OR", "\n   OR")
                .replaceAll("(?i)GROUP BY", "\nGROUP BY")
                .replaceAll("(?i)HAVING", "\nHAVING")
                .replaceAll("(?i)ORDER BY", "\nORDER BY")
                .replaceAll("(?i)LIMIT", "\nLIMIT")
                .replaceAll("(?i)OFFSET", "\nOFFSET")
                .replaceAll("(?i)UPDATE", "\nUPDATE")
                .replaceAll("(?i)DELETE", "\nDELETE")
                .replaceAll("(?i)INSERT", "\nINSERT")
                .replaceAll("(?i)VALUES", "\nVALUES");
        return sql;
    }

    /**
     * 格式化SQL，将占位符替换为实际参数值
     */
    private String formatSql(Configuration configuration, String sql, Object parameterObject, List<ParameterMapping> parameterMappings) {
        if (sql == null || sql.length() == 0 || parameterObject == null) {
            return sql;
        }

        // 替换换行符
        sql = sql.replaceAll("[\\s\\n ]+", " ");

        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return sql;
        }

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        try {
            // 替换参数
            for (ParameterMapping parameterMapping : parameterMappings) {
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    continue;
                }

                String propertyName = parameterMapping.getProperty();
                Object value;

                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else if (parameterObject instanceof java.util.Map) {
                    value = ((java.util.Map<?, ?>) parameterObject).get(propertyName);
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    if (metaObject.hasGetter(propertyName)) {
                        value = metaObject.getValue(propertyName);
                    } else {
                        continue; // 找不到属性，跳过
                    }
                }

                String paramValue = formatParameterValue(value);
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(paramValue));
            }
        } catch (Exception e) {
            // 参数替换失败，返回原始SQL
            log.debug("SQL参数替换失败", e);
            return sql;
        }

        return sql;
    }

    /**
     * 格式化参数值
     */
    private String formatParameterValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String) {
            return "'" + value.toString().replaceAll("'", "''") + "'";
        }

        if (value instanceof Date) {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return "'" + dateFormat.format(value) + "'";
        }

        if (value instanceof java.time.temporal.Temporal) {
            return "'" + value + "'";
        }

        return String.valueOf(value);
    }
}
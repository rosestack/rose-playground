package io.github.rosestack.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.TableId;
import io.github.rosestack.mybatis.annotation.AuditLog;
import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.desensitization.SensitiveDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.github.rosestack.mybatis.util.TenantContextHolder.getCurrentTenantId;

/**
 * 审计拦截器
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class AuditInterceptor implements Interceptor {

    private final RoseMybatisProperties properties;
    private final AuditStorage auditStorage;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];

        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // 检查是否需要审计
        if (!shouldAudit(sqlCommandType)) {
            return invocation.proceed();
        }

        // 获取变更前的数据（仅对UPDATE操作）
        Object oldValue = null;
        if (sqlCommandType == SqlCommandType.UPDATE) {
            oldValue = getOldValue(mappedStatement, parameter);
        }

        try {
            // 执行SQL操作
            Object result = invocation.proceed();

            // 记录审计日志
            recordAuditLog(mappedStatement, parameter, oldValue, startTime, true, null);

            return result;
        } catch (Exception e) {
            // 记录失败的审计日志
            recordAuditLog(mappedStatement, parameter, oldValue, startTime, false, e.getMessage());
            throw e;
        }
    }

    /**
     * 判断是否需要审计
     */
    private boolean shouldAudit(SqlCommandType sqlCommandType) {
        if (!properties.getAudit().isEnabled()) {
            return false;
        }

        switch (sqlCommandType) {
            case INSERT:
            case UPDATE:
            case DELETE:
                return true;
            case SELECT:
            default:
                return false;
        }
    }

    /**
     * 记录统一审计日志
     */
    private void recordAuditLog(MappedStatement mappedStatement, Object parameter, Object oldValue,
                                long startTime, boolean success, String errorMessage) {
        try {
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String sql = boundSql.getSql();
            long executionTime = System.currentTimeMillis() - startTime;

            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

            // 构建统一审计日志
            AuditLogEntry auditLogEntry = AuditLogEntry.builder()
                    .timestamp(LocalDateTime.now())
                    .operation(sqlCommandType.name())
                    .mapperId(mappedStatement.getId())
                    .sql(formatSql(sql))
                    .parameters(formatParameters(parameter))
                    .executionTime(executionTime)
                    .success(success)
                    .errorMessage(errorMessage)
                    .userId(getCurrentUserId())
                    .tenantId(getCurrentTenantId())
                    .requestId(getCurrentRequestId())
                    .build();

            // 如果是UPDATE操作且有变更前数据，记录字段变更
            if (sqlCommandType == SqlCommandType.UPDATE && oldValue != null && parameter != null) {
                io.github.rosestack.mybatis.annotation.AuditLog auditLogAnnotation = getChangeLogAnnotation(mappedStatement, parameter);
                if (auditLogAnnotation != null) {
                    List<FieldChange> fieldChanges = compareObjects(oldValue, parameter, auditLogAnnotation.ignoreFields());
                    auditLogEntry.setFieldChanges(fieldChanges);
                    auditLogEntry.setModule(auditLogAnnotation.module());
                    auditLogEntry.setBusinessOperation(auditLogAnnotation.operation());

                    // 获取实体信息
                    auditLogEntry.setEntityClass(parameter.getClass().getSimpleName());
                    auditLogEntry.setEntityId(getIdValue(parameter));
                }
            }

            // 存储审计日志
            auditStorage.save(auditLogEntry);

            // 输出日志
            outputAuditLog(auditLogEntry);

        } catch (Exception e) {
            log.warn("记录统一审计日志失败: {}", e.getMessage());
        }
    }

    /**
     * 获取变更前的数据
     */
    private Object getOldValue(MappedStatement mappedStatement, Object parameter) {
        try {
            if (parameter != null && hasIdField(parameter)) {
                Object id = getIdValue(parameter);
                if (id != null) {
                    // 简化实现：克隆当前对象作为"变更前"数据
                    // 实际项目中应该查询数据库获取真实的变更前数据
                    return cloneObject(parameter);
                }
            }
        } catch (Exception e) {
            log.debug("获取变更前数据失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 比较对象变更（支持敏感字段脱敏）
     */
    private List<FieldChange> compareObjects(Object oldValue, Object newValue, String[] ignoreFields) {
        List<FieldChange> changes = new ArrayList<>();
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreFields));

        try {
            Field[] fields = newValue.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (ignoreSet.contains(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                Object oldFieldValue = field.get(oldValue);
                Object newFieldValue = field.get(newValue);

                if (!Objects.equals(oldFieldValue, newFieldValue)) {
                    // 检查是否为敏感字段，需要脱敏
                    SensitiveField sensitiveAnnotation = field.getAnnotation(SensitiveField.class);

                    String oldValueStr = oldFieldValue != null ? oldFieldValue.toString() : null;
                    String newValueStr = newFieldValue != null ? newFieldValue.toString() : null;

                    // 对敏感字段进行脱敏处理
                    if (sensitiveAnnotation != null) {
                        if (oldValueStr != null) {
                            oldValueStr = SensitiveDataProcessor.desensitizeObject(oldFieldValue).toString();
                        }
                        if (newValueStr != null) {
                            newValueStr = SensitiveDataProcessor.desensitizeObject(newValueStr).toString();
                        }
                    }

                    changes.add(FieldChange.builder()
                            .fieldName(field.getName())
                            .fieldType(field.getType().getSimpleName())
                            .oldValue(oldValueStr)
                            .newValue(newValueStr)
                            .sensitive(sensitiveAnnotation != null)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("比较对象变更失败: {}", e.getMessage());
        }

        return changes;
    }

    /**
     * 获取变更日志注解
     */
    private AuditLog getChangeLogAnnotation(MappedStatement mappedStatement, Object parameter) {
        try {
            // 检查参数对象的类注解
            if (parameter != null) {
                AuditLog annotation = parameter.getClass().getAnnotation(AuditLog.class);
                if (annotation != null) {
                    return annotation;
                }
            }

            // 检查 Mapper 方法注解
            String className = mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.'));
            String methodName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf('.') + 1);

            Class<?> mapperClass = Class.forName(className);
            for (java.lang.reflect.Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(AuditLog.class);
                }
            }
        } catch (Exception e) {
            log.debug("获取变更日志注解失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 格式化SQL
     */
    private String formatSql(String sql) {
        if (!properties.getAudit().isIncludeSql()) {
            return "[SQL_HIDDEN]";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 格式化参数
     */
    private String formatParameters(Object parameter) {
        if (!properties.getAudit().isIncludeParameters()) {
            return "[PARAMS_HIDDEN]";
        }

        if (parameter == null) {
            return "null";
        }

        String paramStr = parameter.toString();
        if (paramStr.length() > 500) {
            return paramStr.substring(0, 500) + "...";
        }

        return paramStr;
    }

    /**
     * 检查对象是否有ID字段
     */
    private boolean hasIdField(Object obj) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("id".equals(field.getName()) ||
                        field.isAnnotationPresent(com.baomidou.mybatisplus.annotation.TableId.class)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("检查ID字段失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取ID值
     */
    private Object getIdValue(Object obj) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("id".equals(field.getName()) ||
                        field.isAnnotationPresent(TableId.class)) {
                    field.setAccessible(true);
                    return field.get(obj);
                }
            }
        } catch (Exception e) {
            log.debug("获取ID值失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 克隆对象（简化实现）
     */
    private Object cloneObject(Object obj) {
        // 简化实现，实际项目中可以使用深拷贝工具
        return obj;
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        // TODO: 集成实际的用户上下文
        return "SYSTEM";
    }

    /**
     * 获取当前请求ID
     */
    private String getCurrentRequestId() {
        try {
            return org.slf4j.MDC.get("requestId");
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 输出审计日志
     */
    private void outputAuditLog(AuditLogEntry auditLogEntry) {
        String logLevel = properties.getAudit().getLogLevel();
        String logMessage = formatAuditLogMessage(auditLogEntry);

        switch (logLevel.toUpperCase()) {
            case "DEBUG":
                log.debug(logMessage);
                break;
            case "INFO":
                log.info(logMessage);
                break;
            case "WARN":
                log.warn(logMessage);
                break;
            case "ERROR":
                log.error(logMessage);
                break;
            default:
                log.info(logMessage);
        }
    }

    /**
     * 格式化审计日志消息
     */
    private String formatAuditLogMessage(AuditLogEntry auditLogEntry) {
        StringBuilder sb = new StringBuilder();

        // 基础信息
        sb.append(String.format("[UNIFIED_AUDIT] %s | %s | %s | %dms | %s | User:%s | Tenant:%s | Request:%s",
                auditLogEntry.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                auditLogEntry.getOperation(),
                auditLogEntry.isSuccess() ? "SUCCESS" : "FAILED",
                auditLogEntry.getExecutionTime(),
                auditLogEntry.getMapperId(),
                auditLogEntry.getUserId(),
                auditLogEntry.getTenantId(),
                auditLogEntry.getRequestId()));

        // 业务信息
        if (auditLogEntry.getModule() != null && auditLogEntry.getBusinessOperation() != null) {
            sb.append(String.format(" | Business:%s.%s", auditLogEntry.getModule(), auditLogEntry.getBusinessOperation()));
        }

        // 实体信息
        if (auditLogEntry.getEntityClass() != null && auditLogEntry.getEntityId() != null) {
            sb.append(String.format(" | Entity:%s[%s]", auditLogEntry.getEntityClass(), auditLogEntry.getEntityId()));
        }

        // 字段变更信息
        if (auditLogEntry.getFieldChanges() != null && !auditLogEntry.getFieldChanges().isEmpty()) {
            sb.append(" | Changes:");
            for (FieldChange change : auditLogEntry.getFieldChanges()) {
                sb.append(String.format(" %s:%s->%s",
                        change.getFieldName(),
                        change.getOldValue(),
                        change.getNewValue()));
                if (change.isSensitive()) {
                    sb.append("(脱敏)");
                }
            }
        }

        // SQL信息
        if (auditLogEntry.isSuccess() && auditLogEntry.getSql() != null) {
            sb.append(" | SQL:").append(auditLogEntry.getSql());
        }

        // 错误信息
        if (!auditLogEntry.isSuccess() && auditLogEntry.getErrorMessage() != null) {
            sb.append(" | Error:").append(auditLogEntry.getErrorMessage());
        }

        return sb.toString();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置中读取属性
    }

    /**
     * 统一审计日志实体
     */
    @lombok.Data
    @lombok.Builder
    public static class AuditLogEntry {
        // SQL审计信息
        private LocalDateTime timestamp;
        private String operation;
        private String mapperId;
        private String sql;
        private String parameters;
        private long executionTime;
        private boolean success;
        private String errorMessage;

        // 上下文信息
        private String userId;
        private String tenantId;
        private String requestId;

        // 业务变更信息
        private String module;
        private String businessOperation;
        private String entityClass;
        private Object entityId;
        private List<FieldChange> fieldChanges;
    }

    /**
     * 字段变更记录
     */
    @lombok.Data
    @lombok.Builder
    public static class FieldChange {
        private String fieldName;
        private String fieldType;
        private String oldValue;
        private String newValue;
        private boolean sensitive; // 是否为敏感字段
    }

    /**
     * 审计存储接口
     */
    public interface AuditStorage {
        void save(AuditLogEntry auditLogEntry);
    }
}

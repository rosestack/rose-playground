package io.github.rosestack.mybatis.interceptor;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.support.encryption.FieldEncryptor;
import io.github.rosestack.mybatis.support.encryption.HashService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.sql.Statement;
import java.util.Collection;

/**
 * 字段加密拦截器
 * <p>
 * 拦截 MyBatis 的执行过程，对标记了 @EncryptField 注解的字段进行自动加密和解密。
 * - 在插入和更新时自动加密敏感字段，并生成对应的哈希字段（如果启用）
 * - 在查询结果返回时自动解密敏感字段
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class FieldEncryptionInterceptor implements Interceptor {

    private final FieldEncryptor fieldEncryptor;
    private final HashService hashService;

    public FieldEncryptionInterceptor(FieldEncryptor fieldEncryptor, HashService hashService) {
        this.fieldEncryptor = fieldEncryptor;
        this.hashService = hashService;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();

        if (target instanceof Executor) {
            return handleExecutorUpdate(invocation);
        } else if (target instanceof ResultSetHandler) {
            return handleResultSetQuery(invocation);
        }

        return invocation.proceed();
    }

    /**
     * 处理插入和更新操作，加密敏感字段
     */
    private Object handleExecutorUpdate(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];

        // 只处理插入和更新操作
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.UPDATE) {
            encryptFields(parameter);
        }

        return invocation.proceed();
    }

    /**
     * 处理查询结果，解密敏感字段
     */
    private Object handleResultSetQuery(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        if (result != null) {
            decryptFields(result);
        }

        return result;
    }

    /**
     * 加密字段
     */
    private void encryptFields(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            if (!CollectionUtils.isEmpty(collection)) {
                for (Object item : collection) {
                    encryptFields(item);
                }
            }
            return;
        }

        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            EncryptField encryptField = AnnotationUtils.findAnnotation(field, EncryptField.class);
            if (encryptField != null) {
                try {
                    ReflectionUtils.makeAccessible(field);
                    Object value = ReflectionUtils.getField(field, obj);

                    if (value instanceof String) {
                        String plainText = (String) value;
                        String encryptedText = fieldEncryptor.encrypt(plainText, encryptField.value());
                        ReflectionUtils.setField(field, obj, encryptedText);
                        log.debug("字段 {} 已加密", field.getName());

                        // 如果启用了哈希查询，生成哈希字段
                        if (encryptField.searchable()) {
                            generateHashField(obj, field, plainText, encryptField);
                        }
                    }
                } catch (Exception e) {
                    log.error("加密字段 {} 失败: {}", field.getName(), e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 解密字段
     */
    private void decryptFields(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            if (!CollectionUtils.isEmpty(collection)) {
                for (Object item : collection) {
                    decryptFields(item);
                }
            }
            return;
        }

        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            EncryptField encryptField = AnnotationUtils.findAnnotation(field, EncryptField.class);
            if (encryptField != null) {
                try {
                    ReflectionUtils.makeAccessible(field);
                    Object value = ReflectionUtils.getField(field, obj);

                    if (value instanceof String) {
                        String cipherText = (String) value;
                        String decryptedText = fieldEncryptor.decrypt(cipherText, encryptField.value());
                        ReflectionUtils.setField(field, obj, decryptedText);
                        log.debug("字段 {} 已解密", field.getName());
                    }
                } catch (Exception e) {
                    log.error("解密字段 {} 失败: {}", field.getName(), e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 生成哈希字段
     */
    private void generateHashField(Object obj, java.lang.reflect.Field originalField, String plainText, EncryptField encryptField) {
        try {
            String hashFieldName = hashService.generateHashFieldName(originalField.getName(), encryptField.hashField());
            java.lang.reflect.Field hashField = ReflectionUtils.findField(obj.getClass(), hashFieldName);

            if (hashField != null) {
                ReflectionUtils.makeAccessible(hashField);
                String hashValue = hashService.generateHash(plainText, encryptField.hashType());
                ReflectionUtils.setField(hashField, obj, hashValue);
                log.debug("字段 {} 的哈希字段 {} 已生成", originalField.getName(), hashFieldName);
            } else {
                log.warn("未找到哈希字段 {}，请确保实体类中定义了该字段", hashFieldName);
            }
        } catch (Exception e) {
            log.error("生成哈希字段失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
    }
}

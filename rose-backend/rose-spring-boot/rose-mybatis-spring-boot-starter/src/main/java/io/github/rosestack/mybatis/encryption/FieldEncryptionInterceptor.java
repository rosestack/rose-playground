package io.github.rosestack.mybatis.encryption;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.sql.Statement;
import java.util.Collection;

/**
 * 字段加密拦截器
 * <p>
 * 拦截 MyBatis 的执行过程，对标记了 @EncryptField 注解的字段进行自动加密和解密。
 * - 在插入和更新时自动加密敏感字段
 * - 在查询结果返回时自动解密敏感字段
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class FieldEncryptionInterceptor implements Interceptor {

    private final FieldEncryptor fieldEncryptor;

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

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            EncryptField encryptField = field.getAnnotation(EncryptField.class);
            if (encryptField != null) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);

                    if (value instanceof String) {
                        String plainText = (String) value;
                        String encryptedText = fieldEncryptor.encrypt(plainText, encryptField.value());
                        field.set(obj, encryptedText);
                        log.debug("字段 {} 已加密", field.getName());

                        // 如果支持查询，生成哈希字段
                        if (encryptField.searchable()) {
                            setHashField(obj, field.getName(), plainText);
                        }
                    }
                } catch (Exception e) {
                    log.error("加密字段 {} 失败: {}", field.getName(), e.getMessage(), e);
                }
            }
        }
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

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            EncryptField encryptField = field.getAnnotation(EncryptField.class);
            if (encryptField != null) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);

                    if (value instanceof String) {
                        String cipherText = (String) value;
                        String decryptedText = fieldEncryptor.decrypt(cipherText, encryptField.value());
                        field.set(obj, decryptedText);
                        log.debug("字段 {} 已解密", field.getName());
                    }
                } catch (Exception e) {
                    log.error("解密字段 {} 失败: {}", field.getName(), e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
        // 可以从配置中读取属性
    }

    /**
     * 设置哈希字段用于查询
     * 使用加盐的 SHA-256 哈希，提高安全性
     */
    private void setHashField(Object obj, String fieldName, String plainText) {
        try {
            String hashFieldName = fieldName + "_hash";
            Field hashField = obj.getClass().getDeclaredField(hashFieldName);
            hashField.setAccessible(true);

            // 使用加盐的 SHA-256 哈希
            String hashValue = createSecureHash(plainText);
            hashField.set(obj, hashValue);
            log.debug("字段 {} 的安全哈希值已设置到 {}", fieldName, hashFieldName);
        } catch (NoSuchFieldException e) {
            log.warn("未找到哈希字段 {}_hash，跳过哈希值设置", fieldName);
        } catch (Exception e) {
            log.error("设置哈希字段失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建安全的哈希值
     * 使用应用级固定盐 + 动态盐 + SHA-256
     */
    private String createSecureHash(String plainText) {
        try {
            // 应用级固定盐（从配置获取，生产环境应该从外部配置）
            String appSalt = getAppSalt();
            if (appSalt == null || appSalt.isEmpty()) {
                appSalt = "DEFAULT_APP_SALT_2024";
            }

            // 基于明文生成确定性的动态盐（确保同样的明文总是生成同样的哈希）
            String dynamicSalt = DigestUtils.md5DigestAsHex((plainText + appSalt).getBytes()).substring(0, 8);

            // 组合盐值
            String combinedSalt = appSalt + dynamicSalt;

            // 使用 SHA-256 + 盐值
            String saltedText = combinedSalt + plainText + combinedSalt;
            return sha256Hex(saltedText.getBytes());
        } catch (Exception e) {
            log.warn("创建安全哈希失败，降级使用简单哈希: {}", e.getMessage());
            return DigestUtils.md5DigestAsHex(plainText.getBytes());
        }
    }
    /**
     * 获取应用盐值
     */
    private String getAppSalt() {
        try {
            // 通过反射获取加密器的配置
            if (fieldEncryptor instanceof DefaultFieldEncryptor) {
                Field propertiesField = fieldEncryptor.getClass().getDeclaredField("properties");
                propertiesField.setAccessible(true);
                RoseMybatisProperties properties = (RoseMybatisProperties) propertiesField.get(fieldEncryptor);
                return properties.getEncryption().getSecretKey();
            }
        } catch (Exception e) {
            log.debug("获取应用盐值失败: {}", e.getMessage());
        }
        return "DEFAULT_APP_SALT_2024";
    }

    /**
     * SHA-256 哈希工具方法
     */
    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // 降级使用 MD5
            return DigestUtils.md5DigestAsHex(data);
        }
    }
}

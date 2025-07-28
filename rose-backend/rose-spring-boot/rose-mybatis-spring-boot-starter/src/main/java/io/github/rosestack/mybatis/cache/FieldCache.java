package io.github.rosestack.mybatis.cache;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.annotation.SensitiveField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段缓存工具类
 * <p>
 * 缓存类的字段信息，避免重复反射操作，提升性能。
 * 支持加密字段、敏感字段等的缓存。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class FieldCache {

    /**
     * 加密字段缓存
     */
    private static final Map<Class<?>, List<Field>> ENCRYPT_FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 敏感字段缓存
     */
    private static final Map<Class<?>, List<Field>> SENSITIVE_FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 所有字段缓存
     */
    private static final Map<Class<?>, List<Field>> ALL_FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取类的所有加密字段
     *
     * @param clazz 目标类
     * @return 加密字段列表
     */
    public static List<Field> getEncryptFields(Class<?> clazz) {
        return ENCRYPT_FIELDS_CACHE.computeIfAbsent(clazz, k -> {
            log.debug("缓存类 {} 的加密字段", k.getSimpleName());
            List<Field> encryptFields = new ArrayList<>();

            // 使用 Spring 的 ReflectionUtils 遍历所有字段
            ReflectionUtils.doWithFields(k, field -> {
                if (AnnotationUtils.findAnnotation(field, EncryptField.class) != null) {
                    ReflectionUtils.makeAccessible(field);
                    encryptFields.add(field);
                }
            });

            return encryptFields;
        });
    }

    /**
     * 获取类的所有敏感字段
     *
     * @param clazz 目标类
     * @return 敏感字段列表
     */
    public static List<Field> getSensitiveFields(Class<?> clazz) {
        return SENSITIVE_FIELDS_CACHE.computeIfAbsent(clazz, k -> {
            log.debug("缓存类 {} 的敏感字段", k.getSimpleName());
            List<Field> sensitiveFields = new ArrayList<>();

            // 使用 Spring 的 ReflectionUtils 遍历所有字段
            ReflectionUtils.doWithFields(k, field -> {
                if (AnnotationUtils.findAnnotation(field, SensitiveField.class) != null) {
                    ReflectionUtils.makeAccessible(field);
                    sensitiveFields.add(field);
                }
            });

            return sensitiveFields;
        });
    }

    /**
     * 获取类的所有字段（包括父类字段）
     *
     * @param clazz 目标类
     * @return 所有字段数组
     */
    public static Field[] getAllFields(Class<?> clazz) {
        return ALL_FIELDS_CACHE.computeIfAbsent(clazz, k -> {
            log.debug("缓存类 {} 的所有字段", k.getSimpleName());
            List<Field> allFields = new ArrayList<>();

            // 使用 Spring 的 ReflectionUtils 获取所有字段（包括父类）
            ReflectionUtils.doWithFields(k, field -> {
                ReflectionUtils.makeAccessible(field);
                allFields.add(field);
            });

            return allFields;
        }).toArray(new Field[0]);
    }

    /**
     * 检查类是否有加密字段
     *
     * @param clazz 目标类
     * @return 是否有加密字段
     */
    public static boolean hasEncryptFields(Class<?> clazz) {
        return !getEncryptFields(clazz).isEmpty();
    }

    /**
     * 检查类是否有敏感字段
     *
     * @param clazz 目标类
     * @return 是否有敏感字段
     */
    public static boolean hasSensitiveFields(Class<?> clazz) {
        return !getSensitiveFields(clazz).isEmpty();
    }

    /**
     * 清空所有缓存
     */
    public static void clearCache() {
        log.info("清空字段缓存");
        ENCRYPT_FIELDS_CACHE.clear();
        SENSITIVE_FIELDS_CACHE.clear();
        ALL_FIELDS_CACHE.clear();
    }

    /**
     * 清空指定类的缓存
     *
     * @param clazz 目标类
     */
    public static void clearCache(Class<?> clazz) {
        log.debug("清空类 {} 的字段缓存", clazz.getSimpleName());
        ENCRYPT_FIELDS_CACHE.remove(clazz);
        SENSITIVE_FIELDS_CACHE.remove(clazz);
        ALL_FIELDS_CACHE.remove(clazz);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public static CacheStats getCacheStats() {
        return CacheStats.builder()
                .encryptFieldsCacheSize(ENCRYPT_FIELDS_CACHE.size())
                .sensitiveFieldsCacheSize(SENSITIVE_FIELDS_CACHE.size())
                .allFieldsCacheSize(ALL_FIELDS_CACHE.size())
                .build();
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int encryptFieldsCacheSize;
        private final int sensitiveFieldsCacheSize;
        private final int allFieldsCacheSize;

        private CacheStats(int encryptFieldsCacheSize, int sensitiveFieldsCacheSize, int allFieldsCacheSize) {
            this.encryptFieldsCacheSize = encryptFieldsCacheSize;
            this.sensitiveFieldsCacheSize = sensitiveFieldsCacheSize;
            this.allFieldsCacheSize = allFieldsCacheSize;
        }

        public static CacheStatsBuilder builder() {
            return new CacheStatsBuilder();
        }

        public int getEncryptFieldsCacheSize() { return encryptFieldsCacheSize; }
        public int getSensitiveFieldsCacheSize() { return sensitiveFieldsCacheSize; }
        public int getAllFieldsCacheSize() { return allFieldsCacheSize; }

        @Override
        public String toString() {
            return String.format("CacheStats{encryptFields=%d, sensitiveFields=%d, allFields=%d}",
                    encryptFieldsCacheSize, sensitiveFieldsCacheSize, allFieldsCacheSize);
        }

        public static class CacheStatsBuilder {
            private int encryptFieldsCacheSize;
            private int sensitiveFieldsCacheSize;
            private int allFieldsCacheSize;

            public CacheStatsBuilder encryptFieldsCacheSize(int size) {
                this.encryptFieldsCacheSize = size;
                return this;
            }

            public CacheStatsBuilder sensitiveFieldsCacheSize(int size) {
                this.sensitiveFieldsCacheSize = size;
                return this;
            }

            public CacheStatsBuilder allFieldsCacheSize(int size) {
                this.allFieldsCacheSize = size;
                return this;
            }

            public CacheStats build() {
                return new CacheStats(encryptFieldsCacheSize, sensitiveFieldsCacheSize, allFieldsCacheSize);
            }
        }
    }
}

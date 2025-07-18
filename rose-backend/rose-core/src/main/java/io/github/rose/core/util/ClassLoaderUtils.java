package io.github.rose.core.util;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassLoaderUtils {
    private static final Logger log = LoggerFactory.getLogger(ClassLoaderUtils.class);
    private static final ConcurrentMap<String, Class<?>> loadedClassesCache = new ConcurrentHashMap<>(256);

    @Nullable
    public static Class<?> loadClass(@Nullable String className) {
        return loadClass(className, getDefaultClassLoader());
    }

    @Nullable
    public static Class<?> loadClass(@Nullable String className, @Nullable ClassLoader classLoader) {
        return loadClass(className, classLoader, false);
    }

    @Nullable
    public static Class<?> loadClass(@Nullable String className, @Nullable ClassLoader classLoader, boolean cached) {
        if (StringUtils.isBlank(className)) {
            return null;
        }

        ClassLoader actualClassLoader = findClassLoader(classLoader);
        if (cached) {
            String cacheKey = buildCacheKey(actualClassLoader, className);
            return loadedClassesCache.computeIfAbsent(cacheKey, k -> doLoadClass(actualClassLoader, className));
        }
        return doLoadClass(actualClassLoader, className);
    }

    protected static Class<?> doLoadClass(ClassLoader classLoader, String className) {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        try {
            return classLoader.loadClass(className);
        } catch (Throwable e) {
            if (log.isTraceEnabled()) {
                log.trace("The Class[name : '{}'] can't be loaded from the ClassLoader : {}", className, classLoader, e);
            }
        }
        return null;
    }

    @Nullable
    private static ClassLoader findClassLoader(@Nullable ClassLoader classLoader) {
        return classLoader == null ? getDefaultClassLoader() : classLoader;
    }

    private static String buildCacheKey(ClassLoader classLoader, String className) {
        String cacheKey = className + classLoader.hashCode();
        return cacheKey;
    }

    @Nullable
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignored) {
        }

        if (classLoader == null) {
            classLoader = ClassLoaderUtils.class.getClassLoader();
        }

        if (classLoader == null) {
            // classLoader is null indicates the bootstrap ClassLoader
            try {
                classLoader = ClassLoader.getSystemClassLoader();
            } catch (Throwable ignored) {
            }
        }
        return classLoader;
    }
}

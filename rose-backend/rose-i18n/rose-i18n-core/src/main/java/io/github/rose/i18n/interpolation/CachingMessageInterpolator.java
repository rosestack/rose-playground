package io.github.rose.i18n.interpolation;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 支持 LRU 缓存的消息插值器包装器
 */
public class CachingMessageInterpolator implements MessageInterpolator {
    private final MessageInterpolator delegate;
    private final Map<CacheKey, String> cache;
    private final int capacity;

    public CachingMessageInterpolator(MessageInterpolator delegate, int capacity) {
        this.delegate = Objects.requireNonNull(delegate);
        this.capacity = capacity > 0 ? capacity : 100;
        this.cache = new LinkedHashMap<>(this.capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, String> eldest) {
                return size() > CachingMessageInterpolator.this.capacity;
            }
        };
    }

    @Override
    public String interpolate(String template, Object args, Locale locale) {
        CacheKey key = new CacheKey(template, args, locale);
        synchronized (cache) {
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
        }
        String result = delegate.interpolate(template, args, locale);
        synchronized (cache) {
            cache.put(key, result);
        }
        return result;
    }

    /**
     * 缓存键，包含模板、参数和 locale
     */
    private static class CacheKey {
        private final String template;
        private final Object args;
        private final Locale locale;
        private final int hash;

        CacheKey(String template, Object args, Locale locale) {
            this.template = template;
            this.args = args;
            this.locale = locale;
            this.hash = Objects.hash(template, deepArgsHash(args), locale);
        }

        private static int deepArgsHash(Object args) {
            if (args == null) return 0;
            if (args instanceof Object[]) return Arrays.deepHashCode((Object[]) args);
            return args.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(template, cacheKey.template)
                    && Objects.equals(locale, cacheKey.locale)
                    && deepEqualsArgs(args, cacheKey.args);
        }

        private static boolean deepEqualsArgs(Object a, Object b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            if (a instanceof Object[] && b instanceof Object[]) {
                return Arrays.deepEquals((Object[]) a, (Object[]) b);
            }
            return a.equals(b);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
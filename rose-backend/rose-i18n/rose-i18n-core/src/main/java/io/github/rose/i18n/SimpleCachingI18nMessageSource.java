package io.github.rose.i18n;

import jakarta.annotation.Nonnull;
import lombok.ToString;

import java.util.*;

/**
 * 装饰器：为 I18nMessageSource 添加简单缓存能力
 */
public class SimpleCachingI18nMessageSource implements I18nMessageSource {
    public static final int MAX_SIZE = 512;
    private final I18nMessageSource delegate;
    private final Map<CacheKey, String> cache;
    private final int maxSize;

    public SimpleCachingI18nMessageSource(I18nMessageSource delegate) {
        this(delegate, MAX_SIZE);
    }

    public SimpleCachingI18nMessageSource(I18nMessageSource delegate, int maxSize) {
        this.delegate = delegate;
        this.maxSize = maxSize > 0 ? maxSize : MAX_SIZE;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, String> eldest) {
                return size() > SimpleCachingI18nMessageSource.this.maxSize;
            }
        });
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        CacheKey key = new CacheKey(code, locale, args);
        String cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        String value = delegate.getMessage(code, locale, args);
        if (value != null) {
            cache.put(key, value);
        }
        return value;
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        cache.clear();
    }

    @ToString
    private static class CacheKey {
        private final String code;
        private final Object[] args;
        private final Locale locale;

        CacheKey(String code, Locale locale, Object... args) {
            this.code = code;
            this.args = args != null ? Arrays.copyOf(args, args.length) : null;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey that = (CacheKey) o;
            return Objects.equals(code, that.code)
                    && Arrays.deepEquals(args, that.args)
                    && Objects.equals(locale, that.locale);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(code, locale);
            result = 31 * result + Arrays.deepHashCode(args);
            return result;
        }
    }
}

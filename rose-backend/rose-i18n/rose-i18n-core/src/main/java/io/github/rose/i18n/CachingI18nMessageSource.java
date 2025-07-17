package io.github.rose.i18n;

import jakarta.annotation.Nonnull;

import java.util.*;

/**
 * 装饰器：为 I18nMessageSource 添加简单缓存能力
 */
public class CachingI18nMessageSource implements I18nMessageSource {
    public static final int MAX_SIZE = 512;
    private final I18nMessageSource delegate;
    private final Map<CacheKey, String> cache;
    private final int maxSize;

    public CachingI18nMessageSource(I18nMessageSource delegate) {
        this(delegate, MAX_SIZE);
    }

    public CachingI18nMessageSource(I18nMessageSource delegate, int maxSize) {
        this.delegate = delegate;
        this.maxSize = maxSize > 0 ? maxSize : MAX_SIZE;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, String> eldest) {
                return size() > CachingI18nMessageSource.this.maxSize;
            }
        });
    }

    @Override
    public String getMessage(String code, Locale locale, String defaultMessage, Object... args) {
        CacheKey key = new CacheKey(code, defaultMessage, locale, args);
        String cached = cache.get(key);
        if (cached != null) return cached;
        String value = delegate.getMessage(code, args, defaultMessage, locale);
        cache.put(key, value);
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

    private static class CacheKey {
        private final String code;
        private final Object[] args;
        private final String defaultMessage;
        private final Locale locale;

        CacheKey(String code, String defaultMessage, Locale locale, Object... args) {
            this.code = code;
            this.args = args != null ? Arrays.copyOf(args, args.length) : null;
            this.defaultMessage = defaultMessage;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey that = (CacheKey) o;
            return Objects.equals(code, that.code)
                    && Arrays.deepEquals(args, that.args)
                    && Objects.equals(defaultMessage, that.defaultMessage)
                    && Objects.equals(locale, that.locale);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(code, defaultMessage, locale);
            result = 31 * result + Arrays.deepHashCode(args);
            return result;
        }
    }
}

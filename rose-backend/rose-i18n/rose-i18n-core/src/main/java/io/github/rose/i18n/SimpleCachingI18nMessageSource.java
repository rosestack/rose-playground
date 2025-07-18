package io.github.rose.i18n;

import io.github.rose.i18n.util.CacheKey;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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

    @Nullable
    @Override
    public Map<String, String> getMessages(Locale locale) {
        return delegate.getMessages(locale);
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public void init() {
        delegate.init();
    }

    @Override
    public void destroy() {
        cache.clear();
        delegate.destroy();
    }
}

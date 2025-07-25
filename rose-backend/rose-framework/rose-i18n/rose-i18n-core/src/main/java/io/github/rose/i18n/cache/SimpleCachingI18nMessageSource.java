package io.github.rose.i18n.cache;

import io.github.rose.i18n.I18nMessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Decorator: Adds simple caching capability to I18nMessageSource
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
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize cannot be negative: " + maxSize);
        }
        this.maxSize = maxSize > 0 ? maxSize : MAX_SIZE;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(this.maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, String> eldest) {
                return size() > SimpleCachingI18nMessageSource.this.maxSize;
            }
        });
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        if (code == null) {
            return null;
        }
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

    @NonNull
    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public void init() {
        if (delegate != null) {
            delegate.init();
        }
    }

    @Override
    public void destroy() {
        cache.clear();
        if (delegate != null) {
            delegate.destroy();
        }
    }
}

package io.github.rose.i18n;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rose.i18n.util.CacheKey;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MultiCachingMessageSource implements I18nMessageSource {
    // L1缓存：本地高速缓存
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats()
            .build();

    private final I18nMessageSource delegate;
    // L2缓存：分布式缓存
    private final RedisTemplate<String, String> redisTemplate;

    public MultiCachingMessageSource(I18nMessageSource delegate, RedisTemplate<String, String> redisTemplate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate cannot be null");
    }

    @Override
    public void init() {
        delegate.init();
        preloadCache();
    }

    @Override
    public void destroy() {
        localCache.cleanUp();
        delegate.destroy();
    }

    // 缓存预热
    public void preloadCache() {
        CompletableFuture.runAsync(() -> {
            try {
                Set<Locale> supportedLocales = getSupportedLocales();
                if (supportedLocales == null || supportedLocales.isEmpty()) {
                    return;
                }

                for (Locale locale : supportedLocales) {
                    Map<String, String> messages = getMessages(locale);
                    if (messages != null && !messages.isEmpty()) {
                        for (String key : messages.keySet()) {
                            try {
                                getMessage(key, locale);
                            } catch (Exception e) {
                                // 忽略预加载失败的消息
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略预加载过程中的异常
            }
        });
    }

    @Nullable
    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        if (code == null) {
            return null;
        }

        // 先从L1缓存获取
        CacheKey cacheKey = new CacheKey(code, locale, args);
        String message = localCache.getIfPresent(cacheKey.toString());
        if (message != null) {
            return message;
        }

        // 从L2缓存获取
        try {
            message = redisTemplate.opsForValue().get(cacheKey);
            if (message != null) {
                // 回填L1缓存
                localCache.put(cacheKey.toString(), message);
                return message;
            }
        } catch (Exception e) {
            // Redis异常时继续从delegate获取
        }

        // 从delegate获取
        message = delegate.getMessage(code, locale, args);
        if (message != null) {
            // 同时缓存到L1和L2
            localCache.put(cacheKey.toString(), message);
            try {
                redisTemplate.opsForValue().set(cacheKey.toString(), message);
            } catch (Exception e) {
                // Redis异常时忽略
            }
        }

        return message;
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
}

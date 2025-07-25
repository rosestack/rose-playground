package io.github.rosestack.i18n.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高级缓存消息源实现
 * 提供两级缓存：本地缓存（Caffeine）+ 分布式缓存（Redis）
 *
 * @author rose
 * @since 1.0.0
 */
@Slf4j
public class RedisCachingMessageSource implements I18nMessageSource {

    private static final String CACHE_PREFIX = "i18n:message:";
    private static final Duration DEFAULT_REDIS_TTL = Duration.ofHours(24);
    private static final Duration DEFAULT_LOCAL_TTL = Duration.ofMinutes(30);

    private final I18nMessageSource delegate;
    private final Cache<CacheKey, String> localCache;
    private final RedisTemplate<String, String> redisTemplate;
    private final CacheConfig config;
    private final MessageSourceStats stats;
    private final Locale defaultLocale;
    private final String source;

    private volatile boolean initialized = false;
    private volatile boolean destroyed = false;

    /**
     * 构造函数
     *
     * @param delegate      委托的消息源
     * @param redisTemplate Redis模板
     * @param config        缓存配置
     */
    public RedisCachingMessageSource(I18nMessageSource delegate,
                                     RedisTemplate<String, String> redisTemplate,
                                     CacheConfig config) {
        this(delegate, redisTemplate, config, delegate.getDefaultLocale(), delegate.getSource());
    }

    /**
     * 构造函数
     *
     * @param delegate      委托的消息源
     * @param redisTemplate Redis模板
     * @param config        缓存配置
     * @param defaultLocale 默认locale
     * @param source        消息源名称
     */
    public RedisCachingMessageSource(I18nMessageSource delegate,
                                     RedisTemplate<String, String> redisTemplate,
                                     CacheConfig config,
                                     Locale defaultLocale,
                                     String source) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
        this.config = config;
        this.defaultLocale = defaultLocale;
        this.source = source;
        this.stats = new MessageSourceStats();

        this.localCache = buildLocalCache();

        log.info("RedisCachingMessageSource initialized with config: {}", config);
    }

    /**
     * 构建本地缓存
     */
    private Cache<CacheKey, String> buildLocalCache() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(config.getMaxSize())
                .expireAfterWrite(config.getExpireAfterWrite() != null ?
                        config.getExpireAfterWrite() : DEFAULT_LOCAL_TTL);

        if (config.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(config.getExpireAfterAccess());
        }

        if (config.isEnableStatistics()) {
            builder.recordStats();
        }

        // 添加缓存监听器
        builder.removalListener((key, value, cause) -> {
            if (log.isDebugEnabled()) {
                log.debug("Local cache entry removed: key={}, cause={}", key, cause);
            }
        });

        return builder.build();
    }

    @Override
    public void init() {
        if (initialized) {
            log.warn("RedisCachingMessageSource already initialized");
            return;
        }

        try {
            // 初始化委托消息源
            if (delegate instanceof Lifecycle) {
                delegate.init();
            }

            // 执行缓存预热（如果配置了）
            if (config.isEnablePreload()) {
                preloadCache();
            }

            initialized = true;
            log.info("RedisCachingMessageSource initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize RedisCachingMessageSource", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    @Override
    public void destroy() {
        if (destroyed) {
            log.warn("RedisCachingMessageSource already destroyed");
            return;
        }

        try {
            // 清空本地缓存
            localCache.invalidateAll();

            // 销毁委托消息源
            if (delegate instanceof Lifecycle) {
                delegate.destroy();
            }

            destroyed = true;
            log.info("RedisCachingMessageSource destroyed successfully");
        } catch (Exception e) {
            log.error("Failed to destroy RedisCachingMessageSource", e);
        }
    }

    @Override
    @Nullable
    public String getMessage(String code, Locale locale, Object... args) {
        if (!StringUtils.hasText(code) || locale == null) {
            log.warn("Invalid parameters: code={}, locale={}", code, locale);
            return null;
        }

        if (!initialized) {
            log.warn("RedisCachingMessageSource not initialized, falling back to delegate");
            return delegate.getMessage(code, locale, args);
        }

        stats.incrementTotalRequests();
        CacheKey cacheKey = new CacheKey(code, locale, args);

        try {
            // L1缓存查找
            String message = getFromLocalCache(cacheKey);
            if (message != null) {
                return message;
            }

            // L2缓存查找
            message = getFromRedisCache(cacheKey);
            if (message != null) {
                // 回填到本地缓存
                localCache.put(cacheKey, message);
                return message;
            }

            // 从委托源获取
            message = getFromDelegate(cacheKey);
            if (message != null) {
                // 缓存到L1和L2
                cacheMessage(cacheKey, message);
            }

            return message;

        } catch (Exception e) {
            log.error("Error getting message for code: {}, locale: {}", code, locale, e);
            // 降级到委托源
            return delegate.getMessage(code, locale, args);
        }
    }

    @Override
    @Nullable
    public Map<String, String> getMessages(Locale locale) {
        if (locale == null) {
            log.warn("Invalid locale parameter: {}", locale);
            return null;
        }

        if (!initialized) {
            log.warn("RedisCachingMessageSource not initialized, falling back to delegate");
            return delegate.getMessages(locale);
        }

        try {
            // 尝试从缓存获取
            Map<String, String> cachedMessages = getMessagesFromCache(locale);
            if (cachedMessages != null && !cachedMessages.isEmpty()) {
                return cachedMessages;
            }

            // 从委托源获取
            Map<String, String> messages = delegate.getMessages(locale);
            if (messages != null && !messages.isEmpty()) {
                // 缓存消息
                cacheMessages(locale, messages);
            }

            return messages;
        } catch (Exception e) {
            log.error("Error getting messages for locale: {}", locale, e);
            return delegate.getMessages(locale);
        }
    }

    @Override
    @NonNull
    public Locale getLocale() {
        return defaultLocale;
    }

    @Override
    @NonNull
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        try {
            return delegate.getSupportedLocales();
        } catch (Exception e) {
            log.warn("Failed to get supported locales from delegate", e);
            return Set.of(getDefaultLocale(), Locale.ENGLISH);
        }
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public MessageSourceStats getStats() {
        // 合并Caffeine缓存统计
        if (config.isEnableStatistics()) {
            CacheStats caffeineStats = localCache.stats();
            stats.merge(new MessageSourceStats(
                    (int) caffeineStats.requestCount(),
                    getSupportedLocales()
            ));
        }

        return stats;
    }

    /**
     * 从本地缓存获取消息
     */
    private String getFromLocalCache(CacheKey cacheKey) {
        String message = localCache.getIfPresent(cacheKey);
        if (message != null) {
            stats.incrementCacheHits();
            if (log.isDebugEnabled()) {
                log.debug("Cache hit (L1): {}", cacheKey);
            }
        }
        return message;
    }

    /**
     * 从Redis缓存获取消息
     */
    private String getFromRedisCache(CacheKey cacheKey) {
        try {
            String redisKey = buildRedisKey(cacheKey);
            String message = redisTemplate.opsForValue().get(redisKey);

            if (message != null) {
                stats.incrementCacheHits();
                if (log.isDebugEnabled()) {
                    log.debug("Cache hit (L2): {}", cacheKey);
                }
            }

            return message;
        } catch (Exception e) {
            log.warn("Redis cache access failed for key: {}", cacheKey, e);
            stats.incrementCacheMisses();
            return null;
        }
    }

    /**
     * 从委托源获取消息
     */
    private String getFromDelegate(CacheKey cacheKey) {
        long startTime = System.currentTimeMillis();
        try {
            String message = delegate.getMessage(cacheKey.getCode(), cacheKey.getLocale(), cacheKey.getArgs());
            stats.addLoadTime(System.currentTimeMillis() - startTime);

            if (log.isDebugEnabled()) {
                log.debug("Loaded from delegate: {} ({}ms)", cacheKey, System.currentTimeMillis() - startTime);
            }

            return message;
        } catch (Exception e) {
            log.error("Failed to load message from delegate: {}", cacheKey, e);
            stats.addLoadTime(System.currentTimeMillis() - startTime);
            return null;
        }
    }

    /**
     * 从缓存获取消息集合
     */
    private Map<String, String> getMessagesFromCache(Locale locale) {
        try {
            String pattern = CACHE_PREFIX + "*:" + locale.toString() + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return null;
            }

            Map<String, String> messages = new HashMap<>();
            for (String key : keys) {
                String message = redisTemplate.opsForValue().get(key);
                if (message != null) {
                    // 从key中提取code
                    String code = extractCodeFromKey(key);
                    if (code != null) {
                        messages.put(code, message);
                    }
                }
            }

            return messages;
        } catch (Exception e) {
            log.warn("Failed to get messages from cache for locale: {}", locale, e);
            return null;
        }
    }

    /**
     * 缓存消息集合
     */
    private void cacheMessages(Locale locale, Map<String, String> messages) {
        CompletableFuture.runAsync(() -> {
            try {
                Duration ttl = config.getExpireAfterWrite() != null ?
                        config.getExpireAfterWrite() : DEFAULT_REDIS_TTL;

                for (Map.Entry<String, String> entry : messages.entrySet()) {
                    CacheKey cacheKey = new CacheKey(entry.getKey(), locale);
                    String redisKey = buildRedisKey(cacheKey);
                    redisTemplate.opsForValue().set(redisKey, entry.getValue(), ttl);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Cached {} messages for locale: {}", messages.size(), locale);
                }
            } catch (Exception e) {
                log.warn("Failed to cache messages for locale: {}", locale, e);
            }
        });
    }

    /**
     * 缓存消息到L1和L2
     */
    private void cacheMessage(CacheKey cacheKey, String message) {
        // 缓存到本地
        localCache.put(cacheKey, message);

        // 异步缓存到Redis
        CompletableFuture.runAsync(() -> {
            try {
                String redisKey = buildRedisKey(cacheKey);
                Duration ttl = config.getExpireAfterWrite() != null ?
                        config.getExpireAfterWrite() : DEFAULT_REDIS_TTL;

                redisTemplate.opsForValue().set(redisKey, message, ttl);

                if (log.isDebugEnabled()) {
                    log.debug("Cached to Redis: {}", cacheKey);
                }
            } catch (Exception e) {
                log.warn("Failed to cache message to Redis: {}", cacheKey, e);
            }
        });
    }

    /**
     * 构建Redis键
     */
    private String buildRedisKey(CacheKey cacheKey) {
        return CACHE_PREFIX + cacheKey.toString();
    }

    /**
     * 从Redis键中提取消息代码
     */
    private String extractCodeFromKey(String redisKey) {
        try {
            // 假设key格式为: i18n:message:code:locale:args
            String[] parts = redisKey.split(":");
            if (parts.length >= 3) {
                return parts[2];
            }
        } catch (Exception e) {
            log.warn("Failed to extract code from key: {}", redisKey, e);
        }
        return null;
    }

    /**
     * 缓存预热
     */
    @Async
    public CompletableFuture<Void> preloadCache() {
        log.info("Starting cache preload...");

        try {
            Set<Locale> supportedLocales = getSupportedLocales();
            AtomicInteger totalMessages = new AtomicInteger();

            for (Locale locale : supportedLocales) {
                Map<String, String> messages = delegate.getMessages(locale);
                if (messages != null && !messages.isEmpty()) {
                    messages.forEach((code, message) -> {
                        try {
                            getMessage(code, locale);
                            totalMessages.getAndIncrement();
                        } catch (Exception e) {
                            log.warn("Failed to preload message: code={}, locale={}", code, locale, e);
                        }
                    });
                }
            }

            log.info("Cache preload completed. Total messages: {}", totalMessages.get());

        } catch (Exception e) {
            log.error("Cache preload failed", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 缓存失效
     */
    public void evictCache(String code, Locale locale) {
        if (!StringUtils.hasText(code) || locale == null) {
            log.warn("Invalid parameters for cache eviction: code={}, locale={}", code, locale);
            return;
        }

        CacheKey cacheKey = new CacheKey(code, locale);

        // 失效本地缓存
        localCache.invalidate(cacheKey);

        // 失效Redis缓存
        try {
            String redisKey = buildRedisKey(cacheKey);
            redisTemplate.delete(redisKey);

            if (log.isDebugEnabled()) {
                log.debug("Cache evicted: {}", cacheKey);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache from Redis: {}", cacheKey, e);
        }
    }

    /**
     * 批量缓存失效
     */
    public void evictCacheByLocale(Locale locale) {
        if (locale == null) {
            log.warn("Invalid locale for cache eviction: {}", locale);
            return;
        }

        try {
            // 清除本地缓存中指定locale的所有条目
            localCache.asMap().keySet().removeIf(key -> locale.equals(key.getLocale()));

            // 清除Redis缓存中指定locale的所有条目
            String pattern = CACHE_PREFIX + "*:" + locale.toString() + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            log.info("Cache evicted for locale: {}", locale);
        } catch (Exception e) {
            log.error("Failed to evict cache for locale: {}", locale, e);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        try {
            // 清空本地缓存
            localCache.invalidateAll();

            // 清空Redis缓存
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            log.info("All cache cleared");
        } catch (Exception e) {
            log.error("Failed to clear all cache", e);
        }
    }

    /**
     * 获取本地缓存统计
     */
    public CacheStats getLocalCacheStats() {
        return localCache.stats();
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            // 检查Redis连接
            redisTemplate.opsForValue().get("health_check");
            return initialized && !destroyed;
        } catch (Exception e) {
            log.warn("Health check failed", e);
            return false;
        }
    }

    /**
     * 获取缓存大小
     */
    public long getLocalCacheSize() {
        return localCache.estimatedSize();
    }

    /**
     * 获取Redis缓存大小
     */
    public long getRedisCacheSize() {
        try {
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.warn("Failed to get Redis cache size", e);
            return 0;
        }
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 检查是否已销毁
     */
    public boolean isDestroyed() {
        return destroyed;
    }
}
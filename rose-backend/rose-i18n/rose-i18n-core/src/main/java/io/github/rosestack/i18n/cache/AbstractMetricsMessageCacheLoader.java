package io.github.rosestack.i18n.cache;

import io.github.rosestack.i18n.MessageCacheLoader;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带监控指标的消息缓存加载器抽象基类
 *
 * <p>基于 Micrometer 提供标准化的缓存监控指标，包括命中率、响应时间、缓存大小等关键指标。
 *
 * <p>
 *
 * <h3>监控指标：</h3>
 *
 * <ul>
 *   <li>i18n.cache.hits - 缓存命中次数
 *   <li>i18n.cache.misses - 缓存未命中次数
 *   <li>i18n.cache.puts - 缓存写入次数
 *   <li>i18n.cache.evictions - 缓存淘汰次数
 *   <li>i18n.cache.size - 当前缓存大小
 *   <li>i18n.cache.get.time - 缓存获取耗时
 *   <li>i18n.cache.put.time - 缓存写入耗时
 * </ul>
 *
 * @author chensoul
 * @since 1.0.0
 */
public abstract class AbstractMetricsMessageCacheLoader implements MessageCacheLoader {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMetricsMessageCacheLoader.class);

    protected final MeterRegistry meterRegistry;
    protected final CacheProperties cacheProperties;

    // 计数器指标
    protected final Counter hitCounter;
    protected final Counter missCounter;
    protected final Counter putCounter;
    protected final Counter evictionCounter;

    // 计时器指标
    protected final Timer getTimer;
    protected final Timer putTimer;

    // 缓存大小指标（原子计数器）
    protected final AtomicLong cacheSize = new AtomicLong(0);

    protected AbstractMetricsMessageCacheLoader(CacheProperties cacheProperties, MeterRegistry meterRegistry) {
        this.cacheProperties = cacheProperties;
        this.meterRegistry = meterRegistry;

        String cacheType = cacheProperties.getType().name();

        // 初始化计数器指标
        this.hitCounter = Counter.builder("i18n.cache.hits")
                .description("缓存命中次数")
                .tag("type", cacheType)
                .register(meterRegistry);

        this.missCounter = Counter.builder("i18n.cache.misses")
                .description("缓存未命中次数")
                .tag("type", cacheType)
                .register(meterRegistry);

        this.putCounter = Counter.builder("i18n.cache.puts")
                .description("缓存写入次数")
                .tag("type", cacheType)
                .register(meterRegistry);

        this.evictionCounter = Counter.builder("i18n.cache.evictions")
                .description("缓存淘汰次数")
                .tag("type", cacheType)
                .register(meterRegistry);

        // 初始化计时器指标
        this.getTimer = Timer.builder("i18n.cache.get.time")
                .description("缓存获取耗时")
                .tag("type", cacheType)
                .register(meterRegistry);

        this.putTimer = Timer.builder("i18n.cache.put.time")
                .description("缓存写入耗时")
                .tag("type", cacheType)
                .register(meterRegistry);

        // 注册缓存大小指标
        Gauge.builder("i18n.cache.size", this, loader -> loader.cacheSize.get())
                .description("当前缓存大小")
                .tag("type", cacheType)
                .register(meterRegistry);

        logger.debug("初始化 {} 类型缓存监控指标", cacheType);
    }

    @Override
    public final String getFromCache(String code, Locale locale) {
        try {
            return getTimer.recordCallable(() -> {
                String cacheKey = buildCacheKey(code, locale, getCacheKeyPrefix());
                String result = doGetFromCache(cacheKey);
                if (result != null) {
                    hitCounter.increment();
                } else {
                    missCounter.increment();
                }
                return result;
            });
        } catch (Exception e) {
            missCounter.increment();
            return null;
        }
    }

    @Override
    public final Map<String, String> getFromCache(String[] codes, Locale locale) {
        try {
            return getTimer.recordCallable(() -> {
                String[] cacheKeys = buildCacheKeys(codes, locale, getCacheKeyPrefix());
                Map<String, String> result = doGetFromCache(cacheKeys, codes);
                if (result != null && !result.isEmpty()) {
                    hitCounter.increment(result.size());
                    missCounter.increment(codes.length - result.size());
                } else {
                    missCounter.increment(codes.length);
                    return new HashMap<>();
                }
                return result;
            });
        } catch (Exception e) {
            missCounter.increment(codes.length);
            return new HashMap<>();
        }
    }

    @Override
    public final void putToCache(String code, Locale locale, String message) {
        try {
            putTimer.recordCallable(() -> {
                String cacheKey = buildCacheKey(code, locale, getCacheKeyPrefix());
                doPutToCache(cacheKey, message);
                putCounter.increment();
                return null;
            });
        } catch (Exception e) {
            // 忽略异常，记录失败
        }
    }

    @Override
    public final void putToCache(Map<String, String> messages, Locale locale) {
        try {
            putTimer.recordCallable(() -> {
                String prefix = getCacheKeyPrefix();
                Map<String, String> cacheKeyToMessage = new HashMap<>();
                for (Map.Entry<String, String> entry : messages.entrySet()) {
                    String cacheKey = buildCacheKey(entry.getKey(), locale, prefix);
                    cacheKeyToMessage.put(cacheKey, entry.getValue());
                }
                doPutToCache(cacheKeyToMessage);
                putCounter.increment(messages.size());
                return null;
            });
        } catch (Exception e) {
            // 忽略异常，记录失败
        }
    }

    @Override
    public final void evictCache(Locale locale) {
        String keyPattern = buildCacheKeyPattern(locale, getCacheKeyPrefix());
        long evictedCount = doEvictCache(keyPattern);
        if (evictedCount > 0) {
            evictionCounter.increment(evictedCount);
            cacheSize.addAndGet(-evictedCount);
        }
    }

    @Override
    public final void clearCache() {
        String keyPattern = buildCacheKeyPattern(null, getCacheKeyPrefix());
        long clearedCount = doClearCache(keyPattern);
        if (clearedCount > 0) {
            evictionCounter.increment(clearedCount);
            cacheSize.set(0);
        }
    }

    /**
     * 获取缓存键前缀
     *
     * @return 缓存键前缀，如果不需要前缀则返回 null
     */
    protected String getCacheKeyPrefix() {
        return cacheProperties.getKeyPrefix();
    }

    /**
     * 实际的缓存获取实现
     *
     * @param cacheKey 已组装的缓存键
     * @return 缓存的消息，如果不存在则返回 null
     */
    protected abstract String doGetFromCache(String cacheKey);

    /**
     * 实际的批量缓存获取实现
     *
     * @param cacheKeys 已组装的缓存键列表
     * @param codes     原始消息代码列表（用于构建结果映射）
     * @return 缓存的消息映射
     */
    protected abstract Map<String, String> doGetFromCache(String[] cacheKeys, String[] codes);

    /**
     * 实际的缓存写入实现
     *
     * @param cacheKey 已组装的缓存键
     * @param message  消息内容
     */
    protected abstract void doPutToCache(String cacheKey, String message);

    /**
     * 实际的批量缓存写入实现
     *
     * @param cacheKeyToMessage 缓存键到消息的映射
     */
    protected abstract void doPutToCache(Map<String, String> cacheKeyToMessage);

    /**
     * 实际的缓存清除实现
     *
     * @param keyPattern 缓存键模式（用于模式匹配删除）
     * @return 清除的缓存条目数量
     */
    protected abstract long doEvictCache(String keyPattern);

    /**
     * 实际的缓存全部清除实现
     *
     * @param keyPattern 缓存键模式（用于模式匹配删除）
     * @return 清除的缓存条目数量
     */
    protected abstract long doClearCache(String keyPattern);

    /**
     * 构建缓存键模式（用于删除操作）
     *
     * @param locale 语言环境，如果为 null 则匹配所有语言
     * @param prefix 可选的键前缀
     * @return 缓存键模式
     */
    protected final String buildCacheKeyPattern(Locale locale, String prefix) {
        String pattern;
        if (locale != null) {
            pattern = locale.toString() + ":*";
        } else {
            pattern = "*";
        }
        return prefix != null ? prefix + pattern : pattern;
    }

    /**
     * 更新缓存大小计数器
     *
     * @param delta 变化量（正数表示增加，负数表示减少）
     */
    protected final void updateCacheSize(long delta) {
        cacheSize.addAndGet(delta);
    }

    /**
     * 设置缓存大小计数器
     *
     * @param size 当前缓存大小
     */
    protected final void setCacheSize(long size) {
        cacheSize.set(size);
    }

    /**
     * 构建缓存键
     *
     * @param code   消息代码
     * @param locale 语言环境
     * @return 缓存键
     */
    protected final String buildCacheKey(String code, Locale locale) {
        return buildCacheKey(code, locale, null);
    }

    /**
     * 构建缓存键（支持前缀）
     *
     * @param code   消息代码
     * @param locale 语言环境
     * @param prefix 可选的键前缀
     * @return 缓存键
     */
    protected final String buildCacheKey(String code, Locale locale, String prefix) {
        String baseKey = String.format("%s:%s", locale.toString(), code);
        return prefix != null ? prefix + baseKey : baseKey;
    }

    /**
     * 批量构建缓存键
     *
     * @param codes  消息代码列表
     * @param locale 语言环境
     * @return 缓存键列表
     */
    protected final String[] buildCacheKeys(String[] codes, Locale locale) {
        return buildCacheKeys(codes, locale, null);
    }

    /**
     * 批量构建缓存键（支持前缀）
     *
     * @param codes  消息代码列表
     * @param locale 语言环境
     * @param prefix 可选的键前缀
     * @return 缓存键列表
     */
    protected final String[] buildCacheKeys(String[] codes, Locale locale, String prefix) {
        String[] keys = new String[codes.length];
        String localeStr = locale.toString();
        for (int i = 0; i < codes.length; i++) {
            String baseKey = String.format("%s:%s", localeStr, codes[i]);
            keys[i] = prefix != null ? prefix + baseKey : baseKey;
        }
        return keys;
    }

    /**
     * 获取当前缓存命中率
     *
     * @return 命中率（0.0 - 1.0）
     */
    @Override
    public final double getHitRate() {
        double hits = hitCounter.count();
        double total = hits + missCounter.count();
        return total > 0 ? hits / total : 0.0;
    }

    /**
     * 获取缓存统计信息摘要
     *
     * @return 统计信息字符串
     */
    @Override
    public final String getStatisticsSummary() {
        return String.format(
                "Cache[%s] - Hits: %.0f, Misses: %.0f, Puts: %.0f, Evictions: %.0f, Size: %d, HitRate: %.2f%%",
                cacheProperties.getType(),
                hitCounter.count(),
                missCounter.count(),
                putCounter.count(),
                evictionCounter.count(),
                cacheSize.get(),
                getHitRate() * 100);
    }
}

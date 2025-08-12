package io.github.rosestack.i18n.spring.boot.cache;

import io.github.rosestack.i18n.cache.AbstractMetricsMessageCacheLoader;
import io.github.rosestack.i18n.cache.CacheProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于 Redis 的消息缓存加载器实现
 * <p>
 * 使用 RedisTemplate 提供高性能的分布式缓存功能，支持批量操作、过期时间设置、模式匹配删除和 Micrometer 监控。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>支持单条和批量消息的缓存操作</li>
 *   <li>支持按语言环境清除缓存</li>
 *   <li>支持全局缓存清除</li>
 *   <li>支持缓存过期时间设置</li>
 *   <li>基于 Micrometer 的监控指标</li>
 *   <li>提供详细的错误处理和日志记录</li>
 * </ul>
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
public class RedisMessageCacheLoader extends AbstractMetricsMessageCacheLoader {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageCacheLoader.class);

    /**
     * Redis 操作模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Lua 脚本：批量删除匹配模式的键
     */
    private static final String DELETE_PATTERN_SCRIPT =
            "local keys = redis.call('keys', ARGV[1]) " +
                    "if #keys > 0 then " +
                    "  return redis.call('del', unpack(keys)) " +
                    "else " +
                    "  return 0 " +
                    "end";

    private final DefaultRedisScript<Long> deletePatternScript;

    /**
     * 构造函数（使用默认的 SimpleMeterRegistry）
     *
     * @param cacheProperties 缓存配置属性
     * @param redisTemplate   Redis 操作模板
     */
    public RedisMessageCacheLoader(CacheProperties cacheProperties, RedisTemplate<String, Object> redisTemplate) {
        this(cacheProperties, redisTemplate, new SimpleMeterRegistry());
    }

    /**
     * 构造函数
     *
     * @param cacheProperties 缓存配置属性
     * @param redisTemplate   Redis 操作模板
     * @param meterRegistry   Micrometer 指标注册表
     */
    public RedisMessageCacheLoader(CacheProperties cacheProperties, RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        super(cacheProperties, meterRegistry);
        this.redisTemplate = redisTemplate;

        // 初始化 Lua 脚本
        this.deletePatternScript = new DefaultRedisScript<>();
        this.deletePatternScript.setScriptText(DELETE_PATTERN_SCRIPT);
        this.deletePatternScript.setResultType(Long.class);
    }

    @Override
    protected String doGetFromCache(String cacheKey) {
        if (!cacheEnabled() || !StringUtils.hasText(cacheKey)) {
            return null;
        }

        try {
            Object value = redisTemplate.opsForValue().get(cacheKey);

            if (value != null) {
                logger.debug("从 Redis 缓存中获取消息: key={}, value={}", cacheKey, value);
                return value.toString();
            }

            logger.debug("Redis 缓存中未找到消息: key={}", cacheKey);
            return null;

        } catch (Exception e) {
            logger.error("从 Redis 缓存获取消息失败: key={}", cacheKey, e);
            return null;
        }
    }

    @Override
    protected Map<String, String> doGetFromCache(String[] cacheKeys, String[] codes) {
        if (!cacheEnabled() || cacheKeys == null || cacheKeys.length == 0 || codes == null) {
            return Collections.emptyMap();
        }

        try {
            // 过滤有效的缓存键
            List<String> validKeys = Arrays.stream(cacheKeys)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            if (validKeys.isEmpty()) {
                return Collections.emptyMap();
            }

            // 批量获取缓存值
            List<Object> values = redisTemplate.opsForValue().multiGet(validKeys);
            Map<String, String> result = new HashMap<>();

            // 组装结果，使用原始代码作为键
            for (int i = 0; i < validKeys.size() && i < values.size() && i < codes.length; i++) {
                Object value = values.get(i);
                if (value != null) {
                    result.put(codes[i], value.toString());
                }
            }

            logger.debug("从 Redis 缓存批量获取消息: 请求数量={}, 命中数量={}",
                    cacheKeys.length, result.size());

            return result;

        } catch (Exception e) {
            logger.error("从 Redis 缓存批量获取消息失败: keys={}", Arrays.toString(cacheKeys), e);
            return Collections.emptyMap();
        }
    }

    @Override
    protected void doPutToCache(String cacheKey, String message) {
        if (!cacheEnabled() || !StringUtils.hasText(cacheKey) || message == null) {
            return;
        }

        try {
            Duration expireTime = cacheProperties.getExpireAfterWrite();

            if (expireTime != null && !expireTime.isZero()) {
                redisTemplate.opsForValue().set(cacheKey, message, expireTime.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(cacheKey, message);
            }

            // 更新缓存大小计数器（Redis 缓存大小难以精确统计，这里简单递增）
            updateCacheSize(1);

            logger.debug("消息已存入 Redis 缓存: key={}, message={}, expireTime={}", cacheKey, message, expireTime);

        } catch (Exception e) {
            logger.error("存储消息到 Redis 缓存失败: key={}, message={}", cacheKey, message, e);
        }
    }

    @Override
    protected void doPutToCache(Map<String, String> cacheMap) {
        if (!cacheEnabled() || cacheMap == null || cacheMap.isEmpty()) {
            return;
        }

        try {
            Duration expireTime = cacheProperties.getExpireAfterWrite();

            // 批量设置缓存
            redisTemplate.opsForValue().multiSet(cacheMap);

            // 如果设置了过期时间，需要逐个设置过期时间（Redis 的 mset 不支持过期时间）
            if (expireTime != null && !expireTime.isZero()) {
                for (String key : cacheMap.keySet()) {
                    redisTemplate.expire(key, expireTime.toMillis(), TimeUnit.MILLISECONDS);
                }
            }

            // 更新缓存大小计数器
            updateCacheSize(cacheMap.size());

            logger.debug("批量消息已存入 Redis 缓存: 数量={}, expireTime={}",
                    cacheMap.size(), expireTime);

        } catch (Exception e) {
            logger.error("批量存储消息到 Redis 缓存失败: cacheMap={}", cacheMap, e);
        }
    }

    @Override
    protected long doEvictCache(String cacheKeyPattern) {
        if (!cacheEnabled() || !StringUtils.hasText(cacheKeyPattern)) {
            return 0;
        }

        try {
            Long deletedCount = redisTemplate.execute(deletePatternScript, Collections.emptyList(), cacheKeyPattern);

            logger.debug("清除 Redis 缓存: pattern={}, 删除数量={}", cacheKeyPattern, deletedCount);
            return deletedCount != null ? deletedCount : 0;

        } catch (Exception e) {
            logger.error("清除 Redis 缓存失败: pattern={}", cacheKeyPattern, e);
            return 0;
        }
    }

    @Override
    protected long doClearCache(String cacheKeyPattern) {
        if (!cacheEnabled() || !StringUtils.hasText(cacheKeyPattern)) {
            return 0;
        }

        try {
            Long deletedCount = redisTemplate.execute(deletePatternScript, Collections.emptyList(), cacheKeyPattern);

            logger.debug("清除所有 Redis 缓存: pattern={}, 删除数量={}", cacheKeyPattern, deletedCount);
            return deletedCount != null ? deletedCount : 0;

        } catch (Exception e) {
            logger.error("清除所有 Redis 缓存失败: pattern={}", cacheKeyPattern, e);
            return 0;
        }
    }

    /**
     * 检查缓存是否启用
     *
     * @return 如果缓存启用且类型为 REDIS 则返回 true
     */
    private boolean cacheEnabled() {
        return cacheProperties != null
                && cacheProperties.isEnabled()
                && CacheProperties.CacheType.REDIS.equals(cacheProperties.getType());
    }
}
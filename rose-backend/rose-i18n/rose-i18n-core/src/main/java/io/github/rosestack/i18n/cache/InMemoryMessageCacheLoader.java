package io.github.rosestack.i18n.cache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于内存的消息缓存加载器实现
 *
 * <p>提供高性能的内存缓存功能，支持 LRU 淘汰策略、过期时间、Micrometer 监控等特性。
 *
 * <p>
 *
 * <h3>核心特性：</h3>
 *
 * <ul>
 *   <li>支持 LRU 缓存淘汰策略
 *   <li>支持缓存过期时间配置
 *   <li>基于 Micrometer 的监控指标
 *   <li>线程安全的并发访问
 *   <li>支持缓存预热和批量操作
 * </ul>
 *
 * @author chensoul
 * @since 1.0.0
 */
public class InMemoryMessageCacheLoader extends AbstractMetricsMessageCacheLoader {

	private static final Logger logger = LoggerFactory.getLogger(InMemoryMessageCacheLoader.class);

	/**
	 * 缓存数据存储
	 */
	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	/**
	 * 定时清理任务执行器
	 */
	private final ScheduledExecutorService cleanupExecutor;

	/**
	 * 构造函数（使用默认的 SimpleMeterRegistry）
	 *
	 * @param cacheProperties 缓存配置属性
	 */
	public InMemoryMessageCacheLoader(CacheProperties cacheProperties) {
		this(cacheProperties, new SimpleMeterRegistry());
	}

	/**
	 * 构造函数
	 *
	 * @param cacheProperties 缓存配置属性
	 * @param meterRegistry   Micrometer 指标注册表
	 */
	public InMemoryMessageCacheLoader(CacheProperties cacheProperties, MeterRegistry meterRegistry) {
		super(cacheProperties, meterRegistry);
		this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r, "i18n-cache-cleanup");
			thread.setDaemon(true);
			return thread;
		});

		// 启动定时清理过期缓存任务
		if (cacheEnabled() && cacheProperties.getExpireAfterWrite() != null) {
			long cleanupInterval =
				Math.max(cacheProperties.getExpireAfterWrite().toMinutes() / 2, 1);
			cleanupExecutor.scheduleAtFixedRate(
				this::cleanupExpiredEntries, cleanupInterval, cleanupInterval, TimeUnit.MINUTES);
		}

		// 初始化缓存大小
		setCacheSize(0);

		logger.debug("InMemoryMessageCacheLoader 初始化完成，配置: {}", cacheProperties);
	}

	@Override
	protected String doGetFromCache(String cacheKey) {
		if (!cacheEnabled() || cacheKey == null) {
			return null;
		}

		CacheEntry entry = cache.get(cacheKey);

		if (entry == null) {
			return null;
		}

		if (isExpired(entry)) {
			cache.remove(cacheKey);
			updateCacheSize(-1);
			return null;
		}

		// 更新访问时间（用于 LRU）
		entry.updateAccessTime();
		return entry.getValue();
	}

	@Override
	protected Map<String, String> doGetFromCache(String[] cacheKeys, String[] codes) {
		if (!cacheEnabled() || cacheKeys == null || codes == null) {
			return new HashMap<>();
		}

		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < cacheKeys.length && i < codes.length; i++) {
			String cacheKey = cacheKeys[i];
			String code = codes[i];

			CacheEntry entry = cache.get(cacheKey);
			if (entry != null && !isExpired(entry)) {
				result.put(code, entry.getValue());
				// 更新访问时间
				entry.updateAccessTime();
			}
		}
		return result;
	}

	@Override
	protected void doPutToCache(String cacheKey, String message) {
		if (!cacheEnabled() || cacheKey == null || message == null) {
			return;
		}

		// 检查缓存大小限制
		if (cache.size() >= cacheProperties.getMaxSize()) {
			evictLeastRecentlyUsed();
		}

		CacheEntry entry = new CacheEntry(message);
		boolean isNewEntry = cache.put(cacheKey, entry) == null;

		if (isNewEntry) {
			updateCacheSize(1);
		}

		logger.trace("缓存消息: key={}, message={}", cacheKey, message);
	}

	@Override
	protected void doPutToCache(Map<String, String> cacheKeyToMessage) {
		if (!cacheEnabled() || cacheKeyToMessage == null) {
			return;
		}

		for (Map.Entry<String, String> entry : cacheKeyToMessage.entrySet()) {
			doPutToCache(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected long doEvictCache(String keyPattern) {
		if (!cacheEnabled() || keyPattern == null) {
			return 0;
		}

		// 将模式转换为前缀匹配
		String prefix = keyPattern.replace("*", "");
		int removedCount = 0;

		var iterator = cache.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			if (entry.getKey().startsWith(prefix)) {
				iterator.remove();
				removedCount++;
			}
		}

		logger.debug("清除缓存模式 {} 的缓存，共清除 {} 条记录", keyPattern, removedCount);
		return removedCount;
	}

	@Override
	protected long doClearCache(String keyPattern) {
		if (!cacheEnabled()) {
			return 0;
		}

		if (keyPattern == null || "*".equals(keyPattern)) {
			// 清除所有缓存
			int size = cache.size();
			cache.clear();
			logger.debug("清除所有缓存，共清除 {} 条记录", size);
			return size;
		} else {
			// 按模式清除
			return doEvictCache(keyPattern);
		}
	}

	/**
	 * 获取当前缓存大小
	 *
	 * @return 缓存中的条目数量
	 */
	public int getCacheSize() {
		return cache.size();
	}

	/**
	 * 销毁缓存加载器，释放资源
	 */
	public void destroy() {
		cleanupExecutor.shutdown();
		try {
			if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				cleanupExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			cleanupExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}
		doClearCache("*");
		logger.debug("InMemoryMessageCacheLoader 已销毁");
	}

	/**
	 * 检查缓存是否启用
	 *
	 * @return 如果缓存启用返回 true，否则返回 false
	 */
	private boolean cacheEnabled() {
		return cacheProperties != null && cacheProperties.isEnabled();
	}

	/**
	 * 检查缓存条目是否过期
	 *
	 * @param entry 缓存条目
	 * @return 如果过期返回 true，否则返回 false
	 */
	private boolean isExpired(CacheEntry entry) {
		if (cacheProperties.getExpireAfterWrite() == null) {
			return false;
		}

		return entry.getCreateTime().plus(cacheProperties.getExpireAfterWrite()).isBefore(LocalDateTime.now());
	}

	/**
	 * 淘汰最近最少使用的缓存条目
	 */
	private void evictLeastRecentlyUsed() {
		if (cache.isEmpty()) {
			return;
		}

		String lruKey = null;
		LocalDateTime oldestAccessTime = LocalDateTime.now();

		for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
			LocalDateTime accessTime = entry.getValue().getLastAccessTime();
			if (accessTime.isBefore(oldestAccessTime)) {
				oldestAccessTime = accessTime;
				lruKey = entry.getKey();
			}
		}

		if (lruKey != null) {
			cache.remove(lruKey);
			updateCacheSize(-1);
			logger.trace("淘汰 LRU 缓存条目: {}", lruKey);
		}
	}

	/**
	 * 清理过期的缓存条目
	 */
	private void cleanupExpiredEntries() {
		if (!cacheEnabled() || cacheProperties.getExpireAfterWrite() == null) {
			return;
		}

		int removedCount = 0;
		var iterator = cache.entrySet().iterator();

		while (iterator.hasNext()) {
			var entry = iterator.next();
			if (isExpired(entry.getValue())) {
				iterator.remove();
				removedCount++;
			}
		}

		if (removedCount > 0) {
			updateCacheSize(-removedCount);
			logger.debug("定时清理过期缓存，共清除 {} 条记录", removedCount);
		}
	}
}

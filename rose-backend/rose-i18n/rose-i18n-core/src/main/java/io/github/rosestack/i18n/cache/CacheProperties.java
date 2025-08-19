package io.github.rosestack.i18n.cache;

import lombok.Data;

import java.time.Duration;

@Data
public class CacheProperties {

	/**
	 * 是否启用缓存
	 */
	private boolean enabled = true;

	/**
	 * 缓存类型
	 */
	private CacheType type = CacheType.MEMORY;

	/**
	 * 缓存最大大小
	 */
	private int maxSize = 512;

	/**
	 * 缓存过期时间
	 */
	private Duration expireAfterWrite = Duration.ofMinutes(60);

	/**
	 * 是否启用缓存统计
	 */
	private boolean statisticsEnabled = false;

	/**
	 * 是否启用缓存预加载
	 */
	private boolean preloadEnabled = false;

	private String keyPrefix = "i18n:";

	/**
	 * 缓存类型枚举
	 */
	public enum CacheType {
		MEMORY,
		/**
		 * Redis 缓存
		 */
		REDIS
	}

	/**
	 * 缓存淘汰策略枚举
	 */
	public enum EvictionPolicy {
		/**
		 * 最近最少使用
		 */
		LRU,
		/**
		 * 先进先出
		 */
		FIFO,
		/**
		 * 最近最少频率使用
		 */
		LFU
	}
}

package io.github.rosestack.i18n.cache;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class CacheConfig {
    private int maxSize;
    private Duration expireAfterWrite;
    private Duration expireAfterAccess;
    private boolean enableStatistics;
    private boolean enablePreload; // 新增：是否启用缓存预热
    private String cacheName;
    private CacheEvictionPolicy evictionPolicy;

    public enum CacheEvictionPolicy {
        LRU, LFU, FIFO
    }
}
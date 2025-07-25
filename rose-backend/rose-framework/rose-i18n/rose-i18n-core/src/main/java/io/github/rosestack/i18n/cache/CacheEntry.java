package io.github.rosestack.i18n.cache;

import java.time.LocalDateTime;

/**
 * 缓存条目内部类
 */
public class CacheEntry {
    /**
     * 缓存值
     */
    private final String value;
    /**
     * 创建时间
     */
    private final LocalDateTime createTime;
    /**
     * 最后访问时间
     */
    private volatile LocalDateTime lastAccessTime;

    public CacheEntry(String value) {
        this.value = value;
        this.createTime = LocalDateTime.now();
        this.lastAccessTime = this.createTime;
    }

    public String getValue() {
        return value;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
}
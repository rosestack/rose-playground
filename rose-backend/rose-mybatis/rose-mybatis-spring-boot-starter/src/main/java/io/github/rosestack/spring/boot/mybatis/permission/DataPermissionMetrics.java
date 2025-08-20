package io.github.rosestack.spring.boot.mybatis.permission;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 数据权限相关指标埋点。
 */
public class DataPermissionMetrics {
    private final Timer getSqlSegmentTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter errorCounter;

    public DataPermissionMetrics(MeterRegistry registry) {
        this.getSqlSegmentTimer = Timer.builder("rose.mybatis.permission.get_sql_segment.duration")
                .description("Duration of building permission SQL segment")
                .register(registry);
        this.cacheHitCounter = Counter.builder("rose.mybatis.permission.cache.hit")
                .description("permission cache hit count")
                .register(registry);
        this.cacheMissCounter = Counter.builder("rose.mybatis.permission.cache.miss")
                .description("permission cache miss count")
                .register(registry);
        this.errorCounter = Counter.builder("rose.mybatis.permission.errors")
                .description("permission handler errors")
                .register(registry);
    }

    public Timer.Sample start() {
        return Timer.start();
    }

    public void record(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(getSqlSegmentTimer);
        }
    }

    public void incrementCacheHit() {
        cacheHitCounter.increment();
    }

    public void incrementCacheMiss() {
        cacheMissCounter.increment();
    }

    public void incrementError() {
        errorCounter.increment();
    }
}

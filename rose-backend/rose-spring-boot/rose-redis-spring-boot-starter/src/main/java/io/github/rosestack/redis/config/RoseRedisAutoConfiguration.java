package io.github.rosestack.redis.config;

import io.github.rosestack.core.spring.YmlPropertySourceFactory;
import io.github.rosestack.redis.lock.DistributedLockManager;
import io.github.rosestack.redis.lock.aspect.LockAspect;
import io.github.rosestack.redis.ratelimit.RateLimitManager;
import io.github.rosestack.redis.ratelimit.aspect.RateLimitAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Rose Redis 自动配置类
 * <p>
 * 提供 Redis 增强功能的自动配置，包括分布式锁、缓存、限流、消息队列、会话管理等。
 * 基于现有的 Redis 基础设施进行扩展，不替换现有配置。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
@PropertySource(value = "classpath:application-rose-redis.yml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseRedisProperties.class)
@ComponentScan(basePackages = "io.github.rosestack.redis")
public class RoseRedisAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("Rose Redis 自动配置已启用");
    }

    /**
     * 分布式锁配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rose.redis.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class RoseRedisLockConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(RedisTemplate.class)
        public DistributedLockManager distributedLockManager(RedisTemplate<String, Object> redisTemplate,
                                                             RoseRedisProperties properties) {
            log.info("启用 Rose Redis 分布式锁功能");
            return new DistributedLockManager(redisTemplate, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        @ConditionalOnBean(DistributedLockManager.class)
        public LockAspect distributedLockAspect(DistributedLockManager lockManager) {
            log.info("启用 Rose Redis 分布式锁切面");
            return new LockAspect(lockManager);
        }
    }

    /**
     * 缓存增强配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rose.redis.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class RoseRedisCacheConfiguration {
        // TODO: 实现缓存增强配置
    }

    /**
     * 限流配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rose.redis.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class RoseRedisRateLimitConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(RedisTemplate.class)
        public RateLimitManager rateLimitManager(RedisTemplate<String, Object> redisTemplate,
                                                RoseRedisProperties properties) {
            log.info("启用 Rose Redis 限流功能");
            return new RateLimitManager(redisTemplate, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        @ConditionalOnBean(RateLimitManager.class)
        public RateLimitAspect rateLimitAspect(RateLimitManager rateLimitManager,
                                              RoseRedisProperties properties) {
            log.info("启用 Rose Redis 限流切面");
            return new RateLimitAspect(rateLimitManager, properties);
        }
    }

    /**
     * 消息队列配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rose.redis.message", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class RoseRedisMessageConfiguration {
        // TODO: 实现消息队列配置
    }

    /**
     * 会话管理配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rose.redis.session", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class RoseRedisSessionConfiguration {
        // TODO: 实现会话管理配置
    }

    /**
     * 数据结构操作配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rose.redis.data-structure", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class RoseRedisDataStructureConfiguration {
        // TODO: 实现数据结构操作配置
    }
}
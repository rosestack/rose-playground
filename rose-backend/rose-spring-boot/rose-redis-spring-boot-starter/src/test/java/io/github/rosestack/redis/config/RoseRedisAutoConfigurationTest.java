package io.github.rosestack.redis.config;

import io.github.rosestack.redis.lock.DistributedLockManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rose Redis 自动配置测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
class RoseRedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RedisAutoConfiguration.class,
                    RoseRedisAutoConfiguration.class
            ));

    @Test
    void shouldLoadAutoConfiguration() {
        this.contextRunner
                .withPropertyValues("rose.redis.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RoseRedisProperties.class);
                    assertThat(context).hasSingleBean(RoseRedisAutoConfiguration.class);
                    // 验证分布式锁管理器在有 RedisTemplate 时才创建
                    if (context.containsBean("redisTemplate")) {
                        assertThat(context).hasSingleBean(DistributedLockManager.class);
                    }
                });
    }

    @Test
    void shouldNotLoadWhenDisabled() {
        this.contextRunner
                .withPropertyValues("rose.redis.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RoseRedisAutoConfiguration.class);
                });
    }

    @Test
    void shouldLoadByDefault() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(RoseRedisProperties.class);
                    assertThat(context).hasSingleBean(RoseRedisAutoConfiguration.class);
                    // 验证分布式锁管理器在有 RedisTemplate 时才创建
                    if (context.containsBean("redisTemplate")) {
                        assertThat(context).hasSingleBean(DistributedLockManager.class);
                    }
                });
    }

    @Test
    void shouldLoadWithRedisAutoConfiguration() {
        this.contextRunner
                .run(context -> {
                    // 验证 Redis 相关的 Bean 存在
                    assertThat(context).hasSingleBean(RoseRedisProperties.class);
                    assertThat(context).hasSingleBean(RoseRedisAutoConfiguration.class);

                    // 验证配置属性正确加载
                    RoseRedisProperties properties = context.getBean(RoseRedisProperties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getLock().isEnabled()).isTrue();
                    assertThat(properties.getCache().isEnabled()).isTrue();

                    // 验证分布式锁管理器在有 RedisTemplate 时才创建
                    if (context.containsBean("redisTemplate")) {
                        assertThat(context).hasSingleBean(DistributedLockManager.class);
                    }
                });
    }
}
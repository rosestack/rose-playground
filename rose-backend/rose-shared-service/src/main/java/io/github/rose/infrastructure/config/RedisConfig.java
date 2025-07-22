package io.github.rose.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置类
 * <p>
 * 自定义 RedisTemplate 序列化配置，使用字符串序列化器确保数据的可读性和兼容性。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>所有键值使用字符串序列化，便于调试和监控</li>
 *   <li>支持哈希结构的键值序列化</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private RedisTemplate<String, Object> redisTemplate;
 *
 * public void setUserInfo(String userId, String userInfo) {
 *     redisTemplate.opsForValue().set("user:" + userId, userInfo);
 * }
 *
 * public String getUserInfo(String userId) {
 *     return (String) redisTemplate.opsForValue().get("user:" + userId);
 * }
 * }</pre>
 *
 * @author Rose Framework Team
 * @see RedisTemplate
 * @see StringRedisSerializer
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * 创建自定义 RedisTemplate
     * <p>
     * 配置所有序列化器为字符串类型，确保 Redis 中存储的数据为可读的字符串格式。
     * 适用于需要直接查看 Redis 数据或与其他系统兼容的场景。
     *
     * @param factory Redis 连接工厂
     * @return 配置完成的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper mapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 设置序列化器
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory, ObjectMapper mapper) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(mapper, Object.class)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .transactionAware() // 支持事务
                .build();
    }

    /**
     * 配置String Redis模板
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            for (Object param : params) {
                sb.append(":");
                sb.append(param.toString());
            }
            return sb.toString();
        };
    }

    /**
     * 配置缓存解析器
     */
    @Bean
    public CacheResolver cacheResolver(CacheManager cacheManager) {
        return new SimpleCacheResolver(cacheManager);
    }

    /**
     * 配置Redis监听器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 配置监听器
        container.addMessageListener(new CacheEvictionListener(),
                new ChannelTopic("cache:eviction"));
        return container;
    }

    @Slf4j
    public class CacheEvictionListener implements MessageListener {
        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                String channel = new String(message.getChannel());
                String body = new String(message.getBody());

                log.info("收到缓存失效消息: channel={}, body={}", channel, body);
                handleCacheEviction(body);
            } catch (Exception e) {
                log.error("处理缓存失效消息失败", e);
            }
        }

        private void handleCacheEviction(String cacheKey) {
            if (cacheKey.startsWith("user:")) {
                log.info("用户缓存失效: {}", cacheKey);
            } else if (cacheKey.startsWith("order:")) {
                log.info("订单缓存失效: {}", cacheKey);
            }
        }
    }
}

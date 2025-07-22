package io.github.rose.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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
 * @since 1.0.0
 * @see RedisTemplate
 * @see StringRedisSerializer
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
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
}

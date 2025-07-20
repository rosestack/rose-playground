package io.github.rose.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration class for customizing RedisTemplate serialization behavior.
 *
 * This configuration class provides a customized RedisTemplate bean that uses string
 * serialization for all Redis operations. This approach ensures human-readable keys
 * and values in Redis, making debugging and monitoring easier while maintaining
 * compatibility with various Redis clients and tools.
 *
 * <h3>Serialization Strategy:</h3>
 * The configuration uses StringRedisSerializer for all serialization operations:
 * <ul>
 *   <li><strong>Key Serialization:</strong> Redis keys are stored as UTF-8 strings</li>
 *   <li><strong>Value Serialization:</strong> Redis values are stored as UTF-8 strings</li>
 *   <li><strong>Hash Key Serialization:</strong> Hash field names are stored as UTF-8 strings</li>
 *   <li><strong>Hash Value Serialization:</strong> Hash field values are stored as UTF-8 strings</li>
 * </ul>
 *
 * <h3>Benefits of String Serialization:</h3>
 * <ul>
 *   <li><strong>Human Readable:</strong> Data can be easily inspected using Redis CLI</li>
 *   <li><strong>Cross-Platform:</strong> Compatible with any Redis client or language</li>
 *   <li><strong>Debugging Friendly:</strong> Easy to troubleshoot and monitor</li>
 *   <li><strong>Tool Compatibility:</strong> Works with Redis management tools</li>
 * </ul>
 *
 * <h3>Considerations:</h3>
 * <ul>
 *   <li><strong>Manual Serialization:</strong> Application code must handle object serialization</li>
 *   <li><strong>Type Safety:</strong> No automatic type conversion - requires explicit handling</li>
 *   <li><strong>Performance:</strong> May require additional JSON/XML serialization for complex objects</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * @Autowired
 * private RedisTemplate<String, Object> redisTemplate;
 *
 * public void storeValue(String key, String value) {
 *     redisTemplate.opsForValue().set(key, value);
 * }
 *
 * public String getValue(String key) {
 *     return (String) redisTemplate.opsForValue().get(key);
 * }
 * }</pre>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see RedisTemplate
 * @see StringRedisSerializer
 * @see RedisConnectionFactory
 */
@Configuration
public class RedisConfig {

    /**
     * Creates a customized RedisTemplate with string serialization for all operations.
     *
     * This method configures a RedisTemplate that uses StringRedisSerializer for all
     * serialization operations, ensuring that all data stored in Redis is in a
     * human-readable string format. This configuration is particularly useful for
     * applications that need to maintain compatibility with external systems or
     * require easy debugging and monitoring capabilities.
     *
     * <p><strong>Serializer Configuration:</strong>
     * <ul>
     *   <li><strong>Key Serializer:</strong> StringRedisSerializer for Redis keys</li>
     *   <li><strong>Value Serializer:</strong> StringRedisSerializer for Redis values</li>
     *   <li><strong>Hash Key Serializer:</strong> StringRedisSerializer for hash field names</li>
     *   <li><strong>Hash Value Serializer:</strong> StringRedisSerializer for hash field values</li>
     * </ul>
     *
     * <p><strong>Connection Factory Integration:</strong>
     * The template uses the provided RedisConnectionFactory, which should be configured
     * elsewhere in the application (typically through Spring Boot auto-configuration
     * or custom connection factory beans).
     *
     * <p><strong>Thread Safety:</strong>
     * The returned RedisTemplate is thread-safe and can be safely used across
     * multiple threads and concurrent operations.
     *
     * @param factory The RedisConnectionFactory to use for Redis connections.
     *               Must not be null. Typically provided by Spring Boot auto-configuration.
     * @return A fully configured RedisTemplate with string serialization for all operations
     *
     * @see RedisTemplate#setConnectionFactory(RedisConnectionFactory)
     * @see RedisTemplate#setKeySerializer(org.springframework.data.redis.serializer.RedisSerializer)
     * @see StringRedisSerializer
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // Create new RedisTemplate instance with String key and Object value types
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Set the connection factory for Redis operations
        template.setConnectionFactory(factory);

        // Configure string serialization for all Redis operations
        // This ensures human-readable data in Redis for debugging and monitoring

        // Set key serializer for regular Redis keys
        template.setKeySerializer(new StringRedisSerializer());

        // Set value serializer for regular Redis values
        template.setValueSerializer(new StringRedisSerializer());

        // Set hash key serializer for Redis hash field names
        template.setHashKeySerializer(new StringRedisSerializer());

        // Set hash value serializer for Redis hash field values
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }
}

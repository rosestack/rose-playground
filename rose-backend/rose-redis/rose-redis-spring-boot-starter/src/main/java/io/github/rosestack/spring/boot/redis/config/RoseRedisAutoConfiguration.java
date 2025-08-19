package io.github.rosestack.spring.boot.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.github.rosestack.spring.YmlPropertySourceFactory;
import io.github.rosestack.spring.boot.redis.lock.DistributedLockManager;
import io.github.rosestack.spring.boot.redis.lock.aspect.LockAspect;
import io.github.rosestack.spring.boot.redis.ratelimit.RateLimitManager;
import io.github.rosestack.spring.boot.redis.ratelimit.aspect.RateLimitAspect;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Rose Redis 自动配置类
 *
 * <p>提供 Redis 增强功能的自动配置，包括分布式锁、缓存、限流、消息队列、会话管理等。 基于现有的 Redis 基础设施进行扩展，不替换现有配置。
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
@PropertySource(value = "classpath:application-rose-redis.yaml", factory = YmlPropertySourceFactory.class)
@ConditionalOnProperty(prefix = "rose.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RoseRedisProperties.class)
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
		public DistributedLockManager distributedLockManager(
			RedisTemplate<String, Object> redisTemplate, RoseRedisProperties properties) {
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
		@Bean(name = "redisTemplate")
		@ConditionalOnClass(RedisOperations.class)
		public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
			RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(redisConnectionFactory);
			redisTemplate.setKeySerializer(new StringRedisSerializer());
			redisTemplate.setValueSerializer(redisSerializer());
			redisTemplate.setHashKeySerializer(new StringRedisSerializer());
			redisTemplate.setHashValueSerializer(redisSerializer());
			redisTemplate.afterPropertiesSet();

			return redisTemplate;
		}

		public RedisSerializer<Object> redisSerializer() {
			ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();
			mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
			// 将类型序列化到属性json字符串中
			mapper.activateDefaultTyping(
				LaissezFaireSubTypeValidator.instance,
				ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);
			return new GenericJackson2JsonRedisSerializer(mapper);
		}
	}

	/**
	 * 限流配置
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(
		prefix = "rose.redis.rate-limit",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true)
	static class RoseRedisRateLimitConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnBean(RedisTemplate.class)
		public RateLimitManager rateLimitManager(
			RedisTemplate<String, Object> redisTemplate, RoseRedisProperties properties) {
			log.info("启用 Rose Redis 限流功能");
			return new RateLimitManager(redisTemplate, properties);
		}

		@Bean
		public DefaultRedisScript<Long> limitScript() {
			DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
			redisScript.setScriptText(limitScriptText());
			redisScript.setResultType(Long.class);
			return redisScript;
		}

		private String limitScriptText() {
			return "local key = KEYS[1]\n"
				+ "local count = tonumber(ARGV[1])\n"
				+ "local time = tonumber(ARGV[2])\n"
				+ "local current = redis.call('get', key);\n"
				+ "if current and tonumber(current) > count then\n"
				+ "    return tonumber(current);\n"
				+ "end\n"
				+ "current = redis.call('incr', key)\n"
				+ "if tonumber(current) == 1 then\n"
				+ "    redis.call('expire', key, time)\n"
				+ "end\n"
				+ "return tonumber(current);";
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
		@ConditionalOnBean(RateLimitManager.class)
		public RateLimitAspect rateLimitAspect(RateLimitManager rateLimitManager, RoseRedisProperties properties) {
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
	@ConditionalOnProperty(
		prefix = "rose.redis.data-structure",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true)
	static class RoseRedisDataStructureConfiguration {
		// TODO: 实现数据结构操作配置
	}
}

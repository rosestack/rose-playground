package io.github.rosestack.i18n.spring.boot.autoconfigure;

import io.github.rosestack.i18n.MessageCacheLoader;
import io.github.rosestack.i18n.cache.InMemoryMessageCacheLoader;
import io.github.rosestack.i18n.spring.I18nMessageSourceFactoryBean;
import io.github.rosestack.i18n.spring.annotation.EnableI18n;
import io.github.rosestack.i18n.spring.boot.cache.RedisMessageCacheLoader;
import io.github.rosestack.i18n.spring.boot.condition.ConditionalOnI18nEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * I18n Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ConditionalOnI18nEnabled
@EnableI18n
@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(I18nProperties.class)
@Import(I18nAutoConfiguration.MessageCacheLoaderConfiguration.class)
public class I18nAutoConfiguration {
    private final I18nProperties i18nProperties;

    @Bean
    @ConditionalOnProperty(name = "spring.application.name")
    public I18nMessageSourceFactoryBean applicationMessageSource(
            @Value("${spring.application.name}") String applicationName, MessageCacheLoader messageCacheLoader) {
        return new I18nMessageSourceFactoryBean(applicationName, messageCacheLoader);
    }

    public class MessageCacheLoaderConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(name = "rose.i18n.cache.type", havingValue = "MEMORY", matchIfMissing = true)
        public MessageCacheLoader inMemoryMessageCacheLoader() {
            return new InMemoryMessageCacheLoader(i18nProperties.getCache());
        }

        @Bean
        @ConditionalOnClass(RedisTemplate.class)
        @ConditionalOnProperty(name = "rose.i18n.cache.type", havingValue = "REDIS")
        @ConditionalOnMissingBean
        public MessageCacheLoader redisMessageCacheLoader(RedisTemplate<String, Object> redisTemplate) {
            return new RedisMessageCacheLoader(i18nProperties.getCache(), redisTemplate);
        }
    }
}

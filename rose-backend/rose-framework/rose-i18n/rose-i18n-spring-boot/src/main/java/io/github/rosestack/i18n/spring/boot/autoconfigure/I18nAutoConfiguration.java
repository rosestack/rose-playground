package io.github.rosestack.i18n.spring.boot.autoconfigure;

import io.github.rosestack.i18n.spring.annotation.EnableI18n;
import io.github.rosestack.i18n.spring.I18nMessageSourceFactoryBean;
import io.github.rosestack.i18n.spring.boot.condition.ConditionalOnI18nEnabled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * I18n Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ConditionalOnI18nEnabled
@EnableI18n
public class I18nAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "spring.application.name")
    public I18nMessageSourceFactoryBean applicationMessageSource(
            @Value("${spring.application.name}") String applicationName) {
        return new I18nMessageSourceFactoryBean(applicationName);
    }
}
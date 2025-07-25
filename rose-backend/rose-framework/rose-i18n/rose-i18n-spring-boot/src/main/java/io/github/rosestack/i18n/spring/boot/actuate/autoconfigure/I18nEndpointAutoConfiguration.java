package io.github.rosestack.i18n.spring.boot.actuate.autoconfigure;

import io.github.rosestack.i18n.spring.boot.actuate.I18nEndpoint;
import io.github.rosestack.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import io.github.rosestack.i18n.spring.boot.condition.ConditionalOnI18nEnabled;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * I18n Spring Boot Actuator Endpoint Auto-Configuration
 *
 * @see I18nEndpoint
 * @since 1.0.0
 */
@ConditionalOnClass(name = {
        "org.springframework.boot.actuate.endpoint.annotation.Endpoint", // spring-boot-actuator-autoconfigure
})
@ConditionalOnI18nEnabled
@ConditionalOnAvailableEndpoint(endpoint = I18nEndpoint.class)
@AutoConfigureAfter(I18nAutoConfiguration.class)
public class I18nEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public I18nEndpoint i18nEndpoint() {
        return new I18nEndpoint();
    }
}
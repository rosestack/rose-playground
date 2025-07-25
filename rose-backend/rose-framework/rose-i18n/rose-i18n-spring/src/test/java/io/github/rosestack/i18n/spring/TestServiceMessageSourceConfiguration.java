package io.github.rosestack.i18n.spring;

import io.github.rosestack.i18n.spring.beans.I18nMessageSourceFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TestServiceMessageSourceConfiguration {

    @Bean
    public static I18nMessageSourceFactoryBean testServiceMessageSource() {
        return new I18nMessageSourceFactoryBean("test");
    }
}

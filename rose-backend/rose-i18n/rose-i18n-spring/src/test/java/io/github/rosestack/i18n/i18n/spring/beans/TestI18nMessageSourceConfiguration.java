package io.github.rosestack.i18n.i18n.spring.beans;

import io.github.rosestack.i18n.spring.I18nMessageSourceFactoryBean;
import org.springframework.context.annotation.Bean;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TestI18nMessageSourceConfiguration {

    @Bean
    public static I18nMessageSourceFactoryBean testServiceMessageSource() {
        return new I18nMessageSourceFactoryBean("test");
    }
}

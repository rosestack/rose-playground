package io.github.rosestack.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * 国际化配置
 * <p>
 * 提供 MessageSource 和 LocaleResolver 支持多语言
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
public class MessageConfig {

    private final WebProperties webProperties;

    public MessageConfig(WebProperties webProperties) {
        this.webProperties = webProperties;
    }

    /**
     * 配置 MessageSource
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }

    /**
     * 配置 LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        localeResolver.setSupportedLocales(java.util.Arrays.asList(
                Locale.SIMPLIFIED_CHINESE,
                Locale.ENGLISH
        ));
        return localeResolver;
    }

    /**
     * 配置 LocaleChangeInterceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
} 
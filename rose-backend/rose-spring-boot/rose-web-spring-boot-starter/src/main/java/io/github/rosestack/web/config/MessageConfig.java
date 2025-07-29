package io.github.rosestack.web.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * 国际化配置
 * <p>
 * 提供 MessageSource 和 LocaleResolver 支持多语言
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
public class MessageConfig {
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
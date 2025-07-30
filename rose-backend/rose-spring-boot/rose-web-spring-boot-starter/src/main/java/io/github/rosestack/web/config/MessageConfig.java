package io.github.rosestack.web.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
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
@Slf4j
@RequiredArgsConstructor
public class MessageConfig {
    private final MessageSource messageSource;

    @PostConstruct
    public void init() {
        log.info("启用 MessageSource 资源国际化: {}", messageSource);
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
package io.github.rose.i18n;

import io.github.rose.core.util.ServletUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;


public abstract class AbstractMessageSource implements MessageSource {
    protected final String source;
    protected LocaleResolver localeResolver = new DefaultLocaleResolver();

    public AbstractMessageSource(String source) {
        Objects.requireNonNull(source, "'source' argument must not be null");
        this.source = source;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    public final String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        if (code != null) {
            message = doGetMessage(code, locale, args);
        }
        return message;
    }

    @Override
    public final Locale getLocale() {
        Locale locale = localeResolver.resolveLocale(ServletUtils.getRequest());
        return locale == null ? getDefaultLocale() : locale;
    }

    @Override
    public final String getSource() {
        return source;
    }

    protected abstract String doGetMessage(String code, Locale locale, Object... args);
}

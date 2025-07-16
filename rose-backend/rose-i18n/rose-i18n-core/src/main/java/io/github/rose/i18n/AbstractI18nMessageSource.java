package io.github.rose.i18n;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;


public abstract class AbstractI18nMessageSource implements I18nMessageSource {
    protected final String source;

    public AbstractI18nMessageSource(String source) {
        Objects.requireNonNull(source, "'source' argument must not be null");
        this.source = source;
    }

    @Override
    public final String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        if (code != null) {
            message = doGetMessage(code, locale, locale, args);
        }
        return message;
    }

    @Override
    public final Locale getLocale() {
        Locale locale = doGetLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    @Override
    public final String getSource() {
        return source;
    }

    protected Locale doGetLocale() {
        return LocaleContextHolder.getLocale();
    }

    protected abstract String doGetMessage(String code, Locale locale, Object... args);
}

package io.github.rose.i18n;

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
            message = doGetMessage(code, locale, args);
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
        // 默认实现返回 null，实际使用 getDefaultLocale
        return null;
    }

    protected abstract String doGetMessage(String code, Locale locale, Object... args);
}

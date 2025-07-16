package io.github.rose.i18n;

import java.util.Locale;

import static java.util.Objects.requireNonNull;

public abstract class AbstractI18nMessageSource implements I18nMessageSource {
    protected final String source;

    public AbstractI18nMessageSource(String source) {
        requireNonNull(source, "'source' argument must not be null");
        this.source = source;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public final String getMessage(String code, Object... args) {
        return I18nMessageSource.super.getMessage(code, args);
    }

    @Override
    public final String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        if (code != null) {
            String resolvedCode = resolveMessageCode(code);
            if (resolvedCode != null) {
                Locale resolvedLocale = resolveLocale(locale);
                message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
            }
        }
        return message;
    }

    @Override
    public final Locale getLocale() {
        Locale locale = getInternalLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    @Override
    public final String getSource() {
        return source;
    }

    protected Locale getInternalLocale() {
        return null;
    }

    protected String resolveMessageCode(String code) {
        return code;
    }

    protected Locale resolveLocale(Locale locale) {
        return locale;
    }

    protected abstract String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args);
}

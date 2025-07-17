package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;
import jakarta.annotation.Nonnull;

import java.util.Locale;

public class EmptyMessageSource implements I18nMessageSource {
    public static final EmptyMessageSource INSTANCE = new EmptyMessageSource();

    @Override
    public String getMessage(String code, Locale locale, String defaultMessage, Object... args) {
        return null;
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}

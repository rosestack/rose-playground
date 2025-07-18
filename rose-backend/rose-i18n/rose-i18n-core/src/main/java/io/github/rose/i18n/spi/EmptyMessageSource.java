package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Locale;
import java.util.Map;

public class EmptyMessageSource implements I18nMessageSource {
    public static final EmptyMessageSource INSTANCE = new EmptyMessageSource();

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return null;
    }

    @Nullable
    @Override
    public Map<String, String> getMessages(Locale locale) {
        return Map.of();
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

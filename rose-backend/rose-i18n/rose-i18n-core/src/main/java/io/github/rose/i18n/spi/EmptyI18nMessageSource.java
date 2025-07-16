package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EmptyI18nMessageSource implements I18nMessageSource {

    public static final EmptyI18nMessageSource INSTANCE = new EmptyI18nMessageSource();

    private EmptyI18nMessageSource() {
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return null;
    }

    @Nullable
    @Override
    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        return Map.of();
    }

    @Nullable
    @Override
    public Map<String, String> getAllMessages(Locale locale) {
        return Map.of();
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return getDefaultLocale();
    }

    @Override
    public String getSource() {
        return "Empty";
    }
}
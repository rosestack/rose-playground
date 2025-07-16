package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageSource;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EmptyMessageSource implements MessageSource {

    public static final EmptyMessageSource INSTANCE = new EmptyMessageSource();

    private EmptyMessageSource() {
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

    @Override
    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        return Map.of();
    }

    @Override
    public Map<String, String> getMessages(Locale locale) {
        return Map.of();
    }

    @Override
    public Locale getLocale() {
        return getDefaultLocale();
    }

    @Override
    public String getSource() {
        return "Empty";
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return Set.of(getDefaultLocale());
    }
}
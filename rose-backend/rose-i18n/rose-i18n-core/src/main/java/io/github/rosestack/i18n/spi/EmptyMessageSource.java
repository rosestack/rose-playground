package io.github.rosestack.i18n.spi;

import io.github.rosestack.i18n.I18nMessageSource;
import java.util.Locale;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class EmptyMessageSource implements I18nMessageSource {
    public static final EmptyMessageSource INSTANCE = new EmptyMessageSource();

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return null;
    }

    @Nullable @Override
    public Map<String, String> getMessages(Locale locale) {
        return Map.of();
    }

    @NonNull @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}
}

package io.github.rose.i18n;

import io.github.rose.core.lang.Prioritized;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

public interface I18nMessageSource extends Prioritized {
    String COMMON_SOURCE = "common";

    @Nullable
    String getMessage(String code, Locale locale, Object... args);

    @Nullable
    default String getMessage(String code, Object... args) {
        return getMessage(code, Locale.getDefault(), args);
    }

    @Nullable
    default String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    @Nullable
    Map<String, String> getMessages(Set<String> codes, Locale locale);

    @Nullable
    Map<String, String> getAllMessages(Locale locale);

    @Nonnull
    default Set<Locale> getSupportedLocales() {
        return new TreeSet<>(Arrays.asList(getDefaultLocale(), Locale.ENGLISH));
    }

    @Nonnull
    default Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    @Nonnull
    Locale getLocale();

    void init();

    void destroy();

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }
}
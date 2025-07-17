package io.github.rose.i18n;

import io.github.rose.core.lang.Prioritized;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Locale;
import java.util.Set;

public interface I18nMessageSource extends Lifecycle, Prioritized {
    String COMMON_SOURCE = "common";

    @Nullable
    String getMessage(String code, Locale locale, Object... args);

    default String getMessage(String code, Object... args) {
        return getMessage(code, getLocale(), args);
    }

    @Nonnull
    Locale getLocale();

    @Nonnull
    default Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    default Set<Locale> getSupportedLocales() {
        return Set.of(getDefaultLocale(), Locale.ENGLISH);
    }

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }
}

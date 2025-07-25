package io.github.rose.i18n.spring.util;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

import java.util.Locale;

public abstract class LocaleUtils {

    /**
     * Get the {@link Locale} from Thread-Locale scoped {@link LocaleContext}
     *
     * @return <code>null</code> if can't be found
     */
    @Nullable
    public static Locale getLocaleFromLocaleContext() {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        return localeContext == null ? null : localeContext.getLocale();
    }
}
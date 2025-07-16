package io.github.rose.i18n;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

public class DefaultLocaleResolver implements LocaleResolver {
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return Locale.getDefault();
    }
}

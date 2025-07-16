package io.github.rose.i18n;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

public interface LocaleResolver {
    Locale resolveLocale(HttpServletRequest request);
}

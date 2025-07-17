package io.github.rose.i18n;

import java.util.Locale;

public interface ReloadedMessageSource {
    void reloadResource(Locale locale);

    String getResourceDir();
}

package io.github.rosestack.i18n.render;

import java.util.Locale;

public interface MessageRenderer {

    String render(String message, Locale locale, Object args);
}

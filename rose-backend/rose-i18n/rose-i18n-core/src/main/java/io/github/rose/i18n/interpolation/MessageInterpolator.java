package io.github.rose.i18n.interpolation;

import java.util.Locale;
import java.util.Map;

public interface MessageInterpolator {

    String interpolate(String message, Locale locale, Object args);
}

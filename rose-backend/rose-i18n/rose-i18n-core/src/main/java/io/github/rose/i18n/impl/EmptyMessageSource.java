package io.github.rose.i18n.impl;

import io.github.rose.i18n.MessageSource;
import jakarta.annotation.Nonnull;
import java.util.Locale;

/**
 * Empty {@link MessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
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

    @Nonnull
    @Override
    public Locale getLocale() {
        return getDefaultLocale();
    }

    @Override
    public String getSource() {
        return "Empty";
    }
}

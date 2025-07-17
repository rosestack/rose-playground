package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;

import java.util.Locale;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class EmptyMessageSource implements I18nMessageSource {
    public static final EmptyMessageSource INSTANCE = new EmptyMessageSource();


    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}

package io.github.rose.i18n.spring;

import io.github.rose.i18n.AbstractMessageSource;

import java.util.Locale;

public class PropertySourcesMessageSource extends AbstractMessageSource {

    @Override
    protected String getMessageInternal(String code, Object[] args, Locale locale) {
        return "";
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}

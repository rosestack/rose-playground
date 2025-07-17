package io.github.rose.i18n;

import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Set;

public abstract class AbstractMessageSource implements HierarchicalMessageSource, I18nMessageSource {
    private I18nMessageSource parentMessageSource;
    private Set<Locale> supportedLocales;

    @Override
    public void setParentMessageSource(@Nullable I18nMessageSource parent) {
        this.parentMessageSource = parent;
    }

    @Nullable
    @Override
    public I18nMessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String msg = this.getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        } else {
            msg = getMessageFromParent(code, args, locale);
            if (msg != null) {
                return msg;
            }
            return defaultMessage;
        }
    }

    @Nullable
    protected abstract String getMessageInternal(@Nullable String code, @Nullable Object[] args, @Nullable Locale locale);

    @Nullable
    protected String getMessageFromParent(String code, @Nullable Object[] args, Locale locale) {
        I18nMessageSource parent = this.getParentMessageSource();
        if (parent != null) {
            if (parent instanceof AbstractMessageSource) {
                AbstractMessageSource abstractMessageSource = (AbstractMessageSource) parent;
                return abstractMessageSource.getMessageInternal(code, args, locale);
            } else {
                return parent.getMessage(code, args, (String) null, locale);
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }
        return HierarchicalMessageSource.super.getSupportedLocales();
    }

    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
}

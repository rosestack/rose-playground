package io.github.rose.i18n;


import org.springframework.lang.Nullable;

public interface HierarchicalMessageSource extends I18nMessageSource {
    void setParentMessageSource(@Nullable I18nMessageSource parent);

    @Nullable
    I18nMessageSource getParentMessageSource();
}
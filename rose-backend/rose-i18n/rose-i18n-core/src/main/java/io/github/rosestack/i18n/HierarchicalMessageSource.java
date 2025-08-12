package io.github.rosestack.i18n;

import org.springframework.lang.Nullable;

public interface HierarchicalMessageSource extends I18nMessageSource {
    @Nullable I18nMessageSource getParentMessageSource();

    void setParentMessageSource(@Nullable I18nMessageSource parent);
}

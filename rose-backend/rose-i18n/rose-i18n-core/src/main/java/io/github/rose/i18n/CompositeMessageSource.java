package io.github.rose.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 组合型 I18nMessageSource，可聚合多个消息源，按顺序查找。
 */
public class CompositeMessageSource implements I18nMessageSource {
    private final List<I18nMessageSource> sources = new ArrayList<>();

    public CompositeMessageSource(List<I18nMessageSource> sources) {
        this.sources.addAll(sources);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        for (I18nMessageSource source : sources) {
            String msg = source.getMessage(code, args, defaultMessage, locale);
            if (msg != null && !msg.isEmpty()) {
                return msg;
            }
        }
        return defaultMessage;
    }

    @Override
    public void init() {
        sources.forEach(I18nMessageSource::init);
    }

    @Override
    public void destroy() {
        sources.forEach(I18nMessageSource::destroy);
    }
}

package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spi.EmptyMessageSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile I18nMessageSource i18nMessageSource;

    public static I18nMessageSource i18nMessageSource() {
        if (i18nMessageSource == null) {
            logger.warn("serviceMessageSource is not initialized, EmptyServiceMessageSource will be used");
            return EmptyMessageSource.INSTANCE;
        }
        return i18nMessageSource;
    }

    public static void setI18nMessageSource(I18nMessageSource serviceMessageSource) {
        I18nUtils.i18nMessageSource = serviceMessageSource;
        logger.debug("I18nUtils.serviceMessageSource is initialized : {}", serviceMessageSource);
    }

    public static void destroyMessageSource() {
        i18nMessageSource = null;
        logger.debug("messageSource is destroyed");
    }

    public static String getLocalizedMessage(String messagePattern, Object... args) {
        I18nMessageSource serviceMessageSource = I18nUtils.i18nMessageSource();
        Locale locale = serviceMessageSource.getLocale();
        return serviceMessageSource.getMessage(messagePattern, args, locale);
    }

    public static List<I18nMessageSource> findAllMessageSources(I18nMessageSource serviceMessageSource) {
        List<I18nMessageSource> allServiceMessageSources = new LinkedList<>();
        initMessageSources(serviceMessageSource, allServiceMessageSources);
        return Collections.unmodifiableList(allServiceMessageSources);
    }

    public static void initMessageSources(I18nMessageSource serviceMessageSource,
                                          List<I18nMessageSource> allServiceMessageSources) {
        if (serviceMessageSource instanceof CompositeMessageSource) {
            CompositeMessageSource compositeServiceMessageSource = (CompositeMessageSource) serviceMessageSource;
            for (I18nMessageSource subServiceMessageSource : compositeServiceMessageSource.getMessageSources()) {
                initMessageSources(subServiceMessageSource, allServiceMessageSources);
            }
        } else {
            allServiceMessageSources.add(serviceMessageSource);
        }
    }

    /**
     * 生成 locale 的 fallback 列表，如 zh_CN → [zh_CN, zh, ROOT]
     */
    public static Set<Locale> getFallbackLocales(Locale locale) {
        Set<Locale> fallbacks = new TreeSet<>();
        if (locale == null) {
            fallbacks.add(Locale.ROOT);
            return fallbacks;
        }
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasRegion = StringUtils.isNotBlank(region);
        boolean hasVariant = StringUtils.isNotBlank(variant);

        if (!hasRegion && !hasVariant) {
            return fallbacks;
        }

        if (hasVariant) {
            fallbacks.add(new Locale(language, region));
        }

        if (hasRegion) {
            fallbacks.add(new Locale(language));
        }

        fallbacks.add(Locale.ROOT);
        return fallbacks;
    }
}
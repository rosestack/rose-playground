package io.github.rosestack.i18n.util;

import io.github.rosestack.i18n.CompositeMessageSource;
import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.spi.EmptyMessageSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyList;

public abstract class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile I18nMessageSource i18nMessageSource;

    public static I18nMessageSource i18nMessageSource() {
        if (i18nMessageSource == null) {
            logger.warn("i18nMessageSource is not initialized, EmptyMessageSource will be used");
            return EmptyMessageSource.INSTANCE;
        }
        return i18nMessageSource;
    }

    public static void setI18nMessageSource(I18nMessageSource i18nMessageSource) {
        I18nUtils.i18nMessageSource = i18nMessageSource;
        logger.debug("I18nUtils.i18nMessageSource is initialized : {}", i18nMessageSource);
    }

    public static void destroyMessageSource() {
        i18nMessageSource = null;
        logger.debug("messageSource is destroyed");
    }

    public static List<I18nMessageSource> findAllMessageSources(I18nMessageSource i18nMessageSource) {
        List<I18nMessageSource> alli18nMessageSources = new LinkedList<>();
        initMessageSources(i18nMessageSource, alli18nMessageSources);
        return Collections.unmodifiableList(alli18nMessageSources);
    }

    public static void initMessageSources(I18nMessageSource i18nMessageSource,
                                          List<I18nMessageSource> allMessageSources) {
        if (i18nMessageSource == null || allMessageSources == null) {
            return;
        }

        if (i18nMessageSource instanceof CompositeMessageSource) {
            CompositeMessageSource compositeServiceMessageSource = (CompositeMessageSource) i18nMessageSource;
            List<I18nMessageSource> messageSources = compositeServiceMessageSource.getMessageSources();
            if (messageSources != null) {
                for (I18nMessageSource subServiceMessageSource : messageSources) {
                    initMessageSources(subServiceMessageSource, allMessageSources);
                }
            }
        } else {
            allMessageSources.add(i18nMessageSource);
        }
    }

    public static List<Locale> resolveDerivedLocales(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasRegion = StringUtils.isNotBlank(region);
        boolean hasVariant = StringUtils.isNotBlank(variant);

        if (!hasRegion && !hasVariant) {
            return emptyList();
        }

        List<Locale> derivedLocales = new LinkedList<>();

        if (hasVariant) {
            derivedLocales.add(new Locale(language, region));
        }

        if (hasRegion) {
            derivedLocales.add(new Locale(language));
        }

        return derivedLocales;
    }

    public static Locale resolveLocale(String resource) {
        if (StringUtils.isBlank(resource)) {
            return null;
        }

        String[] localeParts = StringUtils.split(resource, "_");
        if (localeParts.length == 0) {
            return null;
        }

        String language = localeParts[0];
        String region = localeParts.length > 1 ? localeParts[1] : null;
        String variant = localeParts.length > 2 ? localeParts[2] : null;

        return new Locale(language, region, variant);
    }

    /**
     * Generate fallback list for locale, e.g. zh_CN → [zh_CN, zh, ROOT]
     */
    public static Set<Locale> getFallbackLocales(Locale locale) {
        Set<Locale> fallbacks = new LinkedHashSet<>();
        if (locale == null) {
            fallbacks.add(Locale.ROOT);
            return fallbacks;
        }
        fallbacks.add(locale);

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
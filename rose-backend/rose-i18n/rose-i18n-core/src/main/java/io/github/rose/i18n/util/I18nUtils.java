package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeI18nMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spi.EmptyI18nMessageSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashSet;

import static java.util.Collections.unmodifiableList;

/**
 * Internationalization Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class I18nUtils {

    private static volatile I18nMessageSource i18nMessageSource;

    public static I18nMessageSource i18nMessageSource() {
        if (i18nMessageSource == null) {
            return EmptyI18nMessageSource.INSTANCE;
        }
        return i18nMessageSource;
    }

    public static void setI18nMessageSource(I18nMessageSource i18nMessageSource) {
        I18nUtils.i18nMessageSource = i18nMessageSource;
    }

    public static void destroyI18nMessageSource() {
        i18nMessageSource = null;
    }

    public static List<I18nMessageSource> findAllI18nMessageSources(I18nMessageSource i18nMessageSource) {
        List<I18nMessageSource> allI18nMessageSources = new LinkedList<>();
        initI18nMessageSources(i18nMessageSource, allI18nMessageSources);
        return unmodifiableList(allI18nMessageSources);
    }

    public static void initI18nMessageSources(I18nMessageSource serviceMessageSource,
                                              List<I18nMessageSource> allI18nMessageSources) {
        if (serviceMessageSource instanceof CompositeI18nMessageSource) {
            CompositeI18nMessageSource compositeI18nMessageSource = (CompositeI18nMessageSource) serviceMessageSource;
            for (I18nMessageSource subI18nMessageSource : compositeI18nMessageSource.getServiceMessageSources()) {
                initI18nMessageSources(subI18nMessageSource, allI18nMessageSources);
            }
        } else {
            allI18nMessageSources.add(serviceMessageSource);
        }
    }

    /**
     * 解析支持的 Locale 列表，包含派生 Locale
     */
    public static Set<Locale> resolveLocales(Set<Locale> supportedLocales) {
        Set<Locale> resolvedLocales = new LinkedHashSet<>();
        for (Locale supportedLocale : supportedLocales) {
            addLocale(resolvedLocales, supportedLocale);
            for (Locale derivedLocale : resolveDerivedLocales(supportedLocale)) {
                addLocale(resolvedLocales, derivedLocale);
            }
        }
        return Collections.unmodifiableSet(resolvedLocales);
    }

    /**
     * 添加 Locale 到集合，避免重复
     */
    public static void addLocale(Set<Locale> locales, Locale locale) {
        if (!locales.contains(locale)) {
            locales.add(locale);
        }
    }

    /**
     * 获取 Locale 派生列表（如 zh_CN -> zh）
     */
    public static List<Locale> resolveDerivedLocales(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasRegion = region != null && !region.isEmpty();
        boolean hasVariant = variant != null && !variant.isEmpty();

        if (!hasRegion && !hasVariant) {
            return Collections.emptyList();
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
}
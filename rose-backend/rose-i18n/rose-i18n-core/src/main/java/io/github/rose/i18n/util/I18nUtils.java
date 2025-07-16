package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeI18nMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spi.EmptyI18nMessageSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
        return i18nMessageSource != null ? i18nMessageSource : EmptyI18nMessageSource.INSTANCE;
    }

    public static void setI18nMessageSource(I18nMessageSource i18nMessageSource) {
        I18nUtils.i18nMessageSource = i18nMessageSource;
    }

    public static void destroyI18nMessageSource() {
        i18nMessageSource = null;
    }

    public static List<I18nMessageSource> findAllI18nMessageSources(I18nMessageSource rootSource) {
        List<I18nMessageSource> allSources = new LinkedList<>();
        initI18nMessageSources(rootSource, allSources);
        return unmodifiableList(allSources);
    }

    public static void initI18nMessageSources(I18nMessageSource source, List<I18nMessageSource> allSources) {
        if (source instanceof CompositeI18nMessageSource composite) {
            for (I18nMessageSource subSource : composite.getServiceMessageSources()) {
                initI18nMessageSources(subSource, allSources);
            }
            return;
        }
        allSources.add(source);
    }

    /**
     * 解析文件名中的 Locale，例如 i18n_messages_zh_CN.properties -> zh_CN
     */
    public static Locale parseLocaleFromFileName(String fileName) {
        if ("i18n_messages.properties".equals(fileName)) return Locale.getDefault();
        String prefix = "i18n_messages_";
        String suffix = ".properties";
        if (fileName.startsWith(prefix) && fileName.endsWith(suffix)) {
            String localeStr = fileName.substring(prefix.length(), fileName.length() - suffix.length());
            String[] parts = localeStr.split("_");
            return switch (parts.length) {
                case 1 -> new Locale(parts[0]);
                case 2 -> new Locale(parts[0], parts[1]);
                case 3 -> new Locale(parts[0], parts[1], parts[2]);
                default -> null;
            };
        }
        return null;
    }
}
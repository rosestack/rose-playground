package io.github.rose.i18n.util;

import io.github.rose.i18n.CompositeI18nMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spi.EmptyI18nMessageSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Set;
import java.util.LinkedHashSet;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.URL;
import java.nio.charset.Charset;

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
     * 解析文件名中的 Locale，支持多种后缀（如 .properties, .yaml, .yml）
     */
    public static Locale parseLocaleFromFileName(String fileName, String... suffixes) {
        String prefix = "i18n_messages_";
        for (String suffix : suffixes) {
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
        }
        if (("i18n_messages.properties".equals(fileName) || Arrays.stream(suffixes).anyMatch(s -> ("i18n_messages" + s).equals(fileName)))) {
            return Locale.getDefault();
        }
        return null;
    }

    /**
     * 扫描目录下所有指定后缀的文件，发现所有 Locale
     */
    public static Set<Locale> findLocalesInDirectory(java.io.File dir, String... suffixes) {
        Set<Locale> locales = new LinkedHashSet<>();
        java.io.File[] files = dir.listFiles((d, name) -> Arrays.stream(suffixes).anyMatch(name::endsWith));
        if (files != null) {
            for (java.io.File file : files) {
                Locale locale = parseLocaleFromFileName(file.getName(), suffixes);
                if (locale != null) locales.add(locale);
            }
        }
        return locales;
    }

    /**
     * 扫描 jar 包下所有指定后缀的文件，发现所有 Locale
     */
    public static Set<Locale> findLocalesInJar(java.util.jar.JarFile jar, String basePath, String... suffixes) {
        Set<Locale> locales = new LinkedHashSet<>();
        java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(basePath) && Arrays.stream(suffixes).anyMatch(name::endsWith)) {
                String fileName = name.substring(name.lastIndexOf('/') + 1);
                Locale locale = parseLocaleFromFileName(fileName, suffixes);
                if (locale != null) locales.add(locale);
            }
        }
        return locales;
    }

    /**
     * 按后缀批量加载资源 Reader
     */
    public static List<Reader> loadResources(String basePath, String resourceBase, String[] suffixes, Charset encoding) throws IOException {
        List<Reader> readers = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String suffix : suffixes) {
            String fullResourcePath = basePath + resourceBase + suffix;
            Enumeration<URL> resources = classLoader.getResources(fullResourcePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                readers.add(new InputStreamReader(url.openStream(), encoding));
            }
        }
        return readers;
    }

    /**
     * 按 Locale 生成资源文件名（如 i18n_messages_zh_CN.yaml）
     */
    public static String getResourceNameByLocale(Locale locale, String suffix) {
        StringBuilder base = new StringBuilder("i18n_messages_").append(locale.getLanguage().toLowerCase());
        if (!locale.getCountry().isEmpty()) base.append("_").append(locale.getCountry().toUpperCase());
        if (!locale.getVariant().isEmpty()) base.append("_").append(locale.getVariant());
        return base + suffix;
    }
}
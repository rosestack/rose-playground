package io.github.rose.i18n.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.JarFile;

public class I18nResourceUtils {
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

    public static Locale parseLocale(String fileName) {
        String localeStr = fileName.substring(0, fileName.lastIndexOf("."));
        if (localeStr == null || localeStr.isEmpty()) {
            return Locale.ROOT;
        }
        if (localeStr.startsWith("_")) {
            localeStr = localeStr.substring(1);
        }
        String[] parts = localeStr.split("_");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        }
        if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        }
        if (parts.length == 3) {
            return new Locale(parts[0], parts[1], parts[2]);
        }
        return null;
    }
} 
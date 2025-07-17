package io.github.rose.i18n.util;

import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.JarFile;

public class I18nResourceUtils {
    public static final String FILE_PROTOCOL = "file";
    public static final String JAR_PROTOCOL = "jar";

    public static boolean isSupportedResource(String fileName, String basename, List<String> extensions) {
        if (!fileName.startsWith(basename)) {
            return false;
        }
        for (String ext : extensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static void loadResourceMessages(String resourceDir, String basename, List<String> extensions, BiConsumer<String, InputStream> consumer) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> urls = classLoader.getResources(resourceDir);
            while (urls.hasMoreElements()) {
                URL dirUrl = urls.nextElement();
                String protocol = dirUrl.getProtocol();
                if (FILE_PROTOCOL.equals(protocol)) {
                    File dir = new File(dirUrl.toURI());
                    if (dir.exists() && dir.isDirectory()) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                String fileName = file.getName();
                                if (I18nResourceUtils.isSupportedResource(fileName, basename, extensions)) {
                                    try (InputStream in = new FileInputStream(file)) {
                                        consumer.accept(fileName, in);
                                    }
                                }
                            }
                        }
                    }
                } else if (JAR_PROTOCOL.equals(protocol)) {
                    String path = dirUrl.getPath();
                    String jarPath = path.substring(5, path.indexOf("!"));
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            java.util.jar.JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();
                            if (entryName.startsWith(resourceDir) && !entry.isDirectory()) {
                                String fileName = entryName.substring(resourceDir.length());
                                if (I18nResourceUtils.isSupportedResource(fileName, basename, extensions)) {
                                    try (InputStream in = jarFile.getInputStream(entry)) {
                                        consumer.accept(fileName, in);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 可选：日志记录
        }
    }

    public static String getExtension(String fileName, List<String> extensions) {
        for (String ext : extensions) {
            if (fileName.endsWith(ext)) {
                return ext;
            }
        }
        return null;
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
        fallbacks.add(locale);
        if (!"".equals(locale.getLanguage())) {
            if (!"".equals(locale.getCountry())) {
                fallbacks.add(new Locale(locale.getLanguage()));
            }
            fallbacks.add(Locale.ROOT);
        }
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
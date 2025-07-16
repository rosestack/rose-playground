package io.github.rose.i18n.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.rose.i18n.AbstractResourceMessageSource.DEFAULT_RESOURCE_NAME_PREFIX;

/**
 * Internationalization Utilities class
 * <p>
 * 提供 Locale 解析、资源扫描、资源加载等通用工具方法。
 * 支持 properties/yaml/yml 等多格式，便于扩展。
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class I18nUtils {

    private I18nUtils() {}

    /**
     * 解析文件名中的 Locale，支持多种后缀（如 .properties, .yaml, .yml）
     * @param fileName 文件名
     * @param suffixes 支持的后缀
     * @return 解析出的 Locale，未识别返回默认 Locale
     */
    public static Locale parseLocaleFromFileName(String fileName, String... suffixes) {
        String localeStr = extractLocaleString(fileName, DEFAULT_RESOURCE_NAME_PREFIX, suffixes);
        return parseLocale(localeStr);
    }

    /**
     * 提取文件名中的 locale 字符串部分
     */
    private static String extractLocaleString(String fileName, String prefix, String... suffixes) {
        for (String suffix : suffixes) {
            if (fileName.startsWith(prefix) && fileName.endsWith(suffix)) {
                return fileName.substring(prefix.length(), fileName.length() - suffix.length());
            }
        }
        if ((DEFAULT_RESOURCE_NAME_PREFIX + "properties").equals(fileName) || Arrays.stream(suffixes).anyMatch(s -> (DEFAULT_RESOURCE_NAME_PREFIX.substring(0, DEFAULT_RESOURCE_NAME_PREFIX.length() - 1) + s).equals(fileName))) {
            return ""; // 默认 locale
        }
        return null;
    }

    /**
     * 将字符串解析为 Locale
     */
    private static Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) return Locale.getDefault();
        String[] parts = localeStr.split("_");
        return switch (parts.length) {
            case 1 -> new Locale(parts[0]);
            case 2 -> new Locale(parts[0], parts[1]);
            case 3 -> new Locale(parts[0], parts[1], parts[2]);
            default -> Locale.getDefault();
        };
    }

    /**
     * 扫描目录或 jar 包下所有指定后缀的文件，发现所有 Locale
     * @param root 目录或 jar 文件
     * @param basePath jar 包下的基础路径（目录扫描可为 null）
     * @param suffixes 支持的后缀
     * @return 发现的 Locale 集合
     */
    public static Set<Locale> findLocales(Object root, String basePath, String... suffixes) {
        if (root instanceof File dir) {
            return findLocalesInDirectory(dir, suffixes);
        } else if (root instanceof JarFile jar) {
            return findLocalesInJar(jar, basePath, suffixes);
        }
        return Set.of();
    }

    /**
     * 扫描目录下所有指定后缀的文件，发现所有 Locale
     */
    public static Set<Locale> findLocalesInDirectory(File dir, String... suffixes) {
        Set<Locale> locales = new LinkedHashSet<>();
        File[] files = dir.listFiles((d, name) -> Arrays.stream(suffixes).anyMatch(name::endsWith));
        if (files != null) {
            for (File file : files) {
                Locale locale = parseLocaleFromFileName(file.getName(), suffixes);
                if (locale != null) locales.add(locale);
            }
        }
        return locales;
    }

    /**
     * 扫描 jar 包下所有指定后缀的文件，发现所有 Locale
     */
    public static Set<Locale> findLocalesInJar(JarFile jar, String basePath, String... suffixes) {
        Set<Locale> locales = new LinkedHashSet<>();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
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
     * 按后缀批量加载资源 Reader，支持自定义 ClassLoader
     * @param basePath 资源基础路径
     * @param resourceBase 资源名（不含后缀）
     * @param suffixes 支持的后缀
     * @param encoding 字符集
     * @param classLoader 类加载器
     * @return Reader 列表
     * @throws IOException 读取异常
     */
    public static List<Reader> loadResources(String basePath, String resourceBase, String[] suffixes, Charset encoding, ClassLoader classLoader) throws IOException {
        List<Reader> readers = new ArrayList<>();
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
     * 按后缀批量加载资源 Reader，使用当前线程的 ClassLoader
     */
    public static List<Reader> loadResources(String basePath, String resourceBase, String[] suffixes, Charset encoding) throws IOException {
        return loadResources(basePath, resourceBase, suffixes, encoding, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 按 Locale 生成资源文件名（如 i18n_messages_zh_CN.yaml），支持自定义前缀
     * @param locale 区域
     * @param suffix 后缀
     * @param prefix 前缀
     * @return 文件名
     */
    public static String getResourceNameByLocale(Locale locale, String suffix, String prefix) {
        StringBuilder base = new StringBuilder(prefix).append(locale.getLanguage().toLowerCase());
        if (!locale.getCountry().isEmpty()) base.append("_").append(locale.getCountry().toUpperCase());
        if (!locale.getVariant().isEmpty()) base.append("_").append(locale.getVariant());
        return base + suffix;
    }

    /**
     * 按 Locale 生成资源文件名（如 i18n_messages_zh_CN.yaml），使用默认前缀
     */
    public static String getResourceNameByLocale(Locale locale, String suffix) {
        return getResourceNameByLocale(locale, suffix, DEFAULT_RESOURCE_NAME_PREFIX);
    }
}
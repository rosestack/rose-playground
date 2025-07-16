package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.util.I18nUtils;
import io.github.rose.i18n.I18nMessageException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ClassPathPropertiesResourceI18nMessageSource
 * <p>
 * 自动扫描 classpath 下 META-INF/i18n/{source}/ 目录的 properties 文件，
 * 支持 file 和 jar 协议，自动发现所有支持的 Locale。
 * 结果做懒加载缓存，提升性能。
 * 发现异常时抛出 I18nMessageException。
 * Locale 解析逻辑见 I18nUtils.parseLocaleFromFileName。
 * </p>
 */
public class ClassPathPropertiesResourceI18nMessageSource extends AbstractPropertiesResourceI18nMessageSource {

    /**
     * 缓存已发现的 Locale，避免重复扫描
     */
    private final AtomicReference<Set<Locale>> cachedLocales = new AtomicReference<>();

    public ClassPathPropertiesResourceI18nMessageSource(String source) {
        super(source);
    }

    @Override
    protected List<Reader> loadPropertiesResources(String resource) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String fullResourcePath = getBasePath() + resource;
        Enumeration<URL> resources = classLoader.getResources(fullResourcePath);
        List<Reader> propertiesResources = new LinkedList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            propertiesResources.add(new InputStreamReader(url.openStream(), getEncoding()));
        }
        return propertiesResources;
    }

    /**
     * 自动发现支持的 Locale，支持 file 和 jar 协议，结果做懒加载缓存。
     *
     * @return 支持的 Locale 集合，若未发现则返回默认 Locale
     * @throws I18nMessageException 发现异常时抛出
     */
    @Override
    public Set<Locale> getSupportedLocales() {
        Set<Locale> locales = cachedLocales.get();
        if (locales != null) {
            return locales;
        }
        Set<Locale> discovered = new LinkedHashSet<>();
        String basePath = getBasePath();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(basePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    java.io.File dir = new java.io.File(url.toURI());
                    java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".properties"));
                    if (files != null) {
                        for (java.io.File file : files) {
                            Locale locale = I18nUtils.parseLocaleFromFileName(file.getName());
                            if (locale != null) discovered.add(locale);
                        }
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    String jarPath = url.getPath();
                    String[] parts = jarPath.split("!/");
                    if (parts.length == 2) {
                        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(parts[0].replaceFirst("^file:/+", "/"))) {
                            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                            while (entries.hasMoreElements()) {
                                java.util.jar.JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.startsWith(basePath) && name.endsWith(".properties")) {
                                    String fileName = name.substring(name.lastIndexOf('/') + 1);
                                    Locale locale = I18nUtils.parseLocaleFromFileName(fileName);
                                    if (locale != null) discovered.add(locale);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new I18nMessageException("Failed to discover supported locales in " + basePath, e);
        }
        if (discovered.isEmpty()) {
            discovered.add(Locale.getDefault());
        }
        cachedLocales.set(discovered);
        return discovered;
    }

    /**
     * 获取资源基础路径，如 META-INF/i18n/{source}/
     *
     * @return 资源基础路径
     */
    protected String getBasePath() {
        return String.format(RESOURCE_PATH_PATTERN, getSource());
    }
}
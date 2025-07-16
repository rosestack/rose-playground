package io.github.rose.i18n.spi;

import io.github.rose.i18n.AbstractResourceI18nMessageSource;
import io.github.rose.i18n.I18nMessageException;
import io.github.rose.i18n.util.I18nUtils;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象 classpath 资源型国际化消息源，封装 Locale 自动发现、资源加载、基础路径等通用逻辑。
 * 子类只需实现 getResourceSuffixes() 和 loadMessages(String resource)。
 */
public abstract class AbstractClassPathResourceI18nMessageSource extends AbstractResourceI18nMessageSource {
    private final AtomicReference<Set<Locale>> cachedLocales = new AtomicReference<>();

    public AbstractClassPathResourceI18nMessageSource(String source) {
        super(source);
    }

    /**
     * 子类需提供支持的资源后缀（如 .properties/.yaml/.yml）
     */
    protected abstract String[] getResourceSuffixes();

    /**
     * 获取 classpath 下资源基础路径
     */
    protected String getBasePath() {
        return String.format(RESOURCE_PATH_PATTERN, getSource());
    }

    /**
     * 按后缀批量加载资源 Reader
     */
    protected List<Reader> loadResourceReaders(String resource) throws IOException {
        return I18nUtils.loadResources(getBasePath(), resource, getResourceSuffixes(), getEncoding());
    }

    /**
     * 统一 Locale 自动发现逻辑
     */
    @Override
    public Set<Locale> getSupportedLocales() {
        Set<Locale> cached = cachedLocales.get();
        if (cached != null) return cached;
        Set<Locale> discovered = new LinkedHashSet<>();
        String basePath = getBasePath();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(basePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    java.io.File dir = new java.io.File(url.toURI());
                    discovered.addAll(I18nUtils.findLocalesInDirectory(dir, getResourceSuffixes()));
                } else if ("jar".equals(url.getProtocol())) {
                    String jarPath = url.getPath();
                    String[] parts = jarPath.split("!/");
                    if (parts.length == 2) {
                        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(parts[0].replaceFirst("^file:/+", "/"))) {
                            discovered.addAll(I18nUtils.findLocalesInJar(jar, basePath, getResourceSuffixes()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new I18nMessageException("Failed to discover supported locales in " + basePath, e);
        }
        if (discovered.isEmpty()) discovered.add(Locale.getDefault());
        cachedLocales.set(discovered);
        return discovered;
    }

    /**
     * 统一资源命名逻辑，取第一个后缀为主
     */
    @Override
    protected String getResourceName(Locale locale) {
        return I18nUtils.getResourceNameByLocale(locale, getResourceSuffixes()[0]);
    }
} 
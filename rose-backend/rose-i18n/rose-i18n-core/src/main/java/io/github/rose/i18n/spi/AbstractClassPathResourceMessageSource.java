package io.github.rose.i18n.spi;

import io.github.rose.i18n.AbstractResourceMessageSource;
import io.github.rose.i18n.MessageException;
import io.github.rose.i18n.util.I18nUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;

/**
 * Abstract classpath resource-based message source.
 * <p>
 * Encapsulates locale auto-discovery, resource loading, and base path logic for classpath resources.
 * Subclasses only need to implement getResourceSuffixes() and loadMessages(String resource).
 */
public abstract class AbstractClassPathResourceMessageSource extends AbstractResourceMessageSource {
    protected static final String FILE_PROTOCOL = "file";
    protected static final String JAR_PROTOCOL = "jar";
    private final AtomicReference<Set<Locale>> cachedLocales = new AtomicReference<>();

    public AbstractClassPathResourceMessageSource(String source) {
        super(source);
    }

    /**
     * Subclasses need to provide supported resource suffixes (e.g., .properties/.yaml/.yml).
     */
    protected abstract String[] getResourceSuffixes();

    /**
     * Get the base path for classpath resources.
     */
    protected String getBasePath() {
        return String.format(RESOURCE_PATH_PATTERN, getSource());
    }

    /**
     * Batch load resources as Readers by suffix.
     */
    protected List<Reader> loadResourceReaders(String resourceName) throws IOException {
        return I18nUtils.loadResources(getBasePath(), resourceName, getResourceSuffixes(), getEncoding());
    }

    /**
     * Unified locale auto-discovery logic.
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
                if (FILE_PROTOCOL.equals(url.getProtocol())) {
                    File dir = new File(url.toURI());
                    discovered.addAll(I18nUtils.findLocalesInDirectory(dir, getResourceSuffixes()));
                } else if (JAR_PROTOCOL.equals(url.getProtocol())) {
                    String jarPath = url.getPath();
                    String[] parts = jarPath.split("!/");
                    if (parts.length == 2) {
                        try (JarFile jar = new JarFile(parts[0].replaceFirst("^file:/+", "/"))) {
                            discovered.addAll(I18nUtils.findLocalesInJar(jar, basePath, getResourceSuffixes()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new MessageException("Failed to discover supported locales in " + basePath, e);
        }
        if (discovered.isEmpty()) discovered.add(Locale.getDefault());
        cachedLocales.set(discovered);
        return discovered;
    }

    /**
     * Unified resource naming logic, taking the first suffix as the primary.
     */
    @Override
    protected String getResourceName(Locale locale) {
        if (locale == null) {
            return DEFAULT_RESOURCE_NAME_PREFIX.substring(0, DEFAULT_RESOURCE_NAME_PREFIX.length() - 1);
        }
        return I18nUtils.getResourceNameByLocale(locale);
    }
} 
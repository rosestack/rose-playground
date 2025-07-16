package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.util.I18nUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;

/**
 * Default {@link I18nMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ClassPathPropertiesResourceI18nMessageSource extends AbstractPropertiesResourceI18nMessageSource {
    protected static final String RESOURCE_PATH_PATTERN = "META-INF/i18n/%s/";

    public ClassPathPropertiesResourceI18nMessageSource(String source) {
        super(source);
    }

    @Override
    protected List<Reader> loadPropertiesResources(String resource) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(resource);
        List<Reader> propertiesResources = new LinkedList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            propertiesResources.add(new InputStreamReader(url.openStream(), getEncoding()));
        }
        return propertiesResources;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        Set<Locale> locales = new LinkedHashSet<>();
        String basePath = getBastPath();
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(basePath);
            while (resources.hasMoreElements()) {
                URL dirUrl = resources.nextElement();
                // 只处理 file 协议
                if ("file".equals(dirUrl.getProtocol())) {
                    java.io.File dir = new java.io.File(dirUrl.toURI());
                    java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".properties"));
                    if (files != null) {
                        for (java.io.File file : files) {
                            String fileName = file.getName();
                            // 解析 Locale
                            Locale locale = I18nUtils.parseLocaleFromFileName(fileName);
                            if (locale != null) {
                                locales.add(locale);
                            }
                        }
                    }
                }
                // 可扩展：支持 jar 包内资源
            }
        } catch (Exception e) {
            // ignore or log
        }
        return locales.isEmpty() ? Collections.singleton(Locale.getDefault()) : locales;
    }

    private String getBastPath() {
        return String.format(RESOURCE_PATH_PATTERN, getSource());
    }
}
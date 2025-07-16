package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageSource;

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

    /**
     * Resource path pattern
     */
    protected static final String RESOURCE_LOCATION_PATTERN = "META-INF/i18n/{}/{}";

    public ClassPathPropertiesResourceI18nMessageSource(String source) {
        super(source);
    }

    protected String getResource(String resourceName) {
        return String.format(RESOURCE_LOCATION_PATTERN.replace("{}", "%s"), getSource(), resourceName);
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
}
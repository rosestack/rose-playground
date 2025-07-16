package io.github.rose.i18n.impl;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.MessageSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;


/**
 * Default {@link MessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ClassPathPropertiesMessageSource extends AbstractPropertiesResourceMessageSource {

    /**
     * Resource path pattern
     */
    protected static final String RESOURCE_LOCATION_PATTERN = "META-INF/i18n/{}/{}";

    public ClassPathPropertiesMessageSource(String source) {
        super(source);
    }

    protected String getResource(String resourceName) {
        return FormatUtils.format(RESOURCE_LOCATION_PATTERN, getSource(), resourceName);
    }

    @Override
    protected List<Reader> loadAllPropertiesResources(String resource) throws IOException {
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

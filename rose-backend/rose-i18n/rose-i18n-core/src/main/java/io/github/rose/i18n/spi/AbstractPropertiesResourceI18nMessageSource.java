package io.github.rose.i18n.spi;


import io.github.rose.i18n.AbstractResourceI18nMessageSource;
import io.github.rose.i18n.I18nMessageSource;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * {@link Properties} Resource {@link I18nMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractPropertiesResourceI18nMessageSource extends AbstractResourceI18nMessageSource {
    public static final String DEFAULT_RESOURCE_NAME_SUFFIX = ".properties";

    public AbstractPropertiesResourceI18nMessageSource(String source) {
        super(source);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = null;
        try {
            Properties properties = loadProperties(resource);
            if (properties != null && !properties.isEmpty()) {
                messages = new HashMap<>(properties.size());
                messages.putAll((Map) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException("Source '" + source + "' Messages Properties Resource[name : " + resource + "] loading is failed", e);
        }
        return messages == null ? emptyMap() : unmodifiableMap(messages);
    }

    @Override
    protected String getResourceName(Locale locale) {
        StringBuilder sb = new StringBuilder(DEFAULT_RESOURCE_NAME_PREFIX);
        sb.append(locale.getLanguage().toLowerCase());
        if (!locale.getCountry().isEmpty()) {
            sb.append("_").append(locale.getCountry().toUpperCase());
        }
        if (!locale.getVariant().isEmpty()) {
            sb.append("_").append(locale.getVariant());
        }
        sb.append(DEFAULT_RESOURCE_NAME_SUFFIX);
        return sb.toString();
    }

    protected Properties loadProperties(String resource) throws IOException {
        List<Reader> propertiesResources = loadPropertiesResources(resource);
        if (propertiesResources == null || propertiesResources.isEmpty()) {
            return null;
        }
        Properties properties = new Properties();
        for (Reader propertiesResource : propertiesResources) {
            try (Reader reader = propertiesResource) {
                properties.load(reader);
            }
        }

        return properties;
    }

    protected abstract List<Reader> loadPropertiesResources(String resource) throws IOException;
}
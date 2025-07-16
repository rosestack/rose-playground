package io.github.rose.i18n.spi;


import io.github.rose.i18n.AbstractResourceI18nMessageSource;
import io.github.rose.i18n.I18nMessageException;
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
        try {
            Properties properties = loadProperties(resource);
            if (properties == null || properties.isEmpty()) return emptyMap();
            Map<String, String> messages = new HashMap<>(properties.size());
            messages.putAll((Map) properties);
            return unmodifiableMap(messages);
        } catch (IOException e) {
            throw new I18nMessageException("Source '" + source + "' Messages Properties Resource[name : " + resource + "] loading is failed", e);
        }
    }

    @Override
    protected String getResourceName(Locale locale) {
        // e.g. i18n_messages_zh_CN.properties
        return String.format("%s%s%s%s%s", DEFAULT_RESOURCE_NAME_PREFIX,
                locale.getLanguage().toLowerCase(),
                locale.getCountry().isEmpty() ? "" : "_" + locale.getCountry().toUpperCase(),
                locale.getVariant().isEmpty() ? "" : "_" + locale.getVariant(),
                DEFAULT_RESOURCE_NAME_SUFFIX);
    }

    protected Properties loadProperties(String resource) throws IOException {
        List<Reader> propertiesResources = loadPropertiesResources(resource);
        if (propertiesResources == null || propertiesResources.isEmpty()) {
            return new Properties();
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
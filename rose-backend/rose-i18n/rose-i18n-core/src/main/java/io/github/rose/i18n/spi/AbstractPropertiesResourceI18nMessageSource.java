package io.github.rose.i18n.spi;


import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.AbstractResourceI18nMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * {@link Properties} Resource {@link I18nMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractPropertiesResourceI18nMessageSource extends AbstractResourceI18nMessageSource {

    public AbstractPropertiesResourceI18nMessageSource(String source) {
        super(source);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = null;
        try {
            Properties properties = loadAllProperties(resource);
            if (!MapUtils.isEmpty(properties)) {
                messages = new HashMap<>(properties.size());
                messages.putAll((Map) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(FormatUtils.format("Source '{}' Messages Properties Resource[name : {}] loading is failed", source, resource), e);
        }
        return messages == null ? emptyMap() : unmodifiableMap(messages);
    }

    public Properties loadAllProperties(Locale locale) throws IOException {
        String resource = getResource(locale);
        return loadAllProperties(resource);
    }

    public Properties loadAllProperties(String resource) throws IOException {
        List<Reader> propertiesResources = loadAllPropertiesResources(resource);
        log.debug("Source '{}' loads {} Properties Resources['{}']", source, propertiesResources.size(), resource);
        if (CollectionUtils.isEmpty(propertiesResources)) {
            return null;
        }
        Properties properties = new Properties();
        for (Reader propertiesResource : propertiesResources) {
            try (Reader reader = propertiesResource) {
                properties.load(reader);
            }
        }

        log.debug("Source '{}' loads all Properties Resources[name :{}] : {}", source, resource, properties);
        return properties;
    }

    protected abstract String getResource(String resourceName);

    protected abstract List<Reader> loadAllPropertiesResources(String resource) throws IOException;
}
package io.github.rosestack.i18n.spi;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import io.github.rosestack.core.util.FormatUtils;
import io.github.rosestack.i18n.AbstractResourceMessageSource;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public abstract class AbstractPropertiesResourceMessageSource extends AbstractResourceMessageSource {
    public AbstractPropertiesResourceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String getResourceSuffix() {
        return ".properties";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = null;
        try {
            Properties properties = loadAllProperties(resource);
            if (!ObjectUtils.isEmpty(properties)) {
                messages = new HashMap<>(properties.size());
                messages.putAll((Map) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    FormatUtils.replacePlaceholders(
                            "Source '{}' Messages Properties Resource[name : {}] loading is failed", source, resource),
                    e);
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
        if (ObjectUtils.isEmpty(propertiesResources)) {
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

    protected abstract List<Reader> loadAllPropertiesResources(String resource) throws IOException;
}

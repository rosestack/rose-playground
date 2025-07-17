package io.github.rose.i18n.spi;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.AbstractResourceMessageSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

@Slf4j
public abstract class PropertiesResourceMessageSource extends AbstractResourceMessageSource {
    public PropertiesResourceMessageSource(String source) {
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

    protected abstract List<Reader> loadAllPropertiesResources(String resource) throws IOException;
}

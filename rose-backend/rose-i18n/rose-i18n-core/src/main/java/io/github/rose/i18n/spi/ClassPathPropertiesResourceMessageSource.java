package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageException;

import java.io.Reader;
import java.util.*;

public class ClassPathPropertiesResourceMessageSource extends AbstractClassPathResourceMessageSource {
    private static final String[] PROPERTIES_SUFFIX = {".properties"};

    public ClassPathPropertiesResourceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String[] getResourceSuffixes() {
        return PROPERTIES_SUFFIX;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Map<String, String> loadMessages(String resource) {
        try {
            Properties properties = new Properties();
            List<Reader> readers = loadResourceReaders(resource);
            for (Reader reader : readers) {
                try (reader) {
                    properties.load(reader);
                }
            }
            if (properties.isEmpty()) return Collections.emptyMap();
            Map<String, String> messages = new HashMap<>(properties.size());
            messages.putAll((Map) properties);
            return Collections.unmodifiableMap(messages);
        } catch (Exception e) {
            throw new MessageException("Source '" + getSource() + "' Messages Properties Resource[name : " + resource + "] loading is failed", e);
        }
    }
}
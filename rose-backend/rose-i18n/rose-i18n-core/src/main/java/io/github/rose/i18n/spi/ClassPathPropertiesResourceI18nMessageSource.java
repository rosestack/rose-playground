package io.github.rose.i18n.spi;

import io.github.rose.i18n.I18nMessageException;
import io.github.rose.i18n.util.I18nUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ClassPathPropertiesResourceI18nMessageSource extends AbstractClassPathResourceI18nMessageSource {
    private static final String[] PROPERTIES_SUFFIX = {".properties"};

    public ClassPathPropertiesResourceI18nMessageSource(String source) {
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
            throw new io.github.rose.i18n.I18nMessageException("Source '" + getSource() + "' Messages Properties Resource[name : " + resource + "] loading is failed", e);
        }
    }
}
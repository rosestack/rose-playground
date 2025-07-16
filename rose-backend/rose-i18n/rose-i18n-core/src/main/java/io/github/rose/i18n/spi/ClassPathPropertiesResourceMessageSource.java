package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageException;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Loads i18n messages from properties files under META-INF/i18n/{source}/ in the classpath.
 * Supports both file and jar protocols, and automatically discovers all supported Locales.
 */
public class ClassPathPropertiesResourceMessageSource extends AbstractClassPathResourceMessageSource {
    private static final String[] PROPERTIES_SUFFIXES = {".properties"};

    public ClassPathPropertiesResourceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String[] getResourceSuffixes() {
        return PROPERTIES_SUFFIXES;
    }

    @Override
    protected Map<String, String> loadMessages(String resourceName) {
        Map<String, String> messages = new HashMap<>();
        try {
            List<Reader> readers = loadResourceReaders(resourceName);
            for (Reader reader : readers) {
                try (reader) {
                    Properties props = new Properties();
                    props.load(reader);
                    for (String key : props.stringPropertyNames()) {
                        messages.put(key, props.getProperty(key));
                    }
                }
            }
        } catch (Exception e) {
            throw new MessageException("Source '" + getSource() + "' Messages Properties Resource[" + resourceName + "] loading is failed", e);
        }
        return messages;
    }
}
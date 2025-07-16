package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * Loads i18n messages from yaml/yml files under META-INF/i18n/{source}/ in the classpath.
 * Supports both file and jar protocols, and automatically discovers all supported Locales.
 */
public class ClassPathYamlResourceMessageSource extends AbstractClassPathResourceMessageSource {
    private static final String[] YAML_SUFFIXES = {".yaml", ".yml"};
    private static final Yaml YAML = new Yaml();

    public ClassPathYamlResourceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String[] getResourceSuffixes() {
        return YAML_SUFFIXES;
    }

    @Override
    protected Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = new HashMap<>();
        try {
            List<Reader> readers = loadResourceReaders(resource);
            for (Reader reader : readers) {
                try (reader) {
                    Object data = YAML.load(reader);
                    if (data instanceof Map<?, ?> map) {
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            if (entry.getKey() != null && entry.getValue() != null) {
                                messages.put(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new MessageException("Failed to load yaml resource: " + resource, e);
        }
        return messages;
    }
} 
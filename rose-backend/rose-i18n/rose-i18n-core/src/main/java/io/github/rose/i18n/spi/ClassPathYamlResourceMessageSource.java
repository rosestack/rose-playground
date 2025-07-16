package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageException;
import io.github.rose.i18n.util.I18nUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Loads i18n messages from yaml/yml files under META-INF/i18n/{source}/ in the classpath.
 * Supports both .yaml and .yml suffixes, file and jar protocols, and automatically discovers all supported Locales.
 * Supports nested keys by flattening nested objects into dot-separated keys (e.g., user.login).
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
            List<Reader> readers = I18nUtils.loadResources(getBasePath(), resource, getResourceSuffixes(), getEncoding());
            for (Reader reader : readers) {
                Object loaded = YAML.load(reader);
                if (loaded instanceof Map) {
                    I18nUtils.flattenMap((Map<String, Object>) loaded, "", messages);
                }
                reader.close();
            }
        } catch (Exception e) {
            throw new MessageException("Failed to load yaml resource: " + resource, e);
        }
        return messages;
    }
} 
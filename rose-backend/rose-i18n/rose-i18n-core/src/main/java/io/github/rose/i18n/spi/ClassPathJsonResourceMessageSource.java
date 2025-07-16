package io.github.rose.i18n.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rose.i18n.MessageException;
import io.github.rose.i18n.util.I18nUtils;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads i18n messages from JSON files under META-INF/i18n/{source}/ in the classpath.
 * Supports both file and jar protocols, and automatically discovers all supported Locales.
 * Supports nested keys by flattening nested objects into dot-separated keys (e.g., user.login).
 */
public class ClassPathJsonResourceMessageSource extends AbstractClassPathResourceMessageSource {
    private static final String[] JSON_SUFFIXES = {".json"};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ClassPathJsonResourceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String[] getResourceSuffixes() {
        return JSON_SUFFIXES;
    }

    @Override
    protected String getBasePath() {
        return "META-INF/i18n/" + source + "/";
    }

    @Override
    protected Map<String, String> loadMessages(String resourceName) {
        Map<String, String> messages = new HashMap<>();
        try {
            List<Reader> readers = I18nUtils.loadResources(getBasePath(), resourceName, getResourceSuffixes(), getEncoding());
            for (Reader reader : readers) {
                Map<String, Object> map = OBJECT_MAPPER.readValue(reader, Map.class);
                I18nUtils.flattenMap(map, "", messages);
                reader.close();
            }
        } catch (Exception e) {
            throw new MessageException("Failed to load json resource: " + resourceName, e);
        }
        return messages;
    }
} 
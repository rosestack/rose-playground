package io.github.rose.i18n.spi;

import io.github.rose.i18n.AbstractResourceMessageSource;

import java.io.Reader;
import java.util.*;

public class PropertiesMessageSource extends AbstractResourceMessageSource {
    public PropertiesMessageSource(String source) {
        super(source);
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return Collections.singletonList(".properties");
    }

    @Override
    protected Map<String, String> doLoadMessages(Reader reader) {
        Properties properties = new Properties();
        try {
            properties.load(reader);
            Map<String, String> messages = new HashMap<>();
            for (String key : properties.stringPropertyNames()) {
                messages.put(key, properties.getProperty(key));
            }
            return messages;
        } catch (Exception e) {
            // 可选：日志记录
            return Collections.emptyMap();
        }
    }
}
package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageSourceException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 从 classpath 下 META-INF/i18n/{source}/ 目录读取 properties 文件的国际化消息加载实现。
 * 支持 file 和 jar 协议，自动发现所有支持的 Locale。
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
    protected Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = new HashMap<>();
        try {
            List<Reader> readers = loadResourceReaders(resource);
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
            throw new MessageSourceException("Source '" + getSource() + "' Messages Properties Resource[name : " + resource + "] loading is failed", e);
        }
        return messages;
    }
}
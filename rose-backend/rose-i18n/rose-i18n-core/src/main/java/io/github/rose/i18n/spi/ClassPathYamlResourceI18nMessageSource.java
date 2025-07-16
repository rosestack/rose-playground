package io.github.rose.i18n.spi;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从 classpath 下 META-INF/i18n/{source}/ 目录读取 yaml/yml 文件的国际化消息加载实现。
 * 支持 file 和 jar 协议，自动发现所有支持的 Locale。
 */
public class ClassPathYamlResourceI18nMessageSource extends AbstractClassPathResourceI18nMessageSource {
    private static final String[] YAML_SUFFIXES = {".yaml", ".yml"};
    private final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();

    public ClassPathYamlResourceI18nMessageSource(String source) {
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
                    Object data = yaml.load(reader);
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
            throw new io.github.rose.i18n.I18nMessageException("Failed to load yaml resource: " + resource, e);
        }
        return messages;
    }
} 
package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageSourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathYamlResourceMessageSourceTest {
    private static final String[] PREFIXES = {"test", "foo"};
    private static final Locale[] LOCALES = {Locale.ENGLISH, Locale.getDefault()};

    private ClassPathYamlResourceMessageSource messageSource;

    @BeforeEach
    void setUp() {
        MessageSourceManager.destroy();
        messageSource = new ClassPathYamlResourceMessageSource("yaml");
        messageSource.init();
    }

    @AfterEach
    void tearDown() {
        if (messageSource != null) {
            messageSource.destroy();
            messageSource = null;
        }
        MessageSourceManager.destroy();
    }

    @Test
    void testAllPrefixes() {
        for (Locale locale : LOCALES) {
            boolean isZh = Locale.getDefault().equals(locale);
            for (String prefix : PREFIXES) {
                assertEquals(isZh ? "你好" : "Hello", messageSource.getMessage(prefix + ".message", locale));
                assertEquals(isZh ? "你好, " + prefix : "Hello, " + prefix, messageSource.getMessage(prefix + ".param", locale, prefix));
                assertEquals(isZh ? "你好嵌套" : "Hello Nested", messageSource.getMessage(prefix + ".nested.message", locale, null));
                assertEquals(isZh ? "你好嵌套, " + prefix : "Hello Nested, " + prefix, messageSource.getMessage(prefix + ".nested.param", locale, prefix));
            }
        }
    }

    @Test
    void testDefaultResourceFallback() {
        assertEquals("你好", messageSource.getMessage("test.message", null));
        // 不支持的 locale，优先返回无后缀资源内容
        Locale unsupported = new Locale("fr", "FR");
        assertEquals("你好", messageSource.getMessage("test.message", unsupported));
        // 其他 key
        assertEquals("你好, Rose", messageSource.getMessage("test.param", null, "Rose"));
        assertEquals("你好嵌套", messageSource.getMessage("test.nested.message", null));
        assertEquals("你好嵌套, Rose", messageSource.getMessage("test.nested.param", null, "Rose"));
    }
}
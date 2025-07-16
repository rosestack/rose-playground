package io.github.rose.i18n.spi;

import io.github.rose.i18n.MessageSourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathPropertiesResourceMessageSourceTest {

    private ClassPathPropertiesResourceMessageSource messageSource;

    @BeforeEach
    void setUp() {
        MessageSourceManager.destroy();
        messageSource = new ClassPathPropertiesResourceMessageSource("properties");
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

    private static final String[] PREFIXES = {"test", "message", "demo", "foo", "bar"};
    private static final Locale[] LOCALES = {Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE};

    @Test
    void testAllPrefixes() {
        for (Locale locale : LOCALES) {
            boolean isZh = Locale.SIMPLIFIED_CHINESE.equals(locale);
            for (String prefix : PREFIXES) {
                // message
                if (prefix.equals("demo") || prefix.equals("bar")) continue;
                String expectedMsg = isZh ? "你好" : "Hello";
                assertEquals(expectedMsg, messageSource.getMessage(prefix + ".message", locale));
            }
            // param
            for (String prefix : PREFIXES) {
                if (prefix.equals("message") || prefix.equals("foo")) continue;
                String expectedParam = isZh ? "你好, Rose" : "Hello, Rose";
                assertEquals(expectedParam, messageSource.getMessage(prefix + ".param", locale, "Rose"));
            }
            // nested.message
            for (String prefix : PREFIXES) {
                if (prefix.equals("demo") || prefix.equals("bar") || prefix.equals("message")) continue;
                String expectedNestedMsg = isZh ? "嵌套你好" : "Nested Hello";
                assertEquals(expectedNestedMsg, messageSource.getMessage(prefix + ".nested.message", locale));
            }
            // nested.param
            for (String prefix : PREFIXES) {
                if (prefix.equals("test") || prefix.equals("demo") || prefix.equals("foo") || prefix.equals("message")) continue;
                String expectedNestedParam = isZh ? "嵌套, Rose" : "Nested, Rose";
                assertEquals(expectedNestedParam, messageSource.getMessage(prefix + ".nested.param", locale, "Rose"));
            }
        }
    }

    @Test
    void testNullAndUnsupportedLocale() {
        assertNull(messageSource.getMessage(null, Locale.ENGLISH));
        assertEquals("你好", messageSource.getMessage("test.message", null));
        assertEquals("你好", messageSource.getMessage("test.message", Locale.FRENCH));
    }
}
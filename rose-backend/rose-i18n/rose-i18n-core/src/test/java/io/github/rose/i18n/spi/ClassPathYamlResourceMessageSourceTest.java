package io.github.rose.i18n.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

/**
 * Unit tests for ClassPathYamlResourceMessageSource.
 */
public class ClassPathYamlResourceMessageSourceTest {
    private static ClassPathYamlResourceMessageSource messageSource;

    @BeforeAll
    static void setup() {
        messageSource = new ClassPathYamlResourceMessageSource("yaml");
    }

    @Test
    void testSimpleKeyLoading() {
        Map<String, String> messages = messageSource.loadMessages("i18n_messages_en.yaml");
        Assertions.assertEquals("Hello", messages.get("hello"));
        Assertions.assertEquals("Login", messages.get("user.login"));
    }

    @Test
    void testNestedKeyLoading() {
        Map<String, String> messages = messageSource.loadMessages("i18n_messages_en.yaml");
        Assertions.assertEquals("Login", messages.get("user.login"));
        Assertions.assertEquals("Logout", messages.get("user.logout"));
    }
}
package io.github.rose.i18n.util;

import io.github.rose.i18n.AbstractI18nTest;
import io.github.rose.i18n.spi.ClassPathPropertiesResourceI18nMessageSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.github.rose.i18n.util.I18nUtils.setI18nMessageSource;
import static org.junit.jupiter.api.Assertions.*;

class MessageUtilsTest extends AbstractI18nTest {

    private ClassPathPropertiesResourceI18nMessageSource messageSource;

    @BeforeEach
    public void before() {
        messageSource = new ClassPathPropertiesResourceI18nMessageSource("test");
        messageSource.init();
        setI18nMessageSource(messageSource);
        assertNotNull(I18nUtils.i18nMessageSource(), "MessageSource should be initialized");
    }

    @AfterEach
    public void after() {
        if (messageSource != null) {
            messageSource.destroy();
            messageSource = null;
        }
        I18nUtils.destroyI18nMessageSource();
    }

    @Test
    void testGetLocalizedMessageWithNull() {
        assertNull(MessageUtils.getLocalizedMessage(null));
    }

    @Test
    void testGetLocalizedMessageWithoutPattern() {
        assertEquals("a", MessageUtils.getLocalizedMessage("a"));
    }

    @Test
    void testGetLocalizedMessageWithArgs() {
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", "World"));
        String chineseResult = MessageUtils.getLocalizedMessage("{test.hello}", Locale.SIMPLIFIED_CHINESE, "World");
        assertEquals("您好,World", chineseResult);
        String englishResult = MessageUtils.getLocalizedMessage("{test.hello}", Locale.ENGLISH, "World");
        assertEquals("Hello,World", englishResult);
    }

    @Test
    void testGetLocalizedMessageWithNonExistentCode() {
        assertEquals("{code-not-found}", MessageUtils.getLocalizedMessage("{code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{common.code-not-found}"));
    }
}
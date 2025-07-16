package io.github.rose.i18n.util;

import io.github.rose.i18n.AbstractI18nTest;
import io.github.rose.i18n.MessageSourceManager;
import io.github.rose.i18n.spi.ClassPathPropertiesResourceMessageSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MessageUtilsTest extends AbstractI18nTest {

    private ClassPathPropertiesResourceMessageSource messageSource;

    @BeforeEach
    public void before() {
        messageSource = new ClassPathPropertiesResourceMessageSource("properties");
        messageSource.init();
        assertNotNull(MessageSourceManager.getInstance(), "MessageSource should be initialized");
    }

    @AfterEach
    public void after() {
        if (messageSource != null) {
            messageSource.destroy();
            messageSource = null;
        }
        MessageSourceManager.destroy();
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
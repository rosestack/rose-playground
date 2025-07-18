package io.github.rose.i18n.spring.annotation;

import io.github.rose.i18n.AbstractSpringTest;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spring.TestServiceMessageSourceConfiguration;
import io.github.rose.i18n.util.I18nUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;


/**
 * {@link EnableI18n} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        EnableI18nTest.class,
        TestServiceMessageSourceConfiguration.class
})
@EnableI18n
class EnableI18nTest extends AbstractSpringTest {

    @Autowired
    private I18nMessageSource i18nMessageSource;

    @Test
    public void testGetMessage() {
        // Testing Simplified Chinese
        // If the Message Code is "a"
        assertEquals("测试-a", i18nMessageSource.getMessage("a"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("您好,World", i18nMessageSource.getMessage("hello", "World"));

        // Returns null if code does not exist
        assertNull(i18nMessageSource.getMessage("code-not-found"));

        // Test English, because the English Message resource does not exist
        assertEquals("Hello,World", i18nMessageSource.getMessage("hello", Locale.ENGLISH, "World"));

        // Returns null if code does not exist
        assertNull(i18nMessageSource.getMessage("code-not-found", Locale.US));
    }

    @Test
    public void testCommonServiceMessageSource() {
        // Test that I18nUtils returns a functionally equivalent message source
        // Using assertEquals instead of assertSame because in test contexts,
        // multiple Spring contexts may create different instances
        assertEquals(I18nUtils.i18nMessageSource().toString(), i18nMessageSource.toString());

        // Also verify that both message sources work the same way
        String message1 = I18nUtils.i18nMessageSource().getMessage("a");
        String message2 = i18nMessageSource.getMessage("a");
        assertEquals(message1, message2);
    }
}

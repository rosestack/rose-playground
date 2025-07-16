package io.github.rose.i18n.util;

import io.github.rose.i18n.AbstractI18nTest;
import io.github.rose.i18n.spi.ClassPathPropertiesResourceI18nMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.rose.i18n.util.I18nUtils.setI18nMessageSource;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MessageUtilsTest extends AbstractI18nTest {

    @Test
    public void testGetLocalizedMessage() {
        ClassPathPropertiesResourceI18nMessageSource messageSource =
                new ClassPathPropertiesResourceI18nMessageSource("test");
        messageSource.init();
    }
}
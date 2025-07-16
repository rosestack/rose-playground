package io.github.rose.i18n.util;

import io.github.rose.i18n.AbstractI18nTest;
import io.github.rose.i18n.spi.ClassPathPropertiesResourceI18nMessageSource;
import io.github.rose.i18n.spi.EmptyI18nMessageSource;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * {@link I18nUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nUtilsTest extends AbstractI18nTest {

    @Test
    public void test() {
        assertSame(EmptyI18nMessageSource.INSTANCE, I18nUtils.i18nMessageSource());

        ClassPathPropertiesResourceI18nMessageSource defaultServiceMessageSource =
                new ClassPathPropertiesResourceI18nMessageSource("test");
        I18nUtils.setI18nMessageSource(defaultServiceMessageSource);

        assertSame(defaultServiceMessageSource, I18nUtils.i18nMessageSource());
    }
}
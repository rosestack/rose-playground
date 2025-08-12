package io.github.rosestack.i18n;

import io.github.rosestack.i18n.util.I18nUtils;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Abstract Spring Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSpringTest {

    @BeforeAll
    public static void beforeClass() {
        // Set the simplified Chinese as the default Locale
        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
    }

    @AfterAll
    public static void afterClass() {
        I18nUtils.destroyMessageSource();
        LocaleContextHolder.resetLocaleContext();
    }

    @BeforeEach
    public void before() {
        LocaleContextHolder.resetLocaleContext();
    }
}

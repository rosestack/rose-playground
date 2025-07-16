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
        messageSource.init();
    }

}
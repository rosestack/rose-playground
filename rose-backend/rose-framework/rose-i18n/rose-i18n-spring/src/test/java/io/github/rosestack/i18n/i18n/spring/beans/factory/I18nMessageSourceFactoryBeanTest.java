package io.github.rosestack.i18n.i18n.spring.beans.factory;

import io.github.rosestack.i18n.AbstractSpringTest;
import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.i18n.spring.beans.TestI18nMessageSourceConfiguration;
import io.github.rosestack.i18n.spring.context.ResourceMessageSourceChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link io.github.rosestack.i18n.spring.I18nMessageSourceFactoryBean} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        I18nMessageSourceFactoryBeanTest.class,
        TestI18nMessageSourceConfiguration.class
})
@TestPropertySource(properties = {
        "rose.i18n.default-locale=en",
        "rose.i18n.supported-locales=en",
})
class I18nMessageSourceFactoryBeanTest extends AbstractSpringTest {

    @Autowired
    private I18nMessageSource i18nMessageSource;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    private MockPropertySource propertySource;

    @BeforeEach
    public void before() {
        super.before();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        propertySource = new MockPropertySource("mock");
        environment.getPropertySources().addFirst(propertySource);
    }

    @Test
    public void testGetMessage() {
        assertEquals("test-a", i18nMessageSource.getMessage("a"));
        assertEquals("Hello,World", i18nMessageSource.getMessage("hello", "World"));

        // Test FRANCE
        assertNull(i18nMessageSource.getMessage("a", Locale.FRANCE));

        ResourceMessageSourceChangedEvent event = new ResourceMessageSourceChangedEvent(context, Arrays.asList("test.i18n_messages_en.properties"));
        propertySource.setProperty("test.i18n_messages_en.properties", "a=1");
        eventPublisher.publishEvent(event);
        assertEquals("1", i18nMessageSource.getMessage("a"));
    }

    @Test
    public void testGetLocale() {
        assertEquals(Locale.ENGLISH, i18nMessageSource.getLocale());

        // Test US
        LocaleContextHolder.setLocale(Locale.US);
        assertEquals(Locale.US, i18nMessageSource.getLocale());
    }

    @Test
    public void testGetDefaultLocale() {
        assertEquals(Locale.ENGLISH, i18nMessageSource.getDefaultLocale());
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(asList(Locale.ENGLISH), i18nMessageSource.getSupportedLocales());
    }

    @Test
    public void testGetSource() {
        assertEquals("test", i18nMessageSource.getSource());
    }

}

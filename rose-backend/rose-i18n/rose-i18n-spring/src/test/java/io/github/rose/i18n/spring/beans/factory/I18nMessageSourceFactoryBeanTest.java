package io.github.rose.i18n.spring.beans.factory;

import io.github.rose.core.util.ThreadUtils;
import io.github.rose.i18n.AbstractSpringTest;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spring.beans.I18nMessageSourceFactoryBean;
import io.github.rose.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import io.github.rose.i18n.spring.context.ResourceMessageSourceChangedEvent;
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
import java.util.LinkedHashSet;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link I18nMessageSourceFactoryBean} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        I18nMessageSourceFactoryBeanTest.class,
        TestServiceMessageSourceConfiguration.class
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
        ThreadUtils.sleep(2000);
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
        assertEquals(new LinkedHashSet<>(asList(Locale.ENGLISH)), i18nMessageSource.getSupportedLocales());
    }

    @Test
    public void testGetSource() {
        assertEquals("test", i18nMessageSource.getSource());
    }

}
